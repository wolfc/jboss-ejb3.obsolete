/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.sandbox.stateless.unit;

import javax.naming.InitialContext;

import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.ejb3.test.sandbox.stateless.SimpleStatelessBean;
import org.jboss.ejb3.test.sandbox.stateless.SimpleStatelessLocal;
import org.jnp.server.SingletonNamingServer;

import junit.framework.TestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleStatelessUnitTestCase extends TestCase
{
   public void test1() throws Exception
   {
      SingletonNamingServer namingServer = new SingletonNamingServer();
      
      //StatelessContainer statelessContainer = new StatelessContainer();
      InterceptorContainer container = new InterceptorContainer(SimpleStatelessBean.class);
      
      InitialContext ctx = new InitialContext();
      
      SimpleStatelessLocal bean = (SimpleStatelessLocal) ctx.lookup("SimpleStatelessBean/local");
      
      assertNotNull(bean);
      String result = bean.sayHi("Test");
      assertEquals("Hi Test", result);
      
      namingServer.destroy();
   }
}
