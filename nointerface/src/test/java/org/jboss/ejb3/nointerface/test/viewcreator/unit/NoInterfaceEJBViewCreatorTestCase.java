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
package org.jboss.ejb3.nointerface.test.viewcreator.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.jboss.ejb3.nointerface.NoInterfaceEJBViewCreator;
import org.jboss.ejb3.nointerface.test.viewcreator.ChildBean;
import org.jboss.ejb3.nointerface.test.viewcreator.MethodInvocationTrackingContainer;
import org.jboss.ejb3.nointerface.test.viewcreator.SimpleSLSBWithoutInterface;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * NoInterfaceEJBViewCreatorTestCase
 *
 * Tests the {@link NoInterfaceEJBViewCreator}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceEJBViewCreatorTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceEJBViewCreatorTestCase.class);

   private static NoInterfaceEJBViewCreator noInterfaceViewCreator;

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      noInterfaceViewCreator = new NoInterfaceEJBViewCreator();

   }

   @AfterClass
   public static void afterClass() throws Exception
   {

   }

   /**
    * Test to ensure that all the public (non-static, non-final) methods on a bean
    * are routed through the container
    *
    * @throws Exception
    */
   @Test
   public void testNoInterfaceViewCreator() throws Exception
   {
      MethodInvocationTrackingContainer mockContainer = new MethodInvocationTrackingContainer(
            SimpleSLSBWithoutInterface.class);
      SimpleSLSBWithoutInterface noInterfaceView = noInterfaceViewCreator.createView(mockContainer,
            SimpleSLSBWithoutInterface.class);

      int numberOfMethodsInvokedOnBean = 0;

      String message = noInterfaceView.sayHi("jaikiran");
      numberOfMethodsInvokedOnBean++;

      logger.debug("Bean returned message : " + message);

      noInterfaceView.simplePublicMethod();
      numberOfMethodsInvokedOnBean++;

      logger.info("Number of methods invoked on bean = " + numberOfMethodsInvokedOnBean);

      assertNotNull("Methods on no-interface view were not tracked by container", mockContainer.getTrackedMethodNames());
      assertEquals("The container did not handle the expected number of calls on the bean",
            numberOfMethodsInvokedOnBean, mockContainer.getTrackedMethodNames().size());

   }

   /**
    * Test to ensure that the no-interface view instance does NOT consider
    * a final method on the bean while creating the view
    *
    * @throws Exception
    */
   @Test
   public void testFinalMethodsAreNotConsideredInView() throws Exception
   {
      MethodInvocationTrackingContainer mockContainer = new MethodInvocationTrackingContainer(
            SimpleSLSBWithoutInterface.class);
      SimpleSLSBWithoutInterface noInterfaceView = noInterfaceViewCreator.createView(mockContainer,
            SimpleSLSBWithoutInterface.class);

      noInterfaceView.someFinalMethod();

      assertEquals("Final method of bean " + SimpleSLSBWithoutInterface.class.getName()
            + " was included in no-inteface view", 0, mockContainer.getTrackedMethodNames().size());

   }

   /**
    * Test to ensure that the no-interface view instance does NOT consider
    * a static method on the bean while creating the view
    *
    * @throws Exception
    */
   @Test
   public void testStaticMethodsAreNotConsideredInView() throws Exception
   {
      MethodInvocationTrackingContainer mockContainer = new MethodInvocationTrackingContainer(
            SimpleSLSBWithoutInterface.class);
      SimpleSLSBWithoutInterface noInterfaceView = noInterfaceViewCreator.createView(mockContainer,
            SimpleSLSBWithoutInterface.class);

      noInterfaceView.someStaticMethod();

      assertEquals("Static method of bean " + SimpleSLSBWithoutInterface.class.getName()
            + " was included in no-inteface view", 0, mockContainer.getTrackedMethodNames().size());

      noInterfaceView.someStaticFinalMethod(3);
      assertEquals("Static final method of bean " + SimpleSLSBWithoutInterface.class.getName()
            + " was included in no-inteface view", 0, mockContainer.getTrackedMethodNames().size());

   }

   /**
    * Test that multiple invocations to the {@link NoInterfaceEJBViewCreator#createView(java.lang.reflect.InvocationHandler, Class)}
    * returns different instances of the view with unique view-classnames
    *
    * @throws Exception
    */
   @Test
   public void testViewCreatorCreatesUniqueViewInstanceNames() throws Exception
   {
      MethodInvocationTrackingContainer mockContainer = new MethodInvocationTrackingContainer(
            SimpleSLSBWithoutInterface.class);

      SimpleSLSBWithoutInterface noInterfaceView = noInterfaceViewCreator.createView(mockContainer,
            SimpleSLSBWithoutInterface.class);
      logger.debug("No-interface view for first invocation is " + noInterfaceView);

      SimpleSLSBWithoutInterface anotherNoInterfaceViewOnSameBean = noInterfaceViewCreator.createView(mockContainer,
            SimpleSLSBWithoutInterface.class);
      logger.debug("No-interface view for second invocation is " + anotherNoInterfaceViewOnSameBean);

      assertNotSame("No-interface view returned same instance for two createView invocations", noInterfaceView,
            anotherNoInterfaceViewOnSameBean);
      assertTrue("No-interfave view class name is the same for two createView invocations", !noInterfaceView
            .equals(anotherNoInterfaceViewOnSameBean));
   }

   /**
    * Test that the no-interface view works as expected when the bean extends from some other class
    *
    * @throws Exception
    */
   @Test
   public void testNoInterfaceViewCreatorWithInheritedClasses() throws Exception
   {
      MethodInvocationTrackingContainer mockContainer = new MethodInvocationTrackingContainer(ChildBean.class);

      ChildBean noInterfaceView = noInterfaceViewCreator.createView(mockContainer, ChildBean.class);

      int numberOfInvokedMethods = 0;
      noInterfaceView.sayHiFromBase("jaikiran");
      numberOfInvokedMethods++;

      noInterfaceView.sayHiFromChild("jaikiran");
      numberOfInvokedMethods++;

      noInterfaceView.somePublicMethod(4);
      numberOfInvokedMethods++;

      // Do NOT increment the counter, since we are calling the final method
      // which will NOT be covered by the no-interface view
      noInterfaceView.someFinalMethod();

      logger.debug("Number of methods invoked on bean = " + numberOfInvokedMethods);

      assertNotNull("Methods on no-interface view on inherited bean were not tracked by container", mockContainer.getTrackedMethodNames());
      assertEquals("The container did not handle the expected number of calls on the inherited bean",
            numberOfInvokedMethods, mockContainer.getTrackedMethodNames().size());



   }
}
