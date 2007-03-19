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
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.asynch.AsynchMixin;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.remoting.ClusterConstants;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.ejb3.asynchronous.AsynchronousInterceptor;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.util.id.GUID;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulClusteredProxy extends org.jboss.ejb3.remoting.BaseRemoteProxy
{
   private static final long serialVersionUID = 2702033360945548435L;
   
   private Object id;
   protected FamilyWrapper family;
   protected LoadBalancePolicy lbPolicy;
   AsynchProvider provider;


   public StatefulClusteredProxy(Object containerId, Interceptor[] interceptors, FamilyWrapper family, LoadBalancePolicy lb)
   {
      super(containerId, interceptors);
      this.family = family;
      this.lbPolicy = lb;
   }

   public StatefulClusteredProxy(AsynchProvider provider, Object containerId, Interceptor[] interceptors, FamilyWrapper family, LoadBalancePolicy lb)
   {
      super(containerId, interceptors);
      this.family = family;
      this.lbPolicy = lb;
      this.provider = provider;
   }

   protected StatefulClusteredProxy()
   {
   }

   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      if (method.getDeclaringClass() == AsynchProvider.class)
      {
         return provider.getFuture();
      }

      long hash = MethodHashing.calculateHash(method);
      Object ret = ProxyUtils.handleCallLocally(hash, (JBossProxy) proxy, this, method, args);
      if (ret != null)
      {
         return ret;
      }

      StatefulRemoteInvocation sri = new StatefulRemoteInvocation(interceptors, hash, method, method, null, id);
      sri.setArguments(args);
      sri.setInstanceResolver(metadata);
      sri.getMetaData().addMetaData(Dispatcher.DISPATCHER, Dispatcher.OID, containerId, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.CLUSTER_FAMILY_WRAPPER, family, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.LOADBALANCE_POLICY, lbPolicy, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(InvokeRemoteInterceptor.REMOTING, InvokeRemoteInterceptor.SUBSYSTEM, "AOP", PayloadKey.AS_IS);

      if (provider != null)
      {
         sri.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH, AsynchronousInterceptor.INVOKE_ASYNCH, "YES", PayloadKey.AS_IS);
      }
      try
      {
         Object rtn = sri.invokeNext();
         // if this is first invocation then container passes back actual ID
         if (id == null)
         {
            id = sri.getResponseAttachment(StatefulConstants.NEW_ID);
         }
         return rtn;
      }
      catch (ForwardId forward)
      {
         // if this is first invocation then container passes back actual ID
         // The ForwardId exception is needed if 1st operation throws an exception
         id = forward.getId();
         throw forward.getCause();
      }
   }

   public Object getAsynchronousProxy(Object proxy)
   {
      Class[] infs = proxy.getClass().getInterfaces();
      if (!ProxyUtils.isAsynchronous(infs))
      {
         Class[] interfaces = ProxyUtils.addAsynchProviderInterface(infs);
         AsynchMixin mixin = new AsynchMixin();
         Interceptor[] newInterceptors = ProxyUtils.addAsynchProxyInterceptor(mixin, interceptors);
         StatefulClusteredProxy handler = new StatefulClusteredProxy(mixin, containerId, newInterceptors, family, lbPolicy);
         return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, handler);
      }

      //I was already asynchronous
      return proxy;
   }

   public String toString()
   {
      if (id != null)
      {
         return containerId.toString() + ":" + id.toString();
      }
      else
      {
         //If the proxy has not been used yet, create a temporary id 
         GUID guid = new GUID();
         return containerId.toString() + ":" + guid.toString();
      }
   }

}
