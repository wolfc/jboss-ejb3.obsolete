/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.interceptor.client;

import org.jboss.tutorial.interceptor.bean.EmailSystem;
import org.jboss.tutorial.interceptor.bean.EmailSystem;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      System.out.println("Starting");
      InitialContext ctx = new InitialContext();
      EmailSystem emailSystem = (EmailSystem)ctx.lookup(EmailSystem.class.getName());
      emailSystem.emailLostPassword("whatever");
      System.out.println("Done");
   }
}
