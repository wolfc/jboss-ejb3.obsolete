/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree973.unit;

import javax.ejb.EJBAccessException;
import javax.jms.DeliveryMode;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree973.SpyMe;
import org.jboss.ejb3.test.ejbthree973.WhoAmI;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * Test to see if we can get a caller principal without running in
 * a security domain.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class AnonymousCallerPrincipalTestCase extends JBossTestCase
{

   public AnonymousCallerPrincipalTestCase(String name)
   {
      super(name);
   }

   private WhoAmI lookupBean() throws Exception
   {
      return (WhoAmI) getInitialContext().lookup("WhoAmIBean/remote");
   }
   
   public void testAnonymous() throws Exception
   {
      WhoAmI bean = lookupBean();
      String actual = bean.getCallerPrincipal();
      // "anonymous" is defined in the @SecurityDomain on WhoAmIBean
      assertEquals("anonymous", actual);
   }
   
   public void testAnybody() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("anybody", null);
      client.login();
      try
      {
         WhoAmI bean = lookupBean();
         String actual = bean.getCallerPrincipal();
         // "anonymous" is defined in the @SecurityDomain on WhoAmIBean
         
         /** Anil Changed this to the real principal that made the call"**/
         //assertEquals("anonymous", actual);
         assertEquals("anybody", actual);
      }
      finally
      {
         client.logout();
      }
   }
   
   public void testMDB() throws Exception
   {
      QueueConnectionFactory qFactory = (QueueConnectionFactory) getInitialContext().lookup("ConnectionFactory");
      QueueConnection conn = qFactory.createQueueConnection();
      try
      {
         QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         
         Queue replyQueue = session.createTemporaryQueue();
         MessageConsumer consumer = session.createConsumer(replyQueue);
         conn.start();
         
         Queue sendQueue = (Queue) getInitialContext().lookup("queue/whoAmI");
         QueueSender sender = session.createSender(sendQueue);
         TextMessage message = session.createTextMessage("Hello world");
         message.setJMSReplyTo(replyQueue);
         sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 500);
         
         TextMessage reply = (TextMessage) consumer.receive(1000);
         assertNotNull("No reply received", reply);
         assertEquals("anonymous", reply.getText());
         
         conn.stop();
      }
      finally
      {
         conn.close();
      }
   }
   
   public void testSpy() throws Exception
   {
      SpyMe bean = (SpyMe) getInitialContext().lookup("RunAsSpyBean/remote");
      
      try
      {
         bean.notAllowed();
         fail("Calling notAllowed anonymously should not be allowed");
      }
      catch(EJBAccessException e)
      {
         // this is good
      }
      
      String actual = bean.getCallerPrincipal();
      // "anonymous" is defined in the @SecurityDomain on WhoAmIBean
      assertEquals("anonymous", actual);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(AnonymousCallerPrincipalTestCase.class, "ejbthree973test-service.xml,ejbthree973.jar");
   }
}
