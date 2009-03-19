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

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemoteInvocationHandlerInvocationHandler implements InvocationHandler
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
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // FIXME: associate the necessary thread locals to the invocation
      Map<?, ?> context = null;
      Method invokeMethod = RemotableEndpoint.class.getDeclaredMethod("invoke", Serializable.class, Map.class, Class.class, SerializableMethod.class, Object[].class);
      SerializableMethod businessMethod = new SerializableMethod(method);
      Object invokeArgs[] = { session, context, invokedBusinessInterface, businessMethod, args };
      return delegate.invoke(proxy, invokeMethod, invokeArgs);
   }
}