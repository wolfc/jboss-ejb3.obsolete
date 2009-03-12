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

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalHome;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulRemoteBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulRemoteHome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyStatefulSessionTestCase
 * 
 * Test Cases to ensure proper invocation of SFSBs
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyStatefulSessionTestCase extends SessionTestCaseBase
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
      Object bean = ProxyStatefulSessionTestCase.context.lookup("MyStatefulBean/local");
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
      Object bean = ProxyStatefulSessionTestCase.context.lookup("MyStatefulBean/local-"
            + MyStatefulLocalBusiness.class.getName());
      assertTrue("Bean must be assignable to " + MyStatefulLocalBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulLocalBusiness);

      // Invoke and Test Result
      int result = ((MyStatefulLocalBusiness) bean).getNextCounter();
      assertEquals(result, 0);
   }

   /**
    * Test Remote Business Binding and Invocation
    * 
    * @throws Exception
    */
   @Test
   public void testRemoteBusiness() throws Exception
   {
      // Obtain the Proxy
      Object bean = ProxyStatefulSessionTestCase.context.lookup("MyStatefulBean/remote");
      assertTrue("Bean must be assignable to " + MyStatefulRemoteBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulRemoteBusiness);

      // Invoke and Test Result
      int result = ((MyStatefulRemoteBusiness) bean).getNextCounter();
      assertEquals(result, 0);
   }

   /**
    * Test Remote Business Binding and Invocation
    * to a specific interface
    * 
    * @throws Exception
    */
   @Test
   public void testRemoteBusinessSpecificInterface() throws Exception
   {
      // Obtain the Proxy
      Object bean = ProxyStatefulSessionTestCase.context.lookup("MyStatefulBean/remote-"
            + MyStatefulRemoteBusiness.class.getName());
      assertTrue("Bean must be assignable to " + MyStatefulRemoteBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulRemoteBusiness);

      // Invoke and Test Result
      int result = ((MyStatefulRemoteBusiness) bean).getNextCounter();
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
      Object bean = ProxyStatefulSessionTestCase.context.lookup("MyStatefulBean/localHome");
      assertTrue(bean instanceof MyStatefulLocalHome);
   }

   /**
    * Test Remote Home Binding
    * 
    * @throws Exception
    */
   @Test
   public void testRemoteHome() throws Exception
   {
      Object bean = ProxyStatefulSessionTestCase.context.lookup("MyStatefulBean/home");
      assertTrue(bean instanceof MyStatefulRemoteHome);
   }

   /**
    * Tests that SFSB Proxies invoke within
    * the scope of their own session only
    * 
    * @throws Exception
    */
   @Test
   public void testSessionScoping() throws Exception
   {
      // Lookup 2 Instances
      MyStatefulLocalBusiness bean1 = (MyStatefulLocalBusiness) ProxyStatefulSessionTestCase.context
            .lookup("MyStatefulBean/local");
      MyStatefulLocalBusiness bean2 = (MyStatefulLocalBusiness) ProxyStatefulSessionTestCase.context
            .lookup("MyStatefulBean/local");

      // Invoke continuously on instance one 10 times to ensure that the SFSB counter increments properly
      for (int counter = 0; counter < 10; counter++)
      {
         // Invoke
         int result = bean1.getNextCounter();
         TestCase.assertEquals("SFSB invocations should have memory of the session", counter, result);
      }

      // Invoke once on instance two to ensure its scope is separate from that of instance one
      TestCase
            .assertEquals("SFSB Session Scopes must not be shared between Proxy instances", 0, bean2.getNextCounter());
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
      ProxyStatefulSessionTestCase.bootstrap.deploy(ProxyStatefulSessionTestCase.class);

      // Create a SLSB
      StatefulContainer container = Utils.createSfsb(MyStatefulBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

      // Create JNDI Context
      ProxyStatefulSessionTestCase.context = new InitialContext(); // Props from CP jndi.properties

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
