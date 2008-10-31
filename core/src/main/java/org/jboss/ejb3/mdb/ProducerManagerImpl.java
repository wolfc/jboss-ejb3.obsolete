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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.annotation.DeliveryMode;
import org.jboss.ejb3.annotation.MessageProperties;
import org.jboss.logging.Logger;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class ProducerManagerImpl implements ProducerManager, Externalizable, Interceptor
{
   private static final long serialVersionUID = -3096930718616437880L;

   private static final Logger log = Logger.getLogger(ProducerManagerImpl.class);
   
   private static final int PERSISTENT = javax.jms.DeliveryMode.PERSISTENT;
   private static final int NON_PERSISTENT = javax.jms.DeliveryMode.NON_PERSISTENT;
   
   protected Destination destination;
   protected String factoryLookup;

   protected int deliveryMode = javax.jms.DeliveryMode.PERSISTENT;
   protected int timeToLive = 0;
   protected int priority = 4;
   protected HashMap methodMap;

   protected transient ConnectionFactory factory;
   protected transient Connection connection;
   protected transient Session session;
   protected transient MessageProducer msgProducer;
   protected transient String username;
   protected transient String password;
   protected transient InitialContext initialContext;
   protected Hashtable initialContextProperties;
   
   protected boolean transacted;
   protected int acknowledgeMode;

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeObject(destination);
      out.writeObject(factoryLookup);
      out.writeInt(deliveryMode);
      out.writeInt(timeToLive);
      out.writeInt(priority);
      out.writeObject(methodMap);
      out.writeObject(initialContextProperties);
      if (factoryLookup == null)
      {
         out.writeObject(factory);
      }
      out.writeBoolean(this.transacted);
      out.writeInt(this.acknowledgeMode);
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      destination = (Destination) in.readObject();
      factoryLookup = (String) in.readObject(); 
      deliveryMode = in.readInt();
      timeToLive = in.readInt();
      priority = in.readInt();
      methodMap = (HashMap) in.readObject();
      initialContextProperties = (Hashtable)in.readObject();
      try
      {
         initialContext = InitialContextFactory.getInitialContext(initialContextProperties);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      if (factoryLookup != null)
      {
         try
         {
            factory = (ConnectionFactory) initialContext.lookup(factoryLookup);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         factory = (ConnectionFactory) in.readObject();
      }
      this.transacted = in.readBoolean();
      this.acknowledgeMode = in.readInt();
   }

   public ProducerManagerImpl(Destination destination, ConnectionFactory factory, DeliveryMode deliveryMode,
         int timeToLive, int priority, HashMap methodMap, Hashtable initialContextProperties, boolean transacted,
         int acknowledgeMode)
   {
      this.init(destination, deliveryMode, timeToLive, priority, methodMap, initialContextProperties, transacted,
            acknowledgeMode);
      this.factory = factory;
   }

   public ProducerManagerImpl(Destination destination, String factory, DeliveryMode deliveryMode, int timeToLive,
         int priority, HashMap methodMap, Hashtable initialContextProperties, boolean transacted, int acknowledgeMode)
   {
      this.init(destination, deliveryMode, timeToLive, priority, methodMap, initialContextProperties, transacted,
            acknowledgeMode);
      this.factoryLookup = factory;
   }

   private void init(Destination destination, DeliveryMode deliveryMode, int timeToLive, int priority,
         HashMap methodMap, Hashtable initialContextProperties, boolean transacted, int acknowledgeMode)
   {

      this.initialContextProperties = initialContextProperties;
      try
      {
         this.initialContext = InitialContextFactory.getInitialContext(initialContextProperties);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      this.destination = destination;

      int mode = deliveryMode.ordinal();
      switch (mode)
      {
         case PERSISTENT :
            this.deliveryMode = javax.jms.DeliveryMode.PERSISTENT;
            break;
         case NON_PERSISTENT :
            this.deliveryMode = javax.jms.DeliveryMode.NON_PERSISTENT;
            break;
      }
      this.timeToLive = timeToLive;
      this.priority = priority;
      this.methodMap = methodMap;
      this.transacted = transacted;
      this.acknowledgeMode = acknowledgeMode;
   }


   public ProducerManagerImpl()
   {
   }

   public void setUsername(String user)
   {
      this.username = user;
   }

   public void setPassword(String passwd)
   {
      this.password = passwd;
   }

   public void connect() throws JMSException
   {
      if (factory == null)
      {
         try
         {
            factory = (ConnectionFactory) initialContext.lookup(factoryLookup);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
      }
      if (connection != null) return;
      if (username != null)
      {
         connection = factory.createConnection(username, password);
      }
      else
      {
         connection = factory.createConnection();
      }
      session = connection.createSession(this.transacted, this.acknowledgeMode);
      msgProducer = session.createProducer(destination);
      msgProducer.setDeliveryMode(deliveryMode);
      msgProducer.setTimeToLive(timeToLive);
      msgProducer.setPriority(priority);
   }

   public void close() throws JMSException
   {
      msgProducer.close();
      msgProducer = null;
      session.close();
      session = null;
      connection.close();
      connection = null;
   }

   public void commit() throws JMSException
   {
      session.commit();
   }

   public void rollback() throws JMSException
   {
      session.rollback();
   }

   public String getName()
   {
      return ProducerManager.class.getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      if (session == null)
      {
         throw new RuntimeException("You must call connect() on the producer.  The JMS session has not been set");
      }
      ObjectMessage msg = session.createObjectMessage((Serializable) invocation);
//      MethodInvocation mi = (MethodInvocation) invocation;
//      MessageProperties props = (MessageProperties)methodMap.get(new Long(mi.getMethodHash()));
//      if (props != null)
//      {
//         int del = (props.delivery() == DeliveryMode.PERSISTENT) ? javax.jms.DeliveryMode.PERSISTENT : javax.jms.DeliveryMode.NON_PERSISTENT;
         msgProducer.send(msg, this.deliveryMode, this.priority, this.timeToLive);
//      }
//      else
//      {
//         msgProducer.send(msg);
//      }
      return null;
   }
}
