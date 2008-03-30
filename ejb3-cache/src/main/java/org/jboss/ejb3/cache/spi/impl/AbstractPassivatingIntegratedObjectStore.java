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

package org.jboss.ejb3.cache.spi.impl;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.api.CacheItem;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.PassivatingBackingCacheEntry;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.logging.Logger;

/**
 * Abstract superclass for {@link PassivatingIntegratedObjectStore} 
 * implementations.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public abstract class AbstractPassivatingIntegratedObjectStore<C extends CacheItem, T extends PassivatingBackingCacheEntry<C>, K>
      implements PassivatingIntegratedObjectStore<C, T>
{
   protected Logger log = Logger.getLogger(getClass().getName());
   
   /**
    * Support callbacks when our SessionTimeoutThread decides to
    * evict an entry.
    */
   private PassivatingBackingCache<C, T> owningCache;   
   private boolean forGroups;
   private int interval;
   private int maxSize;
   private long idleTimeSeconds;
   private long expirationTimeSeconds;   
   private PassivationExpirationRunner sessionTimeoutRunner;
   private final String name;
   private boolean running = true;
   
   /**
    * Create a new AbstractPassivatingIntegratedObjectStore.
    */
   protected AbstractPassivatingIntegratedObjectStore(CacheConfig config,
                                                      String name,
                                                      boolean forGroups)
   {
      assert config != null : "config is null";
      assert name != null : "name is null";
      
      this.idleTimeSeconds = config.idleTimeoutSeconds();
      this.expirationTimeSeconds = config.removalTimeoutSeconds();
      this.maxSize = config.maxSize();
      this.name = name;
      this.forGroups = forGroups;
   }

   // ---------------------------------------------------------------- Abstract
   
   /**
    * Invoked by {@link #processPassivationExpiration()} to indicate the
    * item associated with the given key needs to be passivated.
    * 
    * @param key  key for the item
    * @param lastUse time item was last used
    */
   protected abstract void processPassivation(K key, long lastUse);
   /**
    * Invoked by {@link #processPassivationExpiration()} to indicate the
    * item associated with the given key needs to be expired.
    * 
    * @param key  key for the item
    * @param lastUse time item was last used
    */
   protected abstract void processExpiration(K key, long lastUse);

   /**
    * Get a set of {@link CacheableTimestamp} representing the items currently
    * in memory.
    * 
    * @return array of {@link CacheableTimestamp}, sorted by 
    *         {@link CacheableTimestamp#getLastUsed() last use}, with least
    *         recently used items first.
    */
   protected abstract CacheableTimestamp<K>[] getInMemoryEntries();
   
   /**
    * Get a set of {@link CacheableTimestamp} representing all items, both
    * those in memory and those passivated.
    * 
    * @return array of {@link CacheableTimestamp}, sorted by 
    *         {@link CacheableTimestamp#getLastUsed() last use}, with least
    *         recently used items first.
    */
   protected abstract CacheableTimestamp<K>[] getAllEntries();
   
   /**
    * Get the number of items in memory.
    */
   public abstract int getInMemoryCount();
   
   /**
    * Get the number of passivated items.
    */
   public abstract int getPassivatedCount();

   // --------------------------------------------------  IntegratedObjectStore

   public void start()
   {    
      log.debug("Starting store " + name);
      
      internalStart();
      
      running = true;
      
      log.debug("Started store " + name);
   }
   
   protected void internalStart()
   {
      if (!forGroups && interval > 0)
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
   }

   public void stop()
   {     
      log.debug("Stopping store " + name);
      
      running = false;
      
      internalStop();      
      
      log.debug("Stopped store " + name);
   }
   
   protected void internalStop()
   {     
      if (sessionTimeoutRunner != null)
      {
         sessionTimeoutRunner.stop();
      }
   }

   // ---------------------------------------  PassivatingIntegratedObjectStore


   public void setPassivatingCache(PassivatingBackingCache<C, T> cache)
   {
      this.owningCache = cache;      
   }
   
   public PassivatingBackingCache<C, T> getPassivatingCache()
   {
      return owningCache;
   }
   
   public int getInterval()
   {
      return interval;
   }

   public void setInterval(int seconds)
   {
      this.interval = seconds;      
   }
   
   public void processPassivationExpiration()
   {
      // Group passivation/expiration is a function of its members
      if (forGroups)
         return;
      
      if (running)
      {
         try
         {
            runPassivation();               
         }
         catch (Exception e)
         {
            log.error("Caught exception processing passivations", e);
         }
      }
      
      if (running)
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
   
   public boolean isPassivationExpirationSelfManaged()
   {
      return interval > 0;
   }
   
   public int getMaxSize()
   {
      return maxSize;
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

   public boolean isForGroups()
   {
      return forGroups;
   }

   public String getName()
   {
      return name;
   }

   public boolean isRunning()
   {
      return running;
   }

   public void setMaxSize(int maxSize)
   {
      this.maxSize = maxSize;
   }
   
   // --------------------------------------------------------------  Protected
   
   
   
   // ----------------------------------------------------------------  Private

   private void runExpiration()
   {
      if (!isForGroups() && getExpirationTimeSeconds() > 0)
      {
         long now = System.currentTimeMillis();
         long minRemovalUse = now - (getExpirationTimeSeconds() * 1000);                     
         for (CacheableTimestamp<K> ts : getAllEntries())
         {
            try
            {
               if (running && minRemovalUse >= ts.getLastUsed())
               {
                  processExpiration(ts.getId(), ts.getLastUsed());
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

   private void runPassivation()
   {
      if (!isForGroups() 
            && (getMaxSize() > 0 || getIdleTimeSeconds() > 0))
      {
         long now = System.currentTimeMillis();
         long minPassUse = (getIdleTimeSeconds() > 0 ? now - (getIdleTimeSeconds() * 1000) : 0);
         
         CacheableTimestamp<K>[] timestamps = getInMemoryEntries();
         int overCount = (getMaxSize() > 0 ? timestamps.length - getMaxSize() : 0);
         for (CacheableTimestamp<K> ts : timestamps)
         {
            try
            {
               if (running && (overCount > 0 || minPassUse >= ts.getLastUsed()))
               {
                  log.trace("attempting to passivate " + ts.getId());
                  processPassivation(ts.getId(), ts.getLastUsed());
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
}
