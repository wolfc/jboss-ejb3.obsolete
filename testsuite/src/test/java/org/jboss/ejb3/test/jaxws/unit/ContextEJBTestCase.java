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
package org.jboss.ejb3.test.jaxws.unit;

// $Id: ContextEJBTestCase.java 1874 2007-01-09 14:28:41Z thomas.diesler@jboss.com $

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ejb3.test.jaxws.EndpointInterface;
import org.jboss.test.JBossTestCase;

/**
 * Test JAXWS WebServiceContext
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class ContextEJBTestCase extends JBossTestCase
{
   public ContextEJBTestCase(String name)
   {
      super(name);
      // TODO Auto-generated constructor stub
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ContextEJBTestCase.class, "jaxws-context.jar");
   }
   
   public void testClientAccess() throws Exception
   {
      log.info("In case of connection exception, there is a host name defined in the wsdl, which used to be '@jbosstest.host.name@'");
      URL wsdlURL = new File("../src/test/resources/test/jaxws/TestService.wsdl").toURL();
      QName qname = new QName("http://org.jboss.ws/jaxws/context", "TestService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      
      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
}
