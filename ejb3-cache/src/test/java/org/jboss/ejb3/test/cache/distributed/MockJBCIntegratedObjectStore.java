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

package org.jboss.ejb3.test.cache.distributed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.api.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.IntegratedObjectStore;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.spi.impl.CacheableTimestamp;
import org.jboss.ejb3.cache.spi.impl.PassivationExpirationRunner;
import org.jboss.logging.Logger;

/**
 * {@link IntegratedObjectStore} implementation that mocks the functions
 * of a JBoss Cache-based version.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class MockJBCIntegratedObjectStore<C extends CacheItem, T extends BackingCacheEntry<C>> 
     implements PassivatingIntegratedObjectStore<C, T>
{
   private static final Logger log = Logger.getLogger(MockJBCIntegratedObjectStore.class);

   /**
    * Qualifier used to scope our keys in the maps
    */
   private final Object keyBase;
   
   /** 
    * Our in-VM "JBoss Cache" instance.
    */
   private final UnmarshallingMap localJBC;
   /** 
    * A remote "JBoss Cache" instance. We only store byte[] values,
    * mocking the effect of replication.
    */
   private final UnmarshallingMap remoteJBC;
   
   /**
    * Those keys in the mockJBC map that haven't been "passivated"
    */
   private final Set<Object> inMemory;
   
   /**
    * Those keys that have been updated locally but not copied to remoteJBC.
    */
   private Set<Object> dirty;
   
   private final Map<Object, Long> timestamps;

   /** A mock transaction manager */
   private final ThreadLocal<Boolean> tm = new ThreadLocal<Boolean>();
   
   /**
    * Support callbacks when our MockEvictionRunner decides to
    * evict an entry.
    */
   private PassivatingBackingCache<C, T> owningCache;   
   private int interval;
   private long idleTimeSeconds;
   private long expirationTimeSeconds;
   private int maxSize;
   private PassivationExpirationRunner sessionTimeoutRunner;
   private final String name;
   private boolean stopped = true;
   
   public MockJBCIntegratedObjectStore(UnmarshallingMap localCache, 
                                       UnmarshallingMap remoteCache,
                                       CacheConfig cacheConfig,
                                       Object keyBase,
                                       String name)
   {      
      this.localJBC = localCache;
      this.remoteJBC = remoteCache;
      this.keyBase = keyBase;
      inMemory = new HashSet<Object>();
      timestamps = new HashMap<Object, Long>();
      this.idleTimeSeconds = cacheConfig.idleTimeoutSeconds();
      this.expirationTimeSeconds = cacheConfig.removalTimeoutSeconds();
      this.maxSize = cacheConfig.maxSize();
      this.name = name;
   }

   // --------------------------------------------------  IntegratedObjectStore
   
   public boolean isClustered()
   {
      return true;
   }
   
   public T get(Object key)
   {      
      T entry = unmarshall(key, localJBC.get(getScopedKey(key)));
      if (entry != null)
         timestamps.put(key, new Long(System.currentTimeMillis()));
      return entry;
   }

   public void insert(T entry)
   {
      putInCache(entry.getId(), entry);
   }
   
   public void update(T entry)
   {
      if (entry.isModified())
      {
         putInCache(entry.getId(), entry);
      }
   }

   @SuppressWarnings("unchecked")
   public void passivate(T entry)
   {
      Object key = entry.getId();
      if (inMemory.contains(key))
      {
         log.trace("converting " + key + " to passivated state");
         ScopedKey skey = getScopedKey(key);
         localJBC.put(skey, marshall((T) localJBC.get(skey)));
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
            replicate(key, unmarshall(key, localJBC.get(getScopedKey(key))));
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
      if (!(obj instanceof byte[]))
         return (T) obj;
      
      log.trace("unmarshalling " + key);
      ScopedKey skey = getScopedKey(key);
      T entry = (T) localJBC.unmarshall(skey);
      localJBC.put(skey, entry);
      inMemory.add(key);
      return entry;
   }
   
   private Object putInCache(Object key, T value)
   {
      ScopedKey skey = getScopedKey(key);
      Object existing = null;
      if (value != null)
      {
         existing = localJBC.put(skey, value);
         inMemory.add(key);
         timestamps.put(key, new Long(System.currentTimeMillis()));
      }
      else
      {
         existing = localJBC.remove(skey);
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
      ScopedKey skey = getScopedKey(key);
      if (value != null)
         remoteJBC.put(skey, marshall(value));
      else
         remoteJBC.remove(skey);
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

   public void runPassivation()
   {
      if (maxSize > 0 || idleTimeSeconds > 0)
      {
         long now = System.currentTimeMillis();
         long minPassUse = (idleTimeSeconds > 0 ? now - (idleTimeSeconds * 1000) : 0);
         
         SortedSet<CacheableTimestamp> timestamps = getInMemoryEntries();
         int overCount = (maxSize > 0 ? timestamps.size() - maxSize : 0);
         for (CacheableTimestamp ts : timestamps)
         {
            try
            {
               if (overCount > 0 || minPassUse >= ts.getLastUsed())
               {
                  log.trace("attempting to passivate " + ts.getId());
                  owningCache.passivate(ts.getId());
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

   public void setPassivatingCache(PassivatingBackingCache<C, T> cache)
   {
      this.owningCache = cache;      
   }

   public void start()
   {
      if (interval > 0)
      {
         if (sessionTimeoutRunner == null)
         {
            assert name != null : "name has not been set";
            assert owningCache != null;
            String timerName = "PassivationExpirationTimer-" + name;
            sessionTimeoutRunner = new PassivationExpirationRunner(this, timerName, interval);
         }
         sessionTimeoutRunner.start();
      }      
      
      stopped = false;
      
      log.debug("Started " + name);
   }

   public void stop()
   {
      if (sessionTimeoutRunner != null)
      {
         sessionTimeoutRunner.stop();
      }          
      
      stopped = true;
      
      log.debug("Stopped " + name);
   }
   
   public long getIdleTimeSeconds()
   {
      return idleTimeSeconds;
   }

   public void setIdleTimeSeconds(long idleTimeSeconds)
   {
      this.idleTimeSeconds = idleTimeSeconds;
   }

   public long getExpirationTimeSeconds()
   {
      return expirationTimeSeconds;
   }
   
   public void setExpirationTimeSeconds(long timeout)
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
   
   private SortedSet<CacheableTimestamp> getAllEntries()
   {
      return getTimestampSet(null);
   }
   
   private SortedSet<CacheableTimestamp> getTimestampSet(Boolean passivated)
   {      
      SortedSet<CacheableTimestamp> set = new TreeSet<CacheableTimestamp>();
      for (Map.Entry<Object, Long> entry : timestamps.entrySet())
      {
         if (passivated == null || passivated.booleanValue() != inMemory.contains(entry.getKey()))
         {
            set.add(new CacheableTimestamp(entry.getKey(), entry.getValue().longValue()));
         }
      }
      return set;
      
   }
   
   public boolean isPassivationExpirationSelfManaged()
   {
      return interval > 0;
   }
   
   public void processPassivationExpiration()
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
   }
   
   private ScopedKey getScopedKey(Object unscoped)
   {
      return new ScopedKey(unscoped, keyBase);
   }
   
   private static class ScopedKey
   {
      private Object unscoped;
      private Object keyBase;
      ScopedKey(Object unscoped, Object keyBase)
      {
         this.unscoped = unscoped;
         this.keyBase = keyBase;
      }
      
      @Override
      public int hashCode()
      {
         int result = 17;
         result += 31 * unscoped.hashCode();
         result += 31 * keyBase.hashCode();
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         
         if (obj instanceof ScopedKey)
         {
            ScopedKey other = (ScopedKey) obj;
            return (this.unscoped.equals(other.unscoped) 
                      && keyBase.equals(other.keyBase));
         }
         return false;
      }
   }
}
