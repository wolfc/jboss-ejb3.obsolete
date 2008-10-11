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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.ejbthree1130.TestBean;
import org.jboss.ejb3.test.proxy.ejbthree1130.TestRemoteBusiness;
import org.jboss.ejb3.test.proxy.session.unit.ProxyStatefulSessionTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that EJBs with a @LocalBinding and no local 
 * business interface fails deployment
 * 
 * EJBTHREE-1130
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class LocalBindingWithNoLocalInterfaceUnitTestCase extends SessionTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * JNDI Context
    */
   private static Context context;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   /**
    * Ensure that EJB with Remote Business interface and 
    * @LocalBinding defined fails deployment
    */
   @Test
   public void testLocalBindingWithNoLocalInterface() throws Exception
   {
      // Initialize
      String jndiName = TestRemoteBusiness.JNDI_NAME;

      try
      {
         // Lookup EJB 
         context.lookup(jndiName);
      }
      catch (NamingException ne)
      {
         // Expected
         return;
      }

      // Should not be reached
      TestCase.fail("EJB should fail deployment and not be available at " + jndiName);
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
      ProxyStatefulSessionTestCase.bootstrap.deploy(SessionTestCaseBase.class);

      // Create a SLSB
      StatefulContainer container = Utils.createSfsb(TestBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

      // Create JNDI Context
      context = new InitialContext(); // Props from CP jndi.properties

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }

}