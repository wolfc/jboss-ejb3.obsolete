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
package org.jboss.ejb3.test.proxy.impl.ejbthree1529.unit;

import java.lang.reflect.Proxy;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.impl.handler.session.SessionRemoteProxyInvocationHandler;
import org.jboss.ejb3.proxy.impl.remoting.ProxyRemotingUtils;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.SessionContainer;
import org.jboss.ejb3.test.proxy.impl.ejbthree1529.ExplicitInvokerNameBean;
import org.jboss.ejb3.test.proxy.impl.ejbthree1529.ExplicitInvokerNameRemoteBusiness;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * RemoteBindingInvokerNameTestCase
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class RemoteBindingInvokerNameTestCase extends SessionTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(RemoteBindingInvokerNameTestCase.class);

   protected static Context context = null;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Tests that a bean defined with an explicit @RemoteBinding.invokerName
    * is able to be reached via its clientBindUrl (InvokerLocator)
    */
   @Test
   public void testExplicitInvokerName() throws Throwable
   {
      // Initialize
      String jndiName = ExplicitInvokerNameRemoteBusiness.JNDI_NAME_EXPLICIT_INVOKER_NAME;

      // Get the bean
      ExplicitInvokerNameRemoteBusiness bean = (ExplicitInvokerNameRemoteBusiness) context.lookup(jndiName);

      /*
       * Break into Proxy internals to assure we've got the right clientBindUrl
       * associated w/ the invoker name
       */

      // Get the expected bind URL
      String invokerName = ExplicitInvokerNameRemoteBusiness.MC_NAME_INVOKER;
      String expectedClientBindUrl = ProxyRemotingUtils.getClientBinding(invokerName);

      // Get the clientBindUrl from the Proxy itself
      Class<?> proxyClass = bean.getClass();
      assert Proxy.isProxyClass(proxyClass) : "Stub from JNDI was not a " + Proxy.class.getName();
      SessionRemoteProxyInvocationHandler handler = (SessionRemoteProxyInvocationHandler) Proxy
            .getInvocationHandler(bean);
      String clientBindUrl = handler.getUrl();

      // Ensure the expected value is what we've got in the proxy
      TestCase.assertEquals("clientBindUrls are not equal", expectedClientBindUrl, clientBindUrl);

      // Invoke
      String returnValue = bean.invoke();

      // Test return value
      TestCase.assertEquals("Didn't get expected return value", ExplicitInvokerNameRemoteBusiness.RETURN_VALUE,
            returnValue);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   /**
    * Perform setup before any tests
    * 
    * @throws Throwable
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Throwable
   {
      // Create Bootstrap and Deploy
      SessionTestCaseBase.setUpBeforeClass();

      // Deploy MC Beans
      bootstrap.deploy(SessionTestCaseBase.class);
      bootstrap.deploy(RemoteBindingInvokerNameTestCase.class);

      // Create container
      SessionContainer container = Utils.createSlsb(ExplicitInvokerNameBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

      // Set Naming COntext
      context = new InitialContext(); // Props from jndi.properties
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }

}
