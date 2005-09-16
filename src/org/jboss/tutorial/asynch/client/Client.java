/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.asynch.client;

import javax.naming.InitialContext;
import org.jboss.aspects.asynch.Future;
import org.jboss.ejb3.asynchronous.Asynch;
import org.jboss.tutorial.asynch.bean.Echo;


public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      Echo echo = (Echo)ctx.lookup(org.jboss.tutorial.asynch.bean.Echo.class.getName());
      System.out.println("-------- Synchronous call");
      String ret = echo.echo("normal call");
      System.out.println(ret);

      Echo asynchEcho = Asynch.getAsynchronousProxy(echo);
      System.out.println("-------- Asynchronous call");
      ret = asynchEcho.echo("asynchronous call");
      System.out.println("Direct return of async invocation is: " + ret);

      System.out.println("-------- Synchronous call");
      ret = echo.echo("normal call 2");
      System.out.println(ret);

      System.out.println("-------- Result of Asynchronous call");
      Future future = Asynch.getFutureResult(asynchEcho);

      System.out.println("Waiting for asynbch invocation to complete");
      while (!future.isDone())
      {
         Thread.sleep(100);
      }
      ret = (String)future.get();
      System.out.println(ret);


   }
}
