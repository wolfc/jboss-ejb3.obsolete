/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.interceptor.bean;

import javax.ejb.MessageDriven;
import javax.ejb.AroundInvoke;
import javax.ejb.InvocationContext;
import javax.ejb.ActivationConfigProperty;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activateConfig =
        {
        @ActivationConfigProperty(name="destinationType", value="javax.jms.Queue"),
        @ActivationConfigProperty(name="destination", value="queue/tutorial/example")
        })
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
