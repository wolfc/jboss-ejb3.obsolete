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
package org.jboss.ejb3.test.consumer;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.JndiInject;
import org.jboss.ejb3.mdb.ProducerManager;
import org.jboss.ejb3.mdb.ProducerObject;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(TestStatus.class)
public class TestStatusBean implements TestStatus
{
   public static String queueRan = null;
   public static String topicRan = null;
   public static String interceptedTopic = null;
   public static String interceptedQueue = null;
   public static boolean postConstruct = false;
   public static boolean preDestroy = false;
   public static boolean fieldMessage = false;
   public static boolean setterMessage = false;


   public void clear()
   {
      queueRan = null;
      topicRan = null;
      interceptedTopic = null;
      interceptedQueue = null;
      postConstruct = false;
      preDestroy = false;
      fieldMessage = setterMessage = false;
   }

   private QueueTestXA xa;
   private ProducerManager xaManager;

   @JndiInject(jndiName="org.jboss.ejb3.test.consumer.QueueTestXA")
   public void setXa(QueueTestXA xa)
   {
      this.xa = xa;
      this.xaManager = ((ProducerObject)xa).getProducerManager();
   }

   private QueueTest local;
   private ProducerManager localManager;

   @JndiInject(jndiName="org.jboss.ejb3.test.consumer.QueueTestLocal")
   public void setLocal(QueueTest local)
   {
      this.local = local;
      this.localManager = ((ProducerObject)local).getProducerManager();
   }
   
   private DeploymentDescriptorQueueTestXA xaDeploymentDescriptor;
   private ProducerManager xaManagerDeploymentDescriptor;

   @JndiInject(jndiName="org.jboss.ejb3.test.consumer.DeploymentDescriptorQueueTestXA")
   public void setXaDeploymentDescriptor(DeploymentDescriptorQueueTestXA xa)
   {
      this.xaDeploymentDescriptor = xa;
      this.xaManagerDeploymentDescriptor = ((ProducerObject)xa).getProducerManager();
   }

   private DeploymentDescriptorQueueTestLocal localDeploymentDescriptor;
   private ProducerManager localManagerDeploymentDescriptor;

   @JndiInject(jndiName="org.jboss.ejb3.test.consumer.DeploymentDescriptorQueueTestLocal")
   public void setLocalDeploymentDescriptor(DeploymentDescriptorQueueTestLocal local)
   {
      this.localDeploymentDescriptor = local;
      this.localManagerDeploymentDescriptor = ((ProducerObject)local).getProducerManager();
   }

   public void testXA() throws Exception
   {

      xaManager.connect();
      xa.method1("testXA", 1);
      Thread.sleep(1000);
      xa.method2("testXA2", 4.4F);
      System.out.println("end TESTXA **");
      xaManager.close();
   }

   public void testLocal() throws Exception
   {

      localManager.connect();
      local.method1("testLocal", 1);
      Thread.sleep(1000);
      local.method2("testLocal2", 4.4F);
      localManager.close();
   }
   
   public void testDeploymentDescriptorXA() throws Exception
   {

      xaManagerDeploymentDescriptor.connect();
      xaDeploymentDescriptor.method1("testXA", 1);
      Thread.sleep(1000);
      xaDeploymentDescriptor.method2("testXA2", 4.4F);
      System.out.println("end TESTXA **");
      xaManagerDeploymentDescriptor.close();
   }

   public void testDeploymentDescriptorLocal() throws Exception
   {

      localManagerDeploymentDescriptor.connect();
      localDeploymentDescriptor.method1("testLocal", 1);
      Thread.sleep(1000);
      localDeploymentDescriptor.method2("testLocal2", 4.4F);
      localManagerDeploymentDescriptor.close();
   }

   public String queueFired()
   {
      return queueRan;
   }

   public String topicFired()
   {
      return topicRan;
   }

   public String interceptedTopic()
   {
      return interceptedTopic;
   }

   public String interceptedQueue()
   {
      return interceptedQueue;
   }

   public boolean postConstruct()
   {
      return postConstruct;
   }

   public boolean preDestroy()
   {
      return preDestroy;
   }

   public boolean fieldMessage()
   {
      return fieldMessage;
   }

   public boolean setterMessage()
   {
      return setterMessage;
   }
}
