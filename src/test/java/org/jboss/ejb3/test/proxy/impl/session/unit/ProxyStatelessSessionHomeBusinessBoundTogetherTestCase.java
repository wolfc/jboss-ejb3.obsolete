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
package org.jboss.ejb3.test.proxy.impl.session.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessBeanWithHomesBoundWithBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessLocal;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessLocalHome;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemoteHome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ProxyStatelessSessionHomeBusinessBoundTogetherTestCase
 * 
 * Test Cases to ensure that an SLSB with Home and Business 
 * Views are bound together as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyStatelessSessionHomeBusinessBoundTogetherTestCase extends SessionTestCaseSupport
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

   /**
    * Test Local Home Binding is made alongside Local Business
    * Default Binding
    * 
    * @throws Exception
    */
   @Test
   public void testLocalHomeBoundAlongsideLocalBusinessDefault() throws Exception
   {
      this.checkBusinessAndHomeInterfacesBoundTogether(MyStatelessBeanWithHomesBoundWithBusiness.LOCAL_JNDI_NAME,
            MyStatelessLocal.class, MyStatelessLocalHome.class);
   }

   /**
    * Test Remote Home Binding is made alongside Remote Business
    * Default Binding
    * 
    * @throws Exception
    */
   @Test
   public void testRemoteHomeBoundAlongsideRemoteBusinessDefault() throws Exception
   {
      this.checkBusinessAndHomeInterfacesBoundTogether(MyStatelessBeanWithHomesBoundWithBusiness.REMOTE_JNDI_NAME,
            MyStatelessRemote.class, MyStatelessRemoteHome.class);
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
      // Create Bootstrap 
      SessionTestCaseBase.setUpBeforeClass();

      // Deploy MC Beans
      ProxyStatelessSessionHomeBusinessBoundTogetherTestCase.bootstrap.deploy(ProxyStatelessSessionTestCase.class);

      // Create a SLSB
      StatelessContainer container = Utils.createSlsb(MyStatelessBeanWithHomesBoundWithBusiness.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

      // Create JNDI Context
      ProxyStatelessSessionHomeBusinessBoundTogetherTestCase.context = new InitialContext(); // Props from CP jndi.properties

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
