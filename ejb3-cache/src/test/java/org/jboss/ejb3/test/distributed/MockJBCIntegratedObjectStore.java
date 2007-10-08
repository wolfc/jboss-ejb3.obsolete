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

package org.jboss.ejb3.test.distributed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.ejb3.cache.Cacheable;
import org.jboss.ejb3.cache.IntegratedObjectStore;
import org.jboss.ejb3.cache.ItemInUseException;
import org.jboss.ejb3.cache.PassivatingCache;
import org.jboss.ejb3.cache.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.impl.CacheableTimestamp;
import org.jboss.logging.Logger;

/**
 * {@link IntegratedObjectStore} implementation that mocks the functions
 * of a JBoss Cache-based version.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class MockJBCIntegratedObjectStore<T extends Cacheable & Serializable> 
     implements PassivatingIntegratedObjectStore<T>
{
   private static final Logger log = Logger.getLogger(MockJBCIntegratedObjectStore.class);

   /** 
    * The "JBoss Cache" instance.
    */
   private Map<Object, T> mockJBC;
   
   /**
    * Those keys in the mockJBC map that haven't been "passivated"
    */
   private Set<Object> inMemory;
   
   /**
    * Support callbacks when our MockEvictionRunner decides to
    * evict an entry.
    */
   private PassivatingCache<T> owningCache;   
   private int interval;
   private int idleTimeSeconds;
   private int expirationTimeSeconds;
   
   private SessionTimeoutRunner sessionTimeoutRunner;
   
   public MockJBCIntegratedObjectStore()
   {      
      mockJBC = new HashMap<Object, T>();
      inMemory = new HashSet<Object>();
   }

   // --------------------------------------------------  IntegratedObjectStore
   
   public boolean isClustered()
   {
      return true;
   }
   
   public T get(Object key)
   {
      T entry = mockJBC.get(key);
      if (entry != null)
      {
         // We just pulled this data "into memory"
         inMemory.add(key);
      }
      return entry;
   }

   public void insert(T entry)
   {
      Object key = entry.getId();
      mockJBC.put(key, entry);
      inMemory.add(key);
   }
   
   public void replicate(T entry)
   {
      mockJBC.put(entry.getId(), entry);
   }

   public void passivate(T entry)
   {
      inMemory.remove(entry.getId());
   }

   public T remove(Object key)
   {
      T entry = mockJBC.remove(key);
      if (entry != null)
      {
         inMemory.remove(key);
      }
      return entry;
   } 

   // ---------------------------------------  PassivatingIntegratedObjectStore
   
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

   public void setPassivatingCache(PassivatingCache<T> cache)
   {
      this.owningCache = cache;      
   }

   public void start()
   {
      if (interval > 0)
      {
         if (sessionTimeoutRunner == null)
         {
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
   
   public int getIdleTimeSeconds()
   {
      return idleTimeSeconds;
   }

   public void setIdleTimeSeconds(int idleTimeSeconds)
   {
      this.idleTimeSeconds = idleTimeSeconds;
   }

   public int getExpirationTimeSeconds()
   {
      return expirationTimeSeconds;
   }
   
   public void setExpirationTimeSeconds(int timeout)
   {
      this.expirationTimeSeconds = timeout;
   } 

   private SortedSet<CacheableTimestamp> getInMemoryEntries()
   {      
      return getTimestampSet(false);
   }

   private SortedSet<CacheableTimestamp> getPassivatedEntries()
   {     
      return getTimestampSet(true);
   }
   
   private SortedSet<CacheableTimestamp> getTimestampSet(boolean passivated)
   {      
      SortedSet<CacheableTimestamp> set = new TreeSet<CacheableTimestamp>();
      for (Map.Entry<Object, T> entry : mockJBC.entrySet())
      {
         if (passivated != inMemory.contains(entry.getKey()))
         {
            set.add(new CacheableTimestamp(entry.getKey(), entry.getValue().getLastUsed()));
         }
      }
      return set;
      
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
            thread = new Thread(this, "MockEvictionThread");
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
