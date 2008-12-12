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
package org.jboss.ejb3.test.stateful.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.ejb3.test.stateful.Stateful;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

import org.jboss.security.SimplePrincipal;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MetricsUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(MetricsUnitTestCase.class);

   public MetricsUnitTestCase(String name)
   {

      super(name);

   }
   
   public void testJmxMetrics() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
	      
	   MBeanServerConnection server = getServer();
	      
	   testJmxMetrics(server, "Stateful", "jboss.j2ee:jar=stateful-test.jar,name=StatefulBean,service=EJB3");
	   testJmxMetrics(server, "TreeCacheStateful", "jboss.j2ee:jar=stateful-test.jar,name=TreeCacheStatefulBean,service=EJB3");
   }
   
   protected void testJmxMetrics(MBeanServerConnection server, String jndiBinding, String objectName) throws Exception
   {
      ObjectName testerName = new ObjectName(objectName);
      
      System.out.println("testPassivation");
      Stateful stateful = (Stateful)getInitialContext().lookup(jndiBinding);
      assertNotNull(stateful);
      stateful.setState("state");
      
      int count = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(1, count);
      
      int size = (Integer)server.getAttribute(testerName, "CacheSize");
      assertEquals(1, size);
      
      assertEquals("state", stateful.getState());
      stateful.testSerializedState("state");
      stateful.clearPassivated();
      assertEquals(null, stateful.getInterceptorState());
      stateful.setInterceptorState("hello world");
      assertFalse(stateful.testSessionContext());
      Thread.sleep(10 * 1000);
      
      size = (Integer)server.getAttribute(testerName, "CacheSize");
      assertEquals(0, size);
      
      count = (Integer)server.getAttribute(testerName, "PassivatedCount");
      assertEquals(1, count);
      assertTrue(stateful.wasPassivated());
      
      assertEquals("state", stateful.getState());
      assertEquals("hello world", stateful.getInterceptorState());

      Stateful another = (Stateful)getInitialContext().lookup(jndiBinding);
      assertEquals(null, another.getInterceptorState());
      another.setInterceptorState("foo");
      assertEquals("foo", another.getInterceptorState());
      assertEquals("hello world", stateful.getInterceptorState());
      
      assertFalse(stateful.testSessionContext());
      
      stateful.testResources();
      
      count = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(2, count);
      
      size = (Integer)server.getAttribute(testerName, "CacheSize");
      assertEquals(2, size);
      
      size = (Integer)server.getAttribute(testerName, "RemoveCount");
      assertEquals(0, size);
      
      another.removeMe();
      size = (Integer)server.getAttribute(testerName, "CacheSize");
      assertEquals(1, size);
      
      size = (Integer)server.getAttribute(testerName, "RemoveCount");
      assertEquals(1, size);
      
      stateful.removeMe();
      size = (Integer)server.getAttribute(testerName, "CacheSize");
      assertEquals(0, size);
      
      size = (Integer)server.getAttribute(testerName, "RemoveCount");
      assertEquals(2, size);
      
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(MetricsUnitTestCase.class, "stateful-test.jar");
   }

}
