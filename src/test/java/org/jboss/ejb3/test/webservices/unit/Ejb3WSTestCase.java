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
import org.jboss.test.JBossTestCase;
import org.jboss.ejb3.test.webservices.Ejb3WSEndpoint;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.util.Hashtable;
import java.net.URL;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class Ejb3WSTestCase extends JBossTestCase
{
   public Ejb3WSTestCase(String name)
   {
      super(name);
   }

   public void testRemoteAccess() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      Ejb3WSEndpoint ejb3Remote = (Ejb3WSEndpoint)iniCtx.lookup("/Ejb3WSEndpoint");

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testWebService() throws Exception
   {
      Service service = Service.create(
        new URL("http://"+getServerHost()+":8080/webservices-ejb3/Ejb3WSEndpointImpl?wsdl"),
        new QName("http://webservices.test.ejb3.jboss.org/","Ejb3WSEndpointImplService")
      );

      Ejb3WSEndpoint port = service.getPort(Ejb3WSEndpoint.class);
      String response = port.echo("Hello");
      assertEquals("Hello", response);            
   }
   
   public void testWebServiceRef() throws Exception
   {
                  
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(Ejb3WSTestCase.class, "webservices-ejb3.jar, webservices-ejb3-client.jar");
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
