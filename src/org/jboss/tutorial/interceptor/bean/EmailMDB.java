/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.interceptor.bean;

import org.jboss.ejb3.mdb.ConnectionConfig;

import javax.ejb.MessageDriven;
import javax.ejb.AroundInvoke;
import javax.ejb.InvocationContext;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven
@ConnectionConfig(destinationType = javax.jms.Queue.class, destinationJndiName = "queue/tutorial/example", durable = true, subscriptionId = "queueExample")
public class EmailMDB implements MessageListener
{
   public void onMessage(Message recvMsg)
   {
      System.out.println("----------------");
      System.out.println("Got message, sending email");
      System.out.println("----------------");
      //Generate and save email
   }

   @AroundInvoke
   public Object mdbInterceptor(InvocationContext ctx) throws Exception
   {
      System.out.println("*** EmailMDB.mdbInterceptor intercepting");
      return ctx.proceed();
   }
}
