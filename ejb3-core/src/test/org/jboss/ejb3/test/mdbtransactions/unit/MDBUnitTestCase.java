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

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.ObjectMessage;

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

   public void testMdbTransactions() throws Exception
   {
      TestStatus status = (TestStatus)getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      
      sendMessages("queue/mdbtest", 2);
      
      Thread.sleep(5000);
      
      assertEquals(1, status.messageCount());
      assertTrue(status.caughtRollback());
   }
   
   protected void sendMessages(String queueName, int numMessages) throws Exception
   {
      QueueConnection connection = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup(
            queueName);
      QueueConnectionFactory factory = getQueueConnectionFactory();
      connection = factory.createQueueConnection();
      connection.start();
      session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
      
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

      sender = session.createSender(queue);
      
      for (int i = 0 ; i < numMessages ; ++i)
         sender.send(msg);
 
      session.close();
      connection.close();
   }
   
   public void testRollback() throws Exception
   {
      TestStatus status = (TestStatus)getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      
      sendMessages("queue/rollbackmdbtest", 1);
      
      Thread.sleep(5000);
      
      Queue queue = (Queue) getInitialContext().lookup("queue/DLQ");
      QueueConnectionFactory factory = getQueueConnectionFactory();
      QueueConnection connection = factory.createQueueConnection();
      connection.start();
      QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
      QueueReceiver receiver = session.createReceiver(queue);
      Message message = receiver.receiveNoWait();
      assertNotNull(message);
      
      session.close();
      connection.close();
   }
   
   protected QueueConnectionFactory getQueueConnectionFactory()
      throws Exception
   {
      try
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "ConnectionFactory");
      } catch (NamingException e)
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "java:/ConnectionFactory");
      }
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