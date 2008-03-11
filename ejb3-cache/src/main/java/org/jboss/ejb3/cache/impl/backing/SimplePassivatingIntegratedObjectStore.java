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
                                                 String name)
   {
      super(config, name);
      
      assert store != null : "store is null";
      
      this.store = store;
      this.cache = new HashMap<Object, T>();
      this.passivatedEntries = new HashMap<Object, Long>();
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
               passivatedEntries.remove(key);
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
         if (cache.containsKey(key) || passivatedEntries.containsKey(key))
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
         if (!cache.containsKey(key) && !passivatedEntries.containsKey(key))
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
         passivatedEntries.put(key, new Long(entry.getLastUsed()));
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
      if (getExpirationTimeSeconds() > 0)
      {
         long now = System.currentTimeMillis();
         long minRemovalUse = now - (getExpirationTimeSeconds() * 1000);                     
         for (CacheableTimestamp ts : getPassivatedEntries())
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
      if (getIdleTimeSeconds() > 0)
      {
         long now = System.currentTimeMillis();
         long minPassUse = now - (getIdleTimeSeconds() * 1000);
         
         // Scan the in-memory entries for passivation or removal
         SortedSet<CacheableTimestamp> timestamps = getInMemoryEntries();
         int overCount = timestamps.size() - getMaxSize();
         for (CacheableTimestamp ts : timestamps)
         {
            try
            {
               long lastUsed = ts.getLastUsed();
               if (overCount > 0 || minPassUse >= lastUsed)
               {
                  synchronized (cache)               
                  {
                     T entry = cache.get(ts.getId());
                     if (entry == null || entry.isInUse())
                        continue;
                  }
                  getPassivatingCache().passivate(ts.getId());
                  overCount--;
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
      for (Map.Entry<Object, Long> entry : passivatedEntries.entrySet())
      {
         set.add(new CacheableTimestamp(entry.getKey(), entry.getValue().longValue()));
      }
      return set;
   }

}
