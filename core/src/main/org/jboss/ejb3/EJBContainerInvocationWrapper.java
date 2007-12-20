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
package org.jboss.ejb3;

import java.lang.reflect.Method;
import java.util.Map;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.logging.Logger;

/**
 * This wrapper class allows you to insert a chain of interceptors into the middle of a call stack.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class EJBContainerInvocationWrapper<A extends EJBContainer, T extends BeanContext> extends EJBContainerInvocation<A, T>
{
   private static final long serialVersionUID = 5402917625526438235L;

   private static final Logger log = Logger.getLogger(EJBContainerInvocationWrapper.class);
   
   protected EJBContainerInvocation<A, T> wrapped;

   public EJBContainerInvocationWrapper(EJBContainerInvocation<A, T> wrapped, Interceptor[] interceptors)
   {
      this.wrapped = wrapped;
      this.interceptors = interceptors;
   }

   public Object invokeNext() throws Throwable
   {
      if (currentInterceptor < interceptors.length)
      {
         try
         {
            return interceptors[currentInterceptor++].invoke(this);
         }
         finally
         {
            // so that interceptors like clustering can reinvoke down the chain
            currentInterceptor--;
         }
      }
      try
      {
         return wrapped.invokeNext();
      }
      finally
      {
         responseContextInfo = wrapped.getResponseContextInfo();
      }
   }

   public Method getMethod()
   {
      return wrapped.getMethod();
   }

   public long getMethodHash()
   {
      return wrapped.getMethodHash();
   }

   public Object getTargetObject()
   {
      return wrapped.getTargetObject();
   }

   public void setTargetObject(Object targetObject)
   {
      wrapped.setTargetObject(targetObject);
   }

   public Object[] getArguments()
   {
      return wrapped.getArguments();
   }

   public void setArguments(Object[] args)
   {
      wrapped.setArguments(args);
   }

   public Object resolveClassAnnotation(Class annotation)
   {
      return wrapped.resolveClassAnnotation(annotation);
   }

   public Object resolveAnnotation(Class annotation)
   {
      return wrapped.resolveAnnotation(annotation);
   }

   public Object getMetaData(Object key, Object attr)
   {
      return wrapped.getMetaData(key, attr);
   }

   public Invocation getWrapper(Interceptor[] newchain)
   {
      return wrapped.getWrapper(newchain);
   }

   public Invocation copy()
   {
      return wrapped.copy();
   }

   public Map getResponseContextInfo()
   {
      return wrapped.getResponseContextInfo();
   }

   public void setResponseContextInfo(Map responseContextInfo)
   {
      wrapped.setResponseContextInfo(responseContextInfo);
   }

   public void addResponseAttachment(Object key, Object val)
   {
      wrapped.addResponseAttachment(key, val);
   }

   public Object getResponseAttachment(Object key)
   {
      return wrapped.getResponseAttachment(key);
   }

   public SimpleMetaData getMetaData()
   {
      return wrapped.getMetaData();
   }

   public void setMetaData(SimpleMetaData data)
   {
      wrapped.setMetaData(data);
   }

   public Object resolveClassMetaData(Object key, Object attr)
   {
      return wrapped.resolveClassMetaData(key, attr);
   }

   public Object invokeNext(Interceptor[] newInterceptors) throws Throwable
   {
      return wrapped.invokeNext(newInterceptors);
   }

   public A getAdvisor()
   {
      return wrapped.getAdvisor();
   }
}
