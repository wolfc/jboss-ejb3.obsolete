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
package org.jboss.ejb3.test.strictpool.unit;

import javax.jms.*;
import javax.naming.InitialContext;
import org.jboss.ejb3.test.strictpool.MDBInvoker;
import org.jboss.ejb3.test.strictpool.SessionInvoker;
import org.jboss.ejb3.test.strictpool.StrictlyPooledSession;
import org.jboss.ejb3.test.strictpool.Counter;
import org.jboss.test.JBossTestCase;
import EDU.oswego.cs.dl.util.concurrent.CountDown;
import junit.framework.Test;


/**
 * Adapted from the EJB 2.1 tests (org.jboss.test.cts.test.StatefulSessionUnitTestCase and
 * org.jboss.test.cts.test.MDBUnitTestCase)
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class StrictPoolUnitTestCase
      extends JBossTestCase
{
   static final int MAX_SIZE = 20;
   static String QUEUE_FACTORY = "ConnectionFactory";


   public StrictPoolUnitTestCase(String name)
   {
      super(name);
   }
   public void testSession() throws Exception
   {
      System.out.println("*** testSession");
      CountDown done = new CountDown(MAX_SIZE);
      InitialContext ctx = new InitialContext();
      StrictlyPooledSession session =(StrictlyPooledSession)ctx.lookup("StrictlyPooledSessionBean/remote");
      SessionInvoker[] threads = new SessionInvoker[MAX_SIZE];
      for(int n = 0; n < MAX_SIZE; n ++)
      {
         SessionInvoker t = new SessionInvoker(n, done, session);
         threads[n] = t;
         t.start();
      }
      boolean ok = done.attempt(1500 * MAX_SIZE);
      super.assertTrue("Acquired done, remaining="+done.currentCount(), ok);

      for(int n = 0; n < MAX_SIZE; n ++)
      {
         SessionInvoker t = threads[n];
         if( t.runEx != null )
         {
            t.runEx.printStackTrace();
            System.err.println("SessionInvoker.runEx != null");
            t.runEx.printStackTrace();
            fail("SessionInvoker.runEx != null");
         }
      }
   }

   public void testMessageDriven() throws Exception
   {
      System.out.println("*** testMessageDriven");
      CountDown done = new CountDown(MAX_SIZE);
      InitialContext ctx = new InitialContext();
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup(QUEUE_FACTORY);
      QueueConnection queConn = factory.createQueueConnection();
      QueueSession session = queConn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
      Queue queueA = (Queue) ctx.lookup("queue/queueA");
      Queue queueB = (Queue) ctx.lookup("queue/queueB");
      queConn.start();
      MDBInvoker[] threads = new MDBInvoker[MAX_SIZE];
      for(int n = 0; n < MAX_SIZE; n ++)
      {
         MDBInvoker t = new MDBInvoker(session, queueA, queueB, n, done);
         threads[n] = t;
         t.start();
      }
      assertTrue("Acquired done", done.attempt(1500 * MAX_SIZE));
      session.close();
      queConn.close();

      for(int n = 0; n < MAX_SIZE; n ++)
      {
         MDBInvoker t = threads[n];
         if( t.runEx != null )
         {
            t.runEx.printStackTrace();
            fail("Inovker.runEx != null, msg="+t.runEx.getMessage());
         }
      }
   }
   public void testPoolTimeout() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ConnectionFactory factory = (ConnectionFactory)ctx.lookup("ConnectionFactory");
      Connection conn = factory.createConnection();
      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queueC = (Queue)ctx.lookup("queue/queueC");
      conn.start();
      MessageProducer sender = session.createProducer(queueC);
      TextMessage msg = session.createTextMessage("hello world");
      msg.setIntProperty("JMS_JBOSS_REDELIVERY_LIMIT", 20);
      sender.send(msg);
      // the second message will timeout 
      TextMessage msg2 = session.createTextMessage("hello world 2");
      msg2.setIntProperty("JMS_JBOSS_REDELIVERY_LIMIT", 20);
      sender.send(msg2);

      Thread.sleep(5000);
      Counter counter = (Counter)ctx.lookup("CounterBean/remote");
      assertEquals(1, counter.getCount());

   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(StrictPoolUnitTestCase.class, "strictpool_mdbtest-service.xml, strictpool-test.jar");

   }

}
