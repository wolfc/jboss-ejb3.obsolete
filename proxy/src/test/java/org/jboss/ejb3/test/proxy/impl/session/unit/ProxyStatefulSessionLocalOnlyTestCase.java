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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalHome;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalOnlyBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyStatefulSessionLocalOnlyTestCase
 * 
 * Test Cases to ensure proper invocation of SFSBs with 
 * only Local Views Defined
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyStatefulSessionLocalOnlyTestCase extends SessionTestCaseBase
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
    * Test Local Business Binding and Invocation
    * 
    * @throws Exception
    */
   @Test
   public void testLocalBusiness() throws Exception
   {
      // Obtain the Proxy
      Object bean = ProxyStatefulSessionLocalOnlyTestCase.context.lookup("MyStatefulLocalOnlyBean/local");
      assertTrue("Bean must be assignable to " + MyStatefulLocalBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulLocalBusiness);

      // Invoke and Test Result
      int result = ((MyStatefulLocalBusiness) bean).getNextCounter();
      assertEquals(result, 0);
   }

   /**
    * Test Local Business Binding and Invocation
    * to a specific interface
    * 
    * @throws Exception
    */
   @Test
   public void testLocalBusinessSpecificInterface() throws Exception
   {
      // Obtain the Proxy
      Object bean = ProxyStatefulSessionLocalOnlyTestCase.context.lookup("MyStatefulLocalOnlyBean/local-"
            + MyStatefulLocalBusiness.class.getName());
      assertTrue("Bean must be assignable to " + MyStatefulLocalBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulLocalBusiness);

      // Invoke and Test Result
      int result = ((MyStatefulLocalBusiness) bean).getNextCounter();
      assertEquals(result, 0);
   }

   /**
    * Test Local Home Binding
    * 
    * @throws Exception
    */
   @Test
   public void testLocalHome() throws Exception
   {
      Object bean = ProxyStatefulSessionLocalOnlyTestCase.context.lookup("MyStatefulLocalOnlyBean/localHome");
      assertTrue(bean instanceof MyStatefulLocalHome);
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
      ProxyStatefulSessionLocalOnlyTestCase.bootstrap.deploy(ProxyStatefulSessionTestCase.class);

      // Create a SLSB
      StatefulContainer container = Utils.createSfsb(MyStatefulLocalOnlyBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

      // Create JNDI Context
      ProxyStatefulSessionLocalOnlyTestCase.context = new InitialContext(); // Props from CP jndi.properties

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
