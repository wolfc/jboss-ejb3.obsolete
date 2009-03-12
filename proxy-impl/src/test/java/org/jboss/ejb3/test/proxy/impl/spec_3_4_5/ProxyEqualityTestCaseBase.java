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
package org.jboss.ejb3.test.proxy.impl.spec_3_4_5;

import java.net.URL;

import junit.framework.TestCase;

import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.ClassAdvisor;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.impl.factory.session.SessionProxyFactory;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.impl.common.container.SessionContainer;
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

   private static final String FILENAME_EJB3_INTERCEPTORS_AOP = "ejb3-interceptors-aop.xml";

   /**
    * Name of the SLSB Container for these tests
    */
   private static String containerName;

   /**
    * The Advisor for these tests
    */
   protected static Advisor advisor = null;

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

      // Load ejb3-interceptors-aop.xml into AspectManager
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL url = cl.getResource(FILENAME_EJB3_INTERCEPTORS_AOP);
      if (url == null)
      {
         throw new RuntimeException("Could not load " + AspectManager.class.getSimpleName()
               + " with definitions from XML as file " + FILENAME_EJB3_INTERCEPTORS_AOP + " could not be found");
      }
      AspectXmlLoader.deployXML(url);
      AspectManager manager = AspectManager.instance();
      Advisor advisor = new ClassAdvisor(ProxyEqualityTestCaseBase.class, manager);
      ProxyEqualityTestCaseBase.advisor = advisor;
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
