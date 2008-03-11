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

package org.jboss.ejb3.test.distributed;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockXPC;
import org.jboss.ejb3.test.cache.mock.tm.MockTransactionManager;

/**
 * @author Brian Stansberry
 *
 */
public class MockCluster
{
   private TransactionManager tm0;
   private TransactionManager tm1;
   private UnmarshallingMap map0;
   private UnmarshallingMap map1;
   private MockClusterMember node0;
   private MockClusterMember node1;
   
   public MockCluster(boolean useCoordinator)
   {
      this(MockTransactionManager.getInstance("node0"),
           MockTransactionManager.getInstance("node1"), 
           useCoordinator, CacheType.DISTRIBUTED);
   }
   
   public MockCluster(TransactionManager tm0, TransactionManager tm1, boolean useCoordinator, CacheType cacheType)
   {
      this(tm0, tm1, useCoordinator, new CacheType[] { cacheType});
   }
   
   public MockCluster(TransactionManager tm0, TransactionManager tm1, boolean useCoordinator, CacheType[] availableTypes)
   {
      this.tm0 = tm0;
      this.tm1 = tm1;
      
      map0 = new UnmarshallingMap();
      map1 = new UnmarshallingMap();
      
      node0 = new MockClusterMember(tm0, useCoordinator, availableTypes, map0, map1);
      node1 = new MockClusterMember(tm1, useCoordinator, availableTypes, map1, map0);
   }

   public TransactionManager getTm0()
   {
      return tm0;
   }

   public TransactionManager getTm1()
   {
      return tm1;
   }

   public UnmarshallingMap getMap0()
   {
      return map0;
   }

   public UnmarshallingMap getMap1()
   {
      return map1;
   }

   public MockClusterMember getNode0()
   {
      return node0;
   }

   public MockClusterMember getNode1()
   {
      return node1;
   }
   
   public MockBeanContainer[] deployBeanContainer(String containerName, String parentContainerName,
         CacheType cacheType, MockCacheConfig cacheConfig, boolean useXPC) throws Exception
   {
      MockXPC xpc0 = useXPC ? new MockXPC() : null;
      MockXPC xpc1 = useXPC ? new MockXPC() : null;
      return deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc0, xpc1);
   }
   
   public MockBeanContainer[] deployBeanContainer(String containerName, String parentContainerName,
         CacheType cacheType, MockCacheConfig cacheConfig, MockXPC xpc0, MockXPC xpc1) throws Exception
   {
      MockBeanContainer[] result = new MockBeanContainer[2];
      result[0] = node0.deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc0);
      result[1] = node1.deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc1);
      return result;
   }
   
}
