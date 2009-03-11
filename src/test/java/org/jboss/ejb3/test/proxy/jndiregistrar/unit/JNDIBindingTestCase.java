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
package org.jboss.ejb3.test.proxy.jndiregistrar.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteHomeBinding;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.handler.session.SessionSpecRemoteProxyInvocationHandler;
import org.jboss.ejb3.proxy.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.jndiregistrar.JndiStatelessSessionRegistrar;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.SessionContainer;
import org.jboss.ejb3.test.proxy.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStateful2xOnlyBean;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStateful30OnlyBean;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulBean;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulBeanWithBindings;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulLocalHome;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulRemoteBusiness;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulRemoteHome;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStateless2xOnlyBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStateless30OnlyBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessBeanWithBindings;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessLocal;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessLocalHome;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessRemoteHome;
import org.jboss.ejb3.test.proxy.jndiregistrar.BindingTest;
import org.jboss.ejb3.test.proxy.jndiregistrar.ClientBindUrlBindingTest;
import org.jboss.ejb3.test.proxy.jndiregistrar.ClientBindUrlTestBean;
import org.jboss.ejb3.test.proxy.jndiregistrar.JndiBindingTestBean;
import org.jboss.ejb3.test.proxy.jndiregistrar.LocalJndiBindingTest;
import org.jboss.ejb3.test.proxy.jndiregistrar.RemoteBindingNoJndiBindingTestBean;
import org.jboss.ejb3.test.proxy.jndiregistrar.RemoteJndiBindingTest;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.RemoteBindingMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.JbossSessionBeanJndiNameResolver;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JNDIBindingTestCase
 * 
 * Tests for verifying that the EJB proxies are bound correctly to the JNDI
 * and also unbound properly on undeploying the bean. 
 * 
 * @author Jaikiran Pai
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JNDIBindingTestCase
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
   private static Logger logger = Logger.getLogger(JNDIBindingTestCase.class);

   private static final String FILENAME_EJB3_INTERCEPTORS_AOP = "ejb3-interceptors-aop.xml";

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

      bootstrap.deploy(JNDIBindingTestCase.class);

      // Load ejb3-interceptors-aop.xml into AspectManager
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL url = cl.getResource(FILENAME_EJB3_INTERCEPTORS_AOP);
      if (url == null)
      {
         throw new RuntimeException("Could not load " + AspectManager.class.getSimpleName() + " with definitions from XML as file " + FILENAME_EJB3_INTERCEPTORS_AOP
               + " could not be found");
      }
      AspectXmlLoader.deployXML(url);

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
      // There might be a case when while running the test, a bean was registered to JNDI
      // but before it got unbound, the test failed (either a "Failure" or an "Error").
      // In such cases, ensure that the bean is unbound from the JNDI, so that if the 
      // subsequent test tries to bind the same EJB again then it won't run into a 
      // name already bound error.
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
    * Tests that an EJB with @RemoteBinding defined with no "jndiBinding" attribute 
    * is bound as expected to the default JNDI name
    * 
    *  EJBTHREE-1525
    * 
    * @throws Throwable
    */
   @Test
   public void testRemoteBindingButNoJndiBinding() throws Throwable
   {
      // Create and bind the bean into JNDI
      this.sessionContainer = Utils.createSlsb(RemoteBindingNoJndiBindingTestBean.class);
      Ejb3RegistrarLocator.locateRegistrar().bind(this.sessionContainer.getName(), this.sessionContainer);

      // Get a JNDI Context
      Context context = new InitialContext(); // Props from jndi.properties on Client CP

      // Lookup JNDI Binding Test EJB at location defined by metadata
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      String jndiBinding = smd.getJndiName();
      Object ejb = null;
      try
      {
         ejb = context.lookup(jndiBinding);
      }
      catch (NameNotFoundException nnfe)
      {
         fail("EJB was not bound as expected into JNDI at " + jndiBinding);
      }
      TestCase.assertNotNull("Expected remote binding in JNDI at " + jndiBinding + " was not found", ejb);
      TestCase.assertTrue(ejb instanceof BindingTest);
      BindingTest test = (BindingTest) ejb;
      String passed = "Testing ALR";
      String returnValue = test.echo(passed);
      TestCase.assertEquals(passed, returnValue);

      // Unbind and test
      this.unbindAndTest(context, this.sessionContainer);
   }
   
   /**
    * Ensures that bindings at N Remote Locations are made and may be
    * invoked upon, and that all associated bindings are cleansed on undeploy
    * 
    * EJBTHREE-1515
    */
   @Test
   public void testMultipleRemoteJndiBindings() throws Throwable
   {
      // Create and bind the bean into JNDI
      this.sessionContainer = Utils.createSlsb(JndiBindingTestBean.class);
      Ejb3RegistrarLocator.locateRegistrar().bind(this.sessionContainer.getName(), this.sessionContainer);

      // Initialize
      String testMessage = "Find me at N JNDI Locations";

      // Get a JNDI Context
      Context context = new InitialContext(); // Props from jndi.properties on Client CP

      // Lookup JNDI Binding Test EJB at Location 1, and invoke
      Object location1 = context.lookup(RemoteJndiBindingTest.JNDI_BINDING_1);
      TestCase.assertNotNull("Expected remote binding in JNDI at " + RemoteJndiBindingTest.JNDI_BINDING_1
            + " was not found", location1);
      TestCase.assertTrue(location1 instanceof BindingTest);
      BindingTest test1 = (BindingTest) location1;
      String result1 = test1.echo(testMessage);
      TestCase.assertEquals(testMessage, result1);

      // Lookup JNDI Binding Test EJB at Location 2, and invoke
      Object location2 = context.lookup(RemoteJndiBindingTest.JNDI_BINDING_2);
      TestCase.assertNotNull("Expected remote binding in JNDI at " + RemoteJndiBindingTest.JNDI_BINDING_2
            + " was not found", location2);
      TestCase.assertTrue(location2 instanceof BindingTest);
      BindingTest test2 = (BindingTest) location2;
      String result2 = test2.echo(testMessage);
      TestCase.assertEquals(testMessage, result2);

      // Lookup JNDI Binding Test EJB at Location 3, and invoke
      Object location3 = context.lookup(RemoteJndiBindingTest.JNDI_BINDING_DECLARED_BY_BUSINESS_INTERFACE);
      TestCase.assertNotNull("Expected remote binding in JNDI at "
            + RemoteJndiBindingTest.JNDI_BINDING_DECLARED_BY_BUSINESS_INTERFACE + " was not found", location2);
      TestCase.assertTrue(location3 instanceof BindingTest);
      BindingTest test3 = (BindingTest) location3;
      String result3 = test3.echo(testMessage);
      TestCase.assertEquals(testMessage, result3);

      // Unbind and test
      this.unbindAndTest(context, this.sessionContainer);
   }
   
   /**
    * Ensures that bindings at N Remote Locations, each with separate
    * clientBindUrls, have the proper URL for the InvokerLocator
    * 
    * EJBTHREE-1515
    */
   @Test
   public void testExplicitClientBindUrls() throws Throwable
   {
      // Create and bind the bean into JNDI
      this.sessionContainer = Utils.createSlsb(ClientBindUrlTestBean.class);
      Ejb3RegistrarLocator.locateRegistrar().bind(this.sessionContainer.getName(), this.sessionContainer);

      // Get a JNDI Context
      Context context = new InitialContext(); // Props from jndi.properties on Client CP

      /*
       * Test Location 1
       */
      
      // Lookup
      Object obj1 = context.lookup(ClientBindUrlBindingTest.JNDI_BINDING_1);
      
      // Get the underlying URL
      SessionSpecRemoteProxyInvocationHandler handler1 = (SessionSpecRemoteProxyInvocationHandler) Proxy
            .getInvocationHandler(obj1);
      String url1 = handler1.getUrl();
      
      // Test
      TestCase.assertEquals(ClientBindUrlBindingTest.CLIENT_BIND_URL_1, url1);
      
      /*
       * Test Location 2
       */
      
      // Lookup
      Object obj2 = context.lookup(ClientBindUrlBindingTest.JNDI_BINDING_2);
      
      // Get the underlying URL
      SessionSpecRemoteProxyInvocationHandler handler2 = (SessionSpecRemoteProxyInvocationHandler) Proxy
            .getInvocationHandler(obj2);
      String url2 = handler2.getUrl();
      
      // Test
      TestCase.assertEquals(ClientBindUrlBindingTest.CLIENT_BIND_URL_2, url2);
      
      /*
       * Cleanup
       */
      
      // Unbind and test
      this.unbindAndTest(context, this.sessionContainer);
   }

   /**
    * Ensures that an alternate local binding is made and may 
    * be invoked upon, and that all associated bindings are cleansed 
    * upon undeploy
    * 
    * EJBTHREE-1515
    * 
    * @throws Exception
    */
   @Test
   public void testAlternateLocalJndiBinding() throws Throwable
   {
      // Create and bind the bean into JNDI
      this.sessionContainer = Utils.createSlsb(JndiBindingTestBean.class);
      Ejb3RegistrarLocator.locateRegistrar().bind(this.sessionContainer.getName(), this.sessionContainer);
      
      // Initialize
      String testMessage = "Find me at Overridden Local JNDI Location";

      // Get a JNDI Context
      Context context = new InitialContext(); // Props from jndi.properties on Client CP

      // Lookup JNDI Binding Test EJB at alternate local location, and invoke
      Object localLocation = context.lookup(LocalJndiBindingTest.JNDI_BINDING);
      TestCase.assertNotNull("Expected local binding in JNDI at " + localLocation + " was not found", localLocation);
      TestCase.assertTrue(localLocation instanceof BindingTest);
      BindingTest test = (BindingTest) localLocation;
      String result = test.echo(testMessage);
      TestCase.assertEquals(testMessage, result);
      
      // Unbind and test
      this.unbindAndTest(context, this.sessionContainer);
   }

   /**
    * Test that the 2.x SLSB bean is unbound from the jndi, after the 
    * {@link JndiStatelessSessionRegistrar#unbindEjb(Context, org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData)}
    * is called. <br>
    * 
    * This test ensures that all the jndi related bindings for a 2.x SLSB are unbound when the unBindEjb is invoked. 
    * Before testing the unbindEjb, this test does check for the existence of a couple of (but not all) bean related 
    * bindings in the JNDI.
    *      
    * 
    * @throws Throwable
    */
   @Test
   public void testUnbindEjbFor2xOnlySLSB() throws Throwable
   {
      // create the bean
      this.sessionContainer = Utils.createSlsb(MyStateless2xOnlyBean.class);

      // bind the bean to the jndi
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // We are testing unbindEjb (and not bindEjb), so checking the existence of only a few 
      // bean related bindings in the JNDI should be enough. Need not check all possible bindings

      // lookup the bean home to ensure its been bound to the jndi
      Context ctx = new InitialContext();
      Object home = ctx.lookup(getHomeJndiName(sessionContainer));
      logger.info("Lookup of remote home of SLSB returned " + home);

      assertNotNull("Failure - Lookup of remote home of SLSB returned null", home);

      assertTrue("Failure - Remote Home of SLSB, returned from lookup, is NOT instance of " + MyStatelessRemoteHome.class, (home instanceof MyStatelessRemoteHome));

      // lookup the bean local home to ensure its been bound to the jndi
      Object localHome = ctx.lookup(getLocalHomeJndiName(sessionContainer));
      logger.info("Lookup of local SLSB returned " + localHome);

      assertNotNull("Failure - Lookup of local home of SLSB, returned null", localHome);

      assertTrue("Failure - Local SLSB returned from lookup is NOT instance of " + MyStatelessLocalHome.class, (localHome instanceof MyStatelessLocalHome));

      unbindAndTest(ctx, sessionContainer);

      logger.debug(sessionContainer.getName() + " unbound successfully");
   }

   /**
    * Test that the 3.0 (no home/localhome defined on the bean) SLSB bean is unbound from the jndi, after the 
    * {@link JndiStatelessSessionRegistrar#unbindEjb(Context, org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData)}
    * is called. <br>
    * 
    * This test ensures that all the jndi related bindings for a 3.0 SLSB are unbound when the unBindEjb is invoked. 
    * Before testing the unbindEjb, this test does check for the existence of a couple of (but not all) bean related 
    * bindings in the JNDI.
    * 
    * @throws Throwable
    */
   @Test
   public void testUnbindEjbFor30OnlySLSB() throws Throwable
   {
      // create the bean
      this.sessionContainer = Utils.createSlsb(MyStateless30OnlyBean.class);

      // bind the bean to the jndi
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // We are testing unbindEjb (and not bindEjb), so checking the existence of only a few 
      // bean related bindings in the JNDI should be enough. Need not check all possible bindings

      // lookup the remote bean to ensure its been bound to the jndi
      Context ctx = new InitialContext();
      Object remote = ctx.lookup(getDefaultBusinessRemoteJndiName(sessionContainer));

      assertNotNull("Failure - Remote object of 3.0 SLSB is null", remote);

      assertTrue("Failure - Remote object of 3.0 SLSB is NOT an instance of " + MyStatelessRemote.class, (remote instanceof MyStatelessRemote));

      // lookup the local bean to ensure its been bound to the jndi
      Object local = ctx.lookup(getDefaultBusinessLocalJndiName(sessionContainer));

      assertNotNull("Failure - Local object of 3.0 SLSB is null", local);

      assertTrue("Failure - Local object of 3.0 SLSB is NOT an instance of " + MyStatelessLocal.class, (local instanceof MyStatelessLocal));

      unbindAndTest(ctx, sessionContainer);

      logger.debug(sessionContainer.getName() + " unbound successfully");

   }

   /**
    * Test that the 3.0 SLSB (with home/localhome/remote/local views) is unbound from the jndi, after the 
    * {@link JndiStatelessSessionRegistrar#unbindEjb(Context, org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData)}
    * is called. <br> 
    * 
    * This test ensures that all the jndi related bindings for a 3.0 SLSB are unbound when the unBindEjb is invoked. 
    * Before testing the unbindEjb, this test does check for the existence of a couple of (but not all) bean related 
    * bindings in the JNDI.
    * 
    * @throws Throwable
    */
   @Test
   public void testUnbindEjbForSLSB() throws Throwable
   {

      // create the bean
      this.sessionContainer = Utils.createSlsb(MyStatelessBean.class);

      // bind the bean to the jndi
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // We are testing unbindEjb (and not bindEjb), so checking the existence of only a few 
      // bean related bindings in the JNDI should be enough. Need not check all possible bindings

      // lookup the remote home to ensure its been bound to the jndi
      Context ctx = new InitialContext();
      Object home = ctx.lookup(getHomeJndiName(sessionContainer));

      assertNotNull("Failure - Remote home of 3.0 SLSB is null", home);

      assertTrue("Failure - Remote home of 3.0 SLSB is NOT an instance of " + MyStatelessRemoteHome.class, (home instanceof MyStatelessRemoteHome));

      Object localHome = ctx.lookup(getLocalHomeJndiName(sessionContainer));

      assertNotNull("Failure - Local home of 3.0 SLSB is null", localHome);

      assertTrue("Failure - Remote home of 3.0 SLSB is NOT an instance of " + MyStatelessLocalHome.class, (localHome instanceof MyStatelessLocalHome));

      unbindAndTest(ctx, sessionContainer);

      logger.debug(sessionContainer.getName() + " unbound successfully");

   }

   /**
    * Test that the {@link JndiStatelessSessionRegistrar#unbindEjb(Context, org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData)}
    * unbinds <i>only the object(s) related to the bean.</i> <br>  
    * 
    * Note: This does not test that all bean related jndi objects are unbound from the JNDI. It leaves this, to the other 
    * tests.
    * 
    * @throws Throwable
    */
   @Test
   public void testUnbindEjbForNonEjbSpecificJNDIObjects() throws Throwable
   {

      // deploy the bean
      this.sessionContainer = Utils.createSlsb(MyStatelessBean.class);

      // We are testing unbindEjb (and not bindEjb), so checking the existence of only a few 
      // bean related bindings in the JNDI should be enough. Need not check all possible bindings

      // bind in jndi
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      Context ctx = new InitialContext();
      Object remote = ctx.lookup(getDefaultBusinessRemoteJndiName(sessionContainer));

      assertNotNull("Failure - Lookup of remote bean returned null", remote);

      assertTrue("Failure - Remote bean returned from lookup is NOT instance of " + MyStatelessRemote.class, (remote instanceof MyStatelessRemote));

      // Now bind to the JNDI, some object
      ctx.bind("TestJndiName", "TestJndiObject");
      // just a check to ensure the object was indeed bound
      assertNotNull("Failure - could not bind object to JNDI", ctx.lookup("TestJndiName"));

      unbindAndTest(ctx, sessionContainer);

      logger.debug(sessionContainer.getName() + " unbound successfully");

   }

   /**
    * Test that the  3.0 SFSB (with home/localhome/remote/local views) is unbound from the jndi, after the 
    * {@link JndiStatelessSessionRegistrar#unbindEjb(Context, org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData)}
    * is called.
    * 
    * This test ensures that all the jndi related bindings for a 3.0 SFSB are unbound when the unBindEjb is invoked. 
    * Before testing the unbindEjb, this test does check for the existence of a couple of (but not all) bean related 
    * bindings in the JNDI.
    * 
    * @throws Throwable
    */
   @Test
   public void testUnbindEjbForSFSB() throws Throwable
   {
      // create the SFSB container
      this.sessionContainer = Utils.createSfsb(MyStatefulBean.class);

      // bind the SFSB bean
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // We are testing unbindEjb (and not bindEjb), so checking the existence of only a few 
      // bean related bindings in the JNDI should be enough. Need not check all possible bindings

      Context ctx = new InitialContext();
      // lookup remote sfsb
      Object remote = ctx.lookup(getDefaultBusinessRemoteJndiName(sessionContainer));
      logger.info("Lookup of remote SFSB returned " + remote);

      assertNotNull("Failure - Lookup of remote SFSB returned null", remote);

      assertTrue("Failure - Remote SFSB returned from lookup is NOT instance of " + MyStatefulRemoteBusiness.class, (remote instanceof MyStatefulRemoteBusiness));

      // lookup local
      Object local = (Object) ctx.lookup(getDefaultBusinessLocalJndiName(sessionContainer));
      logger.info("Lookup of local SFSB returned " + local);

      assertNotNull("Failure - Lookup of local SFSB returned null", local);

      assertTrue("Failure - Local SFSB returned from lookup is NOT instance of " + MyStatefulLocalBusiness.class, (local instanceof MyStatefulLocalBusiness));

      unbindAndTest(ctx, sessionContainer);

      logger.debug(sessionContainer.getName() + " unbound successfully");

   }

   /**
    * Test that the  3.0 SFSB (NO home/localhome views) is unbound from the jndi, after the 
    * {@link JndiStatelessSessionRegistrar#unbindEjb(Context, org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData)}
    * is called.
    * 
    * This test ensures that all the jndi related bindings for a 3.0 SFSB are unbound when the unBindEjb is invoked. 
    * Before testing the unbindEjb, this test does check for the existence of a couple of (but not all) bean related 
    * bindings in the JNDI.
    * 
    * @throws Throwable
    */
   @Test
   public void testUnbindEjbFor30OnlySFSB() throws Throwable
   {

      // create the SFSB container
      this.sessionContainer = Utils.createSfsb(MyStateful30OnlyBean.class);

      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // We are testing unbindEjb (and not bindEjb), so checking the existence of only a few 
      // bean related bindings in the JNDI should be enough. Need not check all possible bindings

      Context ctx = new InitialContext();
      // lookup the remote 
      Object remote = ctx.lookup(getDefaultBusinessRemoteJndiName(sessionContainer));

      assertNotNull("Failure - Lookup of remote for SFSB returned null", remote);

      assertTrue("Failure - Remote SFSB is NOT instance of " + MyStatefulRemoteBusiness.class, (remote instanceof MyStatefulRemoteBusiness));

      // lookup the local 
      Object local = ctx.lookup(getDefaultBusinessLocalJndiName(sessionContainer));

      assertNotNull("Failure - Lookup of local for SFSB returned null", local);

      assertTrue("Failure - Local SFSB is NOT instance of " + MyStatefulLocalBusiness.class, (local instanceof MyStatefulLocalBusiness));

      unbindAndTest(ctx, sessionContainer);

      logger.debug(sessionContainer.getName() + " unbound successfully");

   }

   /**
    * Test that the  2.x SFSB is unbound from the jndi, after the 
    * {@link JndiStatelessSessionRegistrar#unbindEjb(Context, org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData)}
    * is called.
    * 
    * This test ensures that all the jndi related bindings for a 2.x SFSB are unbound when the unBindEjb is invoked. 
    * Before testing the unbindEjb, this test does check for the existence of a couple of (but not all) bean related 
    * bindings in the JNDI.
    * 
    * @throws Throwable
    */
   @Test
   public void testUnbindEjbFor2xSFSB() throws Throwable
   {

      // create the SFSB container
      this.sessionContainer = Utils.createSfsb(MyStateful2xOnlyBean.class);

      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      Context ctx = new InitialContext();
      // lookup the remote home
      Object home = ctx.lookup(getHomeJndiName(sessionContainer));

      assertNotNull("Failure - Lookup of remote home for SFSB returned null", home);

      assertTrue("Failure - Remote home lookup of SFSB is NOT instance of " + MyStatefulRemoteHome.class, (home instanceof MyStatefulRemoteHome));

      // lookup the local home
      Object localHome = ctx.lookup(getLocalHomeJndiName(sessionContainer));

      assertNotNull("Failure - Lookup of local home for SFSB returned null", localHome);

      assertTrue("Failure - Local home lookup of SFSB is NOT instance of " + MyStatefulLocalHome.class, (localHome instanceof MyStatefulLocalHome));

      unbindAndTest(ctx, sessionContainer);

      logger.debug(sessionContainer.getName() + " unbound successfully");

   }

   /**
    * Test that the {@link RemoteBinding} is honoured for SLSB<br/>
    * 
    * Ensure that the remote is bound to the jndi name specified by the @RemoteBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * @throws Throwable
    */
   @Test
   public void testRemoteBindingForSlsb() throws Throwable
   {
      // create the bean
      this.sessionContainer = Utils.createSlsb(MyStatelessBeanWithBindings.class);

      // bind it to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      List<RemoteBindingMetaData> remoteBindingsMetadata = sessionContainer.getMetaData().getRemoteBindings();

      assertNotNull("No RemoteBindingMetadata available", remoteBindingsMetadata);
      // make sure that the remotebinding metadata list has 1 @RemoteBinding information
      assertEquals("RemoteBindingMetadata does not have any RemoteBinding information available", remoteBindingsMetadata.size(), 1);

      // Ensure that the RemoteBindingMetaData is created properly with the specified jndiBinding name.
      Iterator<RemoteBindingMetaData> remoteBindingsMetadataIterator = remoteBindingsMetadata.iterator();
      RemoteBindingMetaData remoteBindingMetadata = remoteBindingsMetadataIterator.next();

      assertEquals("RemoteBinding JNDI name does not match " + MyStatelessBeanWithBindings.REMOTE_JNDI_NAME, MyStatelessBeanWithBindings.REMOTE_JNDI_NAME, remoteBindingMetadata
            .getJndiName());

      // Now ensure that the RemoteBindingMetaData is used for binding the 
      // remote interface of the bean.
      Context ctx = new InitialContext();
      String remoteJndiName = remoteBindingMetadata.getJndiName();
      logger.info("Remote binding jndi = " + remoteJndiName);
      Object remoteBean = ctx.lookup(remoteJndiName);
      logger.info("Object is : " + remoteBean);
      assertNotNull("Remote bean returned from JNDI lookup is null", remoteBean);
      assertTrue("Remote bean returned from JNDI lookup is NOT an instance of " + MyStatelessRemote.class, (remoteBean instanceof MyStatelessRemote));

      // Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);
   }

   /**
    * Test that the {@link LocalBinding} is honoured for SLSB<br/>
    * 
    * Ensure that the local is bound to the jndi name specified by the @LocalBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * @throws Throwable
    */
   @Test
   public void testLocalBindingForSlsb() throws Throwable
   {
      //create the bean
      this.sessionContainer = Utils.createSlsb(MyStatelessBeanWithBindings.class);

      //bind the bean
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // Ensure that the local jndi name is set to the @LocalBinding value
      String localJndiName = sessionContainer.getMetaData().getLocalJndiName();

      assertNotNull("Local jndi name is null", localJndiName);
      assertEquals("Local jndi name does not match the jndiBinding value specified in @LocalBinding", MyStatelessBeanWithBindings.LOCAL_JNDI_NAME, localJndiName);

      // Lookup using the local jndi name and check that the Local object is bound in the JNDI
      Context ctx = new InitialContext();
      Object local = ctx.lookup(localJndiName);

      assertNotNull("Local object bound to JNDI is null", local);
      assertTrue("Object bound to local JNDI name is NOT an instance of " + MyStatelessLocal.class, (local instanceof MyStatelessLocal));

      // Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);

   }

   /**
    * Test that the {@link LocalHomeBinding} is honoured for SLSB<br/>
    * 
    * Ensure that the localhome object is bound to the jndi name specified by the @LocalHomeBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * @throws Throwable
    */
   @Test
   public void testLocalHomeBindingForSlsb() throws Throwable
   {
      // create bean
      this.sessionContainer = Utils.createSlsb(MyStatelessBeanWithBindings.class);

      // bind the bean
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // Ensure that the localhome jndi name is set to the @LocalHomeBinding value
      String localHomeJndiName = sessionContainer.getMetaData().getLocalHomeJndiName();

      assertNotNull("Local home jndi name is null", localHomeJndiName);
      assertEquals("Local home jndi name does not match the jndiBinding value specified in @LocalHomeBinding", MyStatelessBeanWithBindings.LOCAL_HOME_JNDI_NAME, localHomeJndiName);

      // lookup using the localhome jndi name and ensure the localhome is bound to this name
      Context ctx = new InitialContext();
      Object localHome = ctx.lookup(localHomeJndiName);

      assertNotNull("Local home is null", localHome);
      assertTrue("Local home is not an instance of " + MyStatelessLocalHome.class, (localHome instanceof MyStatelessLocalHome));

      //  Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);

   }

   /**
    * Test that the {@link RemoteHomeBinding} is honoured for SLSB<br/>
    * 
    * Ensure that the remotehome object is bound to the jndi name specified by the @RemoteHomeBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * @throws Throwable
    */
   @Test
   public void testRemoteHomeBindingForSlsb() throws Throwable
   {
      // create the bean
      this.sessionContainer = Utils.createSlsb(MyStatelessBeanWithBindings.class);

      // bind the bean
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      // check the remotehome jndi name
      String remoteHomeJndiName = sessionContainer.getMetaData().getHomeJndiName();

      assertNotNull("Remote home jndi name is null", remoteHomeJndiName);
      assertEquals("Remote home jndi name does not match the jndibinding value specified in @RemoteHomeBinding", MyStatelessBeanWithBindings.REMOTE_HOME_JNDI_NAME,
            remoteHomeJndiName);

      // lookup using the remote home jndi name and ensure the remotehome is bound to this name
      Context ctx = new InitialContext();
      Object remoteHome = ctx.lookup(remoteHomeJndiName);

      assertNotNull("Remote home is null", remoteHome);
      assertTrue("Remote home is not an instance of " + MyStatelessRemoteHome.class, (remoteHome instanceof MyStatelessRemoteHome));

      // Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);
   }

   /**
    * Test that the {@link RemoteBinding} is honoured for SFSB<br/>
    * 
    * Ensure that the remote object is bound to the jndi name specified by the @RemoteBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * @throws Throwable
    */
   @Test
   public void testRemoteBindingForSfsb() throws Throwable
   {
      //create bean
      this.sessionContainer = Utils.createSfsb(MyStatefulBeanWithBindings.class);

      // bind the bean
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      List<RemoteBindingMetaData> remoteBindingsMetadata = this.sessionContainer.getMetaData().getRemoteBindings();

      assertEquals("Expected 1 @RemoteBinding metadata for Sfsb", remoteBindingsMetadata.size(), 1);

      RemoteBindingMetaData remoteBindingMetadata = remoteBindingsMetadata.get(0);
      String remoteJndiName = remoteBindingMetadata.getJndiName();

      assertNotNull("Remote jndi name is null for Sfsb", remoteJndiName);
      assertEquals("Remote jndi name does not match the jndiBinding value specified in @RemoteBinding", MyStatefulBeanWithBindings.REMOTE_JNDI_NAME, remoteJndiName);

      // lookup and check the object
      Context ctx = new InitialContext();
      Object remote = ctx.lookup(remoteJndiName);

      assertNotNull("Remote object bound to jndi is null", remote);
      assertTrue("Remote object bound to jndi is not an instance of " + MyStatefulRemoteBusiness.class, (remote instanceof MyStatefulRemoteBusiness));

      // Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);

   }

   /**
    * Test that the {@link LocalBinding} is honoured for SFSB<br/>
    * 
    * Ensure that the local object is bound to the jndi name specified by the @LocalBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * @throws Throwable
    */
   @Test
   public void testLocalBindingForSfsb() throws Throwable
   {
      // create the bean
      this.sessionContainer = Utils.createSfsb(MyStatefulBeanWithBindings.class);

      // bind to jndi
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      String localJndiName = this.sessionContainer.getMetaData().getLocalJndiName();

      assertNotNull("Local jndi name is null", localJndiName);
      assertEquals("Local jndi name does not match the jndiBindingValue specified in @LocalBinding for Sfsb", MyStatefulBeanWithBindings.LOCAL_JNDI_NAME, localJndiName);

      // lookup and check the bound object
      Context ctx = new InitialContext();
      Object local = ctx.lookup(localJndiName);

      assertNotNull("Local object bound to jndi is null", local);
      assertTrue("Local object bound to jndi is not an instance of " + MyStatefulLocalBusiness.class, (local instanceof MyStatefulLocalBusiness));

      //  Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);

   }

   /**
    * Test that the {@link LocalHomeBinding} is honoured for SFSB<br/>
    * 
    * Ensure that the localhome object is bound to the jndi name specified by the @LocalHomeBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * @throws Throwable
    */
   @Test
   public void testLocalHomeBindingForSfsb() throws Throwable
   {
      // create the bean
      this.sessionContainer = Utils.createSfsb(MyStatefulBeanWithBindings.class);

      // bind the bean
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      String localHomeJndiName = this.sessionContainer.getMetaData().getLocalHomeJndiName();

      assertNotNull("Local home jndi name is null", localHomeJndiName);
      assertEquals("Local home jndi name does not match the jndiBinding value specified in @LocalHomeBinding", MyStatefulBeanWithBindings.LOCAL_HOME_JNDI_NAME, localHomeJndiName);

      // lookup and check the object bound
      Context ctx = new InitialContext();
      Object localHome = ctx.lookup(localHomeJndiName);

      assertNotNull("Local home bound to jndi is null", localHome);
      assertTrue("Local home bound to jndi is not an instance of " + MyStatefulLocalHome.class, (localHome instanceof MyStatefulLocalHome));

      // Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);

   }

   /**
    * Test that the {@link RemoteHomeBinding} is honoured for SFSB<br/>
    * 
    * Ensure that the remotehome object is bound to the jndi name specified by the @RemoteHomeBinding
    * annotation on the bean. Also ensure that the jndi objects are unbound when the bean
    * is undeployed
    * 
    * 
    * @throws Throwable
    */
   @Test
   public void testRemoteHomeBindingForSfsb() throws Throwable
   {
      // create the bean
      this.sessionContainer = Utils.createSfsb(MyStatefulBeanWithBindings.class);

      // bind the bean
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);

      String remoteHomeJndiName = this.sessionContainer.getMetaData().getHomeJndiName();

      assertNotNull("Remote home jndi name is null", remoteHomeJndiName);
      assertEquals("Remote home jndi name does not match the jndiBinding value specified in @RemoteHomeBinding", MyStatefulBeanWithBindings.REMOTE_HOME_JNDI_NAME,
            remoteHomeJndiName);

      // lookup and check the object bound
      Context ctx = new InitialContext();
      Object remoteHome = ctx.lookup(remoteHomeJndiName);

      assertNotNull("Remote home bound to jndi is null", remoteHome);
      assertTrue("Remote home bound to jndi is not an instance of " + MyStatefulRemoteHome.class, (remoteHome instanceof MyStatefulRemoteHome));

      // Now its time to undeploy the bean and ensure the bindings are also removed
      unbindAndTest(ctx, sessionContainer);
   }

   /**
    * Returns the default business remote jndi name for the <code>sessionContainer</code>
    * 
    * @param sessionContainer
    * @return
    */
   private String getDefaultBusinessRemoteJndiName(SessionContainer sessionContainer)
   {
      JBossSessionBeanMetaData metadata = sessionContainer.getMetaData();
      return metadata.getJndiName();
   }

   /**
    * Returns the default business local jndi name for the <code>sessionContainer</code>
    * 
    * @param sessionContainer
    * @return
    */
   private String getDefaultBusinessLocalJndiName(SessionContainer sessionContainer)
   {
      JBossSessionBeanMetaData metadata = sessionContainer.getMetaData();
      return metadata.getLocalJndiName();
   }

   /**
    * Returns the local-home jndi name for the <code>sessionContainer</code>
    * 
    * @param sessionContainer
    * @return
    */
   private String getLocalHomeJndiName(SessionContainer sessionContainer)
   {
      JBossSessionBeanMetaData metadata = sessionContainer.getMetaData();
      return metadata.getLocalHomeJndiName();
   }

   /**
    * Returns the remote-home jndi name for the <code>sessionContainer</code>
    * 
    * @param sessionContainer
    * @return
    */
   private String getHomeJndiName(SessionContainer sessionContainer)
   {
      JBossSessionBeanMetaData metadata = sessionContainer.getMetaData();
      return metadata.getHomeJndiName();
   }

   /**
    * Returns all the jndi-names (1 - Default business remote jndi name
    * 2 - Default busines local jndi name
    * 3 - Local home jndi name
    * 4 - Remote home jndi name
    * 5 - Interface specific business remote jndi names
    * 6 - Interface specific business local jndi names
    * 7 - @RemoteBinding.jndiBinding entries
    * ) 
    * for different objects associated with the <code>sessionContainer</code>
    * 
    * @param sessionContainer
    * @return
    */
   private Set<String> getAllAssociatedJndiNames(SessionContainer sessionContainer)
   {
      JBossSessionBeanMetaData metadata = sessionContainer.getMetaData();
      Set<String> jndiNames = new HashSet<String>();

      // default business remote jndi name 
      jndiNames.add(metadata.getJndiName());
      // default business local jndi name
      jndiNames.add(metadata.getLocalJndiName());
      // local home jndi name  
      jndiNames.add(metadata.getLocalHomeJndiName());
      // remote home jndi name
      jndiNames.add(metadata.getHomeJndiName());
      
      // Remote Bindings
      List<RemoteBindingMetaData> remoteBindings = metadata.getRemoteBindings();
      if (remoteBindings != null)
      {
         for (RemoteBindingMetaData remoteBinding : remoteBindings)
         {
            String remoteBindingJndiName = remoteBinding.getJndiName();
            if (remoteBindingJndiName != null && remoteBindingJndiName.trim().length() > 0)
            {
               jndiNames.add(remoteBindingJndiName);
            }
         }
      }

      // Interface specific Business remote jndi names
      BusinessRemotesMetaData businessRemotesMetadata = metadata.getBusinessRemotes();
      if (businessRemotesMetadata != null)
      {
         for (String businessRemoteInterfaceName : businessRemotesMetadata)
         {
            String jndiName = JbossSessionBeanJndiNameResolver.resolveJndiName(metadata, businessRemoteInterfaceName);
            jndiNames.add(jndiName);
         }
      }

      // Interface specific Business local jndi names
      BusinessLocalsMetaData businessLocalsMetadata = metadata.getBusinessLocals();
      if (businessLocalsMetadata != null)
      {
         for (String businessLocalInterfaceName : businessLocalsMetadata)
         {
            String jndiName = JbossSessionBeanJndiNameResolver.resolveJndiName(metadata, businessLocalInterfaceName);
            jndiNames.add(jndiName);

         }
      }

      logger.debug("Number of jndi names associated with session container " + sessionContainer.getName() + " = " + jndiNames.size());

      return jndiNames;

   }

   /**
    * 
    * This method will unbind the <code>sessionContainer</code> from the JNDI and then test
    * that all appropriate bindings in the JNDI have been unbound by 
    * the {@link JndiSessionRegistrarBase#unbindEjb(Context, JBossSessionBeanMetaData)}.
    * 
    * @param ctx
    * @param sessionContainer
    * @throws Throwable
    */
   private void unbindAndTest(final Context ctx, final SessionContainer sessionContainer) throws Throwable
   {

      // Now unbind the bean from the jndi
      Ejb3RegistrarLocator.locateRegistrar().unbind(sessionContainer.getName());

      // Even if the bean was already bound, the metadata will still be intact, so doesn't matter 
      // if we are determining the jndi names from the container even after the bean is unbound
      Set<String> jndiNames = getAllAssociatedJndiNames(sessionContainer);
      // lookup all the related objects from the jndi to ensure that they have been unbound  
      for (String jndiName : jndiNames)
      {
         try
         {
            Object obj = ctx.lookup(jndiName);
            fail("Failure - lookup using " + jndiName + " returned object, even after the SLSB was un-bound " + obj);
         }
         catch (NameNotFoundException ne)
         {
            // NameNotFound indicates that the object was successfully unbound
            logger.debug("Object associated with " + jndiName + " has been successfully unbound");
            continue;

         }
         catch(Throwable t)
         {
            throw new RuntimeException("JNDI Name " + jndiName
                  + " was not unbound, and further an exception was raised upon lookup", t);
         }

      }

   }
}
