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
package org.jboss.ejb3.sandbox.interceptorcontainer.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.logging.Logger;

/**
 * This is the runtime representation of ContainerInterceptors
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ContainerInterceptorsExecutor
{
   private static final Logger log = Logger.getLogger(ContainerInterceptorsExecutor.class);
   
   private List<InterceptorInvoker> preDestroys = new ArrayList<InterceptorInvoker>();
   private List<InterceptorInvoker> postConstructs = new ArrayList<InterceptorInvoker>();
   private List<InterceptorInvoker> classInterceptors = new ArrayList<InterceptorInvoker>();
   
   private static class SubInvocationContext implements InvocationContext
   {
      private InvocationContext original;
      private Iterator<InterceptorInvoker> it;
      
      protected SubInvocationContext(InvocationContext original, List<InterceptorInvoker> interceptors)
      {
         assert original != null : "original is null";
         assert interceptors != null : "interceptors is null";
         
         this.original = original;
         this.it = interceptors.iterator();
      }
      
      public Map<String, Object> getContextData()
      {
         return original.getContextData();
      }

      public Method getMethod()
      {
         return original.getMethod();
      }

      public Object[] getParameters()
      {
         return original.getParameters();
      }

      public Object getTarget()
      {
         return original.getTarget();
      }

      public Object proceed() throws Exception
      {
         if(!it.hasNext())
            return original.proceed();
         InterceptorInvoker interceptor = it.next();
         return interceptor.invoke(this);
      }

      public void setParameters(Object[] params)
      {
         original.setParameters(params);
      }
      
      @Override
      public String toString()
      {
         return original.toString();
      }
   }
   
   protected ContainerInterceptorsExecutor(InterceptorContainer container, Class<?> interceptorClasses[]) throws InstantiationException, IllegalAccessException
   {
      assert container != null : "container is null";
      assert interceptorClasses != null : "interceptorClasses is null";
      Object interceptors[] = new Object[interceptorClasses.length];
      for(int i = 0; i < interceptorClasses.length; i++)
      {
         Class<?> interceptorClass = interceptorClasses[i];
         Object interceptor = interceptors[i] = interceptorClass.newInstance();
         for(Method method : ClassHelper.getAllMethods(interceptorClass))
         {
            if(container.isAnnotationPresent(method, PreDestroy.class))
            {
               preDestroys.add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, method));
            }
            if(container.isAnnotationPresent(method, PostConstruct.class))
            {
               postConstructs.add(new LifecycleCallbackInterceptorMethodInterceptor(interceptor, method));
            }
            if(container.isAnnotationPresent(method, AroundInvoke.class))
            {
               classInterceptors.add(new BusinessMethodInterceptorMethodInterceptor(interceptor, method));
            }
         }
      }
      log.debug("preDestroys = " + preDestroys);
      log.debug("postConstructs = " + postConstructs);
      log.debug("classInterceptors = " + classInterceptors);
   }
   
   protected Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      SubInvocationContext subCtx = new SubInvocationContext(ctx, classInterceptors);
      return subCtx.proceed();
   }
   
   protected void preDestroy(InvocationContext ctx) throws Exception
   {
      SubInvocationContext subCtx = new SubInvocationContext(ctx, preDestroys);
      subCtx.proceed();
   }
   
   protected void postConstruct(InvocationContext ctx) throws Exception
   {
      SubInvocationContext subCtx = new SubInvocationContext(ctx, postConstructs);
      subCtx.proceed();
   }
}
