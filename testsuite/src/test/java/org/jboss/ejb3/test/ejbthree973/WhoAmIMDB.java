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
package org.jboss.ejb3.test.ejbthree973;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
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

import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * Report the caller principal.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@MessageDriven(activationConfig = {
      @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
      @ActivationConfigProperty(propertyName="destination", propertyValue="queue/whoAmI")
      })
@RunAs("Spy")
@SecurityDomain(value="", unauthenticatedPrincipal="anonymous")
public class WhoAmIMDB implements MessageListener
{
   @Resource
   private MessageDrivenContext ctx;
   
   @Resource(mappedName="java:/ConnectionFactory")
   private QueueConnectionFactory qFactory;
   
   public void onMessage(Message message)
   {
      try
      {
         try
         {
            sendOptionalReply(message, ctx.getCallerPrincipal().getName());
         }
         catch(IllegalStateException e)
         {
            e.printStackTrace();
            sendReplyOrThrow(message, e);
         }
      }
      catch(JMSException e)
      {
         throw new RuntimeException(e);
      }
   }

   private void sendOptionalReply(Message message, String replyText) throws JMSException
   {
      // ignore if no reply is wanted
      if(message.getJMSReplyTo() == null)
         return;
      
      sendReply((Queue) message.getJMSReplyTo(), message.getJMSCorrelationID(), replyText);
   }
   
   private void sendReply(Queue destination, String messageID, String text) throws JMSException
   {
      QueueConnection conn = qFactory.createQueueConnection();
      try
      {
         QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         QueueSender sender = session.createSender(destination);
         TextMessage message = session.createTextMessage(text);
         message.setJMSCorrelationID(messageID);
         sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 500);
      }
      finally
      {
         conn.close();
      }
   }
   
   private void sendReplyOrThrow(Message message, RuntimeException e) throws JMSException
   {
      if(message.getJMSReplyTo() == null)
         throw e;
      
      sendOptionalReply(message, e.getClass().getName());
   }
}
