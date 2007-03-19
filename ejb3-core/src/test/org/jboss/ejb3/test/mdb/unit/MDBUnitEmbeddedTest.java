/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.mdb.unit;

import org.jboss.test.JBossTestCase;
import org.jboss.logging.Logger;
import org.jboss.ejb3.ClientKernelAbstraction;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.test.mdb.TestStatus;
import org.jboss.ejb3.test.mdb.Stateless;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.util.collection.CollectionsUtil;
import org.jboss.embedded.junit.EmbeddedTestCase;
import org.jboss.embedded.Bootstrap;

import javax.management.ObjectName;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.jms.DeliveryMode;
import javax.jms.TopicConnection;
import javax.jms.MessageProducer;
import javax.jms.TopicSession;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueBrowser;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Enumeration;

import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: MDBUnitTestCase.java 59422 2007-01-08 23:18:40Z bdecoste $
 */
public class MDBUnitEmbeddedTest extends JBossTestCase
{
   public MDBUnitEmbeddedTest(String name)
   {

      super(name);

   }

   protected void setSecurity(String user, String password)
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal(user));
      SecurityAssociation.setCredential(password.toCharArray());

      InitialContextFactory.setSecurity(user, password);
   }

   protected void clear(TestStatus status)
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



   public void testQueue() throws Exception
   {
      setSecurity("anyone", "password");

      TestStatus status = (TestStatus) getInitialContext().lookup(
            "TestStatusBean/remote");
      clear(status);
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup("queue/mdbtest");
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
      assertTrue(status.interceptedQueue());
      assertTrue(status.postConstruct());
      assertEquals(5, status.messageCount());

      // TODO: Figure out how to test preDestroy gets invoked
      // assertTrue(status.preDestroy());

      setSecurity("anyone", "password");
      Stateless stateless = (Stateless) getInitialContext().lookup("Stateless");
      assertNotNull(stateless);
      String state = stateless.getState();
      assertEquals("Set", state);
      Bootstrap.getInstance().setIgnoreShutdownErrors(true);
   }

   public static Test suite() throws Exception
   {
      return EmbeddedTestCase.getAdaptedSetup(MDBUnitEmbeddedTest.class,
            "mdbtest-service.xml, mdb-test.jar");
   }

}
    