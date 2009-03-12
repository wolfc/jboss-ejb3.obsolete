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
package org.jboss.ejb3.test.proxy.impl.common.container.unit;

import static org.junit.Assert.assertNotNull;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.SessionContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.SimpleSLSBLocal;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.SimpleSLSBean;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link SessionContainer}
 * 
 * SessionContainerTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SessionContainerTestCase
{

   /**
    * Bootstrap
    */
   private static EmbeddedTestMcBootstrap bootstrap;

   /**
    * The {@link SessionContainer} which is instantiated by each test. Depending
    * on the test, its either a {@link StatefulContainer} or a {@link StatelessContainer}
    */
   private SessionContainer sessionContainer;

   /**
    * Instance of logger
    */
   private static Logger logger = Logger.getLogger(SessionContainerTestCase.class);

   /**
    * Initializes the required services
    * 
    * @throws Throwable
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Throwable
   {
      bootstrap = new EmbeddedTestMcBootstrap();
      bootstrap.run();

      // Bind the Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));

      bootstrap.deploy(SessionContainerTestCase.class);

   }

   /**
    * Shutdown the services
    * 
    * @throws Throwable
    */
   @AfterClass
   public static void tearDownAfterClass() throws Throwable
   {
      if (bootstrap != null)
      {
         bootstrap.shutdown();
      }
      bootstrap = null;
   }

   /**
    * This method takes care of any cleanup required after each test.
    */
   @After
   public void cleanupAfterEachTest()
   {

      if (sessionContainer != null)
      {
         logger.info("Unbinding: " + sessionContainer.getName());
         try
         {
            Ejb3RegistrarLocator.locateRegistrar().unbind(sessionContainer.getName());
         }
         catch (NotBoundException nbe)
         {
            // we are ok with this exception, which indicates that the test case had 
            // already unbound the ejb related bindings.
            logger.debug(sessionContainer.getName() + " was already unbound");

         }
      }

   }

   /**
    * Test the {@link SessionContainer#invoke(Object, org.jboss.ejb3.common.lang.SerializableMethod, Object[])}
    * method.
    * 
    * This test will check the invoke method by passing different "types" of parameters
    * to a bean's method. Ex: Pass a subclass param when the bean method accepts a superclass
    * param
    * 
    * @throws Throwable
    */
   @Test
   public void testBeanMethodInvocation() throws Throwable
   {
      // create the SLSB container
      this.sessionContainer = Utils.createSlsb(SimpleSLSBean.class);

      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      Context ctx = new InitialContext();
      // lookup the local bean
      String localJndiName = sessionContainer.getMetaData().getLocalJndiName();
      SimpleSLSBLocal local = (SimpleSLSBLocal) ctx.lookup(localJndiName);

      assertNotNull("Local bean is null", local);

      //call the method with various different parameters
      Object object = new String("I am some object");
      String string = new String("AnotherString");

      // method which accepts object/string
      local.printObject(object);
      local.printObject(string);
      // now pass an java.lang.Integer
      local.printObject(new Integer(34));
      // no param method
      local.noop();
      // method with return type
      int i = local.someMethodWithReturnType();
      // method with multiple different type of params
      local.printMultipleObjects(string, 2, 2.3f, new Float(34.2), 44.2d, new Double(22.234));

   }

   /**
    * 
    * Test the {@link SessionContainer#invoke(Object, org.jboss.ejb3.common.lang.SerializableMethod, Object[])}
    * method.
    * 
    * This test will check the invoke method by passing null value to the bean methods.
    * The intention of this test case is to ensure that there are no exceptions (especially
    * {@link NullPointerException} when the params being passed to bean methods are null)
    * 
    * @throws Throwable
    */
   @Test
   public void testBeanMethodInvocationForNullParams() throws Throwable
   {
      // create the SLSB container
      this.sessionContainer = Utils.createSlsb(SimpleSLSBean.class);

      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      Context ctx = new InitialContext();
      // lookup the local bean
      String localJndiName = sessionContainer.getMetaData().getLocalJndiName();
      SimpleSLSBLocal local = (SimpleSLSBLocal) ctx.lookup(localJndiName);

      assertNotNull("Local bean is null", local);

      // pass null to some params
      local.printMultipleObjects("hello", 2, 2.2f, null, 3.2, new Double(4.4));
      // pass null
      local.printObject(null);
      // pass null to all object params
      local.printMultipleObjects(null, 0, 0.0f, null, 0.0, null);
   }

}
