/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.ejb3.cache.impl.backing;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.ObjectStore;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.spi.impl.AbstractPassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.spi.impl.CacheableTimestamp;
import org.jboss.logging.Logger;

/**
 * A {@link PassivatingIntegratedObjectStore} that stores in a simple
 * <code>Map</code> and delegates to a provided {@link ObjectStore} for 
 * persistence.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class SimplePassivatingIntegratedObjectStore<C extends CacheItem, T extends BackingCacheEntry<C>>
      extends AbstractPassivatingIntegratedObjectStore<C, T>
{
   private static final Logger log = Logger.getLogger(SimplePassivatingIntegratedObjectStore.class);
   
   private final ObjectStore<T> store;
   private Map<Object, T> cache;
   private Map<Object, Long> passivatedEntries;
   
   /**
    * Create a new SimpleIntegratedObjectStore.
    */
   public SimplePassivatingIntegratedObjectStore(ObjectStore<T> store, 
                                                 CacheConfig config,
                                                 String name,
                                                 boolean forGroups)
   {
      super(config, name, forGroups);
      
      assert store != null : "store is null";
      
      this.store = store;
      this.cache = new HashMap<Object, T>();
      if (!forGroups)
      {
         this.passivatedEntries = new HashMap<Object, Long>();
      }
   }
   
   public boolean isClustered()
   {
      return false;
   }

   public T get(Object key)
   {
      synchronized (cache)
      {
         T entry = cache.get(key);
         if(entry == null)
         {
            entry = store.load(key);
            if(entry != null)
            {
               cache.put(key, entry);
               if (!isForGroups())
               {
                  passivatedEntries.remove(key);
               }
            }
         }
         return entry;
      }
   }

   public void insert(T entry)
   {
      Object key = entry.getId();
      synchronized (cache)
      {
         if (cache.containsKey(key) 
               || (!isForGroups() && passivatedEntries.containsKey(key)))
         {
            throw new IllegalStateException(key + " is already in store");
         }
         cache.put(key, entry);
      }
   }
   
   public void update(T entry)
   {
      Object key = entry.getId();
      synchronized (cache)
      {
         if (!cache.containsKey(key) && 
               (isForGroups() || !passivatedEntries.containsKey(key)))
         {
            throw new IllegalStateException(key + " is not managed by this store");
         }
         
         // Otherwise we do nothing; we already have a ref to the entry
      }
   }

   public void passivate(T entry)
   {
      synchronized (cache)
      {
         Object key = entry.getId();
         store.store(entry);  
         if (!isForGroups())
         {
            passivatedEntries.put(key, new Long(entry.getLastUsed()));
         }
         cache.remove(key);
      }
   }

   public T remove(Object id)
   {
      synchronized (cache)
      {
         T entry = get(id);
         if (entry != null)
         {
            cache.remove(id);
         }
         return entry;
      }
   } 

   public void start()
   {
      store.start();
      
      super.start();
   }

   public void stop()
   {      
      store.stop();
      
      super.stop();
   }

   // -------------------------------  AbstractPassivatingIntegratedObjectStore

   @Override
   protected void runExpiration()
   {
      if (!isForGroups() && getExpirationTimeSeconds() > 0)
      {
         long now = System.currentTimeMillis();
         long minRemovalUse = now - (getExpirationTimeSeconds() * 1000);                     
         for (CacheableTimestamp ts : getAllEntries())
         {
            try
            {
               if (minRemovalUse >= ts.getLastUsed())
               {
                  remove(ts.getId());
               }
            }
            catch (IllegalStateException ise)
            {
               // Not so great; we're assuming it's 'cause item's in use
               log.trace("skipping in-use entry " + ts.getId(), ise);
            }
         }    
      }      
   }

   @Override
   protected void runPassivation()
   {
      if (!isForGroups() 
            && (getMaxSize() > 0 || getIdleTimeSeconds() > 0))
      {
         long now = System.currentTimeMillis();
         long minPassUse = (getIdleTimeSeconds() > 0 ? now - (getIdleTimeSeconds() * 1000) : 0);
         
         SortedSet<CacheableTimestamp> timestamps = getInMemoryEntries();
         int overCount = (getMaxSize() > 0 ? timestamps.size() - getMaxSize() : 0);
         for (CacheableTimestamp ts : timestamps)
         {
            try
            {
               if (overCount > 0 || minPassUse >= ts.getLastUsed())
               {
                  log.trace("attempting to passivate " + ts.getId());
                  getPassivatingCache().passivate(ts.getId());
                  overCount--;
               }
               else
               {
                  break;
               }
            }
            catch (IllegalStateException ise)
            {
               // Not so great; we're assuming it's 'cause item's in use
               log.trace("skipping in-use entry " + ts.getId(), ise);
            }
         }
      }      
   }

   private SortedSet<CacheableTimestamp> getInMemoryEntries()
   {      
      SortedSet<CacheableTimestamp> set = new TreeSet<CacheableTimestamp>();
      for (Map.Entry<Object, T> entry : cache.entrySet())
      {
         set.add(new CacheableTimestamp(entry.getKey(), entry.getValue().getLastUsed()));
      }
      return set;
   }

   private SortedSet<CacheableTimestamp> getPassivatedEntries()
   {
      SortedSet<CacheableTimestamp> set = new TreeSet<CacheableTimestamp>();
      if (!isForGroups())
      {
         for (Map.Entry<Object, Long> entry : passivatedEntries.entrySet())
         {
            set.add(new CacheableTimestamp(entry.getKey(), entry.getValue().longValue()));
         } 
      }
      return set;
   }
   
   private SortedSet<CacheableTimestamp> getAllEntries()
   {
      SortedSet<CacheableTimestamp> set = getInMemoryEntries();
      if (!isForGroups())
      {
         for (Map.Entry<Object, Long> entry : passivatedEntries.entrySet())
         {
            set.add(new CacheableTimestamp(entry.getKey(), entry.getValue().longValue()));
         } 
      }
      return set;
   }

}
