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
package org.jboss.ejb3.test.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.ejb3.annotation.Pool;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@MessageDriven(activationConfig =
        {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="queue/mismatchedmdbtest"),
        @ActivationConfigProperty(propertyName="maxSession", propertyValue="100")
        })
@Pool(value="StrictMaxPool", maxSize=1, timeout=10000)
public class MismatchedQueueMDB
implements MessageListener
{
   private static final Logger log = Logger.getLogger(MismatchedQueueMDB.class);
   
   private int count = 0;
   
   public void onMessage(Message recvMsg)
   {
      ++TestStatusBean.mismatchedQueueRan;
      ++count;
      
      try
      {
         System.out.println("+++ MismatchedQueueMDB onMessage " + TestStatusBean.mismatchedQueueRan + " " + count + " " + ((TextMessage)recvMsg).getText());
      }
      catch (Exception e){}
   }
}
