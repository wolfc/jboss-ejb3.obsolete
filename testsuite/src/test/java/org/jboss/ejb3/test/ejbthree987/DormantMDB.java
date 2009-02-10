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
package org.jboss.ejb3.test.ejbthree987;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

/**
 * This MDB starts up dormant.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@MessageDriven(activationConfig =
{
   @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
   @ActivationConfigProperty(propertyName="destination", propertyValue="queue/ejbthree987"),
   @ActivationConfigProperty(propertyName="DeliveryActive", propertyValue="false")
})
public class DormantMDB implements MessageListener
{
   @Resource(mappedName="ConnectionFactory")
   private QueueConnectionFactory factory;
   
   private QueueConnection connection;
   private QueueSession session;
   private QueueSender sender;
   
   public void onMessage(Message message)
   {
      try
      {
         Message reply = session.createObjectMessage(new Date());
         sender.send(message.getJMSReplyTo(), reply);
      }
      catch (JMSException e)
      {
         throw new RuntimeException(e);
      }
   }

   @PostConstruct
   public void postConstruct()
   {
      try
      {
         connection = factory.createQueueConnection();
         session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         sender = session.createSender(null);
      }
      catch(JMSException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @PreDestroy
   public void preDestroy()
   {
      try
      {
         if(sender != null)
            sender.close();
         if(session != null)
            session.close();
         if(connection != null)
            connection.close();
      }
      catch(JMSException e)
      {
         throw new RuntimeException(e);
      }
   }
}
