/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.interceptor.bean;

import javax.ejb.AroundInvoke;
import javax.ejb.InvocationContext;

public class OtherInterceptor
{
   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("*** OtherInterceptor intercepting");
      return ctx.proceed();
   }

}
