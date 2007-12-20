/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.interceptor.InvocationContext;
import javax.ejb.PostActivate;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate;

import org.jboss.ejb3.BeanContext;
import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public abstract class LifecycleInvocationContextImpl implements InvocationContext
{
   private static final Logger log = Logger.getLogger(LifecycleInvocationContextImpl.class);
   
   private int currentInterceptor;
   private int currentMethod;
   BeanContext beanContext;
   InterceptorInfo[] interceptorInfos;
   Object[] instances;
   private Method[] beanMethods;
   private HashMap metadata;
   private Object[] parameters;

   protected LifecycleInvocationContextImpl()
   {
   }
   
   public static InvocationContext getLifecycleInvocationContext(Class type, BeanContext beanContext, InterceptorInfo[] interceptorInfos, Method[] beanMethods)
   {
      LifecycleInvocationContextImpl ic = null;
      if (type == PostConstruct.class) ic = new PostConstructICtxImpl();
      else if (type == PostActivate.class) ic = new PostActivateICtxImpl();
      else if (type == PrePassivate.class) ic = new PrePassivateICtxImpl();
      else if (type == PreDestroy.class) ic = new PreDestroyICtxImpl();
      else throw new RuntimeException("Unsupported lifecycle event: " + type);
      
      ic.instances = beanContext.getInterceptorInstances(interceptorInfos);
      if (interceptorInfos.length != ic.instances.length)
      {
         throw new RuntimeException("interceptorInfos and instances have different length");
      }
      
      ic.beanContext = beanContext;
      ic.beanMethods = beanMethods;
      ic.interceptorInfos = interceptorInfos;
      
      return ic;
   }

   public Object getTarget()
   {
      return beanContext.getInstance();
   }

   public Method getMethod()
   {
      return null;
   }

   public Object[] getParameters()
   {
      if (parameters == null)
         parameters = new Object[0];
      
      return parameters;
   }

   public void setParameters(Object[] params)
   {
      this.parameters = params;
   }


   public java.util.Map getContextData()
   {
      if (metadata == null) {
         metadata = new HashMap();
      }
      return metadata;
   }

   public Object proceed() throws Exception
   {
      if (currentInterceptor < interceptorInfos.length)
      {
         int oldInterceptor = currentInterceptor;
         int oldMethod = currentMethod;
         try
         {
            int curr = currentInterceptor;
            int currMethod = currentMethod++;
            InterceptorInfo info = interceptorInfos[curr];
            if (currMethod == getLifecycleMethods(info).length)
            {
               curr = ++currentInterceptor;
               currentMethod = 0;
               currMethod = currentMethod++;
               info = (curr < interceptorInfos.length) ? interceptorInfos[curr] : null;
            }
            
            if (info != null)
            {
               try
               {
                  Method[] methods = getLifecycleMethods(info);
                  return methods[currMethod].invoke(instances[curr], this);
               }
               catch (InvocationTargetException e)
               {
                  if (e.getTargetException() instanceof Exception)
                  {
                     throw ((Exception) e.getCause());
                  }
                  else
                  {
                     throw new RuntimeException(e.getCause());
                  }
               }
            }
         }
         finally
         {
            // so that interceptors like clustering can reinvoke down the chain
            currentInterceptor = oldInterceptor;
            currentMethod = oldMethod;
         }
      }
      if (beanMethods != null)
      {
         try
         {
            for (Method beanMethod : beanMethods)
            {
               if (beanMethod.getParameterAnnotations().length == getParameters().length)
                  beanMethod.invoke(getTarget(), getParameters());
               else
                  log.error ("Skip attempt of invalid lifecycle method invocation: " + beanMethod);
            }
         }
         catch (InvocationTargetException e)
         {
            if (e.getTargetException() instanceof Exception)
            {
               throw ((Exception) e.getCause());
            }
            else
            {
               throw new RuntimeException(e.getCause());
            }
         }
         finally
         {
         }
      }
      
      return null;
   }
   
   abstract Method[] getLifecycleMethods(InterceptorInfo info);
   
   public static class PostConstructICtxImpl extends LifecycleInvocationContextImpl
   {
      Method[] getLifecycleMethods(InterceptorInfo info)
      {
         return info.getPostConstructs();
      }
   }

   public static class PostActivateICtxImpl extends LifecycleInvocationContextImpl
   {
      Method[] getLifecycleMethods(InterceptorInfo info)
      {
         return info.getPostActivates();
      }
   }

   public static class PrePassivateICtxImpl extends LifecycleInvocationContextImpl
   {
      Method[] getLifecycleMethods(InterceptorInfo info)
      {
         return info.getPrePassivates();
      }
   }

   public static class PreDestroyICtxImpl extends LifecycleInvocationContextImpl
   {
      Method[] getLifecycleMethods(InterceptorInfo info)
      {
         return info.getPreDestroys();
      }
   }
}
