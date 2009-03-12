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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import javax.naming.InitialContext;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.ServiceContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyServiceBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyServiceBeanWithExplicitJndiBindings;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyServiceLocalBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyServiceRemoteBusiness;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyServiceTestCase
 *
 * General tests for @Service beans:
 * 
 * - Bound as expected
 * - May be invoked
 * - .equals() implementation of Proxies is correct
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyServiceTestCase extends SessionTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ProxyServiceTestCase.class);

   private static InitialContext context;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests binding and invocation upon the local business 
    * interface
    */
   @Test
   public void testLocalDefaultBindings() throws Exception
   {
      // Define the JNDI name to use
      String jndiName = "MyServiceBean/local";

      // Run test
      this.runLocalTest(jndiName);
   }

   /**
    * Tests binding and invocation upon the remote business 
    * interface
    */
   @Test
   public void testRemoteDefaultBindings() throws Exception
   {
      // Define the JNDI name to use
      String jndiName = "MyServiceBean/remote";

      // Run test
      this.runRemoteTest(jndiName);
   }

   /**
    * Tests binding and invocation upon the local business 
    * interface using @LocalBinding.jnidBinding value
    */
   @Test
   public void testLocalExplicitBindings() throws Exception
   {
      // Define the JNDI name to use
      String jndiName = MyServiceBeanWithExplicitJndiBindings.JNDI_NAME_LOCAL;

      // Run test
      this.runLocalTest(jndiName);
   }

   /**
    * Tests binding and invocation upon the remote business 
    * interface using @RemoteBinding.jnidBinding value
    */
   @Test
   public void testRemoteExplicitBindings() throws Exception
   {
      // Define the JNDI name to use
      String jndiName = MyServiceBeanWithExplicitJndiBindings.JNDI_NAME_REMOTE;

      // Run test
      this.runRemoteTest(jndiName);
   }

   /**
    * Tests that proxies with the same target container are
    * equal
    */
   @Test
   public void testProxiesSameTargetContainerEqual() throws Exception
   {
      // Lookup
      Object bean1 = context.lookup("MyServiceBean/local");
      Object bean2 = context.lookup("MyServiceBean/remote");

      // Ensure equal
      assertEquals("@Service proxies with the same target container must be equal", bean1, bean2);
   }

   /**
    * Tests that proxies with the different target containers are
    * not equal
    */
   @Test
   public void testProxiesDifferentTargetContainersNotEqual() throws Exception
   {
      // Lookup
      Object bean1 = context.lookup("MyServiceBean/local");
      Object bean2 = context.lookup(MyServiceBeanWithExplicitJndiBindings.JNDI_NAME_LOCAL);

      // Ensure equal
      assertNotSame("@Service proxies with the different target containers must not be equal", bean1, bean2);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Run the local test using the specified lookup name
    * 
    * @param jndiName
    */
   protected void runLocalTest(String jndiName) throws Exception
   {
      // Lookup
      Object bean = context.lookup(jndiName);

      // Ensure expected type
      assertTrue(bean instanceof MyServiceLocalBusiness);

      // Cast
      MyServiceLocalBusiness ejb = (MyServiceLocalBusiness) bean;

      // Invoke and log
      log.info("Invoked upon @Service local business view: " + ejb.getUuid());
   }

   /**
    * Run the remote test using the specified lookup name
    * 
    * @param jndiName
    */
   protected void runRemoteTest(String jndiName) throws Exception
   {
      // Lookup
      Object bean = context.lookup(jndiName);

      // Ensure expected type
      assertTrue(bean instanceof MyServiceRemoteBusiness);

      // Cast
      MyServiceRemoteBusiness ejb = (MyServiceRemoteBusiness) bean;

      // Invoke and log
      log.info("Invoked upon @Service remote business view: " + ejb.getUuid());
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
      ProxyServiceTestCase.bootstrap.deploy(SessionTestCaseBase.class);

      // Create @Service containers
      ServiceContainer defaultBindingsContainer = Utils.createService(MyServiceBean.class);
      ServiceContainer explicitBindingsContainer = Utils.createService(MyServiceBeanWithExplicitJndiBindings.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(defaultBindingsContainer.getName(), defaultBindingsContainer);
      Ejb3RegistrarLocator.locateRegistrar().bind(explicitBindingsContainer.getName(), explicitBindingsContainer);

      // Set Naming COntext
      ProxyServiceTestCase.context = new InitialContext(); // Props from jndi.properties

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
