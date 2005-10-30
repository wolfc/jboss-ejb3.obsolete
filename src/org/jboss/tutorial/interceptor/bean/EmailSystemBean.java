/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.tutorial.interceptor.bean;

import javax.ejb.AroundInvoke;
import javax.ejb.Interceptors;
import javax.ejb.InvocationContext;
import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

@Stateless
@Interceptors ({TracingInterceptor.class, OtherInterceptor.class})
@Remote(EmailSystem.class)
public class EmailSystemBean implements EmailSystem
{
   public void emailLostPassword(String username)
   {
      System.out.println("----------------");
      System.out.println("In EmailSystemBean business method");
      System.out.println("----------------");
      //Pretend we are looking up password and email, and place a message on the queue
      String password = "xyz";
      String email = "xyz@lalala.com";
      sendMessage(username, password, email);
   }

   @AroundInvoke
   public Object myBeanInterceptor(InvocationContext ctx) throws Exception
   {
      if (ctx.getMethod().getName().equals("emailLostPassword"))
      {
         System.out.println("*** EmailSystemBean.myBeanInterceptor - username: " + ctx.getParameters()[0]);
      }

      return ctx.proceed();
   }

   private void sendMessage(String username, String pwd, String email)
   {
      try
      {
         QueueConnection cnn = null;
         QueueSender sender = null;
         QueueSession session = null;
         InitialContext ctx = new InitialContext();
         Queue queue = (Queue) ctx.lookup("queue/tutorial/example");
         QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
         cnn = factory.createQueueConnection();
         session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

         TextMessage msg = session.createTextMessage(username + ":" + pwd + ":" + email);

         sender = session.createSender(queue);
         sender.send(msg);
         System.out.println("Message sent successfully to remote queue.");
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
