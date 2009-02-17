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
package org.jboss.ejb3.test.cache.integrated;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockEjb3System;
import org.jboss.ejb3.test.cache.mock.MockPassivationManager;
import org.jboss.ejb3.test.cache.mock.MockXPC;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author Brian Stansberry
 * @version $Revision: 65920 $
 */
public class GroupAwareTransactionalCacheUnitTestCase extends TransactionalCacheUnitTestCase
{
   private static final Logger log = Logger.getLogger(GroupAwareTransactionalCacheUnitTestCase.class);
   
   @Override
   protected Cache<MockBeanContext> createCache() throws Exception
   {
      MockEjb3System system = new MockEjb3System(tm, false, CacheType.SIMPLE);
      MockBeanContainer ejb = system.deployBeanContainer("test", null, CacheType.SIMPLE);
      return ejb.getCache();
   }
   
   public void testNonGroupedPassivation() throws Exception
   {    
      log.info("testNonGroupedPassivation()");
      nonGroupedPassivationTest(false);
   }
   
   public void testNonGroupedPassivationTransactional() throws Exception
   {    
      log.info("testNonGroupedPassivationTransactional()");
      nonGroupedPassivationTest(true);
   }
   
   private void nonGroupedPassivationTest(boolean transactional) throws Exception
   {    
      MockEjb3System system = new MockEjb3System(tm, false, CacheType.SIMPLE);
      MockXPC sharedXPC = new MockXPC();
      MockCacheConfig config = new MockCacheConfig();
      config.setIdleTimeoutSeconds(3);
      MockBeanContainer container = system.deployBeanContainer("MockBeanContainer1", null, CacheType.SIMPLE, config, sharedXPC.getName());
      Cache<MockBeanContext> cache = container.getCache();
      
      if (transactional)
      {
         tm.begin();
      }
      
      Object key = cache.create(null, null);
      MockBeanContext obj = cache.get(key);
      
      cache.release(obj);      
      obj = null;
      
      if (transactional)
      {
         tm.commit();
      }
      
      wait(container);
      
      MockPassivationManager pass = (MockPassivationManager) container.getPassivationManager();
      
      assertEquals("MockBeanContext should have been passivated", 1, pass.getPrePassivateCount());
      
      if (transactional)
      {
         tm.begin();
      }
      
      obj = cache.get(key);
      assertNotNull(obj);
      
      assertEquals("MockBeanContext should have been activated", 1, pass.getPostActivateCount());
      
      sleep(2000);
      
      assertEquals("MockBeanContext should not have been passivated", 1, pass.getPrePassivateCount());
      
      cache.release(obj);      
      obj = null;
      
      if (transactional)
      {
         tm.commit();
      }
      
      wait(container);
      
      assertEquals("MockBeanContext should have been passivated", 2, pass.getPrePassivateCount());
   }

   public void testSimpleGroupPassivation() throws Exception
   {    
      log.info("testSimpleGroupPassivation()");
      simpleGroupPassivationTest(false);
   }

   public void testSimpleGroupPassivationTransactional() throws Exception
   {    
      log.info("testSimpleGroupPassivationTransactional()");
      simpleGroupPassivationTest(true);
   }

   public void simpleGroupPassivationTest(boolean transactional) throws Exception
   {    
      MockEjb3System system = new MockEjb3System(tm, false, CacheType.SIMPLE);
      MockXPC sharedXPC = new MockXPC();
      MockCacheConfig config = new MockCacheConfig();
      config.setIdleTimeoutSeconds(1);
      MockBeanContainer container1 = system.deployBeanContainer("MockBeanContainer1", null, CacheType.SIMPLE, config, sharedXPC.getName());
      MockBeanContainer container2 = system.deployBeanContainer("MockBeanContainer2", "MockBeanContainer1", CacheType.SIMPLE, config, sharedXPC.getName());
      
      log.info("Containers deployed");
      
      assertTrue(container1.hasChild(container2));
      
      try
      {         
         if (transactional)
         {
            tm.begin();
         }
         
         Object key1 = container1.getCache().create(null, null);
         MockBeanContext firstCtx1;
         MockBeanContext ctx1 = firstCtx1 = container1.getCache().get(key1);
         
         assertEquals(sharedXPC, ctx1.getXPC());
         Object key2 = ctx1.getChild(container2.getName());
         MockBeanContext ctx2 = container2.getCache().get(key2);
         assertNotNull(ctx2);
         assertEquals(sharedXPC, ctx2.getXPC());

         container2.getCache().release(ctx2);
         container1.getCache().release(ctx1);
         
         if (transactional)
         {
            tm.commit();
         }
         
         log.info("Finished with contexts");
         
         sleep(2100);
         
         MockPassivationManager pass1 = (MockPassivationManager) container1.getPassivationManager();
         MockPassivationManager pass2 = (MockPassivationManager) container2.getPassivationManager();
         
         
         assertEquals("ctx1 should have been passivated", 1, pass1.getPrePassivateCount());
         assertEquals("ctx2 should have been passivated", 1, pass2.getPrePassivateCount());
         
         log.info("Restoring ctx2");
         
         if (transactional)
         {
            tm.begin();
         }
         
         ctx2 = container2.getCache().get(key2);
         
         log.info("ctx2 = " + ctx2);
         assertNotNull(ctx2);
         
         log.info("Restoring ctx1");
         
         ctx1 = container1.getCache().get(key1);
         
         log.info("ctx1 = " + ctx1);
         
         assertTrue("ctx1 must be different than firstCtx1 (else no passivation has taken place)", ctx1 != firstCtx1);
         
         assertNotNull(ctx1.getXPC());
         assertEquals(ctx1.getXPC(), ctx2.getXPC());
         
         container1.getCache().release(ctx1);         
         container2.getCache().release(ctx2);
         
         if (transactional)
         {
            tm.commit();
         }
         
      }
      finally
      {
         container1.stop();
         container2.stop();
      }
   }
   
   /**
    * Test call to bean1 that calls into bean2 that calls back into bean1
    */
   public void testRecursiveCalls() throws Exception
   {    
      log.info("testRecursiveCalls()");
      recursiveCallsTest(false);
   }
   
   /**
    * Test call to bean1 that calls into bean2 that calls back into bean1
    */
   public void testRecursiveCallsTransactional() throws Exception
   {    
      log.info("testRecursiveCallsTransactional()");
      recursiveCallsTest(true);
   }
   
   /**
    * Test call to bean1 that calls into bean2 that calls back into bean1
    */
   public void recursiveCallsTest(boolean transactional) throws Exception
   {    
      log.info("testRecursiveCalls()");
      
      MockEjb3System system = new MockEjb3System(tm, false, CacheType.SIMPLE);
      MockXPC sharedXPC = new MockXPC();
      MockCacheConfig config = new MockCacheConfig();
      config.setIdleTimeoutSeconds(1);
      MockBeanContainer container1 = system.deployBeanContainer("MockBeanContainer1", null, CacheType.SIMPLE, config, sharedXPC.getName());
      MockBeanContainer container2 = system.deployBeanContainer("MockBeanContainer2", "MockBeanContainer1", CacheType.SIMPLE, config, sharedXPC.getName());
      
      log.info("Containers deployed");
      
      assertTrue(container1.hasChild(container2));
      
      try
      {         
         if (transactional)
         {
            tm.begin();
         }
         
         Object key1 = container1.getCache().create(null, null);
         MockBeanContext firstCtx1;
         MockBeanContext ctx1 = firstCtx1 = container1.getCache().get(key1);
         
         assertEquals(sharedXPC, ctx1.getXPC());
         Object key2 = ctx1.getChild(container2.getName());
         MockBeanContext ctx2 = container2.getCache().get(key2);
         assertNotNull(ctx2);
         assertSame(sharedXPC, ctx2.getXPC());
         
         MockBeanContext secondCtx1 = container1.getCache().get(key1);
         assertSame(firstCtx1, secondCtx1);
         assertSame(sharedXPC, secondCtx1.getXPC());

         container1.getCache().release(secondCtx1);
         container2.getCache().release(ctx2);
         container1.getCache().release(firstCtx1);
         
         if (transactional)
         {
            tm.commit();
         }
         
      }
      finally
      {
         container1.stop();
         container2.stop();
      }
      
   }
}
