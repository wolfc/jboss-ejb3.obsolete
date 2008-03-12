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
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntry;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.logging.Logger;

/**
 * Abstract superclass for {@link PassivatingIntegratedObjectStore} 
 * implementations.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public abstract class AbstractPassivatingIntegratedObjectStore<C extends CacheItem, T extends BackingCacheEntry<C>>
      implements PassivatingIntegratedObjectStore<C, T>
{
   private static final Logger log = Logger.getLogger(AbstractPassivatingIntegratedObjectStore.class);
   
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
   private boolean stopped = true;
   
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
   
   protected abstract void runExpiration();

   protected abstract void runPassivation();

   // --------------------------------------------------  IntegratedObjectStore

   public void start()
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
      
      if (!stopped)
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

   public boolean isStopped()
   {
      return stopped;
   }

   public void setMaxSize(int maxSize)
   {
      this.maxSize = maxSize;
   }
   
   

}
