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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
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
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulLocalHome;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulRemoteBusiness;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulRemoteHome;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStateless2xOnlyBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStateless30OnlyBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessBean;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessLocal;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessLocalHome;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.common.ejb.slsb.MyStatelessRemoteHome;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JndiSessionRegistrarBaseTestCase
 * 
 * Tests for {@link JndiStatelessSessionRegistrar} 
 * 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class JndiSessionRegistrarBaseTestCase
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
   private static Logger logger = Logger.getLogger(JndiSessionRegistrarBaseTestCase.class);

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

      bootstrap.deploy(JndiSessionRegistrarBaseTestCase.class);

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
    * Returns the default business remote jndi name for the <code>sessionContainer</code>
    * 
    * @param sessionContainer
    * @return
    */
   private String getDefaultBusinessRemoteJndiName(SessionContainer sessionContainer)
   {
      JBossSessionBeanMetaData metadata = sessionContainer.getMetaData();
      return metadata.determineJndiName();
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
      return metadata.determineLocalJndiName();
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
      jndiNames.add(metadata.determineJndiName());
      // default business local jndi name
      jndiNames.add(metadata.determineLocalJndiName());
      // local home jndi name  
      jndiNames.add(metadata.getLocalHomeJndiName());
      // remote home jndi name
      jndiNames.add(metadata.getHomeJndiName());

      // Interface specific Business remote jndi names
      BusinessRemotesMetaData businessRemotesMetadata = metadata.getBusinessRemotes();
      if (businessRemotesMetadata != null)
      {
         for (String businessRemoteInterfaceName : businessRemotesMetadata)
         {
            jndiNames.add(metadata.determineResolvedJndiName(businessRemoteInterfaceName));
         }
      }

      // Interface specific Business local jndi names
      BusinessLocalsMetaData businessLocalsMetadata = metadata.getBusinessLocals();
      if (businessLocalsMetadata != null)
      {
         for (String businessLocalInterfaceName : businessLocalsMetadata)
         {
            jndiNames.add(metadata.determineResolvedJndiName(businessLocalInterfaceName));
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

      }

   }
}
