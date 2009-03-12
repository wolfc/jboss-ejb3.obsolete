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

import junit.framework.TestCase;

import org.jboss.ejb3.proxy.impl.factory.session.stateless.StatelessSessionLocalProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.stateless.StatelessSessionRemoteProxyFactory;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.SessionContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessBean;
import org.jboss.ejb3.test.proxy.impl.spec_3_4_5.ProxyEqualityTestCaseBase;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SlsbProxyEqualityTestCase
 * 
 * Test Cases to ensure that SLSB Proxies properly implement
 * the notion of object equality described by 
 * EJB3 Core Specification 3.4.5.2
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SlsbProxyEqualityTestCase extends ProxyEqualityTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SlsbProxyEqualityTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * EJB 3.0 Core Specification 3.4.5.2
    * 
    * Tests that two local proxies to the same SLSB are equal by value
    */
   @Test
   public void testDifferentSlsbLocalProxiesEqual() throws Throwable
   {
      // Make a Local Proxy Factory
      StatelessSessionLocalProxyFactory factory = this.createSessionLocalProxyFactory();

      // Create 2 Proxies
      Object proxy1 = factory.createProxyDefault();
      Object proxy2 = factory.createProxyDefault();

      // Ensure they're equal to one another
      TestCase
            .assertTrue(
                  "EJB 3.0 Core Specification 3.4.5.2 Violation: Different local proxies to same SLSB should be equal by value",
                  proxy1.equals(proxy2));
      TestCase.assertTrue("Hash Codes for equal Proxies should be equal", proxy1.hashCode() == proxy2.hashCode());
   }

   /**
    * EJB 3.0 Core Specification 3.4.5.2
    * 
    * Tests that two remote proxies to the same SLSB are equal by value
    */
   @Test
   public void testDifferentSlsbRemoteProxiesEqual() throws Throwable
   {
      // Make a Remote Proxy Factory
      StatelessSessionRemoteProxyFactory factory = this.createSessionRemoteProxyFactory();
      factory.start();

      // Create 2 Proxies
      Object proxy1 = factory.createProxyDefault();
      Object proxy2 = factory.createProxyDefault();

      // Ensure they're equal to one another
      TestCase
            .assertTrue(
                  "EJB 3.0 Core Specification 3.4.5.2 Violation: Different remote proxies to same SLSB should be equal by value",
                  proxy1.equals(proxy2));
      TestCase.assertTrue("Hash Codes for equal Proxies should be equal", proxy1.hashCode() == proxy2.hashCode());
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Call Super
      ProxyEqualityTestCaseBase.beforeClass();

      // Create a SLSB Container
      StatelessContainer container = Utils.createSlsb(MyStatelessBean.class);
      log.info("Created SLSB Container: " + container.getName());
      SlsbProxyEqualityTestCase.setContainerName(container.getName());

      // Install into MC
      SlsbProxyEqualityTestCase.getBootstrap().installInstance(container.getName(), container);
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
   protected StatelessContainer getContainer()
   {
      return this.getSlsb();
   }

   /**
    * Creates a Proxy Factory for local Proxies
    * 
    * @return
    * @throws Throwable
    */
   @Override
   protected StatelessSessionLocalProxyFactory createSessionLocalProxyFactory() throws Throwable
   {
      // Get Container
      SessionContainer container = this.getContainer();

      // Create the Factory
      StatelessSessionLocalProxyFactory factory = new StatelessSessionLocalProxyFactory(
            StatelessSessionLocalProxyFactory.class.getName(), container.getName(), container.getName(), container
                  .getMetaData(), container.getClassLoader(), null);

      // Start
      factory.start();

      // Return
      return factory;
   }

   /**
    * Creates a Proxy Factory for remote Proxies
    * 
    * @return
    * @throws Throwable
    */
   @Override
   protected StatelessSessionRemoteProxyFactory createSessionRemoteProxyFactory() throws Throwable
   {
      // Get Container
      SessionContainer container = this.getContainer();

      // Create the Factory
      StatelessSessionRemoteProxyFactory factory = new StatelessSessionRemoteProxyFactory(
            StatelessSessionRemoteProxyFactory.class.getName(), container.getName(), container.getName(), container
                  .getMetaData(), container.getClassLoader(), null, ProxyEqualityTestCaseBase.advisor, null);

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
   protected StatelessContainer getSlsb()
   {
      return (StatelessContainer) SlsbProxyEqualityTestCase.getBootstrap().getKernel().getController()
            .getInstalledContext(SlsbProxyEqualityTestCase.getContainerName()).getTarget();
   }

}
