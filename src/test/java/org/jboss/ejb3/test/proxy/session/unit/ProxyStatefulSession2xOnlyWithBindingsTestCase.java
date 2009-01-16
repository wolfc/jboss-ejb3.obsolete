/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.session.unit;

import static org.junit.Assert.assertTrue;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.common.Utils;
import org.jboss.ejb3.test.proxy.common.container.StatefulContainer;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStateful2xOnlyWithBindingsBean;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulLocalHome;
import org.jboss.ejb3.test.proxy.common.ejb.sfsb.MyStatefulRemoteHome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyStatefulSession2xOnlyWithBindingsTestCase
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyStatefulSession2xOnlyWithBindingsTestCase extends SessionTestCaseSupport
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * JNDI Context
    */
   private static Context context;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Test
   public void testLocalHome() throws Exception
   {
      Object bean = this.getNamingContext().lookup(MyStateful2xOnlyWithBindingsBean.JNDI_BINDING_LOCAL_HOME);
      assertTrue(bean instanceof MyStatefulLocalHome);
   }

   @Test
   public void testRemoteHome() throws Exception
   {
      Object bean = this.getNamingContext().lookup(MyStateful2xOnlyWithBindingsBean.JNDI_BINDING_REMOTE_HOME);
      assertTrue(bean instanceof MyStatefulRemoteHome);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Context to be used for JNDI Operations 
    */
   @Override
   protected Context getNamingContext()
   {
      return context;
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
      ProxyStatefulSession2xOnlyWithBindingsTestCase.bootstrap.deploy(ProxyStatefulSessionTestCase.class);

      // Create a SFSB
      StatefulContainer container = Utils.createSfsb(MyStateful2xOnlyWithBindingsBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

      // Create JNDI Context
      context = new InitialContext(); // Props from CP jndi.properties

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
