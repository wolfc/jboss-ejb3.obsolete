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
package org.jboss.ejb3.test.ejbthree1807.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1807.RemoveRejecter;
import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * @author Brian Stansberry
 */
public class RemovalTaskFailureUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(RemovalTaskFailureUnitTestCase.class);
   
   private SecurityClient client = null;

   public RemovalTaskFailureUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      this.client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(RemovalTaskFailureUnitTestCase.class, "ejbthree1807.jar");
   }

   @Override
   protected void tearDown() throws Exception
   {
      if(client == null)
         return;
      client.logout();
      client = null;
      
      super.tearDown();
   }
   
   public void testRemovalTaskFailureHandling() throws Exception
   {
      RemoveRejecter allow = (RemoveRejecter)getInitialContext().lookup("RemoveRejecter");
      assertNotNull(allow);
      allow.setRejectRemove(false);
      RemoveRejecter reject = (RemoveRejecter)getInitialContext().lookup("RemoveRejecter");
      assertNotNull(reject);
      allow.setRejectRemove(true);
      MBeanServerConnection server = getServer();
      
      ObjectName testerName = new ObjectName("jboss.j2ee:jar=ejbthree1807.jar,name=RemoveRejecterBean,service=EJB3");
      int cacheSize = (Integer)server.getAttribute(testerName, "CacheSize");
      assertEquals(2, cacheSize);
      int totalSize = (Integer)server.getAttribute(testerName, "TotalSize");
      assertEquals(2, totalSize);
      
      // Allow removal to run twice
      Thread.sleep(3 * 1000);
      cacheSize = (Integer)server.getAttribute(testerName, "CacheSize");
      assertEquals(0, cacheSize);
      totalSize = (Integer)server.getAttribute(testerName, "TotalSize");
      assertEquals(0, totalSize);
      
      int removeCount = (Integer)server.getAttribute(testerName, "RemoveCount");
      assertEquals(2, removeCount);
   }   
}
