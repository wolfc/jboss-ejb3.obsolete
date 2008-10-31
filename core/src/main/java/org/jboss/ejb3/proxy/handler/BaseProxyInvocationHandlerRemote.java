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
package org.jboss.ejb3.proxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3Registry;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class BaseProxyInvocationHandlerRemote extends BaseProxyInvocationHandler implements java.io.Serializable, InvocationHandler, RemoteProxyInvocationHandler
{
   private static final long serialVersionUID = 1126421850898582900L;
   
   protected String containerId;
   protected String containerGuid;
   protected Interceptor[] interceptors;
   protected SimpleMetaData metadata;

   protected BaseProxyInvocationHandlerRemote(Container container, Interceptor[] interceptors, String businessInterfaceType)
   {
      this.containerId = container.getObjectName().getCanonicalName();
      this.containerGuid = Ejb3Registry.guid(container);
      this.interceptors = interceptors;
      this.setBusinessInterfaceType(businessInterfaceType);
   }
   
   protected BaseProxyInvocationHandlerRemote(String containerId, String containerGuid, Interceptor[] interceptors, String businessInterfaceType)
   {
      this.containerId = containerId;
      this.containerGuid = containerGuid;
      this.interceptors = interceptors;
      this.setBusinessInterfaceType(businessInterfaceType);
   }

   protected BaseProxyInvocationHandlerRemote()
   {
   }

   public SimpleMetaData getMetaData()
   {
      synchronized (this)
      {
         if (metadata == null) metadata = new SimpleMetaData();
      }
      return metadata;
   }

   public abstract Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable;

   //Force all remote proxies to override toString()
   public abstract String toString();
}
