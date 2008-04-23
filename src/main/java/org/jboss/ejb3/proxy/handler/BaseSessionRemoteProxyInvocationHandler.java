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

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.Container;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public abstract class BaseSessionRemoteProxyInvocationHandler extends org.jboss.ejb3.proxy.handler.BaseProxyInvocationHandlerRemote
{
   private static final long serialVersionUID = 8310915813626447181L;
   
   protected Object id;
   
   protected Handle handle;
   protected HomeHandle homeHandle;
   protected EJBMetaData ejbMetaData;
   
   public BaseSessionRemoteProxyInvocationHandler(Container container, Interceptor[] interceptors, String businessInterfaceType)
   {
      super(container, interceptors, businessInterfaceType);
   }

   public BaseSessionRemoteProxyInvocationHandler(String containerId, String containerGuid, Interceptor[] interceptors,
         String businessInterfaceType)
   {
      super(containerId, containerGuid, interceptors, businessInterfaceType);
   }
   
   protected BaseSessionRemoteProxyInvocationHandler()
   {
   }
   
   public Handle getHandle()
   {
      return this.handle;
   }
   
   public void setHandle(Handle handle)
   {
      this.handle = handle;
   }
   
   public void setHomeHandle(HomeHandle homeHandle)
   {
      this.homeHandle = homeHandle;
   }
   
   public void setEjbMetaData(EJBMetaData ejbMetaData)
   {
      this.ejbMetaData = ejbMetaData;
   }
}
