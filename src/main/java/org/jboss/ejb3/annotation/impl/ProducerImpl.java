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
package org.jboss.ejb3.mdb;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import org.jboss.annotation.ejb.Producer;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class ProducerImpl implements Producer, Serializable
{
   private static final long serialVersionUID = -5029440272889667348L;
   
   private String connectionFactory = "";
   private boolean transacted = false;
   private int acknowledgeMode = 1;
   private Class producer = null;
   
   public ProducerImpl(Class producer)
   {
      this.producer = producer;
   }

   public ProducerImpl(String connectionFactory, boolean transacted, int acknowledgeMode)
   {
      this.connectionFactory = connectionFactory;
      this.transacted = transacted;
      this.acknowledgeMode = acknowledgeMode;
   }

   public ProducerImpl(Producer producer)
   {
      connectionFactory = producer.connectionFactory();
      transacted = producer.transacted();
      acknowledgeMode = producer.acknowledgeMode();
   }
   
   public Class producer()
   {
      return producer;
   }

   public String connectionFactory()
   {
      return connectionFactory;
   }
   
   public void setConnectionFactory(String connectionFactory)
   {
      this.connectionFactory = connectionFactory;
   }

   public boolean transacted()
   {
      return transacted;
   }

   public int acknowledgeMode()
   {
      return acknowledgeMode;
   }

   public Class annotationType()
   {
      return Producer.class;
   }
}
