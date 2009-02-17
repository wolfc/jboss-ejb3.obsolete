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

import javax.transaction.Transaction;

import org.jboss.ejb3.test.cache.integrated.Ejb3CacheTestCaseBase;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockSessionSynchronization;
import org.jboss.logging.Logger;

/**
 * @author Brian Stansberry
 *
 */
public class SessionSynchronizationInterceptorUnitTestCase extends Ejb3CacheTestCaseBase
{
   private static final Logger log = Logger.getLogger(GroupedPassivatingUnitTestCase.class);
      
   public void testSessionSynchronization() throws Exception
   {      
      log.info("====== testSessionSynchronization() ======");
      
      MockCluster cluster = new MockCluster(false);
      MockCacheConfig cacheConfig = new MockCacheConfig();
      cacheConfig.setIdleTimeoutSeconds(1);
      MockBeanContainer[] beanSet = cluster.deployBeanContainer("MockBeanContainer1", null, CacheType.DISTRIBUTED, cacheConfig, null, null);
      MockBeanContainer container1A = beanSet[0];
      MockBeanContainer container1B = beanSet[1];
      
      Object key1 = null;
      int count;
      
      // Use the TCCL to focus on the 1st node
      cluster.getNode0().setTCCL();
      try
      {
         cluster.getNode0().getTransactionManager().begin();         
         Transaction tx = cluster.getNode0().getTransactionManager().getTransaction();
         
         key1 = container1A.getCache().create(null, null);
         MockBeanContext ctx1A = container1A.getCache().get(key1);
         
         // This sync will call ctx1A.increment() during beforeCompletion()
         MockSessionSynchronization sync = new MockSessionSynchronization(ctx1A);
         tx.registerSynchronization(sync);
         
         count = ctx1A.increment();
         
         container1A.getCache().release(ctx1A);
         
         cluster.getNode0().getTransactionManager().commit();
         
         count = sync.getCount();
      }
      catch (Exception e)
      {
         container1A.stop();
         throw e;
      }
      finally
      {
         cluster.getNode0().restoreTCCL();         
      }
      
      // Switch to second node
      cluster.getNode1().setTCCL();
      try
      {
         MockBeanContext ctx1B = container1B.getCache().get(key1);
         
         assertEquals("SessionSynchronization's increment not replicated", count, ctx1B.getCount());
         
         container1B.getCache().release(ctx1B);
      }
      catch (Exception e)
      {
         container1B.stop();
         throw e;
      }
      finally
      {
         cluster.getNode1().restoreTCCL();         
      }
   }

}
