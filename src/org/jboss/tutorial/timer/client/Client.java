/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.timer.client;

import org.jboss.tutorial.timer.bean.ExampleTimer;

import javax.naming.InitialContext;


public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      ExampleTimer timer = (ExampleTimer) ctx.lookup(ExampleTimer.class.getName());
      timer.scheduleTimer(5000);
   }
}
