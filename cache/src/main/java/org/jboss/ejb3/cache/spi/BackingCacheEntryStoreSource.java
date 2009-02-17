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

package org.jboss.ejb3.cache.spi;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.StatefulCacheFactory;

/**
 * Provides {@link BackingCacheEntryStore} instances to a 
 * {@link StatefulCacheFactory} that needs to create a 
 * {@link GroupAwareBackingCache}.
 * 
 * @author Brian Stansberry
 */
public interface BackingCacheEntryStoreSource<T extends CacheItem>
{
   /**
    * Provide a {@link BackingCacheEntryStore} for storage
    * of serialization groups.
    * 
    * @param containerName name of the container using this store's cache
    * @param cacheConfigName potentially aliased name of the cache configuration. 
    *                        Implementations should use this value in place
    *                        of {@link CacheConfig#name()}
    * @param config configuration details of the cache
    * @param transactionManager TransactionManager the store should use if
    *                           it needs to monitor transactions
    * @param synchronizationCoordinator SynchronizationCoordinator the store
    *                                   should use if it needs to add
    *                                   tranaction synchronizations
    * @return the store
    */
   BackingCacheEntryStore<T, SerializationGroup<T>> 
         createGroupIntegratedObjectStore(String containerName, 
                                          String cacheConfigName, 
                                          CacheConfig config, 
                                          TransactionManager transactionManager, 
                                          SynchronizationCoordinator synchronizationCoordinator);
   
   /**
    * Provide a {@link BackingCacheEntryStore} for storage
    * of serialization group members.
    * 
    * @param containerName name of the container using this store's cache
    * @param cacheConfigName TODO
    * @param transactionManager TransactionManager the store should use if
    *                           it needs to monitor transactions
    * @param synchronizationCoordinator SynchronizationCoordinator the store
    *                                   should use if it needs to add
    *                                   tranaction synchronizations
    * @param config configuration details of the cache
    * @return the store
    */
   BackingCacheEntryStore<T, SerializationGroupMember<T>> 
         createIntegratedObjectStore(String containerName, 
                                     String cacheConfigName, 
                                     CacheConfig cacheConfig, 
                                     TransactionManager transactionManager, 
                                     SynchronizationCoordinator synchronizationCoordinator);
}
