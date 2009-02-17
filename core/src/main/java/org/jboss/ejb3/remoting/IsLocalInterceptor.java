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
package org.jboss.ejb3.remoting;

import java.io.Serializable;
import java.util.Map;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.util.PayloadKey;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;
import org.jboss.serial.io.MarshalledObjectForLocalCalls;

/**
 * Routes the call to the local container, bypassing further client-side
 * interceptors and any remoting layer, if this interceptor was created 
 * in this JVM.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 *
 * @version $Revision$
 */
public class IsLocalInterceptor implements Interceptor, Serializable
{
   private static final long serialVersionUID = 337700910587744646L;

   private static final Logger log = Logger.getLogger(IsLocalInterceptor.class);
   
   public static final String GUID = "GUID";

   public static final String IS_LOCAL = "IS_LOCAL";
   public static final String IS_LOCAL_EXCEPTION = "IS_LOCAL_EXCEPTION";
   
   private static final long stamp = System.currentTimeMillis();
   private long marshalledStamp = stamp;

   public String getName()
   {
      return getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      if (isLocal())
      {
         String guid = (String)invocation.getMetaData(IS_LOCAL, GUID);
         Container container = Ejb3Registry.getContainer(guid);
         
         return invokeLocal(invocation, container);
      }
      return invocation.invokeNext();
   }
   
   protected Object invokeLocal(Invocation invocation, Container container) throws Throwable
   {
      Invocation copy = (Invocation) new MarshalledObjectForLocalCalls(invocation).get();
      copy.getMetaData().addMetaData(IS_LOCAL, IS_LOCAL, Boolean.TRUE, PayloadKey.AS_IS);
      org.jboss.aop.joinpoint.InvocationResponse response = null;

      // Invoke upon the container
      SessionContainer sc = (SessionContainer) container;
      response = sc.dynamicInvoke(copy);
      
      Map contextInfo = response.getContextInfo();
      if (contextInfo != null)
      {
         MarshalledObjectForLocalCalls wrappedException = (MarshalledObjectForLocalCalls) response.getContextInfo().get(IS_LOCAL_EXCEPTION);
         if (wrappedException != null)
         {
            throw (Throwable) wrappedException.get();
         }
      }
      invocation.setResponseContextInfo(response.getContextInfo());
      MarshalledObjectForLocalCalls wrapped = (MarshalledObjectForLocalCalls) response.getResponse();
      Object rtn = null;
      if (wrapped != null)
      {
         rtn = wrapped.get();
      }
      return rtn;
   }

   protected boolean isLocal()
   {
      return stamp == marshalledStamp;
   }
}
