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

import javax.jms.JMSException;

/**
 * Helper class for setting all @Producer settings
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class ProducerConfig
{
   public static void setUsername(Object producer, String user)
   {
      ProducerManager manager = ((ProducerObject) producer).getProducerManager();
      manager.setUsername(user);
   }

   public static void setPassword(Object producer, String passwd)
   {
      ProducerManager manager = ((ProducerObject) producer).getProducerManager();
      manager.setPassword(passwd);
   }


   public static void connect(Object producer) throws JMSException
   {
      ProducerManager manager = ((ProducerObject) producer).getProducerManager();
      manager.connect();
   }

   public static void close(Object producer) throws JMSException
   {
      ProducerManager manager = ((ProducerObject) producer).getProducerManager();
      manager.close();
   }


   public static void commit(Object producer) throws JMSException
   {
      ProducerManager manager = ((ProducerObject) producer).getProducerManager();
      manager.commit();
   }

   public static void rollback(Object producer) throws JMSException
   {
      ProducerManager manager = ((ProducerObject) producer).getProducerManager();
      manager.rollback();
   }
}
