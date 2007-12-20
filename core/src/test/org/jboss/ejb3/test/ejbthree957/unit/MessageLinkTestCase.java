/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree957.unit;

import javax.jms.DeliveryMode;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * Test message link.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MessageLinkTestCase extends JBossTestCase
{

   public MessageLinkTestCase(String name)
   {
      super(name);
   }

   public void test1() throws Exception
   {
      QueueConnectionFactory qFactory = (QueueConnectionFactory) getInitialContext().lookup("ConnectionFactory");
      QueueConnection conn = qFactory.createQueueConnection();
      try
      {
         QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         
         Queue replyQueue = (Queue) getInitialContext().lookup("queue/ejbthree957Out");
         MessageConsumer consumer = session.createConsumer(replyQueue);
         conn.start();
         
         Queue sendQueue = (Queue) getInitialContext().lookup("queue/ejbthree957In");
         QueueSender sender = session.createSender(sendQueue);
         TextMessage message = session.createTextMessage("Hello world");
         sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 500);
         
         TextMessage reply = (TextMessage) consumer.receive(5000);
         assertNotNull("No reply received", reply);
         assertEquals("Hello world", reply.getText());
         
         conn.stop();
      }
      finally
      {
         conn.close();
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(MessageLinkTestCase.class, "ejbthree957test-service.xml,ejbthree957.ear");
   }

}
