/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.interceptor.bean;

import javax.ejb.AroundInvoke;
import javax.ejb.InvocationContext;

public class TracingInterceptor {

   @AroundInvoke
   public Object log(InvocationContext ctx) throws Exception
   {
      System.out.println("*** TracingInterceptor intercepting");
      long start = System.currentTimeMillis();
      try
      {
         return ctx.proceed();
      }
      catch(Exception e)
      {
         throw e;
      }
      finally
      {
         long time = System.currentTimeMillis() - start;
         String method = ctx.getBean().getClass().getName() + "." + ctx.getMethod().getName() + "()";
         System.out.println("*** TracingInterceptor invocation of " + method + " took " + time + "ms");
      }
   }
}
