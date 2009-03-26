/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.remoting.endpoint.client;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.endpoint.RemotableEndpoint;
import org.jboss.ejb3.remoting.reflect.AbstractInvocationHandler;

/**
 * An invocation handler which delegates to an invocation handler that handles invocations
 * on a RemotableEndpoint. Before calling upon this handler a RemoteContextData must be set.
 * 
 * In theory you the delegate should be an RemotableEndpoint. In practice this is usually an
 * InvocationHandler, so this class skips through immediately to that handler.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemoteInvocationHandlerInvocationHandler extends AbstractInvocationHandler
{
   private InvocationHandler delegate;
   private Serializable session;
   private Class<?> invokedBusinessInterface;

   public RemoteInvocationHandlerInvocationHandler(InvocationHandler delegate, Serializable session, Class<?> invokedBusinessInterface)
   {
      this.delegate = delegate;
      this.session = session;
      this.invokedBusinessInterface = invokedBusinessInterface;
   }
   
   public Object innerInvoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      Map<String, Object> contextData = RemoteContextData.getContextData();
      try
      {
         // make sure we don't leak across recursive calls
         RemoteContextData.cleanContextData();
         Method invokeMethod = RemotableEndpoint.INVOKE_METHOD;
         SerializableMethod businessMethod = new SerializableMethod(method, invokedBusinessInterface);
         Object invokeArgs[] = { session, contextData, businessMethod, args };
         return delegate.invoke(proxy, invokeMethod, invokeArgs);
      }
      finally
      {
         RemoteContextData.setContextData(contextData);
      }
   }
   
   public String toProxyString()
   {
      return "Proxy on " + toString();
   }
   
   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer(super.toString());
      sb.append("{delegate=" + delegate);
      sb.append(",session=" + session);
      sb.append(",invokedBusinessInterface=" + invokedBusinessInterface);
      sb.append("}");
      return sb.toString();
   }
}
