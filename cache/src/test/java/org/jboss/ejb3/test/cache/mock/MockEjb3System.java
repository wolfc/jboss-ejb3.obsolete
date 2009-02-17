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

package org.jboss.ejb3.test.cache.mock;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.cache.StatefulCacheFactory;
import org.jboss.ejb3.cache.impl.factory.GroupAwareCacheFactory;
import org.jboss.ejb3.cache.impl.factory.NonClusteredBackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.impl.factory.NonPassivatingCacheFactory;
import org.jboss.ejb3.cache.impl.factory.PassivationExpirationCoordinatorImpl;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.PassivationExpirationCoordinator;
import org.jboss.ejb3.cache.spi.impl.AbstractStatefulCacheFactory;
import org.jboss.ejb3.cache.spi.impl.SynchronizationCoordinatorImpl;
import org.jboss.ejb3.test.cache.mock.tm.MockTransactionManager;

/**
 * @author Brian Stansberry
 *
 */
public class MockEjb3System
{
   private StatefulCacheFactoryRegistry<MockBeanContext> cacheFactoryRegistry;
   private TransactionManager tm;
   private PassivationExpirationCoordinator coordinator;
   
   public MockEjb3System(boolean useCoordinator, CacheType cacheType)
   {
      this(MockTransactionManager.getInstance(), useCoordinator, cacheType);
   }
   
   public MockEjb3System(TransactionManager tm, 
                         boolean useCoordinator,
                         CacheType cacheType)
   {
      this(tm, useCoordinator, new CacheType[] { cacheType });
   }
   
   public MockEjb3System(TransactionManager tm, 
                         boolean useCoordinator,
                         CacheType[] availableTypes)
   {
      this.tm = tm;
      if (useCoordinator)
         coordinator = new PassivationExpirationCoordinatorImpl();
      
      cacheFactoryRegistry = new StatefulCacheFactoryRegistry<MockBeanContext>();
      if (availableTypes != null)
      {
         Map<String, StatefulCacheFactory<MockBeanContext>> factories = 
            new HashMap<String, StatefulCacheFactory<MockBeanContext>>();
         for (CacheType type : availableTypes)
         {
            StatefulCacheFactory<MockBeanContext> factory = buildCacheFactory(type);
            if (factory != null)
               factories.put(type.mapKey(), factory);
         }
         cacheFactoryRegistry.setFactories(factories);
      }
   }

   public StatefulCacheFactoryRegistry<MockBeanContext> getCacheFactoryRegistry()
   {
      return cacheFactoryRegistry;
   }

   public TransactionManager getTransactionManager()
   {
      return tm;
   }

   public PassivationExpirationCoordinator getCoordinator()
   {
      return coordinator;
   }
   
   public MockBeanContainer getMockBeanContainer(String containerName)
   {
      MockBeanContainer container = (MockBeanContainer) MockRegistry.get(containerName);
      if (container == null)
         throw new IllegalArgumentException("Container " + containerName + " not found");
      return container;
   }
   
   public MockBeanContainer deployBeanContainer(String containerName,
                                                String parentContainerName,
                                                CacheType cacheType) throws Exception
   {
      return deployBeanContainer(containerName, parentContainerName, cacheType, new MockCacheConfig(), null, true);
   }
   
   public MockBeanContainer deployBeanContainer(String containerName,
                                                String parentContainerName,
                                                CacheType cacheType,
                                                MockCacheConfig cacheConfig,
                                                String xpc) throws Exception
   {
      return deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc, true);
   }
   
   public MockBeanContainer deployBeanContainer(String containerName,
                                                String parentContainerName,
                                                CacheType cacheType,
                                                MockCacheConfig cacheConfig,
                                                String xpc,
                                                boolean start) throws Exception
   {
      MockBeanContainer parent = (parentContainerName == null) ? null : getMockBeanContainer(parentContainerName);
      MockBeanContainer container = new MockBeanContainer(containerName, cacheType.mapKey(), cacheFactoryRegistry, cacheConfig);
      container.setXPCName(xpc);
      if (parent != null)
         parent.addChild(container);
      if (start)
         container.start();
      return container;
   }
   
   protected StatefulCacheFactory<MockBeanContext> buildCacheFactory(CacheType type)
   {
      AbstractStatefulCacheFactory<MockBeanContext> factory = null;
      switch(type) {
         case NON_PASSIVATING:
            factory = buildNonPassivatingCacheFactory();
            break;
         case SIMPLE:
            factory = buildSimpleCacheFactory();
            break;
         case DISTRIBUTED:
            factory = buildDistributedCacheFactory();
            break;
         default:
            throw new IllegalArgumentException("Unknown type " + type);
      }
      
      if (factory != null)
      {
         factory.setTransactionManager(tm);
         factory.setPassivationExpirationCoordinator(coordinator);
         // Process passivation/expiration as quickly as possible so tests run fast
         factory.setDefaultPassivationExpirationInterval(1);
         if (tm instanceof MockTransactionManager)
         {
            SynchronizationCoordinatorImpl sci = new SynchronizationCoordinatorImpl();
            sci.setTransactionSynchronizationRegistrySource((MockTransactionManager) tm);
            factory.setSynchronizationCoordinator(sci);
         }
         factory.start();
      }
      return factory;
   }
   
   private AbstractStatefulCacheFactory<MockBeanContext> buildNonPassivatingCacheFactory()
   {
      return new NonPassivatingCacheFactory<MockBeanContext>();
   }
   
   private AbstractStatefulCacheFactory<MockBeanContext> buildSimpleCacheFactory()
   {
      NonClusteredBackingCacheEntryStoreSource<MockBeanContext> source = 
         new NonClusteredBackingCacheEntryStoreSource<MockBeanContext>();
      return new GroupAwareCacheFactory<MockBeanContext>(source);
   }
   
   private AbstractStatefulCacheFactory<MockBeanContext> buildDistributedCacheFactory()
   {
     BackingCacheEntryStoreSource<MockBeanContext> storeSource = getDistributedStoreSource();
     return storeSource == null ? null : new GroupAwareCacheFactory<MockBeanContext>(storeSource);
   }
   
   protected BackingCacheEntryStoreSource<MockBeanContext> getDistributedStoreSource()
   {
      throw new UnsupportedOperationException("Distributed caching not supported");
   }
   
}
