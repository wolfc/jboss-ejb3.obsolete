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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.GroupCompatibilityChecker;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.PersistentObjectStore;
import org.jboss.ejb3.cache.spi.impl.AbstractBackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.impl.CacheableTimestamp;

/**
 * A {@link BackingCacheEntryStore} that stores in a simple
 * <code>Map</code> and delegates to a provided {@link PersistentObjectStore} for 
 * persistence.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class SimpleBackingCacheEntryStore<C extends CacheItem, T extends BackingCacheEntry<C>>
      extends AbstractBackingCacheEntryStore<C, T, Object>
{
   private final PersistentObjectStore<T> store;
   private Map<Object, T> cache;
   private Map<Object, Long> passivatedEntries;
   
   /**
    * Create a new SimpleIntegratedObjectStore.
    */
   public SimpleBackingCacheEntryStore(PersistentObjectStore<T> store, 
                                                 CacheConfig config,
                                                 String name,
                                                 boolean forGroups)
   {
      super(config, name, forGroups);
      
      assert store != null : "store is null";
      
      this.store = store;
      this.cache = new ConcurrentHashMap<Object, T>();
      this.passivatedEntries = new ConcurrentHashMap<Object, Long>();      
   }
   
   public boolean isClustered()
   {
      return false;
   }

   public T get(Object key)
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

   public void insert(T entry)
   {
      Object key = entry.getId();
      if (cache.containsKey(key) || passivatedEntries.containsKey(key))
      {
         throw new IllegalStateException(key + " is already in store");
      }
      cache.put(key, entry);
   }
   
   public void update(T entry, boolean modified)
   {
      Object key = entry.getId();
      if (!cache.containsKey(key) && !passivatedEntries.containsKey(key))
      {
         throw new IllegalStateException(key + " is not managed by this store");
      }
         
      // Otherwise we do nothing; we already have a ref to the entry
   }

   public void passivate(T entry)
   {
      synchronized (entry)
      {
         Object key = entry.getId();
         store.store(entry);  
         passivatedEntries.put(key, new Long(entry.getLastUsed()));         
         cache.remove(key);
      }
   }

   public T remove(Object id)
   {
      T entry = get(id);
      if (entry != null)
      {
         cache.remove(id);
      }
      return entry;
   } 

   protected void internalStart()
   {
      store.start();
      
      super.internalStart();
   }

   protected void internalStop()
   {      
      store.stop();
      
      super.internalStop();
   }

   @SuppressWarnings("unchecked")
   public boolean isCompatibleWith(GroupCompatibilityChecker other)
   {
      if (other instanceof BackingCacheEntryStore)
      {
         return ((BackingCacheEntryStore) other).isClustered() == false;
      }
      return false;
   }

   // -------------------------------  AbstractBackingCacheEntryStore

   @Override
   public int getInMemoryCount()
   {
      return cache.size();
   }
   
   @Override
   public int getPassivatedCount()
   {
      return passivatedEntries.size();
   }   
   
   @Override
   protected void processPassivation(Object key, long lastUse)
   {
      if (!isRunning())
         return;
      
      // If we are for groups we shouldn't be getting a processPassivation
      // call at all, but just to be safe we'll ignore it
      if (!isForGroups())
      {
         getPassivatingCache().passivate(key);
      }
   }
   
   @Override
   protected void processExpiration(Object key, long lastUse)
   {
      if (!isRunning())
         return;
      
      getPassivatingCache().remove(key);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected CacheableTimestamp<Object>[] getInMemoryEntries()
   {     
      Set<CacheableTimestamp<Object>> set = new HashSet<CacheableTimestamp<Object>>();
      for (Map.Entry<Object, T> entry : cache.entrySet())
      {
         set.add(new CacheableTimestamp<Object>(entry.getKey(), entry.getValue().getLastUsed()));
      }
      CacheableTimestamp<Object>[] array = new CacheableTimestamp[set.size()];
      array = set.toArray(array);
      Arrays.sort(array);
      return array;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected CacheableTimestamp<Object>[] getAllEntries()
   {     
      Set<CacheableTimestamp<Object>> set = new HashSet<CacheableTimestamp<Object>>();
      for (Map.Entry<Object, T> entry : cache.entrySet())
      {
         set.add(new CacheableTimestamp<Object>(entry.getKey(), entry.getValue().getLastUsed()));
      }
      CacheableTimestamp<Object>[] inMemory = new CacheableTimestamp[set.size()];
      inMemory = set.toArray(inMemory);   
      
      set = new HashSet<CacheableTimestamp<Object>>();
      for (Map.Entry<Object, Long> entry : passivatedEntries.entrySet())
      {
         set.add(new CacheableTimestamp<Object>(entry.getKey(), entry.getValue()));
      }
      CacheableTimestamp<Object>[] passivated = new CacheableTimestamp[set.size()];
      passivated = set.toArray(passivated);

      CacheableTimestamp<Object>[] all = new CacheableTimestamp[passivated.length + inMemory.length];
      System.arraycopy(passivated, 0, all, 0, passivated.length);
      System.arraycopy(inMemory, 0, all, passivated.length, inMemory.length);
      Arrays.sort(all);
      return all;
   }
   
   /** For unit testing only */
   protected boolean containsInMemoryEntry(Object key)
   {
      return cache.containsKey(key);
   }
   
   /** For unit testing only */
   protected void clear()
   {
      for (Object key : passivatedEntries.keySet())
      {
         get(key);
      }
      cache.clear();
      passivatedEntries.clear();
   }
   
   /** For unit testing only */
   protected T getBackingCacheEntry(Object key)
   {
      return cache.get(key);
   }
}
