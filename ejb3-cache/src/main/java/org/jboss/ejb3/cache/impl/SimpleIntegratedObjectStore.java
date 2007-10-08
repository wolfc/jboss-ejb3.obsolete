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

package org.jboss.ejb3.cache.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.ejb3.cache.Cacheable;
import org.jboss.ejb3.cache.IntegratedObjectStore;
import org.jboss.ejb3.cache.ItemInUseException;
import org.jboss.ejb3.cache.ObjectStore;
import org.jboss.ejb3.cache.PassivatingCache;
import org.jboss.ejb3.cache.PassivatingIntegratedObjectStore;
import org.jboss.logging.Logger;

/**
 * A {@link IntegratedObjectStore} that delegates to a provided
 * {@link ObjectStore} for persistence.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class SimpleIntegratedObjectStore<T extends Cacheable & Serializable> 
      implements PassivatingIntegratedObjectStore<T>
{
   private static final Logger log = Logger.getLogger(SimpleIntegratedObjectStore.class);
   
   private final ObjectStore<T> store;
   private Map<Object, T> cache;
   private Map<Object, Long> passivatedEntries;
   
   /**
    * Support callbacks when our SessionTimeoutThread decides to
    * evict an entry.
    */
   private PassivatingCache<T> owningCache;   
   private int interval;
   private int idleTimeSeconds;
   private int expirationTimeSeconds;   
   private SessionTimeoutRunner sessionTimeoutRunner;
   private String name;
   
   /**
    * Create a new SimpleIntegratedObjectStore.
    * 
    */
   public SimpleIntegratedObjectStore(ObjectStore<T> store)
   {
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
         cache.put(key, entry);
      }
   }
   
   public void replicate(T entry)
   {
      throw new UnsupportedOperationException("Clustering is not supported by " + 
                                              getClass().getName());      
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
      if (interval > 0)
      {
         if (sessionTimeoutRunner == null)
         {
            assert name != null : "name has not been set";
            
            sessionTimeoutRunner = new SessionTimeoutRunner();
         }
         sessionTimeoutRunner.start();
      }
   }

   public void stop()
   {
      if (sessionTimeoutRunner != null)
      {
         sessionTimeoutRunner.stop();
      }      
   }

   // ---------------------------------------  PassivatingIntegratedObjectStore


   public void setPassivatingCache(PassivatingCache<T> cache)
   {
      this.owningCache = cache;      
   }
   
   public int getInterval()
   {
      return interval;
   }

   public void setInterval(int seconds)
   {
      this.interval = seconds;      
   }
   
   public void runExpiration()
   {
      if (expirationTimeSeconds > 0)
      {
         long now = System.currentTimeMillis();
         long minRemovalUse = now - (expirationTimeSeconds * 1000);                     
         for (CacheableTimestamp ts : getPassivatedEntries())
         {
            try
            {
               if (minRemovalUse >= ts.getLastUsed())
               {
                  remove(ts.getId());
               }
            }
            catch (ItemInUseException ignored)
            {
               log.trace("skipping in-use entry " + ts.getId());
            }
         }    
      }      
   }

   public void runPassivation()
   {
      if (idleTimeSeconds > 0)
      {
         long now = System.currentTimeMillis();
         long minPassUse = now - (idleTimeSeconds * 1000);
         
         // Scan the in-memory entries for passivation or removal
         for (CacheableTimestamp ts : getInMemoryEntries())
         {
            try
            {
               long lastUsed = ts.getLastUsed();
               if (minPassUse >= lastUsed)
               {
                  owningCache.passivate(ts.getId());
               }
            }
            catch (ItemInUseException ignored)
            {
               log.trace("skipping in-use entry " + ts.getId());
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

   public void setName(String name)
   {
      this.name = name;
   }
   

   private class SessionTimeoutRunner implements Runnable
   {
      private boolean stopped = true;
      private Thread thread;
      
      public void run()
      {
         while (!stopped)
         {
            try
            {
               runPassivation();               
            }
            catch (Exception e)
            {
               log.error("Caught exception processing passivations", e);
            }
            
            if (!stopped)
            {
               try
               {
                  runExpiration();               
               }
               catch (Exception e)
               {
                  log.error("Caught exception processing expirations", e);
               }               
            }
            
            if (!stopped)
            {
               try
               {
                  Thread.sleep(interval * 1000);
               }
               catch (InterruptedException ignored) {}
            }
         }
      }
      
      void start()
      {
         if (stopped)
         {
            thread = new Thread(this, "SessionTimeoutRunner-" + name);
            thread.setDaemon(true);
            stopped = false;
            thread.start();
         }
      }
      
      void stop()
      {
         stopped = true;
         if (thread != null && thread.isAlive())
         {
            try
            {
               thread.join(1000);
            }
            catch (InterruptedException ignored) {}
            
            if (thread.isAlive())
            {
               thread.interrupt();
            }
            
         }
      }
      
   }

}
