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
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStateless30OnlyBean;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessLocal;
import org.jboss.ejb3.test.proxy.impl.common.ejb.slsb.MyStatelessRemote;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ProxyStatelessSession30OnlyTestCase extends SessionTestCaseBase
{

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Test
   public void testLocal() throws Exception
   {
      InitialContext ctx = new InitialContext();

      Object bean = ctx.lookup("MyStateless30OnlyBean/local");
      assertTrue(bean instanceof MyStatelessLocal);

      String result = ((MyStatelessLocal) bean).sayHi("testLocal");
      assertEquals("Hi testLocal", result);
   }

   @Test
   public void testLocalSpecificInterface() throws Exception
   {
      InitialContext ctx = new InitialContext();

      Object bean = ctx.lookup("MyStateless30OnlyBean/local-" + MyStatelessLocal.class.getName());
      assertTrue(bean instanceof MyStatelessLocal);

      String result = ((MyStatelessLocal) bean).sayHi("testLocal");
      assertEquals("Hi testLocal", result);
   }

   @Test
   public void testRemote() throws Exception
   {
      InitialContext ctx = new InitialContext();

      Object bean = ctx.lookup("MyStateless30OnlyBean/remote");
      assertTrue(bean instanceof MyStatelessRemote);

      String result = ((MyStatelessRemote) bean).sayHi("testRemote");
      assertEquals("Hi testRemote", result);
   }

   @Test
   public void testRemoteSpecificInterface() throws Exception
   {
      InitialContext ctx = new InitialContext();

      Object bean = ctx.lookup("MyStateless30OnlyBean/remote-" + MyStatelessRemote.class.getName());
      assertTrue(bean instanceof MyStatelessRemote);

      String result = ((MyStatelessRemote) bean).sayHi("testRemote");
      assertEquals("Hi testRemote", result);
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
      ProxyStatelessSession30OnlyTestCase.bootstrap.deploy(ProxyStatelessSessionTestCase.class);

      // Create a SLSB
      StatelessContainer container = Utils.createSlsb(MyStateless30OnlyBean.class);

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
