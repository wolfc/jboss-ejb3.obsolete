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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.aop.metadata.ThreadMetaData;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public abstract class EJBInvocation implements Invocation
{
   private static final Logger log = Logger.getLogger(EJBInvocation.class);
   
   protected transient Interceptor[] interceptors;
   protected long methodHash;
   protected transient int currentInterceptor = 0;
   protected transient Method method;
   protected Object[] arguments;
   protected SimpleMetaData metadata = null;
   protected transient Map responseContextInfo = null;

   protected EJBInvocation(Method method, long methodHash, Object[] arguments, Interceptor[] interceptors)
   {
      this.method = method;
      this.methodHash = methodHash;
      this.arguments = arguments;
      this.interceptors = interceptors;
   }

   protected EJBInvocation()
   {
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
         return method.invoke(getTargetObject(), getArguments());
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
   }

   public Method getMethod()
   {
      return method;
   }

   public long getMethodHash()
   {
      return methodHash;
   }

   public Interceptor[] getInterceptors()
   {
      return interceptors;
   }

   public void setInterceptors(Interceptor[] interceptors)
   {
      this.interceptors = interceptors;
   }

   public Object[] getArguments()
   {
      return arguments;
   }

   public void setArguments(Object[] args)
   {
      this.arguments = args;
   }

   public Object getMetaData(Object key, Object attr)
   {
      // todo: set up the chain for metadata resolving
      Object value = null;
      if (metadata != null) value = metadata.getMetaData(key, attr);
      if (value != null) return value;
      value = ThreadMetaData.instance().getMetaData(key, attr);
      return value;
   }

   public Map getResponseContextInfo()
   {
      return responseContextInfo;
   }

   public void setResponseContextInfo(Map responseContextInfo)
   {
      this.responseContextInfo = responseContextInfo;
   }

   public void addResponseAttachment(Object key, Object val)
   {
      if (responseContextInfo == null) responseContextInfo = new HashMap();
      responseContextInfo.put(key, val);
   }

   public Object getResponseAttachment(Object key)
   {
      if (responseContextInfo == null) return null;
      return responseContextInfo.get(key);
   }

   public SimpleMetaData getMetaData()
   {
      if (metadata == null) metadata = new SimpleMetaData();
      return metadata;
   }

   public void setMetaData(SimpleMetaData data)
   {
      this.metadata = data;
   }

   public Object invokeNext(Interceptor[] newInterceptors) throws Throwable
   {
      throw new RuntimeException("NOT IMPLEMENTED");
   }
}
