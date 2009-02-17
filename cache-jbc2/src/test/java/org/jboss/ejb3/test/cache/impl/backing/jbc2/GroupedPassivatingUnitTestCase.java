/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.cache.impl.backing.jbc2;

import org.jboss.ejb3.test.cache.integrated.Ejb3CacheTestCaseBase;
import org.jboss.ejb3.test.cache.jbc2.mock.JBC2MockCluster;
import org.jboss.ejb3.test.cache.jbc2.mock.JBC2MockClusterMember;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockEntity;
import org.jboss.ejb3.test.cache.mock.MockPassivationManager;
import org.jboss.ejb3.test.cache.mock.MockXPC;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author Brian Stansberry
 * @version $Revision: 65920 $
 */
public class GroupedPassivatingUnitTestCase extends Ejb3CacheTestCaseBase
{
   private static final Logger log = Logger.getLogger(GroupedPassivatingUnitTestCase.class);
   
   public void testSimpleGroupPassivation() throws Exception
   {      
      log.info("====== testSimpleGroupPassivation() ======");
      simpleGroupPassivationTest(false);
   }
   
   public void testSimpleGroupPassivationTransactional() throws Exception
   {      
      log.info("====== testSimpleGroupPassivationTransactional() ======");
      simpleGroupPassivationTest(true);
   }
   
   public void simpleGroupPassivationTest(boolean transactional) throws Exception
   {      
      JBC2MockCluster cluster = new JBC2MockCluster(false);
      JBC2MockClusterMember node0 = cluster.getNode0();
      MockCacheConfig cacheConfig = new MockCacheConfig();
      cacheConfig.setIdleTimeoutSeconds(1);
      MockXPC sharedXPC = new MockXPC("XPCA");
      MockBeanContainer container1 = node0.deployBeanContainer("MockBeanContainer1", null, CacheType.DISTRIBUTED, cacheConfig, sharedXPC.getName());
      MockBeanContainer container2 = node0.deployBeanContainer("MockBeanContainer2", "MockBeanContainer1", CacheType.DISTRIBUTED, cacheConfig, sharedXPC.getName());
      
      node0.setTCCL();
      try
      {
         if (transactional)
         {
            node0.getTransactionManager().begin();
         }
         Object key1 = container1.getCache().create(null, null);
         MockBeanContext firstCtx1;
         MockBeanContext ctx1 = firstCtx1 = container1.getCache().get(key1);
         
         Object key2 = ctx1.getChild(container2.getName());
         MockBeanContext ctx2 = container2.getCache().get(key2);
         
         assertNotNull(ctx1.getXPC());
         assertEquals(ctx1.getXPC(), ctx2.getXPC());
         
         container2.getCache().release(ctx2);
         container1.getCache().release(ctx1);
         
         if (transactional)
         {
            node0.getTransactionManager().commit();
         }
         
         sleep(2100);
         
         MockPassivationManager pass1 = (MockPassivationManager) container1.getPassivationManager();
         MockPassivationManager pass2 = (MockPassivationManager) container2.getPassivationManager();
         
         assertEquals("ctx1 should have been passivated", 1, pass1.getPrePassivateCount());
         assertEquals("ctx2 should have been passivated", 1, pass2.getPrePassivateCount());
         
         if (transactional)
         {
            node0.getTransactionManager().begin();
         }
         
         ctx2 = container2.getCache().get(key2);
         
         log.info("ctx2 = " + ctx2);
         assertNotNull(ctx2);
         
         assertEquals("ctx2 should not have been postReplicated", 0, pass2.getPostReplicateCount());        
         assertEquals("ctx2 should have been activated", 1, pass2.getPostActivateCount());
         
         ctx1 = container1.getCache().get(key1);
         
         log.info("ctx1 = " + ctx1);
         assertNotNull(ctx1);
         
         assertEquals("ctx1 should not have been postReplicated", 0, pass1.getPostReplicateCount());        
         assertEquals("ctx1 should have been activated", 1, pass1.getPostActivateCount());
         
         assertTrue("ctx1 must be different than firstCtx1 (else no passivation has taken place)", ctx1 != firstCtx1);
         
         assertNotNull(ctx1.getXPC());
         assertSame(ctx1.getXPC(), ctx2.getXPC());
         
         container1.getCache().release(ctx1);
         container2.getCache().release(ctx2);
         
         if (transactional)
         {
            node0.getTransactionManager().commit();
         }
      }
      finally
      {
         try
         {
            container1.stop();
            container2.stop();
         }
         finally
         {
            node0.restoreTCCL();
         }
      }
   }
   
   public void testSimpleGroupReplication() throws Exception
   {      
      log.info("====== testSimpleGroupReplication() ======");
      simpleGroupReplicationTest(false);
   }
   
   public void testSimpleGroupReplicationTransactional() throws Exception
   {      
      log.info("====== testSimpleGroupReplicationTransactional() ======");
      simpleGroupReplicationTest(true);
   }
   
   public void simpleGroupReplicationTest(boolean transactional) throws Exception
   {      
      JBC2MockCluster cluster = new JBC2MockCluster(false);
      MockCacheConfig cacheConfig = new MockCacheConfig();
      cacheConfig.setIdleTimeoutSeconds(1);
      MockXPC sharedXPC = new MockXPC("XPCA");
      MockBeanContainer[] firstSet = cluster.deployBeanContainer("MockBeanContainer1", null, CacheType.DISTRIBUTED, cacheConfig, sharedXPC.getName(), sharedXPC.getName());
      MockBeanContainer[] secondSet = cluster.deployBeanContainer("MockBeanContainer2", "MockBeanContainer1", CacheType.DISTRIBUTED, cacheConfig, sharedXPC.getName(), sharedXPC.getName());
      MockBeanContainer container1A = firstSet[0];
      MockBeanContainer container1B = firstSet[1];
      MockBeanContainer container2A = secondSet[0];
      MockBeanContainer container2B = secondSet[1];
      
      Object key1 = null;
      Object key2 = null;
      MockEntity entityA = null;
      
      // Use the TCCL to focus on the 1st node
      cluster.getNode0().setTCCL();
      try
      {
         if (transactional)
         {
            cluster.getNode0().getTransactionManager().begin();
         }
         
         key1 = container1A.getCache().create(null, null);
         MockBeanContext ctx1A = container1A.getCache().get(key1);
         
         key2 = ctx1A.getChild(container2A.getName());
         MockBeanContext ctx2A = container2A.getCache().get(key2);
         
         assertNotNull(ctx1A.getXPC());
         assertSame(ctx1A.getXPC(), ctx2A.getXPC());
         
         entityA = ctx2A.createEntity();
         assertSame(entityA, ctx1A.getEntity());
         
         container2A.getCache().release(ctx2A);
         container1A.getCache().release(ctx1A);
         
         if (transactional)
         {
            cluster.getNode0().getTransactionManager().commit();
         }
         
         MockPassivationManager pass1A = (MockPassivationManager) container1A.getPassivationManager();
         MockPassivationManager pass2A = (MockPassivationManager) container2A.getPassivationManager();
         
         assertEquals("ctx1 should have been replicated", 1, pass1A.getPreReplicateCount());
         assertEquals("ctx2 should have been passivated", 1, pass2A.getPreReplicateCount());
         
         if (transactional)
         {
            cluster.getNode0().getTransactionManager().begin();
         }
         
         ctx2A = container2A.getCache().get(key2);
         
         log.info("ctx2 = " + ctx2A);
         assertNotNull(ctx2A);
         
         assertEquals("ctx2 should have been postReplicated", 1, pass2A.getPostReplicateCount());        
         assertEquals("ctx2 should not have been activated", 0, pass2A.getPostActivateCount());
         
         ctx1A = container1A.getCache().get(key1);
         
         log.info("ctx1 = " + ctx1A);
         assertNotNull(ctx1A);
         
         assertEquals("ctx1 should have been postReplicated", 1, pass1A.getPostReplicateCount());        
         assertEquals("ctx1 should not have been activated", 0, pass1A.getPostActivateCount());
         
         assertNotNull(ctx1A.getXPC());
         assertSame(ctx1A.getXPC(), ctx2A.getXPC());
         
         MockEntity entity1x = ctx1A.getEntity();
         assertEquals(entityA, entity1x);
         
         container1A.getCache().release(ctx1A);         
         container2A.getCache().release(ctx2A);
         
         if (transactional)
         {
            cluster.getNode0().getTransactionManager().commit();
         }
      }
      catch (Exception e)
      {
         container1A.stop();
         container2A.stop();
         throw e;
      }
      finally
      {
         cluster.getNode0().restoreTCCL();         
      }
      
      // Let async messages propagate
      sleep(200);
      
      // Switch to second node
      cluster.getNode1().setTCCL();
      try
      {
         if (transactional)
         {
            cluster.getNode1().getTransactionManager().begin();
         }
         
         MockBeanContext ctx1B = container1B.getCache().get(key1);         
         MockBeanContext ctx2B = container2B.getCache().get(key2);
         
         MockEntity entityB = ctx2B.getEntity();
         assertSame(entityB, ctx1B.getEntity());
         assertEquals(entityA, entityB);
         assertNotSame(entityA, entityB);
         
         container2B.getCache().release(ctx2B);         
         container1B.getCache().release(ctx1B);
         
         if (transactional)
         {
            cluster.getNode1().getTransactionManager().commit();
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         cluster.getNode1().restoreTCCL();  
         container1A.stop();
         container2A.stop();  
         container1B.stop();
         container2B.stop();     
      }
   }
}
