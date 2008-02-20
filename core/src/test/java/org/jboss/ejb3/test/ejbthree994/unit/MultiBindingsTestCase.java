/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree994.unit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree994.BusinessInterface;
import org.jboss.remoting.InvokerLocator;
import org.jboss.test.JBossTestCase;


/**
 * Test if multiple remote bindings work correctly.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 63662 $
 */
public class MultiBindingsTestCase extends JBossTestCase
{

   public MultiBindingsTestCase(String name)
   {
      super(name);
   }

   private static String getProxyUri(Object proxy) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      Field f = handler.getClass().getDeclaredField("uri");
      f.setAccessible(true);
      InvokerLocator locator = (InvokerLocator) f.get(handler);
      return locator.getOriginalURI();
   }
   
   public void test1() throws Exception
   {
      {
         BusinessInterface bean = (BusinessInterface) getInitialContext().lookup("Stateful3873");
         String actual = bean.echo("123");
         assertEquals(actual, "*** 123 ***");
         
         String proxyUri = getProxyUri(bean);
         assertEquals(proxyUri, "socket://127.0.0.1:3873");
      }

      {
         BusinessInterface bean = (BusinessInterface) getInitialContext().lookup("Stateful3874");
         String actual = bean.echo("456");
         assertEquals(actual, "*** 456 ***");
         
         String proxyUri = getProxyUri(bean);
         assertEquals(proxyUri, "socket://127.0.0.1:3874");
      }
      
      {
         BusinessInterface bean = (BusinessInterface) getInitialContext().lookup("Stateful3875");
         String actual = bean.echo("789");
         assertEquals(actual, "*** 789 ***");
         
         String proxyUri = getProxyUri(bean);
         assertEquals(proxyUri, "socket://127.0.0.1:3875/");
      }

   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(MultiBindingsTestCase.class, "ejbthree994-connectors-service.xml,ejbthree994.jar");
   }

}
