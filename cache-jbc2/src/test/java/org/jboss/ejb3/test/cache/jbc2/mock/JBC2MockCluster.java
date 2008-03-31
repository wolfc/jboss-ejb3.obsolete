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

package org.jboss.ejb3.test.cache.jbc2.mock;

import javax.transaction.TransactionManager;

import org.jboss.cache.CacheManagerImpl;
import org.jboss.ejb3.cache.impl.backing.jbc2.JBCBackingCacheEntryStoreSource;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.tm.MockTransactionManager;
import org.jgroups.JChannelFactory;

/**
 * @author Brian Stansberry
 *
 */
public class JBC2MockCluster
{
   public static final String JGROUPS_STACKS = "test-jgroups-stacks.xml";
   public static final String JBC_CONFIGS = "test-jbc2-configs.xml";
   
   private TransactionManager tm0;
   private TransactionManager tm1;
   private JBCBackingCacheEntryStoreSource<MockBeanContext> storeSource0;
   private JBCBackingCacheEntryStoreSource<MockBeanContext> storeSource1;
   private JBC2MockClusterMember node0;
   private JBC2MockClusterMember node1;
   
   public JBC2MockCluster(boolean useCoordinator) throws Exception
   {
      this(MockTransactionManager.getInstance("node0"),
           MockTransactionManager.getInstance("node1"), 
           useCoordinator, CacheType.DISTRIBUTED);
   }
   
   public JBC2MockCluster(TransactionManager tm0, TransactionManager tm1, boolean useCoordinator, CacheType cacheType) throws Exception
   {
      this(tm0, tm1, useCoordinator, new CacheType[] { cacheType});
   }
   
   public JBC2MockCluster(TransactionManager tm0, TransactionManager tm1, boolean useCoordinator, CacheType[] availableTypes) throws Exception
   {
      this.tm0 = tm0;
      this.tm1 = tm1;
      
      JChannelFactory channelFactory0 = new JChannelFactory();
      channelFactory0.setMultiplexerConfig(JGROUPS_STACKS);
      CacheManagerImpl mgr0 = new CacheManagerImpl(JBC_CONFIGS, channelFactory0);
      mgr0.start();
      storeSource0 = new JBCBackingCacheEntryStoreSource<MockBeanContext>();
      storeSource0.setCacheManager(mgr0);
      
      JChannelFactory channelFactory1 = new JChannelFactory();
      channelFactory1.setMultiplexerConfig(JGROUPS_STACKS);
      CacheManagerImpl mgr1 = new CacheManagerImpl(JBC_CONFIGS, channelFactory1);
      mgr1.start();
      storeSource1 = new JBCBackingCacheEntryStoreSource<MockBeanContext>();
      storeSource1.setCacheManager(mgr1);
      
      node0 = new JBC2MockClusterMember(tm0, useCoordinator, availableTypes, storeSource0);
      node1 = new JBC2MockClusterMember(tm1, useCoordinator, availableTypes, storeSource1);
   }

   public TransactionManager getTm0()
   {
      return tm0;
   }

   public TransactionManager getTm1()
   {
      return tm1;
   }

   public JBC2MockClusterMember getNode0()
   {
      return node0;
   }

   public JBC2MockClusterMember getNode1()
   {
      return node1;
   }
   
   public MockBeanContainer[] deployBeanContainer(String containerName, String parentContainerName,
         CacheType cacheType, MockCacheConfig cacheConfig, boolean useXPC) throws Exception
   {
      return deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, "xpc0", "xpc1");
   }
   
   public MockBeanContainer[] deployBeanContainer(String containerName, String parentContainerName,
         CacheType cacheType, MockCacheConfig cacheConfig, String xpc0, String xpc1) throws Exception
   {
      MockBeanContainer[] result = new MockBeanContainer[2];
      result[0] = node0.deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc0);
      result[1] = node1.deployBeanContainer(containerName, parentContainerName, cacheType, cacheConfig, xpc1);
      return result;
   }
   
}
