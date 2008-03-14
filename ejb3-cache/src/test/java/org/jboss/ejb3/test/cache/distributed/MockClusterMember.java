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

import org.jboss.ejb3.cache.spi.IntegratedObjectStoreSource;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockEjb3System;

/**
 * @author Brian Stansberry
 */
public class MockClusterMember extends MockEjb3System
{
   private UnmarshallingMap localDistributedCacheMember;
   private UnmarshallingMap remoteDistributedCacheMember;
   private ClassLoader localClassLoader;
   
   public MockClusterMember(TransactionManager tm, 
                            boolean useCoordinator, 
                            CacheType cacheType,
                            UnmarshallingMap local,
                            UnmarshallingMap remote)
   {
      this(tm, useCoordinator, new CacheType[] {cacheType}, local, remote);
   }

   public MockClusterMember(TransactionManager tm, 
         boolean useCoordinator, 
         CacheType[] availableTypes,
         UnmarshallingMap local,
         UnmarshallingMap remote)
   {
      super(tm, useCoordinator, availableTypes);
      this.localDistributedCacheMember = local;
      this.remoteDistributedCacheMember =remote;
      
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      localClassLoader = new ClassLoader(tccl) {};
      
      // Kluge. Rebuild the distributed factory, as the superclass
      // didn't have the maps available when it did it
      for (CacheType type : availableTypes)
      {
         if (type == CacheType.DISTRIBUTED)
            getCacheFactoryRegistry().addCacheFactory(type.mapKey(), buildCacheFactory(type));
      }
   }

   public UnmarshallingMap getLocalDistributedCacheMember()
   {
      return localDistributedCacheMember;
   }

   public UnmarshallingMap getRemoteDistributedCacheMember()
   {
      return remoteDistributedCacheMember;
   }

   public ClassLoader getLocalClassLoader()
   {
      return localClassLoader;
   }   
   
   public boolean setTCCL()
   {
      if (localClassLoader == Thread.currentThread().getContextClassLoader())
         return false;
      Thread.currentThread().setContextClassLoader(localClassLoader);
      return true;
   }
   
   public void restoreTCCL()
   {
      ClassLoader current = Thread.currentThread().getContextClassLoader();
      if (current == localClassLoader)
      {
         Thread.currentThread().setContextClassLoader(localClassLoader.getParent());
      }
      else if (current != localClassLoader.getParent())
      {
         throw new IllegalStateException("Current TCCL is neither localClassLoader nor its parent");
      }
   }

   @Override
   protected IntegratedObjectStoreSource<MockBeanContext> getDistributedStoreSource()
   {
      return new MockIntegratedObjectStoreSource<MockBeanContext>(localDistributedCacheMember, 
                                                                  remoteDistributedCacheMember);
   }

   @Override
   public MockBeanContainer deployBeanContainer(String containerName, String parentContainerName,
         CacheType cacheType, MockCacheConfig cacheConfig, String xpc) throws Exception
   {
      boolean tcclSet = setTCCL();
      try
      {
         return super.deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc);
      }
      finally
      {
         if (tcclSet)
            restoreTCCL();
      }
   }

   @Override
   public MockBeanContainer getMockBeanContainer(String containerName)
   {
      boolean tcclSet = setTCCL();
      try
      {
         return super.getMockBeanContainer(containerName);
      }
      finally
      {
         if (tcclSet)
            restoreTCCL();
      }
   }
   
   
   
   
}
