/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.async.impl.test.cancel.unit;

import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.async.impl.test.cancel.PausableBlockingQueue;
import org.jboss.ejb3.async.impl.test.common.PausableProcessingAsyncContainer;
import org.jboss.ejb3.async.impl.test.common.Pojo;
import org.jboss.ejb3.async.impl.test.common.TestConstants;
import org.jboss.ejb3.async.impl.test.simple.unit.SimpleAsyncTestCase;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CancelAsyncTaskTestCase
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CancelAsyncTaskTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SimpleAsyncTestCase.class);

   private static AOPDeployer aopDeployer = new AOPDeployer(TestConstants.AOP_DEPLOYABLE_FILENAME_SIMPLE);

   private static PausableProcessingAsyncContainer<Pojo> container;

   // --------------------------------------------------------------------------------||
   // Test Lifecycle -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      aopDeployer.deploy();
      container = new PausableProcessingAsyncContainer<Pojo>("Test Pausable Async POJO Container",
            TestConstants.DOMAIN_ASYNC, Pojo.class);
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      aopDeployer.undeploy();
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * In place just so Maven doesn't puke on no valid tests
    */
   @Test
   @Deprecated
   //TODO Remove
   public void noopTest()
   {
   }

   /**
    * Tests that @Asynchronous invocations may be cancelled
    * 
    * @throws Throwable 
    */
   //@Test
   @SuppressWarnings("unchecked")
   public void testCancelAsyncInvocation() throws Throwable
   {
      // Make a new bean instance upon which we'll invoke
      BeanContext<Pojo> bean = container.construct();

      // Set the container to allow processing
      PausableBlockingQueue<?> queue = (PausableBlockingQueue<?>) container.getAsynchronousExecutor().getQueue();
      queue.resume();
      log.info("Work queue is active");

      // Get the counter
      Future<Integer> initialCounterFuture = (Future<Integer>) container.invoke(bean,
            TestConstants.METHOD_NAME_GET_COUNTER);
      int initialCounter = initialCounterFuture.get();

      // Ensure the counter starts at 0
      TestCase.assertEquals("Counter should start at 0", 0, initialCounter);
      log.info("Got counter at start: " + initialCounter);

      // Increment the counter, then get the result
      Future<Void> firstIncrementCounterFuture = (Future<Void>) container.invoke(bean,
            TestConstants.METHOD_NAME_INCREMENT_COUNTER_ASYNCHRONOUS);
      firstIncrementCounterFuture.get(); // Block until done
      Future<Integer> firstIncrementCounterResultFuture = (Future<Integer>) container.invoke(bean,
            TestConstants.METHOD_NAME_GET_COUNTER);
      int firstIncrementCounterResult = firstIncrementCounterResultFuture.get();

      // Ensure the counter has been incremented
      TestCase.assertEquals("Counter should have been incrememted to 1", 1, firstIncrementCounterResult);
      log.info("Got counter after first async increment: " + firstIncrementCounterResult);

      // Set the container to pause processing
      queue.pause();
      log.info("Work queue is paused");

      // Increment the counter, then get the result
      Future<Void> secondIncrementCounterFuture = (Future<Void>) container.invoke(bean,
            TestConstants.METHOD_NAME_INCREMENT_COUNTER_ASYNCHRONOUS);

      // Cancel and test
      boolean isCancelled = secondIncrementCounterFuture.cancel(true);
      TestCase.assertTrue("Request to cancel() not honored", isCancelled);

      // Get the counter again, testing that it hasn't been incremented
      Future<Integer> secondIncrementCounterResultFuture = (Future<Integer>) container.invoke(bean,
            TestConstants.METHOD_NAME_GET_COUNTER);
      int secondIncrementCounterResult = secondIncrementCounterResultFuture.get();
      TestCase.assertEquals("Second call to increment counter should have been cancelled", firstIncrementCounterResult,
            secondIncrementCounterResult);

   }

}
