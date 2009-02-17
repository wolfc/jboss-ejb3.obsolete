/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ssl.unit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.ejb3.test.ssl.BusinessInterface;
import org.jboss.remoting.InvokerLocator;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class SSLUnitTestCase
        extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();


   public SSLUnitTestCase(String name)
   {
      super(name);
   }

   private static String getProxyUri(Object proxy) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
//      System.err.println("class: " + proxy.getClass());
//      for(Class c : proxy.getClass().getClasses())
//      {
//         System.err.println(" - " + c);
//      }
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
//      System.err.println("invocationHandler: " + handler);
//      System.err.println("invocationHandler: " + handler.getClass());
      Field f = handler.getClass().getDeclaredField("url");
      f.setAccessible(true);
      String url = (String) f.get(handler);
      return url;
   }
   
   public void testNoDefaultBinding() throws Exception
   {
      System.out.println("*** TEST No default binding");
      InitialContext ctx = new InitialContext();
      try
      {
         BusinessInterface bi = (BusinessInterface)ctx.lookup(BusinessInterface.class.getName());
         throw new Exception("Nothing should be bound to the default name");
      }
      catch(NamingException e)
      {
         
      }
      System.out.println("*** success");
   }

   public void testSSLBindings() throws Exception
   {
      System.out.println("*** TEST SSL Bindings");
      InitialContext ctx = new InitialContext();
      BusinessInterface sf = (BusinessInterface)ctx.lookup("StatefulSSL");
      String proxyUri = getProxyUri(sf);
      assertTrue("proxy uri should start with sslsocket", proxyUri.startsWith("sslsocket"));
      assertEquals(sf.echo("123"), "*** 123 ***");
      
      BusinessInterface sfc = (BusinessInterface)ctx.lookup("StatefulClusteredSSL");
      assertEquals(sfc.echo("123"), "*** 123 ***");
      
      BusinessInterface sl = (BusinessInterface)ctx.lookup("StatelessSSL");
      assertEquals(sl.echo("123"), "*** 123 ***");

      BusinessInterface slc = (BusinessInterface)ctx.lookup("StatelessClusteredSSL");
      assertEquals(slc.echo("123"), "*** 123 ***");
      System.out.println("*** success");
   }
   
   public void testNormalBindings() throws Exception
   {
      System.out.println("*** TEST Normal Bindings");
      InitialContext ctx = new InitialContext();
      BusinessInterface sf = (BusinessInterface)ctx.lookup("StatefulNormal");
      String proxyUri = getProxyUri(sf);
      assertTrue("proxy uri should start with sslsocket", proxyUri.startsWith("socket"));
      assertEquals(sf.echo("123"), "*** 123 ***");
      
      BusinessInterface sfc = (BusinessInterface)ctx.lookup("StatefulClusteredNormal");
      assertEquals(sfc.echo("123"), "*** 123 ***");
      
      BusinessInterface sl = (BusinessInterface)ctx.lookup("StatelessNormal");
      assertEquals(sl.echo("123"), "*** 123 ***");

      BusinessInterface slc = (BusinessInterface)ctx.lookup("StatelessClusteredNormal");
      assertEquals(slc.echo("123"), "*** 123 ***");

      System.out.println("*** success");
   }
   

   public static Test suite() throws Exception
   {
      return getDeploySetup(SSLUnitTestCase.class, "ssl-test.jar");
   }


}
