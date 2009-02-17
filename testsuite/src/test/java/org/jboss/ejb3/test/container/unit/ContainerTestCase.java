/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.container.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.ejb3.test.reference21_30.Test2;
import org.jboss.ejb3.test.reference21_30.Test2Home;
import org.jboss.ejb3.test.reference21_30.Test3;
import org.jboss.ejb3.test.reference21_30.Test3Business;
import org.jboss.ejb3.test.service.ServiceSixRemote;
import org.jboss.ejb3.test.stateful.Stateful;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class ContainerTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(ContainerTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public ContainerTestCase(String name)
   {
      super(name);
   }

   public void testInvocationStatistics() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      Test3Business test3 = (Test3Business)jndiContext.lookup("Test3Remote");
      assertNotNull(test3);
      test3.testAccess();
      
      Test2Home home = (Test2Home)jndiContext.lookup("Test2");
      assertNotNull(home);
      Test2 test2 = home.create();
      assertNotNull(test2);
      test2.testAccess();
      
      MBeanServerConnection server = getServer();
      
      ObjectName objectName = new ObjectName("jboss.j2ee:jar=multideploy-ejb3.jar,name=Test3,service=EJB3");
      InvocationStatistics stats = (InvocationStatistics)server.getAttribute(objectName, "InvokeStats");
      System.out.println("Stats \n" + stats);
      assertTrue(stats.toString().contains("testAccess"));
      
      ServiceSixRemote test = (ServiceSixRemote) getInitialContext().lookup("serviceSix/remote");
      test.setCalled(false);
      
      objectName = new ObjectName("jboss.j2ee:jar=service-test.jar,name=ServiceSix,service=EJB3");
      stats = (InvocationStatistics)server.getAttribute(objectName, "InvokeStats");
      System.out.println("Stats \n" + stats);
      assertTrue(stats.toString().contains("setCalled"));
      
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
      
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      stateful.getState();
      
      objectName = new ObjectName("jboss.j2ee:jar=stateful-test.jar,name=StatefulBean,service=EJB3");
      stats = (InvocationStatistics)server.getAttribute(objectName, "InvokeStats");
      System.out.println("Stats \n" + stats);
      assertTrue(stats.toString().contains("getState"));
      
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ContainerTestCase.class, "multideploy.jar, multideploy-ejb3.jar, service-test.sar, service-test.jar, stateful-test.jar");
   }

}
