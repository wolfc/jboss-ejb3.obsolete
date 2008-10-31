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
package org.jboss.ejb3.test.ejbthree1123;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

/**
 * MDB Base implementing MessageListener and a Dummy Interface
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public abstract class TestMDBBase implements MessageListener, DummyInterface
{
   // Class Members
   private static final Logger log = Logger.getLogger(TestMDBBase.class);
   
   // MDB Contract
   
   public void onMessage(Message message)
   {
      // Log
      try
      {
         log.info("Message received: " + ((TextMessage)message).getText());
      }
      catch (JMSException e)
      {
         throw new RuntimeException(e);
      }
      
      // Notify the remotely-exposed EJB that we've got the message
      TestStatelessBean.IS_MESSAGE_RECEIVED = true;
   }
}
