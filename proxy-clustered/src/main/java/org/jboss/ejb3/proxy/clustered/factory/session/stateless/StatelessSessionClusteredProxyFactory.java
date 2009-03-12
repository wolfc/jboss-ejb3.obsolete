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
package org.jboss.ejb3.proxy.clustered.factory.session.stateless;

import org.jboss.aop.Advisor;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.proxy.clustered.handler.session.stateless.StatelessClusteredProxyInvocationHandler;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringInfo;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringRegistry;
import org.jboss.ejb3.proxy.impl.factory.session.stateless.StatelessSessionRemoteProxyFactory;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.remoting.InvokerLocator;

/**
 * An SLSB Proxy Factory for Clustered Remote Views
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class StatelessSessionClusteredProxyFactory extends StatelessSessionRemoteProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final String STACK_NAME_CLUSTERED_STATELESS_SESSION_CLIENT_INTERCEPTORS = "ClusteredStatelessSessionClientInterceptors";

   private static final Logger log = Logger.getLogger(StatelessSessionClusteredProxyFactory.class);
   
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private final ProxyClusteringRegistry registry;
   private ProxyClusteringInfo beanClusteringInfo;
   
   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param name The unique name for this ProxyFactory
    * @param containerName The name of the InvokableContext (container)
    *   upon which Proxies will invoke
    * @param containerGuid The globally-unique name of the container
    * @param metadata The metadata representing this SLSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    * @param url The URL to use for remoting
    * @param advisor The Advisor for proxies created by this factory
    * @param interceptorStack
    */
   public StatelessSessionClusteredProxyFactory(final String name, final String containerName,
         final String containerGuid, final JBossSessionBeanMetaData metadata, final ClassLoader classloader,
         final String url, final Advisor advisor, final ProxyClusteringRegistry registry, final String interceptorStack)
   {
      // Call Super
      super(name, containerName, containerGuid, metadata, classloader, url, advisor, interceptorStack);
      
      assert registry != null : "registry is null";
      
      this.registry = registry;
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Return the name of the interceptor stack to apply to 
    * proxies created by this proxy factory
    * 
    * @return
    */
   @Override
   protected String getInterceptorStackName()
   {
      return STACK_NAME_CLUSTERED_STATELESS_SESSION_CLIENT_INTERCEPTORS;
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public void start() throws Exception
   {
      super.start();
      
      this.beanClusteringInfo = this.registry.registerClusteredBean(getContainerName(), getName(), getMetadata(), new InvokerLocator(getUrl()));
      
      // FIXME move to superclass
      // Register with Remoting
      log.debug("Registering with Remoting Dispatcher under name \"" + getName() + "\": " + this);
      Dispatcher.singleton.registerTarget(getName(), this);
   }

   @Override
   public void stop() throws Exception
   {
      this.registry.unregisterClusteredBean(this.beanClusteringInfo);
      
      // FIXME move to superclass
      // Register with Remoting
      log.debug("Unregistering name \"" + getName() + "\" from Remoting Dispatcher");
      Dispatcher.singleton.unregisterTarget(getName());
      
      super.stop();
   }
   
   
   @Override
   protected SessionProxyInvocationHandler createBusinessDefaultInvocationHandler()
   {
      return createInvocationHandler(null, false);
   }

   @Override
   protected SessionProxyInvocationHandler createBusinessInterfaceSpecificInvocationHandler(String businessInterfaceName)
   {
      return createInvocationHandler(businessInterfaceName, false);
   }

   @Override
   protected SessionProxyInvocationHandler createEjb2xComponentInterfaceInvocationHandler()
   {
      return createInvocationHandler(null, false);
   }

   @Override
   protected SessionProxyInvocationHandler createHomeInvocationHandler()
   {
      return createInvocationHandler(null, true);
   }

   // --------------------------------------------------------------------------------||
   // Private ------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   private SessionProxyInvocationHandler createInvocationHandler(String businessInterfaceName, boolean isOnlyHome)   
   {
      // Obtain properties
      String containerName = this.getContainerName();
      String containerGuid = this.getContainerGuid();
      String url = this.getUrl();

      // Get Interceptors
      Interceptor[] interceptors = this.getInterceptors();

      Class<? extends LoadBalancePolicy> policyClass = isOnlyHome ? beanClusteringInfo.getHomeLoadBalancePolicy() : beanClusteringInfo.getLoadBalancePolicy();
      LoadBalancePolicy lbp;
      try
      {
         lbp = policyClass.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot instantiate LoadBalancePolicy class " + policyClass);
      }
      
      // Create
      SessionProxyInvocationHandler handler = new StatelessClusteredProxyInvocationHandler(containerName, containerGuid,
            interceptors, businessInterfaceName, url, beanClusteringInfo.getFamilyWrapper(), 
            lbp, beanClusteringInfo.getPartitionName());

      // Return
      return handler;
   }
   
}
