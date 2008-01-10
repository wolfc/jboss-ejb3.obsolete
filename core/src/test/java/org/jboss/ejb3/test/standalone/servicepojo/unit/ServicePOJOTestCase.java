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
package org.jboss.ejb3.test.standalone.servicepojo.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.test.standalone.servicepojo.ServiceOneLocal;
import org.jboss.ejb3.test.standalone.servicepojo.ServiceTwoLocal;
import org.jboss.mx.util.MBeanServerLocator;

public class ServicePOJOTestCase extends TestCase
{
   public ServicePOJOTestCase(String name)
   {
      super(name);
   }
   
   private static MBeanServerConnection getServer()
   {
      return MBeanServerLocator.locate();
   }
   
   public void testLocalInterface() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ServiceOneLocal service = (ServiceOneLocal) ctx.lookup("ServiceOneBean/local");
      service.setAttribute(1);
   }
   
   public void testManagementInterface() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName name = new ObjectName("jboss.j2ee:jar=embedded-servicepojo,name=ServiceOneBean,service=EJB3,type=ManagementInterface");
      Object params[] = { "me" };
      String signature[] = { String.class.getName() };
      String result = (String) server.invoke(name, "sayHello", params, signature);
      assertEquals("Hello me", result);
   }
   
   public void testPartialManagementInterface() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName name = new ObjectName("jboss.j2ee:jar=embedded-servicepojo,name=ServiceTwoBean,service=EJB3,type=ManagementInterface");
      Object params[] = { "me" };
      String signature[] = { String.class.getName() };
      String result = (String) server.invoke(name, "sayHello", params, signature);
      assertEquals("Hello me", result);
   }
   
   public void testStateOne() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ServiceOneLocal service = (ServiceOneLocal) ctx.lookup("ServiceOneBean/local");
      int actualState = service.getState();
      assertEquals(2, actualState);
   }
   
   public void testStateTwo() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ServiceTwoLocal service = (ServiceTwoLocal) ctx.lookup("ServiceTwoBean/local");
      int actualState = service.getState();
      assertEquals(1, actualState);
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(ServicePOJOTestCase.class);

      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            startupEmbeddedJboss();
         }

         protected void tearDown()
         {
            shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.boot(null);
      EJB3StandaloneBootstrap.scanClasspath("embedded-servicepojo.jar");
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }
}
