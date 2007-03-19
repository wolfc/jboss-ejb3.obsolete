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
package org.jboss.ejb3.test.dd.mdb.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.NamingException;
import org.jboss.ejb3.test.dd.mdb.CustomMessage;
import org.jboss.ejb3.test.dd.mdb.TestStatus;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Some simple tests of MDB. These could be much more elaborated.
 *
 * In the future at least the following tests should be done some how:
 * <ol>
 *   <li>Queue
 *   <li>Topic
 *   <li>Durable topic
 *   <li>Bean TX - with AUTO_ACK and DUPS_OK
 *   <li>CMT Required
 *   <li>CMT NotSupported
 *   <li>Selector
 *   <li>User and password login
 *   <li>Al the stuff with the context
 * </ol>
 *
 * <p>Created: Fri Dec 29 16:53:26 2000
 *
 * @author  <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version <pre>$Revision$</pre>
 */
public class MDBUnitTestCase
   extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(MDBUnitTestCase.class);

   // Provider specific
   static String TOPIC_FACTORY = "ConnectionFactory";
   static String QUEUE_FACTORY = "ConnectionFactory";

   QueueConnection queueConnection;
   TopicConnection topicConnection;
   
   static HashMap jndiToDestName = new HashMap();
   static
   {
      jndiToDestName.put("queue/testQueue", "QUEUE.testQueue");
      jndiToDestName.put("topic/testTopic", "TOPIC.testTopic");
      jndiToDestName.put("topic/testDurableTopic", "TOPIC.testDurableTopic");
      jndiToDestName.put("queue/ex", "QUEUE.ex");
      jndiToDestName.put("queue/A", "QUEUE.A");
      jndiToDestName.put("queue/B", "QUEUE.B");
   }
   

   // JMSProviderAdapter providerAdapter;

   String dest;

   public MDBUnitTestCase(String name, String dest) {
      super(name);
      this.dest = dest;
      // Get JMS JNDI Adapter
      // Class cls = Class.forName(providerAdapterClass);
      // providerAdapter = (JMSProviderAdapter)cls.newInstance();
      // This is not completly clean since it still have to use
      // provider specific queue and topic names!!
   }


   protected void tearDown() throws Exception {
      if (topicConnection != null) {
         topicConnection.close();
      }
      if (queueConnection != null) {
         queueConnection.close();
      }
   }

   protected void printHeader() {
      log.info("\n---- Testing method " + getName() +
                         " for destination " +dest);
   }

   private QueueSession getQueueSession() throws Exception {
      if (queueConnection == null) {
         QueueConnectionFactory queueFactory =
            (QueueConnectionFactory)getInitialContext().lookup(QUEUE_FACTORY);

         queueConnection = queueFactory.createQueueConnection();
      }
      return queueConnection.createQueueSession(false,
                                                Session.AUTO_ACKNOWLEDGE);
   }

   private TopicSession getTopicSession() throws Exception {
      if (topicConnection == null) {
         TopicConnectionFactory topicFactory =
            (TopicConnectionFactory)getInitialContext().lookup(TOPIC_FACTORY);
         topicConnection = topicFactory.createTopicConnection();
      }

      // No transaction & auto ack
      return topicConnection.createTopicSession(false,
                                                Session.AUTO_ACKNOWLEDGE);
   }

   /**
    * Test sending messages to Topic testTopic
    */
   public void testQueue() throws Exception {
      printHeader();
      TestStatus status = (TestStatus)getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      
      QueueSession session = getQueueSession();
      Queue queue = (Queue)getInitialContext().lookup(dest);
      QueueSender sender = session.createSender(queue);

      log.debug("TestQueue: " + dest + " Sending 10 messages 1-10");
      for (int i = 1; i < 11; i++) {
         TextMessage message = session.createTextMessage();
         message.setText("Queue Message " + dest + " nr " + i);
         sender.send(queue, message);
      }

      sender.close();
      
      Thread.currentThread().sleep(10000);
      
      ArrayList destinations = status.getDestinations();
      int size = (dest.equals("queue/ex")) ? 110 : 10;

      assertEquals(size, destinations.size());
      String destinationName = (String)jndiToDestName.get(dest);
      for (Iterator it = destinations.iterator() ; it.hasNext() ;)
      {
         assertEquals(destinationName, it.next());
      }
}

   /**
    * Test sending messages to Queue testQueue
    */
   public void testTopic() throws Exception {
      printHeader();
      TestStatus status = (TestStatus)getInitialContext().lookup("TestStatusBean/remote");
      status.clear();

      TopicSession session = getTopicSession();
      Topic topic = (Topic)getInitialContext().lookup(dest);
      TopicPublisher pub = session.createPublisher(topic);

      log.debug("TestTopic: " + dest +
                         ": Sending 10st messages 1-10");

      for (int i = 1; i < 11; i++) {
         TextMessage message = session.createTextMessage();
         message.setText("Topic Message " + dest + " nr " + i);
         pub.publish(topic, message);
      }

      pub.close();

      Thread.currentThread().sleep(10000);

      ArrayList destinations = status.getDestinations();
      
      assertEquals(10, destinations.size());
      String destinationName = (String)jndiToDestName.get(dest);
      for (Iterator it = destinations.iterator() ; it.hasNext() ;)
      {
         String name = (String)it.next();
         assertTrue("Destination name should start with '" + destinationName + "' it was '" + name + "'", name.startsWith(destinationName));
      }
     
}

   /**
    * Test sending messages to queue testObjectMessage
    */
   public void testObjectMessage() throws Exception {
      printHeader();
      QueueSession session = getQueueSession();
      // Non portable!!
      Queue queue = (Queue)getInitialContext().lookup("queue/testObjectMessage");
      QueueSender sender = session.createSender(queue);

      log.debug("TestQueue: Sending 10 messages 1-10");
      for (int i = 1; i < 11; i++) {
         ObjectMessage message = session.createObjectMessage();
         message.setObject(new CustomMessage(i));
         sender.send(queue, message);
      }

      sender.close();
      session.close();
   }


   public void testWaitForCompletion() throws Exception {
      try { Thread.currentThread().sleep(1000*20);
      } catch ( InterruptedException e ) {}
   }

   public void testNoQueueConstructionForAlreadyExists()
      throws Exception
   {
      try
      {
         getInitialContext().lookup("queue/QueueInADifferentContext");
      }
      catch (NamingException expected)
      {
         return;
      }
      fail("It should not create queue/QueueInADifferentContext");
   }

   public void testNoTopicConstructionForAlreadyExists()
      throws Exception
   {
      try
      {
         getInitialContext().lookup("topic/TopicInADifferentContext");
      }
      catch (NamingException expected)
      {
         return;
      }
      fail("It should not create topic/TopicInADifferentContext");
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new MDBUnitTestCase("testQueue","queue/testQueue"));
      suite.addTest(new MDBUnitTestCase("testTopic","topic/testTopic"));
      suite.addTest(new MDBUnitTestCase("testTopic","topic/testDurableTopic"));
      suite.addTest(new MDBUnitTestCase("testQueue","queue/ex"));
      suite.addTest(new MDBUnitTestCase("testQueue","queue/A"));
      suite.addTest(new MDBUnitTestCase("testWaitForCompletion",""));
      suite.addTest(new MDBUnitTestCase("testQueue","queue/B"));

     return new JBossTestSetup(getDeploySetup(suite, "dd-mdb-jbossmq-destinations-service.xml, dd-mdb-service.xml, dd-mdb.jar"));

   }
}



