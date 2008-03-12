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
package org.jboss.ejb3.test.cache.distributed;

import junit.framework.TestCase;

import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockPassivationManager;
import org.jboss.ejb3.test.cache.mock.MockXPC;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author Brian Stansberry
 * @version $Revision: 65920 $
 */
public class GroupedPassivatingUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(GroupedPassivatingUnitTestCase.class);
   
   private static void sleep(long micros)
   {
      try
      {
         Thread.sleep(micros);
      }
      catch (InterruptedException e)
      {
         // ignore
      }
   }
   
   public void testSimpleGroupPassivation() throws Exception
   {      
      log.info("testSimpleGroupPassivation()");
      
      MockCluster cluster = new MockCluster(false);
      MockClusterMember node0 = cluster.getNode0();
      MockCacheConfig cacheConfig = new MockCacheConfig();
      cacheConfig.setIdleTimeoutSeconds(1);
      MockXPC sharedXPC = new MockXPC();
      MockBeanContainer container1 = node0.deployBeanContainer("MockBeanContainer1", null, CacheType.DISTRIBUTED, cacheConfig, sharedXPC);
      MockBeanContainer container2 = node0.deployBeanContainer("MockBeanContainer2", "MockBeanContainer1", CacheType.DISTRIBUTED, cacheConfig, sharedXPC);
      
      cluster.getNode0().setTCCL();
      try
      {
         Object key1 = container1.getCache().create(null, null);
         MockBeanContext firstCtx1;
         MockBeanContext ctx1 = firstCtx1 = container1.getCache().get(key1);
         
         Object key2 = ctx1.getChild(container2.getName());
         MockBeanContext ctx2 = container2.getCache().get(key2);
         
         assertNotNull(ctx1.getXPC());
         assertEquals(ctx1.getXPC(), ctx2.getXPC());
         
         container2.getCache().finished(ctx2);
         container1.getCache().finished(ctx1);
         
         sleep(2100);
         
         MockPassivationManager pass1 = (MockPassivationManager) container1.getPassivationManager();
         MockPassivationManager pass2 = (MockPassivationManager) container2.getPassivationManager();
         
         assertEquals("ctx1 should have been passivated", 1, pass1.getPrePassivateCount());
         assertEquals("ctx2 should have been passivated", 1, pass2.getPrePassivateCount());
         
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
         assertEquals(ctx1.getXPC(), ctx2.getXPC());
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
            cluster.getNode0().restoreTCCL();
         }
      }
   }
}
