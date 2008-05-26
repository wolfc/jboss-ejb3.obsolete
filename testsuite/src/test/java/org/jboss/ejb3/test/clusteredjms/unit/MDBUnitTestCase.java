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
package org.jboss.ejb3.test.clusteredjms.unit;

import java.util.Properties;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.ejb3.test.clusteredjms.TestStatus;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MDBUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(MDBUnitTestCase.class);

   public MDBUnitTestCase(String name)
   {
      super(name);
   }

   public void testQueue() throws Exception
   {

      TestStatus status = (TestStatus) getInitialContext().lookup(
            "TestStatusBean/remote");
      clear(status);
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup("queue/queuetest");
      QueueConnectionFactory factory = getQueueConnectionFactory();
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createSender(queue);
      sender.send(msg);
      sender.send(msg);
      sender.send(msg);
      sender.send(msg);
      sender.send(msg);
      session.close();
      cnn.close();

      Thread.sleep(2000);
      assertEquals(5, status.queueFired());
      
      assertEquals("queuetest", status.testInjection());
   }

   protected QueueConnectionFactory getQueueConnectionFactory()
         throws Exception
   {
      try
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "ConnectionFactory");
      } catch (NamingException e)
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "java:/ConnectionFactory");
      }
   }

   protected TopicConnectionFactory getTopicConnectionFactory()
         throws Exception
   {
      try
      {
         return (TopicConnectionFactory) getInitialContext().lookup(
               "ConnectionFactory");
      } catch (NamingException e)
      {
         return (TopicConnectionFactory) getInitialContext().lookup(
               "java:/ConnectionFactory");
      }
   }

   protected void clear(TestStatus status)
   {
      status.clear();
   }
   
   protected InitialContext getInitialContext() throws Exception
   {
      Properties env = new Properties();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      //env.put(Context.PROVIDER_URL, "localhost:1100");
      
      return new InitialContext(env);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(MDBUnitTestCase.class, "clusteredjms-test.jar");
   }

}