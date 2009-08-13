/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ejb3.test.proxy.impl.ejbthree1884.unit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.impl.factory.ProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.stateless.StatelessSessionRemoteProxyFactory;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiStatelessSessionRegistrar;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.SessionContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.service.MyServiceBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStateful2xOnlyBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStateful2xOnlyWithBindingsBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStateful30OnlyBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalOnlyBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStateless2xOnlyWithBindingsBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStateless30OnlyBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessBeanWithBindings;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * RemoteProxyFactoryJNDIBindingTestCase
 *
 * Test case to ensure that the remote proxy factory of 
 * a bean is bound in JNDI
 * 
 * @see https://jira.jboss.org/jira/browse/EJBTHREE-1884
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class RemoteProxyFactoryJNDIBindingTestCase extends SessionTestCaseBase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(RemoteProxyFactoryJNDIBindingTestCase.class);

   /**
    * The {@link SessionContainer} which is instantiated by each test. Depending
    * on the test, its either a {@link StatefulContainer} or a {@link StatelessContainer}
    */
   private SessionContainer sessionContainer;

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      SessionTestCaseBase.setUpBeforeClass();
      bootstrap.deploy(SessionTestCaseBase.class);
   }

   @AfterClass
   public static void afterClass() throws Throwable
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
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a EJB3 view SLSB
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForEJB3SLSBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStateless30OnlyBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);

   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a EJB2.x view SLSB
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForEJB2xSLSBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStateful2xOnlyBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);
   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a EJB3 view SFSB
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForEJB3SFSBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStateful30OnlyBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);

   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a EJB2.x view SFSB
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForEJB2xSFSBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStateful2xOnlyBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);
   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a Service bean
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForServiceBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyServiceBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);

   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a SLSB which exposes both EJB2.x and EJB3 view.
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForMixedSLSBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStatelessBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);
   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a bean which exposes EJB3 view with RemoteBindings
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForMixedBeanWithRemoteBindings() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStatelessBeanWithBindings.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);

   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a SFSB which exposes EJB2.x view with RemoteBindings
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForEJB2xSFSBWithRemoteBindings() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStateful2xOnlyWithBindingsBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);

   }

   /**
    * Tests that a remote proxyfactory is bound in the JNDI for 
    * a SLSB which exposes EJB2.x view with RemoteBindings
    * @throws Throwable
    */
   @Test
   public void testRemoteProxyFactoryForEJB2xSLSBWithRemoteBindings() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStateless2xOnlyWithBindingsBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      testRemoteProxyFactoryBinding(smd);

   }

   /**
    * Tests that a remote proxyfactory is NOT bound in the JNDI for 
    * a bean which only exposes a local view
    * @throws Throwable
    */
   @Test
   public void testAbsenceOfRemoteProxyFactoryLocalOnlyBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(MyStatefulLocalOnlyBean.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind to JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      try
      {
         testRemoteProxyFactoryBinding(smd);
         Assert.fail("Found a remote proxyfactory in JNDI for a local-only view bean");
      }
      catch (NameNotFoundException nnfe)
      {
         // expected, since the bean has only local view. So no remote proxyfactory
      }
      // Further lookup the local bean and invoke a method
      Context ctx = new InitialContext();
      String localJndiName = smd.getLocalJndiName();
      logger.debug("Looking up local bean at " + localJndiName);
      Object obj = ctx.lookup(localJndiName);

      Assert.assertNotNull("Lookup of local bean at " + localJndiName + " returned null", obj);
      Assert.assertTrue("Lookup at " + localJndiName + " returned unexpected object " + obj,
            obj instanceof MyStatefulLocalBusiness);

      MyStatefulLocalBusiness bean = (MyStatefulLocalBusiness) obj;
      int counter = bean.getNextCounter();
      logger.debug("Got counter " + counter);

      Assert.assertEquals("Unexpected counter value = " + counter, counter, 0);

   }

   /**
    * Helper method to construct the correct remote proxyfactory JNDI name
    * and lookup the remote proxyfactory with that jndi name
    * @param smd
    * @throws Exception
    */
   private void testRemoteProxyFactoryBinding(JBossSessionBeanMetaData smd) throws Exception
   {
      // A bit of a hack. These are actually internal classes of proxy-impl,
      // but since the proxyfactory jndi name generation is part of these
      // classes, we hence have to rely on them.
      JndiStatelessSessionRegistrar jndiRegistrar = new JndiStatelessSessionRegistrar(
            StatelessSessionRemoteProxyFactory.class.getName());
      String remoteProxyFactoryJNDIName = jndiRegistrar.getProxyFactoryRegistryKey(smd.getJndiName(), smd, false);

      Assert.assertNotNull("Could not get remote proxyfactory jndi name from jndi registrar",
            remoteProxyFactoryJNDIName);

      Context ctx = new InitialContext();
      logger.debug("Looking up remote proxy factory at " + remoteProxyFactoryJNDIName + " for EJB " + smd.getEjbName());
      Object remoteProxyFactory = ctx.lookup(remoteProxyFactoryJNDIName);

      Assert.assertNotNull("Null returned from JNDI for remote proxyfactory key " + remoteProxyFactoryJNDIName,
            remoteProxyFactory);
      Assert.assertTrue("Unexpected object type " + remoteProxyFactory.getClass() + " for JNDI name "
            + remoteProxyFactoryJNDIName, remoteProxyFactory instanceof ProxyFactory);

   }
}
