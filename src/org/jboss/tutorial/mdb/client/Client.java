/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.mdb.client;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;
      InitialContext ctx = new InitialContext();
      Queue queue = (Queue) ctx.lookup("queue/tutorial/example");
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createSender(queue);
      sender.send(msg);
      System.out.println("Message sent successfully to remote queue.");

   }
}
