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

package org.jboss.ejb3.cache.impl.factory;

import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulCacheFactory;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.impl.GroupAwareTransactionalCache;
import org.jboss.ejb3.cache.impl.backing.GroupAwareBackingCacheImpl;
import org.jboss.ejb3.cache.impl.backing.PassivatingBackingCacheImpl;
import org.jboss.ejb3.cache.impl.backing.SerializationGroupContainer;
import org.jboss.ejb3.cache.impl.backing.SerializationGroupMemberContainer;
import org.jboss.ejb3.cache.spi.BackingCacheLifecycleListener;
import org.jboss.ejb3.cache.spi.GroupAwareBackingCache;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.impl.AbstractStatefulCacheFactory;
import org.jboss.logging.Logger;

/**
 * {@link StatefulCacheFactory} implementation that can return a group-aware 
 * cache.  How the cache functions depends on the behavior of the 
 * {@link BackingCacheEntryStore} implementations returned by
 * the injected {@link BackingCacheEntryStoreSource}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class GroupAwareCacheFactory<T extends CacheItem> 
   extends AbstractStatefulCacheFactory<T>
{
   private static final Logger log = Logger.getLogger(GroupAwareCacheFactory.class);
   
   private final Map<String, GroupCacheTracker> groupCaches;
   private final BackingCacheEntryStoreSource<T> storeSource;
   
   /**
    * Creates a new GroupAwareCacheFactory that gets its object stores from
    * the provided source.
    */
   public GroupAwareCacheFactory(BackingCacheEntryStoreSource<T> storeSource)
   {
      assert storeSource != null : "storeSource is null";
      
      this.storeSource = storeSource;
      this.groupCaches = new HashMap<String, GroupCacheTracker>();
   }

   // --------------------------------------------------- StatefulCacheFactory
   
   public Cache<T> createCache(String containerName, 
                               StatefulObjectFactory<T> factory, 
                               PassivationManager<T> passivationManager, 
                               CacheConfig cacheConfig)
   {
      // Figure out our cache's name
      String configName = getCacheConfigName(cacheConfig);
      
      // Create/find the cache for SerializationGroup that the container
      // may be associated with
      PassivatingBackingCache<T, SerializationGroup<T>> groupCache = null;
      GroupMemberCacheLifecycleListener listener = null;
      
      synchronized (groupCaches)
      {
         GroupCacheTracker tracker = groupCaches.get(configName);
         if (tracker == null)
         {
            groupCache = createGroupCache(configName, cacheConfig);
            listener = new GroupMemberCacheLifecycleListener(configName);
            groupCaches.put(configName, new GroupCacheTracker(groupCache, listener));
         }
         else
         {
            groupCache = tracker.groupCache;
            listener = tracker.listener;
         }
      }
      
      // Create the store for SerializationGroupMembers from the container
      BackingCacheEntryStore<T, SerializationGroupMember<T>> store = 
         storeSource.createIntegratedObjectStore(containerName, configName, cacheConfig, 
                                                 getTransactionManager(), getSynchronizationCoordinator());
      
      // Make sure passivation/expiration occurs periodically
      if (store.getInterval() < 1)
      {
         if (getPassivationExpirationCoordinator() != null)
         {
            // Let our coordinator manage this
            getPassivationExpirationCoordinator().addPassivationExpirationProcessor(store);
         }
         else 
         {
            // Tell the store to manage this itself, using our default interval
            store.setInterval(getDefaultPassivationExpirationInterval());
         }
      }
      // else the store is configured to manage processing itself
      
      // Set up the backing cache with the store and group cache
      SerializationGroupMemberContainer<T> memberContainer = 
         new SerializationGroupMemberContainer<T>(factory, passivationManager, store, groupCache);      
      GroupAwareBackingCache<T, SerializationGroupMember<T>> backingCache =
         new GroupAwareBackingCacheImpl<T>(memberContainer, groupCache);
      
      // Listen for backing cache lifecycle changes so we know when to start/stop groupCache
      backingCache.addLifecycleListener(listener);
      
      // Finally, the front-end cache
      Cache<T> cache = new GroupAwareTransactionalCache<T, SerializationGroupMember<T>>(backingCache, getTransactionManager(), getSynchronizationCoordinator(), getStrictGroups());
      
      log.debug("Created cache for " + containerName);
      
      return cache;      
   }

   private PassivatingBackingCache<T, SerializationGroup<T>> createGroupCache(String configName, CacheConfig cacheConfig)
   {
      BackingCacheEntryStore<T, SerializationGroup<T>> store = 
         storeSource.createGroupIntegratedObjectStore(configName, configName, cacheConfig, 
                                                      getTransactionManager(), 
                                                      getSynchronizationCoordinator());
    
      // The group cache store should not passivate/expire -- that's a 
      // function of the caches for the members
      store.setInterval(0);
      
      SerializationGroupContainer<T> container = new SerializationGroupContainer<T>();
      
      PassivatingBackingCache<T, SerializationGroup<T>> groupCache =
         new PassivatingBackingCacheImpl<T, SerializationGroup<T>>(container, container, store);
      
      container.setGroupCache(groupCache);
      
      log.debug("Created groupCache for " + configName);
      
      return groupCache;
   }
   
   private void groupMemberCacheStarting(String cacheConfigName)
   {
      synchronized (groupCaches)
      {
         GroupCacheTracker tracker = groupCaches.get(cacheConfigName);
         if (tracker == null)
            throw new IllegalStateException("unknown cacheConfigName " + cacheConfigName);
         
         if (tracker.incrementLiveMemberCount() == 1)
         {
            // First group member cache is about to start; we need to
            // start the groupCache            
            log.debug("Starting groupCache for " + cacheConfigName);
            tracker.groupCache.start();
         }
      }
   }
   
   private void groupMemberCacheStopped(String cacheConfigName)
   {
      synchronized (groupCaches)
      {
         GroupCacheTracker tracker = groupCaches.get(cacheConfigName);
         if (tracker == null)
            throw new IllegalStateException("unknown cacheConfigName " + cacheConfigName);
         
         if (tracker.decrementLiveMemberCount() == 0)
         {
            // All group member caches have stopped; we need to
            // stop the groupCache           
            log.debug("Stopping groupCache for " + cacheConfigName);
            tracker.groupCache.stop();
         }
      }
   }
   
   private class GroupCacheTracker
   {
      private final PassivatingBackingCache<T, SerializationGroup<T>> groupCache;
      private final GroupMemberCacheLifecycleListener listener;
      private int liveMemberCount;
      
      private GroupCacheTracker(PassivatingBackingCache<T, SerializationGroup<T>> groupCache,
                                GroupMemberCacheLifecycleListener listener)
      {
         assert groupCache != null : "groupCache is null";
         assert listener != null : "listener is null";
         
         this.groupCache = groupCache;
         this.listener = listener;
      }
      
      private int incrementLiveMemberCount()
      {
         return ++liveMemberCount;
      }
      
      private int decrementLiveMemberCount()
      {
         return --liveMemberCount;
      }
   }
   
   /**
    * Listens for lifecycle changes on group member caches so we
    * know when to start/stop the group cache.
    *
    */
   private class GroupMemberCacheLifecycleListener
      implements BackingCacheLifecycleListener
   {
      private final String cacheConfigName;
      
      private GroupMemberCacheLifecycleListener(String cacheConfigName)
      {
         assert cacheConfigName != null : "cacheConfigName is null";
         
         this.cacheConfigName = cacheConfigName;
      }

      public void lifecycleChange(LifecycleState newState)
      {
         switch(newState)
         {
            case STARTING:
               groupMemberCacheStarting(cacheConfigName);
               break;
            case STOPPED:
               groupMemberCacheStopped(cacheConfigName);
               break;
            default:
               break;
         }         
      }
      
   } 
}
