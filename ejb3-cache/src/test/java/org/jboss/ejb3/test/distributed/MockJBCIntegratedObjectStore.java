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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    * Our in-VM "JBoss Cache" instance.
    */
   private Map<Object, Object> localJBC;
   /** 
    * A remote "JBoss Cache" instance. We only store byte[] values,
    * mocking the effect of replication.
    */
   private Map<Object, Object> remoteJBC;
   
   /**
    * Those keys in the mockJBC map that haven't been "passivated"
    */
   private Set<Object> inMemory;
   
   /**
    * Those keys that have been updated locally but not copied to remoteJBC.
    */
   private Set<Object> dirty;
   
   private Map<Object, Long> timestamps;

   /** A mock transaction manager */
   private ThreadLocal<Boolean> tm = new ThreadLocal<Boolean>();
   
   /**
    * Support callbacks when our MockEvictionRunner decides to
    * evict an entry.
    */
   private PassivatingCache<T> owningCache;   
   private int interval;
   private int idleTimeSeconds;
   private int expirationTimeSeconds;
   
   private SessionTimeoutRunner sessionTimeoutRunner;
   
   public MockJBCIntegratedObjectStore(Map<Object, Object> localCache, 
                                       Map<Object, Object> remoteCache)
   {      
      this.localJBC = localCache;
      this.remoteJBC = remoteCache;
      inMemory = new HashSet<Object>();
      timestamps = new HashMap<Object, Long>();
   }

   // --------------------------------------------------  IntegratedObjectStore
   
   public boolean isClustered()
   {
      return true;
   }
   
   public T get(Object key)
   {
      T entry = unmarshall(key, localJBC.get(key));
      timestamps.put(key, new Long(System.currentTimeMillis()));
      return entry;
   }

   public void insert(T entry)
   {
      putInCache(entry.getId(), entry);
   }
   
   public void update(T entry)
   {
      putInCache(entry.getId(), entry);
   }

   @SuppressWarnings("unchecked")
   public void passivate(T entry)
   {
      Object key = entry.getId();
      if (inMemory.contains(key))
      {
         log.trace("converting " + key + " to passivated state");
         localJBC.put(key, marshall((T) localJBC.get(key)));
         inMemory.remove(key);
      }
   }

   public T remove(Object key)
   {
      return unmarshall(key, putInCache(key, null));
   }
   
   
   // ------------------------------------------------------------ Transaction
   
   public void startTransaction()
   {
      if (tm.get() != null)
         throw new IllegalStateException("Transaction already started");
      tm.set(Boolean.TRUE);
   }
   
   public void commitTransaction()
   {
      try
      {
         for (Iterator<Object> iter = dirty.iterator(); iter.hasNext();)
         {
            Object key = iter.next();
            replicate(key, unmarshall(key, localJBC.get(key)));
            iter.remove();
         }
      }
      finally
      {
         tm.set(null);
      }
   }
   
   @SuppressWarnings("unchecked")
   private T unmarshall(Object key, Object obj)
   {
      if (obj == null)
         return null;
      else if (!(obj instanceof byte[]))
         return (T) obj;
      
      log.trace("unmarshalling " + key);
      ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) obj);
      try
      {
         ObjectInputStream ois = new ObjectInputStream(bais);
         T entry = (T) ois.readObject();
         localJBC.put(key, entry);
         inMemory.add(key);
         return entry;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private Object putInCache(Object key, T value)
   {
      Object existing = null;
      if (value != null)
      {
         existing = localJBC.put(key, value);
         inMemory.add(key);
         timestamps.put(key, new Long(System.currentTimeMillis()));
      }
      else
      {
         existing = localJBC.remove(key);
         inMemory.remove(key);
         timestamps.remove(key);
      }
      
      if (tm.get() == null)
      {
         replicate(key, value);
      }
      else
      {
         dirty.add(key);
      }
      
      return existing;
   }
   
   private void replicate(Object key, T value)
   {
      if (value != null)
         remoteJBC.put(key, marshall(value));
      else
         remoteJBC.remove(key);
   }
   
   private byte[] marshall(T value)
   {
      log.trace("marshalling " + value.getId());
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(value);
         oos.close();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      return baos.toByteArray();
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
      for (Map.Entry<Object, Long> entry : timestamps.entrySet())
      {
         if (passivated != inMemory.contains(entry.getKey()))
         {
            set.add(new CacheableTimestamp(entry.getKey(), entry.getValue().longValue()));
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
