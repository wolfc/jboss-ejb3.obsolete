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
package org.jboss.ejb3.test.ejbthree1677.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.service.ServiceMBeanDelegate;
import org.jboss.ejb3.test.ejbthree1677.ServiceManagement;
import org.jboss.ejb3.test.ejbthree1677.ServiceWithOverloadedMethods;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * ServiceTestCase
 *
 * Tests the fix for EJBTHREE-1677.
 *
 * Original issue : If a @Service had overloaded methods
 * (ex: one accepting no parameters and the other accepting a parameter),
 * then when the service was invoked in the following sequence, an exception
 * was thrown:
 * 1) first invoke the overloaded method which does not accept the param
 * 2) then invoke the overloaded method which accepts the param
 * The issue was in the {@link ServiceMBeanDelegate} which has a cache of
 * Method based on method name and signature. The cache was being requested for
 * the corresponding method using only the method name, which resulted in this bug.
 * Fixed appropriately to consult the cache using the entire method name and signature.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ServiceTestCase extends JBossTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(ServiceTestCase.class);

   /**
    * Constructor
    *
    * @param name
    */
   public ServiceTestCase(String name)
   {
      super(name);

   }

   /**
    * Deploy our testable application
    *
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceTestCase.class, "ejbthree1677.jar");
   }

   /**
    * Test that the overloaded methods on a @Service work fine when the
    * service is invoked through a MBeanServer
    *
    * @throws Exception
    */
   public void testOverloadedMethodsAsMBean() throws Exception
   {
      Context ctx = new InitialContext();
      MBeanServerConnection mbeanServer = (MBeanServerConnection) ctx.lookup("jmx/invoker/RMIAdaptor");
      ObjectName serviceObjectName = new ObjectName(ServiceWithOverloadedMethods.OBJECT_NAME);

      // Invoke an overloaded method which does NOT accept param
      String expectedGreeting = "Hi";
      String greeting = (String) mbeanServer.invoke(serviceObjectName, "sayHi", new Object[0], new String[0]);

      assertNotNull("Service did not say hi", greeting);
      assertEquals("Service returned unexpected greeting", expectedGreeting, greeting);

      // Now invoke the overloaded method which accepts params
      String name = "jaikiran";
      String anotherExpectedGreeting = "Hi " + name;
      String[] paramForOverloadedMethod =
      {name};
      String[] signatureOfOverloadedMethod =
      {"java.lang.String"};
      String anotherGreeting = (String) mbeanServer.invoke(serviceObjectName, "sayHi", paramForOverloadedMethod,
            signatureOfOverloadedMethod);
      assertNotNull("Service did not say hi on overloaded method", anotherGreeting);
      assertEquals("Service returned unexpected greeting from overloaded method", anotherExpectedGreeting,
            anotherGreeting);

      // Now invoke a totally unrelated method (just to ensure nothing breaks)
      String message = "Welcome";
      String[] paramForUnrelateMethod =
      {message};
      String[] signatureOfUnrelateMethod =
      {"java.lang.String"};
      String returnedMessage = (String) mbeanServer.invoke(serviceObjectName, "echoMessage", paramForUnrelateMethod,
            signatureOfUnrelateMethod);
      assertNotNull("Service did not echo back the message", returnedMessage);
      assertEquals("Service returned unexpected echo", returnedMessage, message);


   }

   /**
    * Test that the overloaded methods on a @Service work fine when the service
    * methods are invoked using the corresponding management interface.
    *
    * @throws Exception
    */
   public void testAsEJB3Service() throws Exception
   {
      // lookup bean
      Context ctx = new InitialContext();
      ServiceManagement service = (ServiceManagement) ctx.lookup(ServiceWithOverloadedMethods.JNDI_NAME);

      // call the relevant methods
      String message = "hello";
      String returnedMessage = service.echoMessage(message);

      assertNotNull("Service did not echo back the message", returnedMessage);
      assertEquals("Service returned unexpected echo", returnedMessage, message);

      // overloaded method without param
      String expectedGreeting = "Hi";
      String returnedGreeting = service.sayHi();
      assertNotNull("Service did not say hi", expectedGreeting);
      assertEquals("Service returned unexpected greeting", expectedGreeting, returnedGreeting);

      // now the overloaded method with param
      String name = "newuser";
      String anotherExpectedGreeting = "Hi " + name;
      String anotherReturnedGreeting = service.sayHi(name);

      assertNotNull("Service did not say hi on overloaded method", anotherReturnedGreeting);
      assertEquals("Service returned unexpected greeting from overloaded method", anotherExpectedGreeting,
            anotherReturnedGreeting);

   }
}
