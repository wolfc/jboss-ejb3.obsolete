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
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.jboss.ejb3.EJBContainerInvocation;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class InvocationContextImpl implements InvocationContext
{
   private int currentInterceptor;
   private int currentMethod;
   private int currentBeanMethod;
   InterceptorInfo[] interceptorInfos;
   Object[] instances;
   private Method[] beanAroundInvokes;
   private Map metadata;

   EJBContainerInvocation wrapped;
   
   public InvocationContextImpl(EJBContainerInvocation inv, InterceptorInfo[] interceptorInfos, Object[] instances, Method[] beanAroundInvokes)
   {
      wrapped = inv;
      this.beanAroundInvokes = beanAroundInvokes;
      
      if (interceptorInfos.length != instances.length)
      {
         throw new RuntimeException("interceptorInfos and instances have different length");
      }
      
      this.interceptorInfos = interceptorInfos;
      this.instances = instances;
   }

   public Object getTarget()
   {
      return wrapped.getTargetObject();
   }

   public Method getMethod()
   {
      return wrapped.getMethod();
   }

   public Object[] getParameters()
   {
      return wrapped.getArguments();
   }

   public void setParameters(Object[] params)
   {
      wrapped.setArguments(params);
   }

   public java.util.Map getContextData()
   {
      if (metadata == null) {
         metadata = ClientInterceptorUtil.getClientMetadataMap(wrapped);
         if (metadata == null)
         {
            metadata = new HashMap();
         }
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
            if (currMethod == info.getAroundInvokes().length)
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
                  return info.getAroundInvokes()[currMethod].invoke(instances[curr], this);
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
      
      if (beanAroundInvokes != null && currentBeanMethod < beanAroundInvokes.length)
      {
         try
         {
            int curr = currentBeanMethod++;
            return beanAroundInvokes[curr].invoke(getTarget(), this);
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
            currentBeanMethod--;;
         }
      }
      try
      {
         return wrapped.invokeNext();
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }
}
