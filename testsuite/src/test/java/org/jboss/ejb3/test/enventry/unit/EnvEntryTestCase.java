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
package org.jboss.ejb3.test.enventry.unit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.jboss.ejb3.test.enventry.TestEnvEntry;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
public class EnvEntryTestCase extends JBossTestCase
{
  
   public EnvEntryTestCase(String name)
   {
      super(name);
   }

   public void testEnvEntries() throws Exception
   {
      TestEnvEntry test = (TestEnvEntry) getInitialContext().lookup(TestEnvEntry.JNDI_NAME);
      assertNotNull(test);
      
      int maxExceptions = test.getMaxExceptions();
      assertEquals(15, maxExceptions);
      
      int minExceptions = test.getMinExceptions();
      assertEquals(5, minExceptions);
      
      int numExceptions = test.getNumExceptions();
      assertEquals(10, numExceptions);
      
      TestEnvEntry etest = (TestEnvEntry) getInitialContext().lookup("ExtendedTestEnvEntryJndiName");
      assertNotNull(etest);
      
      maxExceptions = etest.getMaxExceptions();
      assertEquals(14, maxExceptions);
      
      minExceptions = etest.getMinExceptions();
      assertEquals(6, minExceptions);
      
      numExceptions = etest.getNumExceptions();
      assertEquals(11, numExceptions);
   }

   public void testEnvEntriesMDB() throws Exception {
      InitialContext ctx = getInitialContext();

      ConnectionFactory factory = (ConnectionFactory)ctx.lookup("ConnectionFactory");
      Connection con = factory.createConnection();
      try {
         Destination dest = (Destination) ctx.lookup("queue/testEnvEntry");
         
         Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(dest);
         
         Queue replyQueue = session.createTemporaryQueue();
         MessageConsumer consumer = session.createConsumer(replyQueue);
         
         con.start();
         
         TextMessage msg = session.createTextMessage();
         msg.setJMSReplyTo(replyQueue);
         msg.setText("This is message one");
         producer.send(msg);
         
         MapMessage replyMsg = (MapMessage) consumer.receive(5000);
         assertNotNull(replyMsg);
         assertEquals(16, replyMsg.getInt("maxExceptions"));
         assertEquals(12, replyMsg.getInt("numExceptions"));
         assertEquals(7, replyMsg.getInt("minExceptions"));
      }
      finally {
         con.close();
      }
   }

   public void testJNDI() throws Exception
   {
      TestEnvEntry test = (TestEnvEntry) getInitialContext().lookup(TestEnvEntry.JNDI_NAME);
      assertNotNull(test);
      
      assertEquals(15, test.checkJNDI());
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EnvEntryTestCase.class, "enventrytest-service.xml,enventry.jar");
   }

}
