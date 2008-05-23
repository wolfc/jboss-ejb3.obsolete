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

import junit.framework.TestCase;

import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.hack.Hack;
import org.jboss.ejb3.test.proxy.common.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.common.container.SessionContainer;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyEqualityTestCaseBase
 * 
 * Support for Proxy Equality Test Cases
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
   private static String slsbContainerName;

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
      // Get the Container
      SessionContainer container = this.getContainer();

      // Make a Local Proxy Factory
      SessionProxyFactory factory = this.createSessionLocalProxyFactory(container);
      factory.start();

      // Create Proxy
      Object proxy = factory.createProxyDefault();

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
      // Get the Session Container
      SessionContainer container = this.getContainer();

      // Make a Local Proxy Factory
      SessionProxyFactory factory = this.createSessionLocalProxyFactory(container);
      factory.start();

      // Create Proxy
      Object proxy = factory.createProxyDefault();

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

      //TODO Remove Hack
      Hack.BOOTSTRAP = bootstrap;
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Shutdown MC
      ProxyEqualityTestCaseBase.getBootstrap().shutdown();

      // Set Bootstrap to null
      ProxyEqualityTestCaseBase.setBootstrap(null);
      Hack.BOOTSTRAP = null;
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
    * @param container
    * @return
    */
   protected abstract SessionProxyFactory createSessionLocalProxyFactory(SessionContainer container);

   /**
    * Creates a Proxy Factory for remote Proxies
    * 
    * @param container
    * @return
    */
   protected abstract SessionProxyFactory createSessionRemoteProxyFactory(SessionContainer container);

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static EmbeddedTestMcBootstrap getBootstrap()
   {
      return ProxyEqualityTestCaseBase.bootstrap;
   }

   public static void setBootstrap(EmbeddedTestMcBootstrap bootstrap)
   {
      ProxyEqualityTestCaseBase.bootstrap = bootstrap;
   }

   protected static String getSlsbContainerName()
   {
      return slsbContainerName;
   }

   protected static void setSlsbContainerName(String slsbContainerName)
   {
      ProxyEqualityTestCaseBase.slsbContainerName = slsbContainerName;
   }
}
