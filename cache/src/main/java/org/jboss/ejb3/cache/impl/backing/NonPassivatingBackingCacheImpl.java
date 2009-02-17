/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.spi.BackingCache;
import org.jboss.ejb3.cache.spi.PassivationExpirationProcessor;
import org.jboss.ejb3.cache.spi.BackingCacheLifecycleListener.LifecycleState;
import org.jboss.ejb3.cache.spi.impl.AbstractBackingCache;
import org.jboss.ejb3.cache.spi.impl.PassivationExpirationRunner;

/**
 * Simple {@link BackingCache} that doesn't handle passivation (although
 * it does handle expiration).  Pure in-VM memory cache. Not group-aware,
 * as there is no point in managing groups if there is no serialization.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class NonPassivatingBackingCacheImpl<C extends CacheItem> 
   extends AbstractBackingCache<C> 
   implements BackingCache<C, NonPassivatingBackingCacheEntry<C>>, PassivationExpirationProcessor
{
   private final StatefulObjectFactory<C> factory;
   private final Map<Object, NonPassivatingBackingCacheEntry<C>> cache;
   private final String name;
   private long interval;
   private int expirationTimeSeconds;   
   private PassivationExpirationRunner sessionTimeoutRunner;
   private boolean stopped = true;
   
   public NonPassivatingBackingCacheImpl(StatefulObjectFactory<C> factory, String name)
   {
      assert factory != null : "factory is null";
      assert name != null : "name is null";
      
      this.factory = factory;
      this.name = name;
      
      this.cache = new ConcurrentHashMap<Object, NonPassivatingBackingCacheEntry<C>>();
   }
   
   public NonPassivatingBackingCacheEntry<C> create(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      C obj = factory.create(initTypes, initValues, sharedState);
      NonPassivatingBackingCacheEntry<C> entry = new NonPassivatingBackingCacheEntry<C>(obj);
      cache.put(obj.getId(), entry);      
      return entry;
   }

   public NonPassivatingBackingCacheEntry<C> get(Object key) throws NoSuchEJBException
   {
      NonPassivatingBackingCacheEntry<C> entry = cache.get(key);
      if(entry == null)
         throw new NoSuchEJBException(String.valueOf(key));
      if(entry.isInUse())
         throw new IllegalStateException("entry " + entry + " is already in use");
      entry.setInUse(true);
      return entry;
   }

   public NonPassivatingBackingCacheEntry<C> peek(Object key) throws NoSuchEJBException
   {
      NonPassivatingBackingCacheEntry<C> entry = cache.get(key);
      if(entry == null)
         throw new NoSuchEJBException(String.valueOf(key));
      return entry;
   }
   
   public NonPassivatingBackingCacheEntry<C> release(Object key)
   {
      NonPassivatingBackingCacheEntry<C> entry = cache.get(key);
      if(entry == null)
         throw new NoSuchEJBException(String.valueOf(key));
      if(!entry.isInUse())
         throw new IllegalStateException("entry " + key + " is not in use");
      entry.setInUse(false);
      return entry;
   }
   
   public void remove(Object key)
   {
      NonPassivatingBackingCacheEntry<C> entry = cache.remove(key);
      if(entry != null && entry.isInUse())
         entry.setInUse(false);  
      if(entry != null)
         factory.destroy(entry.getUnderlyingItem());
   } 
   
   public boolean isClustered()
   {
      return false;
   }

   public void start()
   {
      notifyLifecycleListeners(LifecycleState.STARTING);
      try
      {
         if (interval > 0)
         {
            if (sessionTimeoutRunner == null)
            {
               assert name != null : "name has not been set";
               String timerName = "PassivationExpirationTimer-" + name;
               sessionTimeoutRunner = new PassivationExpirationRunner(this, timerName, interval);
            }
            sessionTimeoutRunner.start();
         }     
         
         stopped = false;
         
         notifyLifecycleListeners(LifecycleState.STARTED);
         
         log.debug("Started " + name);
      }
      catch (RuntimeException e)
      {
         notifyLifecycleListeners(LifecycleState.FAILED);
         throw e;
      }
   }

   public void stop()
   {
      notifyLifecycleListeners(LifecycleState.STOPPING);
      try
      {
         if (sessionTimeoutRunner != null)
         {
            sessionTimeoutRunner.stop();
         }      
         
         stopped = true;
         
         notifyLifecycleListeners(LifecycleState.STOPPED);
         
         log.debug("Stopped " + name);
      }
      catch (RuntimeException e)
      {
         notifyLifecycleListeners(LifecycleState.FAILED);
         throw e;
      }
   }  
   
   public long getInterval()
   {
      return interval;
   }

   public void setInterval(long interval)
   {
      this.interval = interval;
   }

   public String getName()
   {
      return name;
   }

   public int getExpirationTimeSeconds()
   {
      return expirationTimeSeconds;
   }

   public void setExpirationTimeSeconds(int expirationTimeSeconds)
   {
      this.expirationTimeSeconds = expirationTimeSeconds;
   }

   public boolean isPassivationExpirationSelfManaged()
   {
      return interval > 0;
   }

   public void processPassivationExpiration()
   {
      if (!stopped && expirationTimeSeconds > 0)
      {
         // FIXME -- this is totally brute force
         
         long now = System.currentTimeMillis();
         long minRemovalUse = now - (expirationTimeSeconds * 1000); 
         Set<NonPassivatingBackingCacheEntry<C>> entries = new HashSet<NonPassivatingBackingCacheEntry<C>>(cache.values());
         
         for (NonPassivatingBackingCacheEntry<C> entry : entries)
         {
            if (!entry.isInUse() && minRemovalUse >= entry.getLastUsed())
            {
               try
               {
                  remove(entry.getId());               
               }
               catch (IllegalStateException ise)
               {
                  // Not so great; we're assuming it's 'cause item's in use
                  log.trace("skipping in-use entry " + entry.getId(), ise);
               }
            }
         }    
      }      
   }
   
}
