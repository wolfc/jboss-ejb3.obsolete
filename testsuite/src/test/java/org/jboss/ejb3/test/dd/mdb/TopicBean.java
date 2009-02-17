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
package org.jboss.ejb3.test.dd.mdb;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.logging.Logger;

/**
 * MessageBeanImpl.java
 *
 *
 * Created: Sat Nov 25 18:07:50 2000
 *
 * @author 
 * @version
 */

public class TopicBean implements MessageDrivenBean, MessageListener
{
   private static final Logger log = Logger.getLogger(TopicBean.class);

   private MessageDrivenContext ctx = null;

   public TopicBean()
   {

   }

   //@Resource //@see EJBTHREE-1633
   public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException
   {
      this.ctx = ctx;
   }

   public void ejbCreate()
   {
   }

   public void ejbRemove()
   {
      ctx = null;
   }

   public void onMessage(Message message)
   {
      if(ctx==null)
      {
         throw new IllegalStateException("EJBTHREE-1633, Missing setMessageDrivenContext");
      }
      if (ctx.getRollbackOnly())
         throw new IllegalStateException("Error in transaction");
      log.debug("DEBUG: TopicBean got message" + message.toString());
      try
      {
         TestStatusBean.addDestination(message.getJMSDestination());
      }
      catch(JMSException e)
      {
         e.printStackTrace();
      }

   }
} // MessageBeanImpl

