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
import org.jboss.ejb3.cache.spi.GroupAwareBackingCache;
import org.jboss.ejb3.cache.spi.IntegratedObjectStoreSource;
import org.jboss.ejb3.cache.spi.PassivatingBackingCache;
import org.jboss.ejb3.cache.spi.PassivatingIntegratedObjectStore;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.impl.AbstractStatefulCacheFactory;

/**
 * {@link StatefulCacheFactory} implementation that can return a group-aware 
 * cache.  How the cache functions depends on the behavior of the 
 * {@link PassivatingIntegratedObjectStore} implementations returned by
 * the injected {@link IntegratedObjectStoreSource}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class GroupAwareCacheFactory<T extends CacheItem> 
   extends AbstractStatefulCacheFactory<T>
{
   private final Map<String, PassivatingBackingCache<T, SerializationGroup<T>>> groupCaches;
   private final IntegratedObjectStoreSource<T> storeSource;
   
   /**
    * Creates a new GroupAwareCacheFactory that gets its object stores from
    * the provided source.
    */
   public GroupAwareCacheFactory(IntegratedObjectStoreSource<T> storeSource)
   {
      assert storeSource != null : "storeSource is null";
      
      this.storeSource = storeSource;
      this.groupCaches = new HashMap<String, PassivatingBackingCache<T, SerializationGroup<T>>>();
   }

   // --------------------------------------------------- StatefulCacheFactory
   
   public Cache<T> createCache(String containerName, 
                               StatefulObjectFactory<T> factory, 
                               PassivationManager<T> passivationManager, CacheConfig cacheConfig)
   {
      String configName = getCacheConfigName(cacheConfig);
      
      PassivatingBackingCache<T, SerializationGroup<T>> groupCache = groupCaches.get(configName);
      if (groupCache == null)
      {
         groupCache = createGroupCache(containerName, configName, cacheConfig);
         groupCaches.put(configName, groupCache);
      }
      
      PassivatingIntegratedObjectStore<T, SerializationGroupMember<T>> store = 
         storeSource.createIntegratedObjectStore(containerName, configName, cacheConfig, getTransactionManager(), getSynchronizationCoordinator());
      
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
      
      SerializationGroupMemberContainer<T> memberContainer = 
         new SerializationGroupMemberContainer<T>(factory, passivationManager, store, groupCache);
      
      GroupAwareBackingCache<T, SerializationGroupMember<T>> backingCache =
         new GroupAwareBackingCacheImpl<T>(memberContainer, groupCache);
      
      return new GroupAwareTransactionalCache<T, SerializationGroupMember<T>>(backingCache, getTransactionManager(), getSynchronizationCoordinator(), getStrictGroups());
   }

   private PassivatingBackingCache<T, SerializationGroup<T>> createGroupCache(String name, String configName, CacheConfig cacheConfig)
   {
      SerializationGroupContainer<T> container = new SerializationGroupContainer<T>();
      StatefulObjectFactory<SerializationGroup<T>> factory = container;
      PassivationManager<SerializationGroup<T>> passivationManager = container;
      PassivatingIntegratedObjectStore<T, SerializationGroup<T>> store = 
         storeSource.createGroupIntegratedObjectStore(name, configName, cacheConfig, getTransactionManager(), getSynchronizationCoordinator());
    
      // The group cache store should not passivate/expire -- that's a 
      // function of the caches for the members
      store.setInterval(0);
      
      PassivatingBackingCache<T, SerializationGroup<T>> groupCache =
         new PassivatingBackingCacheImpl<T, SerializationGroup<T>>(factory, passivationManager, store);
      
      container.setGroupCache(groupCache);
      
      groupCache.start();
      
      return groupCache;
   }
}
