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
package org.jboss.ejb3.test.proxy.ejbthree1130.unit;

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.spi.common.ErrorCodes;
import org.jboss.ejb3.test.proxy.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.SessionContainer;
import org.jboss.ejb3.test.proxy.ejbthree1130.TestNoLocalBusinessInterfaceWithLocalBindingBean;
import org.jboss.ejb3.test.proxy.ejbthree1130.TestNoRemoteBusinessInterfaceWithRemoteBindingBean;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that EJBs with a @LocalBinding/@RemoteBinding and 
 * no corresponding local/remote business interface 
 * fails deployment
 * 
 * EJBTHREE-1130
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class BindingsWithNoAppropriateInterfaceUnitTestCase extends SessionTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(BindingsWithNoAppropriateInterfaceUnitTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensure that EJB with no local business interface and 
    * @LocalBinding defined fails deployment
    */
   @Test
   public void testLocalBindingWithNoLocalBusinessInterface() throws Throwable
   {
      // Run the test
      this.performTest(TestNoLocalBusinessInterfaceWithLocalBindingBean.class);
   }

   /**
    * Ensure that EJB with no remote business interface and 
    * @RemoteBinding defined fails deployment
    */
   @Test
   public void testRemoteBindingWithNoRemoteBusinessInterface() throws Throwable
   {
      // Run the test
      this.performTest(TestNoRemoteBusinessInterfaceWithRemoteBindingBean.class);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Attempts to install the EJB with the specified implementation class,
    * and checks that deployment fails with "EJBTHREE-1130" noted in the error
    * message
    */
   private void performTest(Class<?> ejbImplementationClass) throws Throwable
   {
      // Initialize
      String errorCode = ErrorCodes.ERROR_MESSAGE_CODE_EJBTHREE1130;

      // Create the EJB
      SessionContainer ejb = Utils.createSlsb(ejbImplementationClass);
      String containerName = ejb.getName();

      // Attempt to install
      try
      {
         // Start
         ejb.start();
      }
      catch (Throwable t)
      {
         // Get the message
         String errorMessage = t.getMessage();

         // Ensure the message contains the proper error code
         TestCase.assertTrue("The EJB " + containerName + " failed as expected, but the message did not contain \""
               + errorCode + "\"; instead was: " + errorMessage, errorMessage.indexOf(errorCode) != -1);

         // Log and return
         log.info("Obtained error message in expected format: " + errorMessage);
         return;

      }
      finally
      {
         // Cleanup
         try
         {
            Ejb3RegistrarLocator.locateRegistrar().unbind(containerName);
         }
         catch (NotBoundException nbe)
         {
            // No one cares there, Bob
         }
      }

      // Should have failed by now
      TestCase.fail("EJB " + containerName + " should have failed deployment with a message containing \"" + errorCode
            + "\"");
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Perform setup before any tests
    * 
    * @throws Throwable
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Throwable
   {
      // Create Bootstrap 
      SessionTestCaseBase.setUpBeforeClass();

      // Deploy MC Beans
      SessionTestCaseBase.bootstrap.deploy(SessionTestCaseBase.class);

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }

}