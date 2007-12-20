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
package org.jboss.ejb3.test.webservices.unit;

import junit.framework.Test;
import org.jboss.ejb3.test.webservices.jsr181.EJB3RemoteBusinessInterface;
import org.jboss.ejb3.test.webservices.jsr181.EndpointInterface;
import org.jboss.ejb3.test.webservices.jsr181.NarrowableEJB3RemoteHomeInterface;
import org.jboss.ejb3.test.webservices.jsr181.NarrowableEJB3RemoteInterface;
import org.jboss.ejb3.test.webservices.jsr181.RemoteHomeInterface;
import org.jboss.ejb3.test.webservices.jsr181.StatelessRemote;
import org.jboss.test.JBossTestCase;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.xml.rpc.Service;
import java.util.Hashtable;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 66256 $
 */
public class JSR181TestCase extends JBossTestCase
{
   public JSR181TestCase(String name)
   {
      super(name);
   }

   public void testRemoteAccess() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      EJB3RemoteBusinessInterface ejb3Remote = (EJB3RemoteBusinessInterface)iniCtx.lookup("/ejb3/EJB3EndpointInterface");

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
   
   public void testHomeRemoteAccess() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      RemoteHomeInterface home = (RemoteHomeInterface)iniCtx.lookup("EJB3Bean/home");
      EJB3RemoteBusinessInterface ejb3Remote = home.create();

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
   
   public void testNarrowedHomeRemoteAccess() throws Exception
   {
      InitialContext jndiContext = getInitialContext();
  
      Object proxy = jndiContext.lookup("NarrowableEJB3Bean/home");
      assertNotNull(proxy);
      
      NarrowableEJB3RemoteHomeInterface home = (NarrowableEJB3RemoteHomeInterface)PortableRemoteObject.narrow(proxy, NarrowableEJB3RemoteHomeInterface.class);
      assertNotNull(home);
      Object obj = home.create();
      System.out.println(obj);
      NarrowableEJB3RemoteInterface ejb3Remote = home.create();

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testWebService() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
   
   public void testWebServiceRef() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      StatelessRemote stateless = (StatelessRemote)jndiContext.lookup("StatelessBean/remote");
      assertNotNull(stateless);
      
      assertEquals("Hello", stateless.echo1("Hello"));
      
      assertEquals("Hello", stateless.echo2("Hello"));
      
      assertEquals("Hello", stateless.echo3("Hello"));
      
      assertEquals("Hello", stateless.echo4("Hello"));
      
      assertEquals("Hello", stateless.echo5("Hello"));
      
      assertEquals("Hello", stateless.echo6("Hello"));
      
      assertEquals("Hello", stateless.echo7("Hello"));
      
      assertEquals("Hello", stateless.echo8("Hello"));
      
      assertEquals("Hello", stateless.echo9("Hello"));
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(JSR181TestCase.class, "jsr181-client.jar, jsr181.jar");
   }
   
   protected InitialContext getInitialContext(String clientName) throws NamingException
   {
      InitialContext iniCtx = new InitialContext();
      Hashtable env = iniCtx.getEnvironment();
      env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming.client");
      env.put("j2ee.clientName", clientName);
      return new InitialContext(env);
   }

   /** Get the client's env context
    */
   protected InitialContext getInitialContext() throws NamingException
   {
      return getInitialContext("jbossws-client");
   }
   
   public String getServerHost()
   {
      String hostName = System.getProperty("jbosstest.server.host", "localhost");
      return hostName;
   }
}
