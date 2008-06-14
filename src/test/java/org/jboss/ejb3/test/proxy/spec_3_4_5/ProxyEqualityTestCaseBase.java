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
package org.jboss.ejb3.test.proxy.spec_3_4_5;

import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandler;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.common.container.SessionContainer;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyEqualityTestCaseBase
 * 
 * Support for Proxy Equality Test Cases as 
 * defined by:
 * 
 * EJB 3.0 Core Specification 3.4.5
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ProxyEqualityTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ProxyEqualityTestCaseBase.class);

   private static EmbeddedTestMcBootstrap bootstrap;

   /**
    * Name of the SLSB Container for these tests
    */
   private static String containerName;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * EJB 3.0 Core Specification 3.4.5
    * 
    * Tests that a local proxy to a Session Bean is equal by value to itself
    */
   @Test
   public void testSameLocalProxyEqual() throws Throwable
   {
      // Make a Local Proxy Factory
      SessionProxyFactory factory = this.createSessionLocalProxyFactory();

      // Create Proxy
      Object proxy = this.createProxyDefault(factory);

      // Manually set the target container
      this.setContainerNameOnProxy(proxy);

      // Ensure equal to itself by value
      TestCase
            .assertTrue(
                  "EJB 3.0 Core Specification 3.4.5 Violation: Local proxy to Session Bean should be equal by value to itself",
                  proxy.equals(proxy));
   }

   /**
    * EJB 3.0 Core Specification 3.4.5
    * 
    * Tests that a remote proxy to a Session Bean is equal by value to itself
    */
   @Test
   public void testSameRemoteProxyEqual() throws Throwable
   {
      // Make a Local Proxy Factory
      SessionProxyFactory factory = this.createSessionLocalProxyFactory();

      // Create Proxy
      Object proxy = this.createProxyDefault(factory);

      // Manually set the target container
      this.setContainerNameOnProxy(proxy);

      // Ensure equal to itself by value
      TestCase
            .assertTrue(
                  "EJB 3.0 Core Specification 3.4.5 Violation: Remote proxy to Session Bean should be equal by value to itself",
                  proxy.equals(proxy));
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set a new MC Bootstrap
      ProxyEqualityTestCaseBase.setBootstrap(EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap());

      // Bind the Ejb3Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Shutdown MC
      ProxyEqualityTestCaseBase.getBootstrap().shutdown();

      // Set Bootstrap to null
      ProxyEqualityTestCaseBase.setBootstrap(null);
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a default proxy for the specified Session ProxyFactory
    */
   protected Object createProxyDefault(SessionProxyFactory factory)
   {
      return factory.createProxyDefault();
   }

   /**
    * Sets the Container Name on the specified proxy
    * 
    * @param proxy
    */
   protected void setContainerNameOnProxy(Object proxy)
   {
      // Get the InvocationHander for the Proxy
      ProxyInvocationHandler handler = (ProxyInvocationHandler) Proxy.getInvocationHandler(proxy);
      handler.setContainerName(ProxyEqualityTestCaseBase.getContainerName());
   }

   // --------------------------------------------------------------------------------||
   // Specifications -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Container for use in this TestCase
    * 
    * @return
    */
   protected abstract SessionContainer getContainer();

   /**
    * Creates a Proxy Factory for local Proxies
    * 
    * @return
    * @throws Throwable
    */
   protected abstract SessionProxyFactory createSessionLocalProxyFactory() throws Throwable;

   /**
    * Creates a Proxy Factory for remote Proxies
    * 
    * @return
    * @throws Throwable
    */
   protected abstract SessionProxyFactory createSessionRemoteProxyFactory() throws Throwable;

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected static EmbeddedTestMcBootstrap getBootstrap()
   {
      return ProxyEqualityTestCaseBase.bootstrap;
   }

   protected static void setBootstrap(EmbeddedTestMcBootstrap bootstrap)
   {
      ProxyEqualityTestCaseBase.bootstrap = bootstrap;
   }

   protected static String getContainerName()
   {
      return containerName;
   }

   protected static void setContainerName(String containerName)
   {
      ProxyEqualityTestCaseBase.containerName = containerName;
   }
}
