/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.mdbtopic.unit;

import javax.jms.MessageProducer;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.ejb3.test.mdbtopic.TestStatus;
//import org.jboss.ejb3.test.mdb.TestStatus; // makes StackOverflowErrors
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 * 
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: MDBUnitTestCase.java 58403 2006-11-15 17:14:37 +0100 (Wed, 15 Nov 2006) wolfc $
 */
public class TopicMDBUnitTestCase extends JBossTestCase
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(TopicMDBUnitTestCase.class);

   static boolean deployed = false;

   static int test = 0;

   public TopicMDBUnitTestCase(String name)
   {
      super(name);
   }

   public void testTopic() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      clear(status);
      TopicConnection cnn = null;
      MessageProducer sender = null;
      TopicSession session = null;

      Topic topic = (Topic) getInitialContext().lookup("topic/mdbtest");
      TopicConnectionFactory factory = getTopicConnectionFactory();
      cnn = factory.createTopicConnection();
      session = cnn.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createProducer(topic);
      sender.send(msg);
      session.close();
      cnn.close();

      Thread.sleep(2000);
      assertEquals(1, status.topicFired());
      assertTrue(status.interceptedTopic());
      assertFalse(status.postConstruct());
      assertFalse(status.preDestroy());
   }

   private TopicConnectionFactory getTopicConnectionFactory()
         throws Exception
   {
      try
      {
         return (TopicConnectionFactory) getInitialContext().lookup("ConnectionFactory");
      }
      catch (NamingException e)
      {
         return (TopicConnectionFactory) getInitialContext().lookup("java:/ConnectionFactory");
      }
   }

   private void clear(TestStatus status)
   {
      status.clear();
      assertEquals(0, status.bmtQueueRan());
      assertEquals(0, status.defaultedQueueFired());
      assertEquals(0, status.messageCount());
      assertEquals(0, status.nondurableQueueFired());
      assertEquals(0, status.overrideDefaultedQueueFired());
      assertEquals(0, status.overrideQueueFired());
      assertEquals(0, status.queueFired());
      assertEquals(0, status.topicFired());
      assertFalse(status.interceptedQueue());
      assertFalse(status.interceptedTopic());
      assertFalse(status.postConstruct());
      assertFalse(status.preDestroy());
   }

//   protected InitialContext getInitialContext() throws Exception
//   {
//      return InitialContextFactory.getInitialContext();
//   }
//
   public static Test suite() throws Exception
   {
      return getDeploySetup(TopicMDBUnitTestCase.class, "mdbtopic-test.jar");
   }

}