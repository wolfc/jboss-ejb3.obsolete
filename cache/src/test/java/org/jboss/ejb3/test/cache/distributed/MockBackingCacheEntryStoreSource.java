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

package org.jboss.ejb3.test.cache.distributed;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStore;
import org.jboss.ejb3.cache.spi.SerializationGroup;
import org.jboss.ejb3.cache.spi.SerializationGroupMember;
import org.jboss.ejb3.cache.spi.SynchronizationCoordinator;

/**
 * @author Brian Stansberry
 *
 */
public class MockBackingCacheEntryStoreSource<T extends CacheItem> 
   implements BackingCacheEntryStoreSource<T>
{
   private UnmarshallingMap localMap;
   private UnmarshallingMap remoteMap;
   
   /**
    * For use by unit tests that aren't actually interested in the
    * store contents.
    */
   public MockBackingCacheEntryStoreSource()
   {
      this(new UnmarshallingMap(), new UnmarshallingMap());
   }
   
   public MockBackingCacheEntryStoreSource(UnmarshallingMap localMap, UnmarshallingMap remoteMap)
   {
      this.localMap = localMap;
      this.remoteMap = remoteMap;
   }
   
   public BackingCacheEntryStore<T, SerializationGroup<T>>  createGroupIntegratedObjectStore(String containerName,
         String cacheConfigName, CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      String keyBase = "GroupCache-" + containerName;
      return new MockJBCBackingCacheEntryStore<T, SerializationGroup<T>>(localMap, remoteMap, cacheConfig, keyBase, keyBase, true);
   }

   public BackingCacheEntryStore<T, SerializationGroupMember<T>>  createIntegratedObjectStore(String containerName, String cacheConfigName,
         CacheConfig cacheConfig, TransactionManager transactionManager, SynchronizationCoordinator synchronizationCoordinator)
   {
      return new MockJBCBackingCacheEntryStore<T, SerializationGroupMember<T>>(localMap, remoteMap, cacheConfig, containerName, containerName, false);
   }

}
