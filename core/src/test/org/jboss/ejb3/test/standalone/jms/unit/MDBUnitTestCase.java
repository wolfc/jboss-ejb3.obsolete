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
package org.jboss.ejb3.test.standalone.jms.unit;

import java.util.Hashtable;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.test.standalone.jms.TestStatus;
import org.jboss.test.JBossTestCase;

/**
 * POJO Environment tests
 * 
 * @author <a href="bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class MDBUnitTestCase extends JBossTestCase
{
   private static boolean booted = false;

   public MDBUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      // set bad properties to make sure that we're injecting InitialContext correct
//      System.setProperty("java.naming.factory.initial", "ERROR");
//      System.setProperty("java.naming.factory.url.pkgs", "ERROR");

      super.setUp();
      BasicConfigurator.resetConfiguration();
      PatternLayout layout = new PatternLayout("%r %-5p [%c{1}] %m%n");
      ConsoleAppender appender = new ConsoleAppender(layout);
      BasicConfigurator.configure(appender);
      String file = System.getProperty("org.jboss.test.logfile");
      if (file != null)
      {
         FileAppender fileAppender = new FileAppender(layout, file);
         BasicConfigurator.configure(fileAppender);
      }
      long start = System.currentTimeMillis();
      log.info("Starting");
      try
      {
         if (!booted)
         {
            booted = true;
            EJB3StandaloneBootstrap.boot("");
            EJB3StandaloneBootstrap.scanClasspath("jms.jar");
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
      log.info("setup took: " + (System.currentTimeMillis() - start));
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      EJB3StandaloneBootstrap.shutdown();
   }
   

   protected InitialContext getLocalInitialContext() throws Exception
   {
      return new InitialContext(getLocalInitialContextProperties());
   }

   protected Hashtable getLocalInitialContextProperties()
   {
      return EJB3StandaloneBootstrap.getInitialContextProperties();
   }

   public void testQueue() throws Throwable
   {
      InitialContext ctx = getLocalInitialContext();

      executeEJBs(ctx);
   }

   private void executeEJBs(InitialContext ctx)
           throws Exception
   {
      TestStatus status = (TestStatus) getLocalInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup("queue/mdbtest");
      QueueConnectionFactory factory = (QueueConnectionFactory) getInitialContext().lookup("ConnectionFactory");
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createSender(queue);
      sender.send(msg);

      Thread.sleep(1000);
      assertTrue(status.queueFired());
   }

   protected void configureLoggingAfterBootstrap()
   {
      //enableTrace("org.jboss.tm");
   }
 /*
   public static Test suite() throws Exception
   {
      return getDeploySetup(MDBUnitTestCase.class, "mdbtest-service.xml");
   }
   */
}