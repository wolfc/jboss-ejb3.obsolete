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
package org.jboss.ejb3.stateless;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.asynch.AsynchMixin;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.remoting.ClusterConstants;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.ejb3.asynchronous.AsynchronousInterceptor;
import org.jboss.ejb3.remoting.BaseRemoteProxy;
import org.jboss.ejb3.remoting.ClusteredIsLocalInterceptor;
import org.jboss.ejb3.remoting.IsLocalInterceptor;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;

/**
 * InvocationHandler for a clustered SLSB proxy.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 * @version $Revision$
 */
public class StatelessClusteredProxy extends BaseRemoteProxy
{   
   private static final long serialVersionUID = -4100015258079818357L;

   protected FamilyWrapper family;
   protected LoadBalancePolicy lbPolicy;
   AsynchProvider provider;
   protected String partitionName;

   public StatelessClusteredProxy(Container container, Interceptor[] interceptors, FamilyWrapper family, LoadBalancePolicy lbPolicy, String partitionName)
   {
      super(container, interceptors);
      this.family = family;
      this.lbPolicy = lbPolicy;
      this.partitionName = partitionName;
   }

   public StatelessClusteredProxy(AsynchProvider provider, String containerId, String containerGuid, Interceptor[] interceptors, FamilyWrapper family, LoadBalancePolicy lbPolicy, String partitionName)
   {
      super(containerId, containerGuid, interceptors);
      this.provider = provider;
      this.family = family;
      this.lbPolicy = lbPolicy;
      this.partitionName = partitionName;
   }

   public StatelessClusteredProxy()
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

      MethodInvocation sri = new MethodInvocation(interceptors, hash, method, method, null);
      sri.setArguments(args);
      sri.setInstanceResolver(metadata);
      sri.getMetaData().addMetaData(Dispatcher.DISPATCHER, Dispatcher.OID, containerId, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.CLUSTER_FAMILY_WRAPPER, family, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.LOADBALANCE_POLICY, lbPolicy, PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(InvokeRemoteInterceptor.REMOTING, InvokeRemoteInterceptor.SUBSYSTEM, "AOP", PayloadKey.AS_IS);
      sri.getMetaData().addMetaData(ClusteredIsLocalInterceptor.PARTITION_NAME, ClusteredIsLocalInterceptor.PARTITION_NAME, partitionName, PayloadKey.TRANSIENT);
      sri.getMetaData().addMetaData(IsLocalInterceptor.IS_LOCAL, IsLocalInterceptor.GUID, containerGuid, PayloadKey.AS_IS);
      
      if (provider != null)
      {
         sri.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH, AsynchronousInterceptor.INVOKE_ASYNCH, "YES", PayloadKey.AS_IS);
      }
      return sri.invokeNext();
   }

   public Object getAsynchronousProxy(Object proxy)
   {
      Class[] infs = proxy.getClass().getInterfaces();
      if (!ProxyUtils.isAsynchronous(infs))
      {
         Class[] interfaces = ProxyUtils.addAsynchProviderInterface(infs);
         AsynchMixin mixin = new AsynchMixin();
         Interceptor[] newInterceptors = ProxyUtils.addAsynchProxyInterceptor(mixin, interceptors);
         StatelessClusteredProxy handler = new StatelessClusteredProxy(mixin, containerId, containerGuid, newInterceptors, family, lbPolicy, partitionName);
         return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, handler);
      }

      //I was already asynchronous
      return proxy;
   }

   public String toString()
   {
      return containerId.toString();
   }

}
