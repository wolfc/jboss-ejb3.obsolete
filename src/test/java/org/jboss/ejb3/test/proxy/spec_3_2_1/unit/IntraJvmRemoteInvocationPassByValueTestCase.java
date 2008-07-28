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
package org.jboss.ejb3.test.proxy.spec_3_2_1.unit;

import org.jboss.aop.Dispatcher;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessBean;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * IntraJvmRemoteInvocationPassByValueTestCase
 * 
 * Tests for EJB 3.0 Core Specification 3.2.1:
 * 
 * "The arguments and results of the methods of 
 * the remote business interface are passed by value."
 * 
 * EJBTHREE-1401
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class IntraJvmRemoteInvocationPassByValueTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(IntraJvmRemoteInvocationPassByValueTestCase.class);

   /*
    * Bootstrap (MC Abstraction)
    */
   private static EmbeddedTestMcBootstrap bootstrap;

   /*
    * SFSB Bean Implementation class to use for testing
    */
   //TODO Needs to be a bean implementation class that will support methods
   // allowing us to test for pass-by-value; this is here as a stub
   private static final Class<?> sfsbImplementationClass = MyStatefulBean.class;

   /*
    * SLSB Bean Implementation class to use for testing
    */
   //TODO Needs to be a bean implementation class that will support methods
   // allowing us to test for pass-by-value; this is here as a stub
   private static final Class<?> slsbImplementationClass = MyStatelessBean.class;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /**
    * This test is in place as an example only.
    * 
    * All tests in this class should be documented for 
    * intent at the method-level, like so.
    */
   @Test
   public void testExampleMethod() throws Throwable{
      
      /*
       * Show what were doing in the next code block
       */
      
      // Give line-specific information if necessary
      log.warn("Needs Implementation");
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      /*
       * Setup the bootstrap environment
       */
      
      // Create and set a new MC Bootstrap
      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();

      // Bind the Ejb3Registrar to the Locator
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));

      // Deploy Beans
      bootstrap.deploy(IntraJvmRemoteInvocationPassByValueTestCase.class);
      
      /*
       * Create EJBs and Install
       */

      // Create a SFSB Container
      StatefulContainer sfsb = Utils.createSfsb(sfsbImplementationClass);
      log.info("Created SFSB Container: " + sfsb.getName());

      // Create a SLSB Container
      StatelessContainer slsb = Utils.createSlsb(slsbImplementationClass);
      log.info("Created SLSB Container: " + slsb.getName());

      // Register Containers w/ Remoting
      Dispatcher.singleton.registerTarget(sfsb.getName(), sfsb);
      Dispatcher.singleton.registerTarget(slsb.getName(), slsb);

      // Install EJBs into MC
      Ejb3RegistrarLocator.locateRegistrar().bind(sfsb.getName(), sfsb);
      Ejb3RegistrarLocator.locateRegistrar().bind(slsb.getName(), slsb);
   }

   /**
    * Lifecycle stop, after the suite is executed
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Unbind the Ejb3Registrar from the Locator
      Ejb3RegistrarLocator.unbindRegistrar();

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }

}
