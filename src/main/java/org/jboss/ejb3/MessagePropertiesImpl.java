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
package org.jboss.annotation.ejb;

import java.lang.annotation.Annotation;
import java.io.Serializable;
import org.jboss.annotation.ejb.DeliveryMode;
import org.jboss.annotation.ejb.MessageProperties;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class MessagePropertiesImpl implements MessageProperties, Serializable
{   
   private static final long serialVersionUID = 4630480271844522009L;
   
   private DeliveryMode deliveryMode = DeliveryMode.PERSISTENT;
   private int ttl = 0;
   private int priority = 4;
   private Class interfac;

   public MessagePropertiesImpl(DeliveryMode deliveryMode, int ttl, int priority)
   {
      this.deliveryMode = deliveryMode;
      this.ttl = ttl;
      this.priority = priority;
   }

   public MessagePropertiesImpl(MessageProperties props)
   {
      deliveryMode = props.delivery();
      ttl = props.timeToLive();
      priority = props.priority();
   }
   
   public void setDelivery(DeliveryMode mode)
   {
      this.deliveryMode = mode;
   }
   
   public void setPriority(int priority)
   {
      this.priority = priority;
   }

   public MessagePropertiesImpl()
   {

   }

   public DeliveryMode delivery()
   {
      return deliveryMode;
   }

   public int timeToLive()
   {
      return ttl;
   }

   public int priority()
   {
      return priority;
   }
   
   public Class getInterface()
   {
      return interfac;
   }
   
   public void setInterface(Class interfac)
   {
      this.interfac = interfac;
   }

   public Class<? extends Annotation> annotationType()
   {
      return MessageProperties.class;
   }
}
