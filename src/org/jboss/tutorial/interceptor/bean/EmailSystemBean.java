/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.interceptor.bean;

import org.jboss.tutorial.interceptor.bean.EmailSystem;

import javax.ejb.*;
import javax.jms.*;
import javax.naming.InitialContext;

@Stateless
@Interceptors ({@Interceptor(TracingInterceptor.class), @Interceptor(OtherInterceptor.class)})
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
