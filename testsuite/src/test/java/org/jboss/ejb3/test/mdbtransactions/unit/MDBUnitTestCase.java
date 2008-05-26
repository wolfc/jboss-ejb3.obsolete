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
package org.jboss.ejb3.test.mdbtransactions.unit;

import java.util.Enumeration;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.test.mdbtransactions.Entity;
import org.jboss.ejb3.test.mdbtransactions.StatelessFacade;
import org.jboss.ejb3.test.mdbtransactions.TestStatus;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MDBUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(MDBUnitTestCase.class);

   static boolean deployed = false;

   static int test = 0;

   public MDBUnitTestCase(String name)
   {

      super(name);

   }
   
   /**
    * This tests the basic functionality of sending a message.
    * Precondition for the actual test.
    * @throws Exception
    */
   public void testSimpleQueue() throws Exception
   {
      InitialContext ctx = new InitialContext();
      Queue queue = (Queue) ctx.lookup("queue/A");
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection connection = factory.createQueueConnection();
      try
      {
         connection.start();
         QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         QueueSender sender = session.createSender(queue);
         TextMessage message = session.createTextMessage("Hello world");
         sender.send(message);
         sender.close();
         QueueReceiver receiver = session.createReceiver(queue);
         Message msg = receiver.receive(5000);
         assertNotNull(msg);
         receiver.close();
         session.close();
      }
      finally
      {
         connection.close();
      }
   }
   
   /**
    * Send a message to a MDB and receive it back on another queue.
    * Precondition for the actual test.
    * @throws Exception
    */
   public void testPingPong() throws Exception
   {
      InitialContext ctx = new InitialContext();
      Queue queue = (Queue) ctx.lookup("queue/B");
      Queue replyQueue = (Queue) ctx.lookup("queue/C");
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection connection = factory.createQueueConnection();
      try
      {
         connection.start();
         QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         QueueSender sender = session.createSender(queue);
         TextMessage message = session.createTextMessage("Hello world");
         sender.send(message);
         sender.close();
         QueueReceiver receiver = session.createReceiver(replyQueue);
         Message msg = receiver.receive(5000);
         assertNotNull(msg);
         receiver.close();
         session.close();
      }
      finally
      {
         connection.close();
      }
   }
   
   public void testMdbTransactions() throws Exception
   {
      TestStatus status = (TestStatus)getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      
      sendMessages("queue/rollbackmdbtest", 2);
      
      Thread.sleep(5000);
      
      assertEquals(1, status.messageCount());
      assertTrue(status.caughtRollback());
      
      Queue queue = (Queue) getInitialContext().lookup("queue/DLQ");
      QueueConnectionFactory factory = getQueueConnectionFactory();
      QueueConnection connection = factory.createQueueConnection();
      try
      {
         connection.start();
         QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         
         QueueReceiver receiver = session.createReceiver(queue);
         
         /*
         {
            Queue sendQueue = (Queue) getInitialContext().lookup("queue/rollbackmdbtest");
            QueueSender sender = session.createSender(sendQueue);
            TextMessage message = session.createTextMessage("Should never arrive");
            message.setIntProperty("JMS_JBOSS_REDELIVERY_LIMIT", 0);
            sender.send(message);
            sender.close();
         }
         */
         
         Message message = receiver.receive(5000);
         assertNotNull(message);
         
         session.close();
         connection.close();
      }
      finally
      {
         connection.close();
      }
      
      sendMessages("queue/mdbtest", 1);
   }
   
   public void testRollback() throws Exception
   {
      TestStatus status = (TestStatus)getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      
   }
   
   /**
    * Test sending a message to a MDB which can not be created.
    * @throws Exception
    */
   public void testBadCreation() throws Exception
   {
      TestStatus status = (TestStatus)getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      
      sendMessages("queue/badcreationmdb", 1);
      
      InitialContext ctx = getInitialContext();
      Queue dlq = (Queue) ctx.lookup("queue/DLQ");
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection connection = factory.createQueueConnection();
      try
      {
         connection.start();
         QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         QueueReceiver receiver = session.createReceiver(dlq);
         
         //sendMessages(session, "queue/badcreationmdb", 1);
         /*
         {
            Queue queue = (Queue) ctx.lookup("queue/badcreationmdb");
            QueueSender sender = session.createSender(queue);
            TextMessage message = session.createTextMessage("Should never arrive");
            message.setIntProperty("JMS_JBOSS_REDELIVERY_LIMIT", 0);
            sender.send(message);
            sender.close();
         }
         */
         
         //Thread.sleep(5000);
         
         log.info("ENTERING RECEIVE");
         Message message = receiver.receive(5000);
         log.info("EXITING RECEIVE WITH " + message);
         assertNotNull(message);
         
         session.close();
      }
      finally
      {
         connection.close();
      }
   }
   
   protected void sendMessages(String queueName, int numMessages) throws Exception
   {
      InitialContext ctx = getInitialContext();
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection connection = factory.createQueueConnection();
      try
      {
         connection.start();
         QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         sendMessages(session, queueName, numMessages);
         session.close();
      }
      finally
      {
         connection.close();
      }
   }
   
   protected void sendMessages(QueueSession session, String queueName, int numMessages) throws Exception
   {
      QueueSender sender = null;

      Queue queue = (Queue) getInitialContext().lookup(
            queueName);
      
      QueueReceiver receiver = session.createReceiver(queue);
      Message message = receiver.receiveNoWait();
      while (message != null)
         message = receiver.receiveNoWait();

      Entity entity = new Entity();
      entity.setName("test-entity");
      entity.setId(1234L);
      
      StatelessFacade stateless = (StatelessFacade)getInitialContext().lookup("StatelessFacade");
      stateless.clear(entity);
      
      ObjectMessage msg = session.createObjectMessage(entity);
      msg.setIntProperty("JMS_JBOSS_REDELIVERY_LIMIT", 0);

      sender = session.createSender(queue);
      
      for (int i = 0 ; i < numMessages ; ++i)
         sender.send(msg);
   }
   
   protected QueueConnectionFactory getQueueConnectionFactory()
      throws Exception
   {
      // We want the server connection factory
      return (QueueConnectionFactory) getInitialContext().lookup("ConnectionFactory");
   }
   
   protected InitialContext getInitialContext() throws Exception
   {
      return InitialContextFactory.getInitialContext();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(MDBUnitTestCase.class,
            "mdbtransactionstest-service.xml, mdbtransactions-test.jar");
   }

}