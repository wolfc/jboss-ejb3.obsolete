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
package org.jboss.ejb3.test.proxy.impl.spec_3_4_5.unit;

import java.io.Serializable;
import java.util.UUID;

import junit.framework.TestCase;

import org.jboss.aop.Dispatcher;
import org.jboss.ejb3.proxy.impl.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.stateful.StatefulSessionLocalProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.stateful.StatefulSessionRemoteProxyFactory;
import org.jboss.ejb3.proxy.spi.intf.SessionProxy;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulBean;
import org.jboss.ejb3.test.proxy.impl.spec_3_4_5.ProxyEqualityTestCaseBase;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SfsbProxyEqualityTestCase
 * 
 * Test Cases to ensure that SFSB Proxies properly implement
 * the notion of object equality described by 
 * EJB3 Core Specification 3.4.5.1
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SfsbProxyEqualityTestCase extends ProxyEqualityTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SfsbProxyEqualityTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * EJB 3.0 Core Specification 3.4.5.1
    * 
    * Tests that two local proxies to the same SFSB are not equal by value
    */
   @Test
   public void testDifferentSfsbLocalProxiesNotEqual() throws Throwable
   {
      // Make a Local Proxy Factory
      StatefulSessionLocalProxyFactory factory = this.createSessionLocalProxyFactory();

      // Create 2 Proxies
      Object proxy1 = factory.createProxyDefault();
      Object proxy2 = factory.createProxyDefault();

      // Manually Set Session IDs
      this.setSessionIdOnProxy(proxy1, new Integer(1));
      this.setSessionIdOnProxy(proxy2, new Integer(2));

      // Ensure they're not equal to one another
      TestCase
            .assertTrue(
                  "EJB 3.0 Core Specification 3.4.5.1 Violation: Different local proxies to same SFSB should not be equal by value",
                  !proxy1.equals(proxy2));
      TestCase.assertTrue("Hash Codes for unequal Proxies should (most likely) not be equal",
            proxy1.hashCode() != proxy2.hashCode());
   }

   /**
    * EJB 3.0 Core Specification 3.4.5.1
    * 
    * Tests that two remote proxies to the same SFSB are not equal by value
    */
   @Test
   public void testDifferentSfsbRemoteProxiesNotEqual() throws Throwable
   {
      // Make a Remote Proxy Factory
      StatefulSessionRemoteProxyFactory factory = this.createSessionRemoteProxyFactory();

      // Create 2 Proxies
      Object proxy1 = factory.createProxyDefault();
      Object proxy2 = factory.createProxyDefault();

      // Manually Set Session IDs
      this.setSessionIdOnProxy(proxy1, new Integer(1));
      this.setSessionIdOnProxy(proxy2, new Integer(2));

      // Ensure they're not equal to one another
      TestCase
            .assertTrue(
                  "EJB 3.0 Core Specification 3.4.5.1 Violation: Different remote proxies to same SFSB should not be equal by value",
                  !proxy1.equals(proxy2));
      TestCase.assertTrue("Hash Codes for unequal Proxies should (most likely) not be equal",
            proxy1.hashCode() != proxy2.hashCode());
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Sets the specified ID on the specified proxy
    * 
    * @param proxy
    * @param id
    */
   private void setSessionIdOnProxy(Object proxy, Serializable id)
   {
      // Get the InvocationHander for the Proxy
      SessionProxy handler = (SessionProxy) proxy;
      handler.setTarget(id);
   }

   /**
    * Creates a default proxy for the specified Session ProxyFactory
    */
   protected Object createProxyDefault(SessionProxyFactory factory)
   {
      // Get Proxy
      Object proxy = super.createProxyDefault(factory);

      // Set a unique ID
      this.setSessionIdOnProxy(proxy, UUID.randomUUID());

      // Return
      return proxy;
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Call Super
      ProxyEqualityTestCaseBase.beforeClass();

      // Deploy Beans
      SfsbProxyEqualityTestCase.getBootstrap().deploy(SfsbProxyEqualityTestCase.class);

      // Create a SFSB Container
      StatefulContainer container = Utils.createSfsb(MyStatefulBean.class);
      log.info("Created SFSB Container: " + container.getName());
      SfsbProxyEqualityTestCase.setContainerName(container.getName());

      // Register Container w/ Remoting
      Dispatcher.singleton.registerTarget(container.getName(), container);

      // Install SFSB into MC
      SfsbProxyEqualityTestCase.getBootstrap().installInstance(container.getName(), container);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Container for use in this TestCase
    * 
    * @return
    */
   @Override
   protected StatefulContainer getContainer()
   {
      return this.getSfsb();
   }

   /**
    * Creates a Proxy Factory for local Proxies
    * 
    * @param container
    * @return
    */
   @Override
   protected StatefulSessionLocalProxyFactory createSessionLocalProxyFactory() throws Throwable
   {
      // Get the SFSB Container
      StatefulContainer sfsb = this.getContainer();

      // Make a Local Proxy Factory
      StatefulSessionLocalProxyFactory factory = new StatefulSessionLocalProxyFactory(
            StatefulSessionLocalProxyFactory.class.getName(), sfsb.getName(), sfsb.getName(), sfsb.getMetaData(), sfsb
                  .getClassLoader(), null);

      // Start
      factory.start();

      // Return
      return factory;
   }

   /**
    * Creates a Proxy Factory for remote Proxies
    * 
    * @param container
    * @return
    */
   @Override
   protected StatefulSessionRemoteProxyFactory createSessionRemoteProxyFactory() throws Throwable
   {
      // Get the SFSB Container
      StatefulContainer sfsb = this.getContainer();

      // Make a Remote Proxy Factory
      StatefulSessionRemoteProxyFactory factory = new StatefulSessionRemoteProxyFactory(
            StatefulSessionRemoteProxyFactory.class.getName(), sfsb.getName(), sfsb.getName(), sfsb.getMetaData(), sfsb
                  .getClassLoader(), "socket://localhost:3874", ProxyEqualityTestCaseBase.advisor, null);

      // Start
      factory.start();

      // Return
      return factory;
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the SLSB Container from MC
    * 
    * @return
    */
   protected StatefulContainer getSfsb()
   {
      return (StatefulContainer) SfsbProxyEqualityTestCase.getBootstrap().getKernel().getController()
            .getInstalledContext(SfsbProxyEqualityTestCase.getContainerName()).getTarget();
   }

}
