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
package org.jboss.ejb3.test.consumer.unit;

import org.jboss.ejb3.mdb.ProducerManager;
import org.jboss.ejb3.mdb.ProducerObject;
import org.jboss.ejb3.test.consumer.QueueTestRemote;
import org.jboss.ejb3.test.consumer.DeploymentDescriptorQueueTestRemote;
import org.jboss.ejb3.test.consumer.TestStatus;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: ConsumerUnitTestCase.java 61136 2007-03-06 09:24:20Z wolfc $
 */

public class ConsumerUnitTestCase
        extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public ConsumerUnitTestCase(String name)
   {

      super(name);

   }

   public void testQueue() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      QueueTestRemote tester = (QueueTestRemote) getInitialContext().lookup("org.jboss.ejb3.test.consumer.QueueTestRemote");
      ProducerManager manager = (ProducerManager) ((ProducerObject) tester).getProducerManager();
      manager.connect();
      try
      {
         tester.method1("testQueue", 5);
         Thread.sleep(1000);
         assertEquals(status.queueFired(), "method1");
         assertEquals(status.interceptedQueue(), "method1");
         assertTrue(status.postConstruct());
         assertTrue(status.fieldMessage());
         assertTrue(status.setterMessage());

         status.clear();
         tester.method2("testQueue", 5.5F);
         Thread.sleep(1000);
         assertEquals(status.queueFired(), "method2");
         assertEquals(status.interceptedQueue(), "method2");
         assertTrue(status.fieldMessage());
         assertTrue(status.setterMessage());
      }
      finally
      {
         manager.close();
      }

      //TODO: Figure out how to test preDestroy gets invoked
      //assertTrue(status.preDestroy());
   }

   public void testQueueXA() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      status.testXA();

      Thread.sleep(1000);
      assertEquals(status.queueFired(), "method2");
      assertEquals(status.interceptedQueue(), "method2");
   }

   public void testQueueLocal() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      status.testLocal();

      Thread.sleep(1000);
      assertEquals(status.queueFired(), "method2");
      assertEquals(status.interceptedQueue(), "method2");
   }
   
   public void testDeploymentDescriptorQueue() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      DeploymentDescriptorQueueTestRemote tester = (DeploymentDescriptorQueueTestRemote) getInitialContext().lookup("org.jboss.ejb3.test.consumer.DeploymentDescriptorQueueTestRemote");
      ProducerManager manager = (ProducerManager) ((ProducerObject) tester).getProducerManager();
      manager.connect();
      try
      {
         tester.method1("testQueue", 5);
         Thread.sleep(1000);
         assertEquals(status.queueFired(), "method1");
         assertEquals(status.interceptedQueue(), "method1");
         assertTrue(status.postConstruct());
         assertTrue(status.fieldMessage());
         assertTrue(status.setterMessage());

         status.clear();
         tester.method2("testQueue", 5.5F);
         Thread.sleep(1000);
         assertEquals(status.queueFired(), "method2");
         assertEquals(status.interceptedQueue(), "method2");
         assertTrue(status.fieldMessage());
         assertTrue(status.setterMessage());
      }
      finally
      {
         manager.close();
      }

      //TODO: Figure out how to test preDestroy gets invoked
      //assertTrue(status.preDestroy());
   }
   
   public void testDeploymentDescriptorQueueXA() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      status.testDeploymentDescriptorXA();

      Thread.sleep(1000);
      assertEquals(status.queueFired(), "method2");
      assertEquals(status.interceptedQueue(), "method2");
   }

   public void testDeploymentDescriptorQueueLocal() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      status.testDeploymentDescriptorLocal();

      Thread.sleep(1000);
      assertEquals(status.queueFired(), "method2");
      assertEquals(status.interceptedQueue(), "method2");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ConsumerUnitTestCase.class, "consumertest-service.xml, consumer-test.jar");
   }

}
