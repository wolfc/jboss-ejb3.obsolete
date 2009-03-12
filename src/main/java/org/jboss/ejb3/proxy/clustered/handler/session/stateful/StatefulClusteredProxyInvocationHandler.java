/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ejb3.proxy.clustered.handler.session.stateful;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.proxy.clustered.invocation.InvokableContextClusteredProxyInvocationHandler;
import org.jboss.ejb3.proxy.impl.handler.session.SessionRemoteProxyInvocationHandler;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.remoting.InvokerLocator;

/**
 * InvocationHandler for remote proxies to clustered SFSBs.
 * 
 * @author Brian Stansberry
 *
 */
public class StatefulClusteredProxyInvocationHandler extends SessionRemoteProxyInvocationHandler
{  

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 8941906289390905059L;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   private FamilyWrapper family;
   private LoadBalancePolicy lbPolicy;
   private String partitionName;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create a new StatefulClusteredRemoteProxyInvocationHandler.
    * 
    * @param family clustering target information. Cannot be <code>null</code>.
    * @param lb LoadBalancePolicy implementation. Cannot be <code>null</code>.
    * @param partitionName  name of the cluster partition. Cannot be <code>null</code>.
    */
   public StatefulClusteredProxyInvocationHandler(String containerName, String containerGuid,
         Interceptor[] interceptors, String businessInterfaceType, String url, FamilyWrapper family,
         LoadBalancePolicy lb, String partitionName)
   {
      super(containerName, containerGuid, interceptors, businessInterfaceType, url);
      
      assert family != null        : "family is null";
      assert lb != null            : "lb is null";
      assert partitionName != null : "partitionName is null";
      
      this.family = family;
      this.lbPolicy = lb;
      this.partitionName = partitionName;  
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   protected InvokableContext createRemoteProxyToContainer(String url)
   {
      // Create an InvokerLocator
      InvokerLocator locator = null;
      try
      {
         locator = new InvokerLocator(url);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException("Could not create " + InvokerLocator.class.getSimpleName() + " to url \"" + url
               + "\"", e);
      }

      /*
       * Define interceptors
       */

      // Get interceptors from the stack
      Interceptor[] interceptors = this.getInterceptors();

      /*
       * Create Proxy
       */

      // Create a POJI Proxy to the Container
      String containerName = this.getContainerName();
      assert containerName != null && containerName.trim().length() > 0 : "Container Name must be set";
      PojiProxy handler = new InvokableContextClusteredProxyInvocationHandler(this.getContainerName(), this
            .getContainerGuid(), locator, interceptors, (Serializable) this.getTarget(), this.getFamilyWrapper(), this
            .getLoadBalancePolicy(), this.getPartitionName(), true);
      Class<?>[] interfaces = new Class<?>[]
      {InvokableContext.class};
      InvokableContext container = (InvokableContext) Proxy.newProxyInstance(InvokableContext.class.getClassLoader(),
            interfaces, handler);

      // Return
      return container;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public FamilyWrapper getFamilyWrapper()
   {
      return family;
   }

   public LoadBalancePolicy getLoadBalancePolicy()
   {
      return lbPolicy;
   }

   public String getPartitionName()
   {
      return partitionName;
   }

}
