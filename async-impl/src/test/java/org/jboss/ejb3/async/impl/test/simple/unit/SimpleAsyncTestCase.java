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
package org.jboss.ejb3.async.impl.test.simple.unit;

import java.util.concurrent.Future;

import javax.ejb.Asynchronous;

import junit.framework.TestCase;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.async.impl.test.common.TestConstants;
import org.jboss.ejb3.async.impl.test.container.AsyncContainer;
import org.jboss.ejb3.async.impl.test.simple.Pojo;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SimpleAsyncTestCase
 * 
 * Tests for some common EJB 3.1 @Asynchronous use cases 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SimpleAsyncTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SimpleAsyncTestCase.class);

   private static AOPDeployer aopDeployer = new AOPDeployer(TestConstants.AOP_DEPLOYABLE_FILENAME_SIMPLE);

   private static AsyncContainer<Pojo> container;

   /*
    * Method names in Test POJO
    */

   private static final String METHOD_NAME_INCREMENT_COUNTER_ASYNCHRONOUS = "incrementCounterAsynchronous";

   private static final String METHOD_NAME_INCREMENT_COUNTER_SYNCHRONOUS = "incrementCounterSynchronous";

   private static final String METHOD_NAME_GET_COUNTER = "getCounter";

   private static final String METHOD_NAME_GET_VALUE_ASYNCHRONOUS = "getValueAsynchronous";

   private static final String METHOD_NAME_GET_VALUE_SYNCHRONOUS = "getValueSynchronous";

   // --------------------------------------------------------------------------------||
   // Test Lifecycle -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      aopDeployer.deploy();
      container = new AsyncContainer<Pojo>("Test Async POJO Container", TestConstants.DOMAIN_ASYNC, Pojo.class);
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      aopDeployer.undeploy();
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that a simple invocation to obtain 
    * some contracted String value returns as expected
    * 
    * @throws Throwable 
    */
   @Test
   public void testSimpleInvocation() throws Throwable
   {
      // Make a new bean instance upon which we'll invoke
      BeanContext<Pojo> bean = container.construct();

      // Use the container to get a contracted value from the bean
      Future<?> futureResult = (Future<?>) container.invoke(bean, METHOD_NAME_GET_VALUE_ASYNCHRONOUS);
      log.info("Obtained result: " + futureResult);

      // Get the Future value
      Object result = futureResult.get();

      // Ensure the value is expected
      TestCase.assertEquals("Did not obtain expected result", Pojo.VALUE, result);
   }

   /**
    * Tests that support for methods annotated as @Asynchronous
    * with return type void succeed
    * 
    * @throws Throwable
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testVoidMethodWithAsynchronous() throws Throwable
   {
      // Make a new bean instance upon which we'll invoke
      BeanContext<Pojo> bean = container.construct();

      // Get the counter as it exists
      Future<Integer> initialCounterFuture = (Future<Integer>) container.invoke(bean, METHOD_NAME_GET_COUNTER);
      int initialCounter = initialCounterFuture.get();

      // Increment the counter 
      Future<Void> incrementCounterFutureResult = (Future<Void>) container.invoke(bean,
            METHOD_NAME_INCREMENT_COUNTER_ASYNCHRONOUS);
      TestCase.assertNotNull("void return type not intercepted as asynchronous invocation",
            incrementCounterFutureResult);
      Object incrementedCounterResult = incrementCounterFutureResult.get();
      TestCase.assertNull("void return types should return null upon Future.get()", incrementedCounterResult);

      // Test the counter was incremented
      Future<Integer> incrementedCounterFuture = (Future<Integer>) container.invoke(bean, METHOD_NAME_GET_COUNTER);
      int incrementedCounter = incrementedCounterFuture.get();
      TestCase.assertEquals("Counter was not incremented", initialCounter + 1, incrementedCounter);
   }

   /**
    * Tests that support for methods not annotated, and of return 
    * type void succeed
    * 
    * @throws Throwable
    */
   @Test
   public void testVoidMethodUnannotated() throws Throwable
   {
      // Make a new bean instance upon which we'll invoke
      BeanContext<Pojo> bean = container.construct();

      // Invoke and test
      Object shouldBeNullReturnValue = container.invoke(bean, METHOD_NAME_INCREMENT_COUNTER_SYNCHRONOUS);
      TestCase.assertNull("methods with void return type not annotated with @" + Asynchronous.class.getSimpleName()
            + " should have null return type from container invocation", shouldBeNullReturnValue);

   }

   /**
    * Tests that support for methods not annotated, and returning
    * some type other than Future succeed
    * 
    * @throws Throwable
    */
   @Test
   public void testUnannotatedMethodsSynchronous() throws Throwable
   {
      // Make a new bean instance upon which we'll invoke
      BeanContext<Pojo> bean = container.construct();

      // Invoke and test
      String value = container.invoke(bean, METHOD_NAME_GET_VALUE_SYNCHRONOUS);
      TestCase.assertEquals("Contracted value not obtained as expected", Pojo.VALUE, value);

   }

}
