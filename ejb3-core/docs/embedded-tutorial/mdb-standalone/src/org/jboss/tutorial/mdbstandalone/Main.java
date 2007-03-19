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
package org.jboss.tutorial.mdbstandalone;

import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Main
{
   public static void main(String[] args) throws Exception
   {
      EJB3StandaloneBootstrap.boot(null);
      // initialize JBoss MQ core services
      EJB3StandaloneBootstrap.deployXmlResource("jboss-jms-beans.xml");
      // initialize configured test queue and topic
      EJB3StandaloneBootstrap.deployXmlResource("testjms.xml");
      // scan classpath for ejbs and MDBs
      EJB3StandaloneBootstrap.scanClasspath();

      executeQueue();
      executeTopic();

      EJB3StandaloneBootstrap.shutdown();
   }

   private static void executeQueue()
           throws Exception
   {

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
      session.close();
      cnn.close();
   }

   private static void executeTopic()
           throws Exception
   {

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
      session.close();
      cnn.close();
   }

   public static InitialContext getInitialContext() throws Exception
   {
      Hashtable props = getInitialContextProperties();
      return new InitialContext(props);
   }

   private static Hashtable getInitialContextProperties()
   {
      Hashtable props = new Hashtable();
      props.put("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      return props;
   }
}
