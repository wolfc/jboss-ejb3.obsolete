/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.asynch.client;

import org.jboss.tutorial.asynch.bean.Echo;
import org.jboss.ejb3.JBossProxy;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.asynch.Future;

import javax.naming.InitialContext;


public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      Echo echo = (Echo)ctx.lookup(org.jboss.tutorial.asynch.bean.Echo.class.getName());
      System.out.println("-------- Synchronous call");
      String ret = echo.echo("normal call");
      System.out.println(ret);

      JBossProxy proxy = (JBossProxy)echo;
      Echo asynchEcho = (Echo)proxy.getAsynchronousProxy();
      System.out.println("-------- Asynchronous call");
      ret = asynchEcho.echo("asynchronous call");
      System.out.println("Direct return of async invocation is: " + ret);

      System.out.println("-------- Synchronous call");
      ret = echo.echo("normal call 2");
      System.out.println(ret);

      System.out.println("-------- Result of Asynchronous call");
      AsynchProvider provider = (AsynchProvider)asynchEcho;
      Future future = provider.getFuture();

      System.out.println("Waiting for asynbch invocation to complete");
      while (!future.isDone())
      {
         Thread.sleep(100);
      }
      ret = (String)future.get();
      System.out.println(ret);


   }
}
