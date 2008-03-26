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
package org.jboss.ejb3.stateful;

import java.lang.reflect.Method;

import javax.ejb.EJBMetaData;
import javax.ejb.HomeHandle;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.ejb3.remoting.IsLocalInterceptor;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulHomeRemoteProxy extends org.jboss.ejb3.session.BaseSessionRemoteProxy
{
   private static final long serialVersionUID = 5509794838403316545L;

   private static final Logger log = Logger.getLogger(StatefulHomeRemoteProxy.class);

   protected InvokerLocator uri;
   private HomeHandle homeHandle;
   private EJBMetaData ejbMetaData;

   public StatefulHomeRemoteProxy(Container container, Interceptor[] interceptors, InvokerLocator uri)
   {
      super(container, interceptors);
      this.uri = uri;
   }

   public StatefulHomeRemoteProxy(Container container, Interceptor[] interceptors, InvokerLocator uri, Object id)
   {
      super(container, interceptors);
      this.uri = uri;
      this.id = id;
   }

   protected StatefulHomeRemoteProxy()
   {
   }

   public void setHandle(StatefulHandleImpl handle)
   {
      this.handle = handle;
      handle.id = id;
   }

   public void setHomeHandle(HomeHandle homeHandle)
   {
      this.homeHandle = homeHandle;
   }

   public void setEjbMetaData(EJBMetaData ejbMetaData)
   {
      this.ejbMetaData = ejbMetaData;
   }

   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      long hash = MethodHashing.calculateHash(method);
      Object ret = ProxyUtils.handleCallLocally(hash, proxy, this, method, args);
      if (ret != null)
      {
         return ret;
      }

      ret = handleEjb21CallLocally(method, args);
      if (ret != null)
      {
         return ret;
      }

      StatefulRemoteInvocation sri = new StatefulRemoteInvocation(interceptors, hash, method, method, null, null);
      sri.setArguments(args);
      sri.setInstanceResolver(metadata);
      sri.getMetaData().addMetaData(Dispatcher.DISPATCHER, Dispatcher.OID, containerId, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(InvokeRemoteInterceptor.REMOTING, InvokeRemoteInterceptor.INVOKER_LOCATOR, uri, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(InvokeRemoteInterceptor.REMOTING, InvokeRemoteInterceptor.SUBSYSTEM, "AOP", PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(IsLocalInterceptor.IS_LOCAL, IsLocalInterceptor.GUID, containerGuid, PayloadKey.AS_IS);

      return sri.invokeNext();
   }

   public Object getAsynchronousProxy(Object proxy)
   {
      throw new RuntimeException("NOT IMPLEMENTED");
   }

   public String toString()
   {
      return containerId.toString() + ":Home";
   }

   private Object handleEjb21CallLocally(Method method, Object[] args)
   {
      if (method.equals(ProxyUtils.GET_HOME_HANDLE))
      {
         return homeHandle;
      }
      else if (method.equals(ProxyUtils.GET_EJB_METADATA))
      {
         return ejbMetaData;
      }

      return null;
   }
}
