package org.jboss.ejb3.interceptors.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

/**
 * The invocation context interceptor makes sure that invocation context
 * is available.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InvocationContextInterceptor
{
   public static final String INVOCATION_CONTEXT_KEY = InvocationContextInterceptor.class.getName() + ".invocationContext";

   public static InvocationContext getInvocationContext(Invocation invocation)
   {
      InvocationContext ctx = (InvocationContext) invocation.getResponseAttachment(INVOCATION_CONTEXT_KEY);
      if(ctx == null)
         throw new IllegalStateException("InvocationContextInterceptor must be in the interceptor chain");
      return ctx;
   }
   
   public String getName()
   {
      return "InvocationContextInterceptor";
   }

   public Object fillMethod(final Invocation invocation) throws Throwable
   {
      assert invocation instanceof MethodInvocation : "InvocationContextInterceptor.fillMethod only works on method invocation " + invocation;
      MethodInvocation mi = (MethodInvocation) invocation;
      InvocationContext ctx = getInvocationContext(invocation);
      ctx.setBusinessMethodInvocation(mi.getActualMethod(), mi.getArguments());
      return invocation.invokeNext();
   }
   
   /**
    * Setup an empty invocation context which can be used for lifecycle callbacks.
    * 
    * @param invocation
    * @return
    * @throws Throwable
    */
   public Object setup(final Invocation invocation) throws Throwable
   {
      InvocationContext ctx = new InvocationContext(invocation);
      invocation.addResponseAttachment(INVOCATION_CONTEXT_KEY, ctx);
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         // TODO: I can't remove the ctx, invalidate it somehow
         invocation.addResponseAttachment(INVOCATION_CONTEXT_KEY, null);
      }
   }

   private static class InvocationContext implements javax.interceptor.InvocationContext
   {
      private Invocation invocation;
      private Map<String, Object> contextData = new HashMap<String, Object>();
      private Method method = null;
      private Object params[] = null;
      
      private InvocationContext(Invocation invocation)
      {
         this.invocation = invocation;
         // Whether we're calling a business method or a lifecycle callback is beyond scope
         // here. This must be explicitly set via setBusinessMethodInvocation.
      }
      
      public Map<String, Object> getContextData()
      {
         return contextData;
      }

      public Method getMethod()
      {
         if(method == null)
            throw new IllegalStateException("Get method is not allowed on lifecycle callbacks (EJB 3 12)");
         return method;
      }

      public Object[] getParameters()
      {
         if(method == null)
            throw new IllegalStateException("Get parameters is not allowed on lifecycle callbacks (EJB 3 12)");
         return params;
      }

      public Object getTarget()
      {
         return invocation.getTargetObject();
      }

      public Object proceed() throws Exception
      {
         try
         {
            return invocation.invokeNext();
         }
         catch(Exception e)
         {
            throw e;
         }
         catch(Throwable t)
         {
            throw new RuntimeException(t);
         }
      }

      private void setBusinessMethodInvocation(Method method, Object params[])
      {
         this.method = method;
         this.params = params;
      }
      
      public void setParameters(Object[] params)
      {
         if(method == null)
            throw new IllegalStateException("Setting parameters is not allowed on lifecycle callbacks (EJB 3 12)");
         // TODO: might need more checks
         this.params = params;
         ((MethodInvocation) invocation).setArguments(params);
      }
   }
}
