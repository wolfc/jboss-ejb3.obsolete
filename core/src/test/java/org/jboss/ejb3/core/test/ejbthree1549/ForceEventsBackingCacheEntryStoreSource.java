/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ejb3.core.test.ejbthree1549;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;
import org.jboss.ejb3.cache.spi.impl.FilePersistentObjectStore;
import org.jboss.ejb3.stateful.StatefulBeanContext;

/**
 * {@link BackingCacheEntryStoreSource} for a non-clustered cache. Uses
 * a {@link FilePersistentObjectStore} store for persistence.
 * 
 * @author Brian Stansberry
 */
public class ForceEventsBackingCacheEntryStoreSource 
   implements BackingCacheEntryStoreSource<StatefulBeanContext>
{
   private Map<String, WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroup<StatefulBeanContext>>>> groupStores =
      new HashMap<String, WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroup<StatefulBeanContext>>>>();
   
   private Map<String, WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroupMember<StatefulBeanContext>>>> groupMemberStores =
      new HashMap<String, WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroupMember<StatefulBeanContext>>>>();
   
   public ForceEventsBackingCacheEntryStore<SerializationGroup<StatefulBeanContext>> getGroupStore(String containerName)
   {
      WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroup<StatefulBeanContext>>> ref = groupStores.get(containerName);
      return ref == null ? null : ref.get();
   }
   
   public ForceEventsBackingCacheEntryStore<SerializationGroupMember<StatefulBeanContext>> getGroupMemberStore(String containerName)
   {
      WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroupMember<StatefulBeanContext>>> ref = groupMemberStores.get(containerName);
      return ref == null ? null : ref.get();
   }
   
   public BackingCacheEntryStore<StatefulBeanContext, SerializationGroup<StatefulBeanContext>> createGroupIntegratedObjectStore(String containerName, String cacheConfigName,
         CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      BlockingPersistentObjectStore<SerializationGroup<StatefulBeanContext>> objectStore = new BlockingPersistentObjectStore<SerializationGroup<StatefulBeanContext>>();
      
      String storeNameSuffix = (cacheConfig.name().length() == 0) ? "" : "-" + cacheConfig;
      String storeName = "StdGroupStore" + storeNameSuffix;
      ForceEventsBackingCacheEntryStore<SerializationGroup<StatefulBeanContext>> store = 
         new ForceEventsBackingCacheEntryStore<SerializationGroup<StatefulBeanContext>>(objectStore, cacheConfig, storeName, true);
      
      groupStores.put(containerName, new WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroup<StatefulBeanContext>>>(store));
      
      return store;
   }

   public BackingCacheEntryStore<StatefulBeanContext, SerializationGroupMember<StatefulBeanContext>> createIntegratedObjectStore(String containerName, String cacheConfigName,
         CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      BlockingPersistentObjectStore<SerializationGroupMember<StatefulBeanContext>> objectStore = new BlockingPersistentObjectStore<SerializationGroupMember<StatefulBeanContext>>();
      
      ForceEventsBackingCacheEntryStore<SerializationGroupMember<StatefulBeanContext>> store = 
         new ForceEventsBackingCacheEntryStore<SerializationGroupMember<StatefulBeanContext>>(objectStore, cacheConfig, containerName, false);
      
      store.setInterval(1);
      
      groupMemberStores.put(containerName, new WeakReference<ForceEventsBackingCacheEntryStore<SerializationGroupMember<StatefulBeanContext>>>(store));
      
      return store;
   }
   
}
