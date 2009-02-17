/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.cache.integrated;

import javax.ejb.NoSuchEJBException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockEjb3System;
import org.jboss.ejb3.test.cache.mock.tm.MockTransactionManager;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * @version $Revision: $
 */
public class TransactionalCacheUnitTestCase extends Ejb3CacheTestCaseBase
{   
   private static final Logger log = Logger.getLogger(TransactionalCacheUnitTestCase.class);
   
   protected TransactionManager tm =  MockTransactionManager.getInstance();
   
   protected Cache<MockBeanContext> createCache() throws Exception
   {
      MockEjb3System system = new MockEjb3System(tm, false, CacheType.NON_PASSIVATING);
      MockBeanContainer ejb = system.deployBeanContainer("test", null, CacheType.NON_PASSIVATING);
      return ejb.getCache();
   }
   
   public void testNonExistingGet() throws Exception
   {      
      log.info("testNonExistingGet()");
      
      Cache<MockBeanContext> cache = createCache();
      
      try
      {
         cache.get(1);
         fail("Object 1 should not be in cache");
      }
      catch(NoSuchEJBException e)
      {
         // good
      }
   }
   
   public void testSimpleLifeCycle() throws Exception
   {
      log.info("testSimpleLifeCycle()");
      simpleLifeCycleTest(false);
   }
   
   public void testSimpleLifeCycleTransactional() throws Exception
   {
      log.info("testSimpleLifeCycleTransactional()");
      simpleLifeCycleTest(true);
   }
   
   private void simpleLifeCycleTest(boolean transactional) throws Exception
   {
      Cache<MockBeanContext> cache = createCache();
      
      if (transactional)
      {
         tm.begin();
      }
      
      Object key = cache.create(null, null);
      MockBeanContext object = cache.get(key);
      
      assertNotNull(object);
      
      cache.remove(key);
      
      if (transactional)
      {
         tm.commit();
      }
      
      try
      {
         cache.get(key);
         fail("Object should not be in cache");
      }
      catch(NoSuchEJBException e)
      {
         // good
      }
   }
   
   public void testSequentialGetCalls() throws Exception
   {
      log.info("testSequentialGetCalls()");
      sequentialGetCallsTest(false);
   }
   
   public void testSequentialGetCallsTransactional() throws Exception
   {
      log.info("testSequentialGetCallsTransactional()");
      sequentialGetCallsTest(true);
   }
   
   private void sequentialGetCallsTest(boolean transactional) throws Exception
   {
      Cache<MockBeanContext> cache = createCache();
      
      if (transactional)
      {
         tm.begin();
      }
      
      Object key = cache.create(null, null);
      MockBeanContext object = cache.get(key);
      
      assertNotNull(object);
      
      MockBeanContext object2 = cache.get(key);
      
      assertSame(object, object2);
      
      cache.release(object2);
      cache.release(object);
      
      cache.remove(key);
      
      if (transactional)
      {
         tm.commit();
      }
   }
   
   public void testExcessFinishedCalls() throws Exception
   {    
      log.info("testExcessFinishedCalls()");
      excessFinishedCallsTest(false);
   }
   
   public void testExcessFinishedCallsTransactional() throws Exception
   {    
      log.info("testExcessFinishedCallsTransactional()");
      excessFinishedCallsTest(true);
   }
   
   private void excessFinishedCallsTest(boolean transactional) throws Exception
   {    
      log.info("testExcessFinishedCalls()");
      
      Cache<MockBeanContext> cache = createCache();
      
      if (transactional)
      {
         tm.begin();
      }
      
      Object key = cache.create(null, null);
      MockBeanContext object = cache.get(key);
      
      assertNotNull(object);
      
      cache.release(object);
      
      try
      {
         cache.release(object);
         fail("Two sequential finished calls should throw ISE");
      }
      catch(IllegalStateException e)
      {
         // good
      }
      finally {
         cache.remove(key);
         
         if (transactional)
         {
            tm.commit();
         }
      }
   }
   
   public void testConcurrentTransactionalAccess() throws Exception
   {      
      log.info("testConcurrentTransactionalAccess()");
      
      Cache<MockBeanContext> cache = createCache();
      
      tm.begin();
      
      Object key = cache.create(null, null);
      MockBeanContext object = cache.get(key);
      
      assertNotNull(object);
      
      Transaction tx1 = tm.suspend();
      
      tm.begin();
      
      try
      {
         cache.get(key);
         fail("Ongoing transaction should have prevent get() by second tx");
      }
      catch (IllegalStateException good) {}
      
      // tx1 calling finished isn't sufficient to release item
      Transaction tx2 = tm.suspend();      
      tm.resume(tx1);
      
      cache.release(object);      

      tm.suspend();
      tm.resume(tx2);
      
      try
      {
         cache.get(key);
         fail("Ongoing transaction should have prevent get() by second tx");
      }
      catch (IllegalStateException good) {}
      
      // Committing tx1 releases the locks
      tm.suspend();
      tm.resume(tx1);
      tm.commit();
      
      tm.resume(tx2);
      MockBeanContext object2 = cache.get(key);
      assertSame(object, object2);
      
      cache.release(object2);
      
      tm.commit();
   }
}
