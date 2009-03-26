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
package org.jboss.ejb3.remoting2.client;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.client.CurrentRemotableClient;
import org.jboss.ejb3.remoting.client.RemotableClient;
import org.jboss.ejb3.remoting.reflect.AbstractInvocationHandler;
import org.jboss.remoting.Client;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemoteInvocationHandler extends AbstractInvocationHandler
   implements RemotableClient
{
   public static final String OID = "OID";
   
   private Client client;
   private Serializable oid;
   
   public RemoteInvocationHandler(Client client, Serializable oid)
   {
      this.client = client;
      this.oid = oid;
   }
   
   public InvocationHandler createHandler(Serializable oid)
   {
      if(equals(this.oid, oid))
         return this;
      return new RemoteInvocationHandler(client, oid);
   }
   
   private static boolean equals(Object obj1, Object obj2)
   {
      if(obj1 == obj2)
         return true;
      
      if(obj1 == null || obj2 == null)
         return false;
      
      return obj1.equals(obj2);
   }
   
   public Object innerInvoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      SerializableMethod serializableMethod = new SerializableMethod(method);
      Object param = new Object[] { serializableMethod, args };
      Map<Object, Object> metadata = new HashMap<Object, Object>();
      metadata.put(OID, oid);
      CurrentRemotableClient.set(this);
      try
      {
         return client.invoke(param, metadata);
      }
      finally
      {
         CurrentRemotableClient.remove();
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
      sb.append("{client=" + client);
      sb.append(",oid=" + oid);
      sb.append("}");
      return sb.toString();
   }
}
