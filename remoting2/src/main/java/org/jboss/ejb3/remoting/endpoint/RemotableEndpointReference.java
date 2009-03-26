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
package org.jboss.ejb3.remoting.endpoint;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.client.CurrentRemotableClient;
import org.jboss.ejb3.remoting.endpoint.client.RemoteContextDataInterceptor;
import org.jboss.ejb3.remoting.endpoint.client.RemoteInvocationHandlerInvocationHandler;
import org.jboss.ejb3.remoting.reflect.AbstractInvocationHandler;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemotableEndpointReference extends AbstractInvocationHandler
   implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private Serializable session;
   private Class<?> businessInterface;
   private Serializable oid;
   private transient RemotableEndpoint endpoint;
   
   /**
    * For serialization only.
    */
   public RemotableEndpointReference()
   {
      
   }
   
   /**
    * @param session
    * @param mockRemotable
    */
   public RemotableEndpointReference(RemotableEndpoint remotableEndpoint, Serializable session, Class<?> businessInterface)
   {
      this.endpoint = remotableEndpoint;
      this.session = session;
      this.businessInterface = businessInterface;
      this.oid = remotableEndpoint.getRemotable().getId();
   }

   protected Object innerInvoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      assert endpoint != null : "reference has crossed the wire and still exists without an endpoint";
      
      // this is a brand new invocation happening locally, so there is no 'remote' context data
      Map<String, Object> contextData = new HashMap<String, Object>();
      SerializableMethod businessMethod = new SerializableMethod(method, businessInterface);
      return endpoint.invoke(session, contextData, businessMethod, args);
   }

   protected Object readResolve() throws ObjectStreamException
   {
      InvocationHandler handler = CurrentRemotableClient.createHandler(oid);
         
      handler = new RemoteInvocationHandlerInvocationHandler(handler, session, businessInterface);
      
      handler = new InterceptorInvocationHandler(handler, new RemoteContextDataInterceptor());
      
      return handler;
   }
   
   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer(super.toString());
      sb.append("{endpoint=" + endpoint);
      sb.append(",oid=" + oid);
      sb.append(",session=" + session);
      sb.append(",businessInterface=" + businessInterface);
      sb.append("}");
      return sb.toString();
   }
   
   @Override
   protected String toProxyString()
   {
      return "Proxy on " + toString();
   }
}
