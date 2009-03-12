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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.naming.InitialContext;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.ejb3.test.proxy.impl.common.Utils;
import org.jboss.ejb3.test.proxy.impl.common.container.StatelessContainer;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemote;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemoteHome;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemoteOnlyBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ProxyStatelessSessionRemoteOnlyTestCase extends SessionTestCaseBase
{

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Test
   public void testRemote() throws Exception
   {
      InitialContext ctx = new InitialContext();

      Object bean = ctx.lookup("MyStatelessRemoteOnlyBean/remote");
      assertTrue(bean instanceof MyStatelessRemote);

      String result = ((MyStatelessRemote) bean).sayHi("testRemote");
      assertEquals("Hi testRemote", result);
   }

   @Test
   public void testRemoteSpecificInterface() throws Exception
   {
      InitialContext ctx = new InitialContext();

      Object bean = ctx.lookup("MyStatelessRemoteOnlyBean/remote-" + MyStatelessRemote.class.getName());
      assertTrue(bean instanceof MyStatelessRemote);

      String result = ((MyStatelessRemote) bean).sayHi("testRemote");
      assertEquals("Hi testRemote", result);
   }

   @Test
   public void testRemoteHome() throws Exception
   {
      InitialContext ctx = new InitialContext();

      Object bean = ctx.lookup("MyStatelessRemoteOnlyBean/home");
      assertTrue(bean instanceof MyStatelessRemoteHome);
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
      ProxyStatelessSessionRemoteOnlyTestCase.bootstrap.deploy(ProxyStatelessSessionTestCase.class);

      // Create a SLSB
      StatelessContainer container = Utils.createSlsb(MyStatelessRemoteOnlyBean.class);

      // Install
      Ejb3RegistrarLocator.locateRegistrar().bind(container.getName(), container);

   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
}
