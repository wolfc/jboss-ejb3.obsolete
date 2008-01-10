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
package org.jboss.ejb3.test.standalone.embeddedjms.unit;

import java.util.Hashtable;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.test.standalone.embeddedjms.TestStatus;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * POJO Environment tests
 *
 * @author <a href="bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class EmbeddedJmsTestCase extends TestCase
{
   private static boolean booted = false;

   public EmbeddedJmsTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(EmbeddedJmsTestCase.class);

      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            startupEmbeddedJboss();
         }

         protected void tearDown()
         {
            shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.boot(null);
      EJB3StandaloneBootstrap.deployXmlResource("jboss-jms-beans.xml");
      EJB3StandaloneBootstrap.deployXmlResource("embeddedjms/testjms.xml");
      EJB3StandaloneBootstrap.scanClasspath("mdb.jar");
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }

   protected InitialContext getInitialContext() throws Exception
   {
      return new InitialContext(getInitialContextProperties());
   }

   protected Hashtable getInitialContextProperties()
   {
      return EJB3StandaloneBootstrap.getInitialContextProperties();
   }


   public void testArchivesByResource() throws Throwable
   {
      InitialContext ctx = getInitialContext();

      executeQueue(ctx);
      executeTopic(ctx);
      executeTemporaryQueue(ctx);
   }


   private void executeQueue(InitialContext ctx)
           throws Exception
   {

      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup("queue/mdbtest");
      QueueConnectionFactory factory = (QueueConnectionFactory) getInitialContext().lookup("java:/ConnectionFactory");
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createSender(queue);
      sender.send(msg);

      Thread.sleep(1000);
      assertTrue(status.queueFired());
   }
   
   private void executeTemporaryQueue(InitialContext ctx)
      throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;
      
      Queue queue = (Queue) getInitialContext().lookup("queue/tempmdbtest");
      QueueConnectionFactory factory = (QueueConnectionFactory) getInitialContext().lookup("java:/ConnectionFactory");
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
      
      TextMessage msg = session.createTextMessage("Hello World");
      
      sender = session.createSender(queue);
      sender.send(msg);
      
      Thread.sleep(1000);
      assertTrue(status.temporaryQueueFired());
   }

   private void executeTopic(InitialContext ctx)
           throws Exception
   {

      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      TopicConnection cnn = null;
      TopicPublisher sender = null;
      TopicSession session = null;

      Topic topic = (Topic) getInitialContext().lookup("topic/topictest");
      TopicConnectionFactory factory = (TopicConnectionFactory) getInitialContext().lookup("java:/ConnectionFactory");
      cnn = factory.createTopicConnection();
      session = cnn.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createPublisher(topic);
      sender.send(msg);

      Thread.sleep(1000);
      assertTrue(status.topicFired());
   }

   protected void configureLoggingAfterBootstrap()
   {
      //enableTrace("org.jboss.tm");
   }
}