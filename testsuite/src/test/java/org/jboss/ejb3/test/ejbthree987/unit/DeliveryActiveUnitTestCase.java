/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree987.unit;

import java.util.Date;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DeliveryActiveUnitTestCase extends JBossTestCase
{
   public DeliveryActiveUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      serverFound();
   }
   
   public void test1() throws Exception
   {
      InitialContext ctx = new InitialContext();
      Queue queue = (Queue) ctx.lookup("queue/ejbthree987");
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection connection = factory.createQueueConnection();
      try
      {
         connection.start();
         
         QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         
         TemporaryQueue replyQueue = session.createTemporaryQueue();
         QueueReceiver receiver = session.createReceiver(replyQueue);
         
         QueueSender sender = session.createSender(queue);
         TextMessage message = session.createTextMessage("Hello world");
         message.setJMSReplyTo(replyQueue);
         sender.send(message);
         sender.close();
         
         long whenSent = System.currentTimeMillis();

         MBeanServerConnection server = getServer();
         ObjectName name = new ObjectName("jboss.j2ee:jar=ejbthree987.jar,name=DormantMDB,service=EJB3");
         assertFalse((Boolean) server.getAttribute(name, "DeliveryActive"));

         Message msg = receiver.receive(5000);
         assertNull(msg);
         
         server.invoke(name, "startDelivery", null, null);
         assertTrue((Boolean) server.getAttribute(name, "DeliveryActive"));
         
         msg = receiver.receive(5000);
         assertNotNull(msg);
         
         Date date = (Date) ((ObjectMessage) msg).getObject();
         long whenProcessed = date.getTime();
         
         // The message should have been processed after we started delivery
         assertTrue((whenProcessed - whenSent) > 4000);
         
         receiver.close();
         session.close();
      }
      finally
      {
         connection.close();
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(DeliveryActiveUnitTestCase.class, "ejbthree987.jar");
   }
}
