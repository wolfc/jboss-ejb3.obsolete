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
package org.jboss.ejb3.test.proxy.impl.session.unit;

import static org.junit.Assert.assertTrue;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStateless2xOnlyBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessLocalHome;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemoteHome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyStatelessSession2xOnlyExplicitJndiNameTestCase
 * 
 * Tests that an EJB with XML Override "jndi-name" is bound as expected
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyStatelessSession2xOnlyExplicitJndiNameTestCase extends SessionTestCaseSupport
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * JNDI Context
    */
   private static Context context;

   private static final String OVERRIDE_JNDI_NAME_LOCAL = "JNDINAMEOVERRIDELOCAL";

   private static final String OVERRIDE_JNDI_NAME_REMOTE = "JNDINAMEOVERRIDEREMOTE";

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests to ensure that the override JNDI name applies to 
    * an EJB 2.x Remote Home, when there's no business interfaces
    */
   @Test
   public void testRemoteHome() throws Exception
   {
      String jndiName = OVERRIDE_JNDI_NAME_REMOTE;
      this.executeTest(jndiName, MyStatelessRemoteHome.class);
   }

   /**
    * Tests to ensure that the override JNDI name applies to 
    * an EJB 2.x Local Home, when there's no business interfaces
    */
   @Test
   public void testLocalHome() throws Exception
   {
      String jndiName = OVERRIDE_JNDI_NAME_LOCAL;
      this.executeTest(jndiName, MyStatelessLocalHome.class);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods  -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that a proxy of specified type can be found at the specified JNDI
    * Address
    */
   public void executeTest(String jndiName, Class<?> expectedType) throws Exception
   {
      Object bean = null;
      try
      {
         bean = this.getNamingContext().lookup(jndiName);
      }
      catch (NameNotFoundException nnfe)
      {
         TestCase.fail("Expected JNDI Name at " + jndiName + " was not found");
      }
      assertTrue(expectedType.isAssignableFrom(bean.getClass()));
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Context to be used for JNDI Operations 
    */
   @Override
   protected Context getNamingContext()
   {
      return context;
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
      // Create Bootstrap and Deploy
      SessionTestCaseBase.setUpBeforeClass();

      // Deploy MC Beans
      ProxyStatelessSession2xOnlyExplicitJndiNameTestCase.bootstrap.deploy(ProxyStatelessSessionTestCase.class);

      // Create a SLSB
      StatelessContainer container = Utils.createSlsb(MyStateless2xOnlyBean.class);

      // Manually set the JNDI name (override to mock XML)

      /*
       * This is the test condition!
       */

      container.getMetaData().setHomeJndiName(OVERRIDE_JNDI_NAME_REMOTE);
      container.getMetaData().setJndiName(OVERRIDE_JNDI_NAME_REMOTE);
      container.getMetaData().setLocalHomeJndiName(OVERRIDE_JNDI_NAME_LOCAL);
      container.getMetaData().setLocalJndiName(OVERRIDE_JNDI_NAME_LOCAL);

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
