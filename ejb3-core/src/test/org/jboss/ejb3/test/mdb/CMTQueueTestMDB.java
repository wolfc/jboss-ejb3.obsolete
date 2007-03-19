/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.mdb;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@MessageDriven(activationConfig =
   {
      @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
      @ActivationConfigProperty(propertyName="destination", propertyValue="queue/cmtmdbtest"),
      @ActivationConfigProperty(propertyName="DLQMaxResent", propertyValue="1")
   })
@RunAs("TestRole")
@SecurityDomain("other")
public class CMTQueueTestMDB implements MessageListener
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(CMTQueueTestMDB.class);
   
   private static boolean thrownException = false;
   @Resource MessageDrivenContext ctx;

   public void onMessage(Message recvMsg)
   {
      System.out.println("*** CMTQueueTestMDB onMessage " + thrownException);
      if (ctx == null) throw new RuntimeException("FAILED ON CTX LOOKUP");
      
      if (!thrownException)
      {
         thrownException = true;
         throw new RuntimeException();
      }
      
      TestStatusBean.cmtQueueRan++;
   }
}
