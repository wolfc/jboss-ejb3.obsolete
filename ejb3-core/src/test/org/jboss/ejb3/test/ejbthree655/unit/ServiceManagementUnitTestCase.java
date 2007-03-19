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
package org.jboss.ejb3.test.ejbthree655.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree655.AbstractStateChecker;
import org.jboss.ejb3.test.ejbthree655.MyService;
import org.jboss.test.JBossTestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ServiceManagementUnitTestCase extends JBossTestCase
{
   public ServiceManagementUnitTestCase(String name)
   {
      super(name);
   }

   public void testHasCreateBeenCalled() throws Exception
   {
      MyService session = (MyService) getInitialContext().lookup("MyServiceBean/remote");
      session.sayHelloTo("me");
      
      AbstractStateChecker.State expected = AbstractStateChecker.State.STARTED;
      assertEquals(expected, session.getState());
   }
   
   public void test2() throws Exception
   {
      MyService session = (MyService) getInitialContext().lookup("MyManagedServiceBean/remote");
      session.sayHelloTo("me");
      
      AbstractStateChecker.State expected = AbstractStateChecker.State.STARTED;
      assertEquals(expected, session.getState());
   }
   
   public void testRestartContainer() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName name = new ObjectName("jboss.j2ee:jar=ejbthree655.jar,name=MyManagedServiceBean,service=EJB3,type=ManagementInterface");
      Object params[] = { };
      String signature[] = { };
      server.invoke(name, "stop", params, signature);
      
      {
         AbstractStateChecker.State expected = AbstractStateChecker.State.STOPPED;
         assertEquals(expected, server.getAttribute(name, "State"));
      }
      
      try
      {
         MyService session = (MyService) getInitialContext().lookup("MyManagedServiceBean/remote");
         session.sayHelloTo("me");
         
         fail("should have failed");
      }
      catch(NameNotFoundException e)
      {
         String expected = "MyManagedServiceBean not bound";
         assertEquals(expected, e.getMessage());
      }
      
      server.invoke(name, "start", params, signature);
      
      {
         AbstractStateChecker.State expected = AbstractStateChecker.State.STARTED;
         assertEquals(expected, server.getAttribute(name, "State"));
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceManagementUnitTestCase.class, "ejbthree655.jar");
   }
}
