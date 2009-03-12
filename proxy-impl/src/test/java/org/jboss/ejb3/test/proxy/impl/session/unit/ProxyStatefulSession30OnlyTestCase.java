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
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStateful30OnlyBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulRemoteBusiness;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyStatefulSession30OnlyTestCase
 * 
 * Test Cases to ensure proper invocation of SFSBs with
 * only EJB3 Views exposed
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyStatefulSession30OnlyTestCase extends SessionTestCaseBase
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
      Object bean = ProxyStatefulSession30OnlyTestCase.context.lookup("MyStateful30OnlyBean/local");
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
      Object bean = ProxyStatefulSession30OnlyTestCase.context.lookup("MyStateful30OnlyBean/local-"
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
      Object bean = ProxyStatefulSession30OnlyTestCase.context.lookup("MyStateful30OnlyBean/remote");
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
      Object bean = ProxyStatefulSession30OnlyTestCase.context.lookup("MyStateful30OnlyBean/remote-"
            + MyStatefulRemoteBusiness.class.getName());
      assertTrue("Bean must be assignable to " + MyStatefulRemoteBusiness.class.getSimpleName() + " but was instead "
            + bean.getClass(), bean instanceof MyStatefulRemoteBusiness);

      // Invoke and Test Result
      int result = ((MyStatefulRemoteBusiness) bean).getNextCounter();
      assertEquals(result, 0);
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
      MyStatefulLocalBusiness bean1 = (MyStatefulLocalBusiness) ProxyStatefulSession30OnlyTestCase.context
            .lookup("MyStateful30OnlyBean/local");
      MyStatefulLocalBusiness bean2 = (MyStatefulLocalBusiness) ProxyStatefulSession30OnlyTestCase.context
            .lookup("MyStateful30OnlyBean/local");

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
      ProxyStatefulSession30OnlyTestCase.bootstrap.deploy(ProxyStatefulSessionTestCase.class);

      // Create a SLSB
      StatefulContainer container = Utils.createSfsb(MyStateful30OnlyBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

      // Create JNDI Context
      ProxyStatefulSession30OnlyTestCase.context = new InitialContext(); // Props from CP jndi.properties

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
