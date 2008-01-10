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
import java.lang.reflect.Proxy;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.rmi.dgc.VMID;

import javax.ejb.EJBException;

import org.jboss.aspects.asynch.AsynchMixin;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.LocalProxy;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.util.id.GUID;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulLocalProxy extends LocalProxy 
{
   private static final long serialVersionUID = 206913210970415540L;
   
   protected Object id;
   AsynchProvider provider;

   protected boolean isClustered = false;

   public StatefulLocalProxy(Container container, Object id, VMID vmid)
   {
      super(container);
      this.id = id;
      this.containerGuid = Ejb3Registry.guid(container, vmid);
      isClustered = ((SessionContainer)container).isClustered();
   }

   public StatefulLocalProxy(AsynchProvider provider, Container container, Object id)
   {
      super(container);
      this.provider = provider;
      this.id = id;
      this.containerGuid = Ejb3Registry.guid(container);
      isClustered = ((SessionContainer)container).isClustered();
   }

   public StatefulLocalProxy()
   {
   }

   //@Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      id = in.readObject();
      isClustered = in.readBoolean();
   }

   //@Override
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(id);
      out.writeBoolean(isClustered);
   }

   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      if (method.getDeclaringClass() == AsynchProvider.class)
      {
         return provider.getFuture();
      }

      // Make sure we get the cache id before getting the asynchronous interface
      Object ret = ProxyUtils.handleCallLocally(proxy, this, method, args);
      if (ret != null)
      {
         return ret;
      }
      
      Container container = Ejb3Registry.findContainer(containerGuid);
      if (isClustered && container == null && Ejb3Registry.hasClusterContainer(containerClusterUid))
         container = Ejb3Registry.getClusterContainer(containerClusterUid);
       
      if (container == null)
         throw new EJBException("Invalid (i.e. remote) invocation of local interface (null container) for " + containerGuid);
   
      SessionContainer sfsb = (SessionContainer)container;
      return sfsb.localInvoke(id, method, args, (FutureHolder) provider);
   }

   public Object getAsynchronousProxy(Object proxy)
   {
      Class[] infs = proxy.getClass().getInterfaces();
      if (!ProxyUtils.isAsynchronous(infs))
      {
         Class[] interfaces = ProxyUtils.addAsynchProviderInterface(infs);
         AsynchMixin mixin = new AsynchMixin();
         StatefulLocalProxy handler = new StatefulLocalProxy(mixin, getContainer(), id);
         return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, handler);
      }

      //I was already asynchronous
      return proxy;
   }

   public String toString()
   {
      if (getContainer() != null && id != null)
      {
         return getContainer().getEjbName().toString() + ":" + id.toString();
      }

      return proxyName;
   }

}
