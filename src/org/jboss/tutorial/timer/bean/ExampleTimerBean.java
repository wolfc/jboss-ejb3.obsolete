/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.timer.bean;

import javax.ejb.Inject;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timer;

import java.util.Date;

@Stateless
public class ExampleTimerBean implements ExampleTimer
{
   private @Inject SessionContext ctx;

   public void scheduleTimer(long milliseconds)
   {
      ctx.getTimerService().createTimer(new Date(new Date().getTime() + milliseconds), "Hello World");
   }

   public void ejbTimeout(Timer timer)
   {
      System.out.println("---------------------");
      System.out.println("* Received Timer event: " + timer.getInfo());
      System.out.println("---------------------");

      timer.cancel();
   }
}
