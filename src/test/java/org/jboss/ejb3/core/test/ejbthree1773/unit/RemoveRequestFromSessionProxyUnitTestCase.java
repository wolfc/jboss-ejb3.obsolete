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
package org.jboss.ejb3.core.test.ejbthree1773.unit;

import javax.ejb.NoSuchEJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.service.ServiceBean;
import org.jboss.ejb3.core.test.stateful.StatefulBean;
import org.jboss.ejb3.core.test.stateful.StatefulLocalBusiness;
import org.jboss.ejb3.core.test.stateless.MyStatelessBean;
import org.jboss.ejb3.proxy.spi.intf.SessionProxy;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * RemoveRequestFromSessionProxyUnitTestCase
 * 
 * Test Cases to ensure requests from SessionProxy.removeTarget
 * are handled accordingly by the Containers
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class RemoveRequestFromSessionProxyUnitTestCase extends AbstractEJB3TestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * Bean Impl Classes
    */

   private static final Class<?> BEAN_IMPL_CLASS_SLSB = MyStatelessBean.class;

   private static final Class<?> BEAN_IMPL_CLASS_SFSB = StatefulBean.class;

   private static final Class<?> BEAN_IMPL_CLASS_SERVICE = ServiceBean.class;

   /*
    * Containers used
    */

   private static SessionContainer slsb;

   private static SessionContainer sfsb;

   private static SessionContainer service;

   /*
    * JNDI Context
    */

   private static Context namingContext;

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures SLSB SessionProxy.removeTarget requests are unsupported
    */
   @Test
   public void testRemoveOnSlsb() throws Throwable
   {
      this.assertUnsupportedRemoveRequest(BEAN_IMPL_CLASS_SLSB);
   }

   /**
    * Ensures @Service SessionProxy.removeTarget requests are unsupported
    */
   @Test
   public void testRemoveOnService() throws Throwable
   {
      this.assertUnsupportedRemoveRequest(BEAN_IMPL_CLASS_SERVICE);
   }

   /**
    * Ensures SFSB SessionProxy.removeTarget requests are honored
    */
   @Test
   public void testRemoveOnSfsb() throws Throwable
   {
      // Initialized
      boolean exceptionReceived = false;

      // Lookup
      SessionProxy proxy = this.lookupProxy(BEAN_IMPL_CLASS_SFSB);

      // Cast
      StatefulLocalBusiness statefulProxy = (StatefulLocalBusiness) proxy;

      // Invoke
      int firstCounter = statefulProxy.getNextCounter();
      int nextCounter = statefulProxy.getNextCounter();
      TestCase.assertTrue("SFSB Invocations not as expected", firstCounter == nextCounter - 1);

      // Request Removal
      proxy.removeTarget();

      // Ensure gone
      try
      {
         statefulProxy.getNextCounter();
      }
      catch (NoSuchEJBException nsejbe)
      {
         exceptionReceived = true;
      }

      // Test
      TestCase.assertTrue("SFSB should have been removed", exceptionReceived);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that the Container for the specified bean impl class does not support
    * removal requests from the proxy
    */
   private void assertUnsupportedRemoveRequest(Class<?> beanImplClass) throws Throwable
   {
      // Initialize
      boolean gotExpectedException = false;

      // Obtain Proxy
      SessionProxy proxy = this.lookupProxy(beanImplClass);

      // Request Removal
      try
      {
         proxy.removeTarget();
      }
      catch (UnsupportedOperationException uoe)
      {
         gotExpectedException = true;
      }

      // Test
      TestCase.assertTrue("Should not support removal requests", gotExpectedException);
   }

   /**
    * Ensures that the Container for the specified bean impl class does not support
    * removal requests from the proxy
    */
   private SessionProxy lookupProxy(Class<?> beanImplClass) throws Throwable
   {
      // Define JNDI Targets for Lookup
      String jndiName = beanImplClass.getSimpleName() + "/" + "local";

      // Obtain Proxy
      SessionProxy proxy = (SessionProxy) namingContext.lookup(jndiName);

      // Return
      return proxy;
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      // Get the Naming Context
      try
      {
         namingContext = new InitialContext();
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }

      // Deploy the test EJBs
      slsb = deploySessionEjb(BEAN_IMPL_CLASS_SLSB);
      sfsb = deploySessionEjb(BEAN_IMPL_CLASS_SFSB);
      service = deploySessionEjb(BEAN_IMPL_CLASS_SERVICE);

   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Undeploy the test EJBs
      undeployEjb(slsb);
      undeployEjb(sfsb);
      undeployEjb(service);

      AbstractEJB3TestCase.afterClass();
   }

}
