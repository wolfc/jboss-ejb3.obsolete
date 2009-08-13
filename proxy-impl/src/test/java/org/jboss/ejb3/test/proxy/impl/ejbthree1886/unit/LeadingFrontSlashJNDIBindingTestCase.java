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
package org.jboss.ejb3.test.proxy.impl.ejbthree1886.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

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
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulRemoteBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessLocal;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.impl.ejbthree1886.ClusteredSFSBWithLeadingFrontSlashForJNDIBindings;
import org.jboss.ejb3.test.proxy.impl.ejbthree1886.ClusteredSLSBWithLeadingFrontSlashForJNDIBindings;
import org.jboss.ejb3.test.proxy.impl.ejbthree1886.ServiceBeanWithLeadingFrontSlashForJNDIBindings;
import org.jboss.ejb3.test.proxy.impl.ejbthree1886.StatefulBeanWithLeadingFrontSlashForJNDIBindings;
import org.jboss.ejb3.test.proxy.impl.ejbthree1886.StatelessBeanWithLeadingFrontSlashForJNDIBindings;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * LeadingFrontSlashJNDIBindingTestCase
 * 
 * Test case for https://jira.jboss.org/jira/browse/EJBTHREE-1886
 * 
 * Tests that a leading "/" for a (remote/local) binding of a bean
 * does NOT lead to a proxy factory being bound at a JNDI name
 * with adjacent "/" characters. The real issue is in jboss-naming
 * which does not handle the "/" character correctly. More discussion
 * here http://www.jboss.org/index.html?module=bb&op=viewtopic&t=159766
 * 
 * As a workaround, the fix in EJBTHREE-1886 ensures that if the proxyfactory
 * jndi binding name consists of adjacent "/" characters then it replaces
 * it with a single "/" character  
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class LeadingFrontSlashJNDIBindingTestCase extends SessionTestCaseBase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(LeadingFrontSlashJNDIBindingTestCase.class);

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
    * Tests that a SLSB is bound properly and can be invoked
    * 
    * @throws Throwable
    */
   @Test
   public void testSLSB() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(StatelessBeanWithLeadingFrontSlashForJNDIBindings.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind into JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      // test proxyfactory binding
      this.testRemoteProxyFactoryBinding(smd);
      // now lookup the bean invoke the methods
      this.testMyStatelessBeanInvocations(smd);
   }

   /**
    * Tests that a SFSB is bound properly and can be invoked
    * 
    * @throws Throwable
    */
   @Test
   public void testSFSB() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(StatefulBeanWithLeadingFrontSlashForJNDIBindings.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind into JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      // test proxyfactory binding
      this.testRemoteProxyFactoryBinding(smd);
      // now lookup the bean invoke the methods
      this.testMyStatefulBeanInvocations(smd);
   }

   /**
    * Tests that a Service bean is bound properly and can be invoked
    * 
    * @throws Throwable
    */
   @Test
   public void testServiceBean() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(ServiceBeanWithLeadingFrontSlashForJNDIBindings.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind into JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      // test proxyfactory binding
      this.testRemoteProxyFactoryBinding(smd);
      // now lookup the bean invoke the methods
      this.testMyStatelessBeanInvocations(smd);
   }

   /**
    * Tests that a clustered SLSB is bound properly and can be invoked
    * 
    * @throws Throwable
    */
   @Test
   public void testClusteredSLSB() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(ClusteredSLSBWithLeadingFrontSlashForJNDIBindings.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind into JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      // test proxyfactory binding
      this.testRemoteProxyFactoryBinding(smd);
      // now lookup the bean invoke the methods
      this.testMyStatelessBeanInvocations(smd);
   }

   /**
    * Tests that a clustered SFSB is bound properly and can be invoked
    * 
    * @throws Throwable
    */
   @Test
   public void testClusteredSFSB() throws Throwable
   {
      this.sessionContainer = Utils.createSlsb(ClusteredSFSBWithLeadingFrontSlashForJNDIBindings.class);
      JBossSessionBeanMetaData smd = this.sessionContainer.getMetaData();
      // bind into JNDI
      Ejb3RegistrarLocator.locateRegistrar().bind(sessionContainer.getName(), sessionContainer);
      // test proxyfactory binding
      this.testRemoteProxyFactoryBinding(smd);
      // now lookup the bean invoke the methods
      this.testMyStatefulBeanInvocations(smd);
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

   /**
    * Utility method which invokes the local and remote business interfaces 
    * {@link MyStatelessLocal} and {@link MyStatelessRemote}
    * 
    * @param smd
    * @throws Exception
    */
   private void testMyStatelessBeanInvocations(JBossSessionBeanMetaData smd) throws Exception
   {
      logger.debug("Testing MyStateless");
      // lookup local and invoke on it
      String localJNDIName = smd.getLocalJndiName();
      Context ctx = new InitialContext();
      logger.debug("Looking up bean at local jndi name " + localJNDIName);
      Object obj = ctx.lookup(localJNDIName);

      Assert.assertNotNull("Local jndi name " + localJNDIName + " lookup returned null", obj);
      Assert.assertTrue("Local jndi name lookup at " + localJNDIName + " returned unexpected object " + obj,
            obj instanceof MyStatelessLocal);

      String name = "ejbthree1886";
      String expectedMessage = "Hi " + name;
      MyStatelessLocal localBean = (MyStatelessLocal) obj;

      String message = localBean.sayHi(name);
      Assert.assertEquals("Unexpected greeting message: " + message + " from local stateless bean", message,
            expectedMessage);

      // now lookup remote and invoke on it
      String remoteJNDIName = smd.getJndiName();

      logger.debug("Looking up bean at remote jndi name " + remoteJNDIName);
      Object remote = ctx.lookup(remoteJNDIName);

      Assert.assertNotNull("Remote jndi name " + remoteJNDIName + " lookup returned null", remote);
      Assert.assertTrue("Remote jndi name lookup at " + remoteJNDIName + " returned unexpected object " + remote,
            remote instanceof MyStatelessRemote);

      MyStatelessRemote remoteBean = (MyStatelessRemote) remote;

      String remoteMessage = remoteBean.sayHi(name);
      Assert.assertEquals("Unexpected greeting message: " + message + " from remote stateless bean", remoteMessage,
            expectedMessage);

   }

   /**
    * Utility method which invokes the local and remote business interfaces 
    * {@link MyStatefulLocalBusiness} and {@link MyStatefulRemoteBusiness}
    * 
    * @param smd
    * @throws Exception
    */
   private void testMyStatefulBeanInvocations(JBossSessionBeanMetaData smd) throws Exception
   {
      logger.debug("Testing MyStateful");
      // lookup local and invoke on it
      String localJNDIName = smd.getLocalJndiName();
      Context ctx = new InitialContext();
      logger.debug("Looking up bean at local jndi name " + localJNDIName);
      Object obj = ctx.lookup(localJNDIName);

      Assert.assertNotNull("Local jndi name " + localJNDIName + " lookup returned null", obj);
      Assert.assertTrue("Local jndi name lookup at " + localJNDIName + " returned unexpected object " + obj,
            obj instanceof MyStatefulLocalBusiness);

      MyStatefulLocalBusiness localBean = (MyStatefulLocalBusiness) obj;

      int count = localBean.getNextCounter();
      Assert.assertEquals("Unexpected counter value: " + count + " from local stateful bean", count, 0);

      // now lookup remote and invoke on it
      String remoteJNDIName = smd.getJndiName();

      logger.debug("Looking up bean at remote jndi name " + remoteJNDIName);
      Object remote = ctx.lookup(remoteJNDIName);

      Assert.assertNotNull("Remote jndi name " + remoteJNDIName + " lookup returned null", remote);
      Assert.assertTrue("Remote jndi name lookup at " + remoteJNDIName + " returned unexpected object " + remote,
            remote instanceof MyStatefulRemoteBusiness);

      MyStatefulRemoteBusiness remoteBean = (MyStatefulRemoteBusiness) remote;

      int countFromRemoteBean = remoteBean.getNextCounter();
      Assert.assertEquals("Unexpected count value: " + countFromRemoteBean + " from remote stateful bean",
            countFromRemoteBean, 0);

   }
}
