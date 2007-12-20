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
package org.jboss.ejb3.test.ejbthree939;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

/**
 * This bean has a deployment descriptor which may override the
 * message listener interface.
 * 
 * Implements a dummy interface as well to fool MDB.getMessagingType()
 * because that made the bug slip past us.
 * 
 * Now that getMessagingType can't guess the correct interface, it must
 * use messageListenerInterface.
 * 
 * But this one wasn't set in the deployment descriptor and the one
 * from the annotation is ignored (/ not merged).
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@MessageDriven(activationConfig =
        {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="queue/ejbthree939")
        },
        messageListenerInterface=MessageListener.class)
public class MultiInterfaceMDB implements MessageListener, DummyInterface
{
   @Resource(mappedName="java:/ConnectionFactory")
   private QueueConnectionFactory qFactory;
   
   public void onMessage(Message message)
   {
      System.out.println("*** MultiInterfaceMDB onMessage");
      
      try
      {
         if(message.getJMSReplyTo() != null)
            sendReply((Queue) message.getJMSReplyTo(), message.getJMSMessageID());
      }
      catch(JMSException e)
      {
         throw new RuntimeException(e);
      }
   }

   private void sendReply(Queue destination, String messageID) throws JMSException
   {
      QueueConnection conn = qFactory.createQueueConnection();
      try
      {
         QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         QueueSender sender = session.createSender(destination);
         TextMessage message = session.createTextMessage("SUCCESS");
         message.setJMSCorrelationID(messageID);
         sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 500);
      }
      finally
      {
         conn.close();
      }
   }
}
