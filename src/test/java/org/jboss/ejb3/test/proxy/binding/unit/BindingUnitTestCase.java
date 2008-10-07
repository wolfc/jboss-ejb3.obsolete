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
package org.jboss.ejb3.test.proxy.binding.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.binding.BindingTest;
import org.jboss.ejb3.test.proxy.binding.JndiBindingTestBean;
import org.jboss.ejb3.test.proxy.binding.LocalJndiBindingTest;
import org.jboss.ejb3.test.proxy.binding.RemoteJndiBindingTest;
import org.jboss.ejb3.test.proxy.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.StatelessContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * BindingUnitTestCase
 * 
 * Unit Tests for explicitly-declared JNDI Bindings
 * 
 * EJBTHREE-1515
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class BindingUnitTestCase extends SessionTestCaseBase
{

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that bindings at N Remote Locations are made and may be
    * invoked upon.
    */
   @Test
   public void testMultipleRemoteJndiBindings() throws Exception
   {
      // Initialize
      String testMessage = "Find me at N JNDI Locations";

      // Get a JNDI Context
      Context context = new InitialContext(); // Props from jndi.properties on Client CP

      // Lookup JNDI Binding Test EJB at Location 1, and invoke
      Object location1 = context.lookup(RemoteJndiBindingTest.JNDI_BINDING_1);
      TestCase.assertNotNull("Expected remote binding in JNDI at " + location1 + " was not found", location1);
      TestCase.assertTrue(location1 instanceof BindingTest);
      BindingTest test1 = (BindingTest) location1;
      String result1 = test1.echo(testMessage);
      TestCase.assertEquals(testMessage, result1);

      // Lookup JNDI Binding Test EJB at Location 2, and invoke
      Object location2 = context.lookup(RemoteJndiBindingTest.JNDI_BINDING_2);
      TestCase.assertNotNull("Expected remote binding in JNDI at " + location2 + " was not found", location2);
      TestCase.assertTrue(location2 instanceof BindingTest);
      BindingTest test2 = (BindingTest) location2;
      String result2 = test2.echo(testMessage);
      TestCase.assertEquals(testMessage, result2);

      // Lookup JNDI Binding Test EJB at Location 3, and invoke
      Object location3 = context.lookup(RemoteJndiBindingTest.JNDI_BINDING_DECLARED_BY_BUSINESS_INTERFACE);
      TestCase.assertNotNull("Expected remote binding in JNDI at " + location3 + " was not found", location2);
      TestCase.assertTrue(location3 instanceof BindingTest);
      BindingTest test3 = (BindingTest) location3;
      String result3 = test3.echo(testMessage);
      TestCase.assertEquals(testMessage, result3);
   }

   /**
    * Ensures that an alternate local binding is made and may 
    * be invoked upon
    * 
    * @throws Exception
    */
   @Test
   public void testAlternateLocalJndiBinding() throws Exception
   {
      // Initialize
      String testMessage = "Find me at Overridden Local JNDI Location";

      // Get a JNDI Context
      Context context = new InitialContext(); // Props from jndi.properties on Client CP

      // Lookup JNDI Binding Test EJB at alternate local location, and invoke
      Object localLocation = context.lookup(LocalJndiBindingTest.JNDI_BINDING);
      TestCase.assertNotNull("Expected local binding in JNDI at " + localLocation + " was not found", localLocation);
      TestCase.assertTrue(localLocation instanceof BindingTest);
      BindingTest test = (BindingTest) localLocation;
      String result = test.echo(testMessage);
      TestCase.assertEquals(testMessage, result);
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
      // Initialize
      SessionTestCaseBase.setUpBeforeClass();

      // Deploy MC Beans
      bootstrap.deploy(SessionTestCaseBase.class);

      // Create a SLSB
      StatelessContainer container = Utils.createSlsb(JndiBindingTestBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
