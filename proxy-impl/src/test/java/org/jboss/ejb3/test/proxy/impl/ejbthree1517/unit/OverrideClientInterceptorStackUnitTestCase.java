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
package org.jboss.ejb3.test.proxy.impl.ejbthree1517.unit;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.SessionContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.ejbthree1517.ReturnStaticValueInterceptor;
import org.jboss.ejb3.test.proxy.impl.ejbthree1517.TestClientInterceptorStack;
import org.jboss.ejb3.test.proxy.impl.ejbthree1517.TestClientInterceptorStackStatefulBean;
import org.jboss.ejb3.test.proxy.impl.ejbthree1517.TestClientInterceptorStackStatelessBean;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * OverrideClientInterceptorStackUnitTestCase
 * 
 * Test Cases to ensure that the client interceptor stack
 * may be overridden via @RemoteBinding.interceptorStack
 * 
 * EJBTHREE-1517
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class OverrideClientInterceptorStackUnitTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(OverrideClientInterceptorStackUnitTestCase.class);

   protected static EmbeddedTestMcBootstrap bootstrap;

   protected static final String FILENAME_EJB3_INTERCEPTORS_AOP = "org/jboss/ejb3/test/proxy/impl/ejbthree1517/unit/ejb3-interceptors-ejbthree1517-aop.xml";

   protected static Context context = null;

   protected static final Set<SessionContainer> containers = new HashSet<SessionContainer>();

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that SLSB bindings for both a standard interceptor stack
    * and an overridden one may be made side-by-side, and further that
    * the overridden stack is applied
    */
   @Test
   public void testSlsbClientInterceptorStackOverride() throws Exception
   {
      // Lookup the proxies
      TestClientInterceptorStack normal = (TestClientInterceptorStack) context
            .lookup(TestClientInterceptorStack.JNDI_BINDING_NORMAL_STACK_SLSB);
      TestClientInterceptorStack overridden = (TestClientInterceptorStack) context
            .lookup(TestClientInterceptorStack.JNDI_BINDING_OVERRIDDEN_STACK_SLSB);

      // Invoke upon the standard
      String returnValueStandard = normal.getValue();
      TestCase.assertEquals(TestClientInterceptorStack.DEFAULT_RETURN_VALUE, returnValueStandard);
      log.info("Got expected result from SLSB Standard Binding: " + returnValueStandard);

      // Invoke upon the overridden
      String returnValueOverridden = overridden.getValue();
      TestCase.assertEquals(ReturnStaticValueInterceptor.RETURN_VALUE, returnValueOverridden);
      log.info("Got expected result from SLSB Overridden Binding: " + returnValueOverridden);
   }

   /**
    * Ensures that SFSB bindings for both a standard interceptor stack
    * and an overridden one may be made side-by-side, and further that
    * the overridden stack is applied
    */
   @Test
   public void testSfsbClientInterceptorStackOverride() throws Exception
   {
      // Lookup the proxies
      TestClientInterceptorStack normal = (TestClientInterceptorStack) context
            .lookup(TestClientInterceptorStack.JNDI_BINDING_NORMAL_STACK_SFSB);
      TestClientInterceptorStack overridden = (TestClientInterceptorStack) context
            .lookup(TestClientInterceptorStack.JNDI_BINDING_OVERRIDDEN_STACK_SFSB);

      // Invoke upon the standard
      String returnValueStandard = normal.getValue();
      TestCase.assertEquals(TestClientInterceptorStack.DEFAULT_RETURN_VALUE, returnValueStandard);
      log.info("Got expected result from SFSB Standard Binding: " + returnValueStandard);

      // Invoke upon the overridden
      String returnValueOverridden = overridden.getValue();
      TestCase.assertEquals(ReturnStaticValueInterceptor.RETURN_VALUE, returnValueOverridden);
      log.info("Got expected result from SFSB Overridden Binding: " + returnValueOverridden);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void setUpBeforeClass() throws Throwable
   {
      // Start up MC
      bootstrap = new EmbeddedTestMcBootstrap();
      bootstrap.run();

      // Bind the Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));

      // Load ejb3-interceptors-aop.xml into AspectManager
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String ejb3InterceptorsAopFilename = getEjb3InterceptorsAopFilename();
      URL url = cl.getResource(ejb3InterceptorsAopFilename);
      if (url == null)
      {
         throw new RuntimeException("Could not load " + AspectManager.class.getSimpleName()
               + " with definitions from XML as file " + ejb3InterceptorsAopFilename + " could not be found");
      }
      AspectXmlLoader.deployXML(url);
      log.info("Deployed AOP XML: " + ejb3InterceptorsAopFilename);

      // Throw Session EJB Support into MC
      bootstrap.deploy(SessionTestCaseBase.class);

      // Create the EJBs
      StatefulContainer sfsb = Utils.createSfsb(TestClientInterceptorStackStatefulBean.class);
      StatelessContainer slsb = Utils.createSlsb(TestClientInterceptorStackStatelessBean.class);

      // Add the containers for accessing later
      containers.add(sfsb);
      containers.add(slsb);

      // Bind the containers
      for (SessionContainer container : containers)
      {
         Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);
      }

      // Create JNDI Context
      context = new InitialContext(); // Props from CP jndi.properties
   }

   @AfterClass
   public static void tearDownAfterClass() throws Throwable
   {
      // For each container
      for (SessionContainer container : containers)
      {
         // Bring down the house
         Ejb3RegistrarLocator.locateRegistrar().unbind(container.getName());
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected static String getEjb3InterceptorsAopFilename()
   {
      return OverrideClientInterceptorStackUnitTestCase.FILENAME_EJB3_INTERCEPTORS_AOP;
   }

}
