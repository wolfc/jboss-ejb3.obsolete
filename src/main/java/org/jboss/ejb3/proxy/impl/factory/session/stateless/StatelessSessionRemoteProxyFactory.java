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
package org.jboss.ejb3.proxy.impl.factory.session.stateless;

import java.util.Set;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.proxy.impl.factory.session.SessionSpecProxyFactory;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.impl.handler.session.SessionRemoteProxyInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatelessSessionRemoteProxyFactory
 * 
 * A SLSB Proxy Factory for Remote Views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessSessionRemoteProxyFactory extends StatelessSessionProxyFactoryBase
      implements
         SessionSpecProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger logger = Logger.getLogger(StatelessSessionRemoteProxyFactory.class);

   private static final String DEFAULT_STACK_NAME_STATELESS_SESSION_CLIENT_INTERCEPTORS = "StatelessSessionClientInterceptors";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * URL to be used in Remoting
    */
   private String url;

   /**
    * Name of the interceptor stack to use (overrides the default)
    */
   private String interceptorStackName;

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
    * @param interceptorStackName The name of the client-side interceptor stack to use.
    *       If null the default will apply.
    */
   public StatelessSessionRemoteProxyFactory(final String name, final String containerName, final String containerGuid,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader, final String url,
         final Advisor advisor, final String interceptorStackName)
   {
      // Call Super
      super(name, containerName, containerGuid, metadata, classloader, advisor);

      // Set properties
      this.setUrl(url);
      this.setInterceptorStackName(interceptorStackName);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns the a Set of String representations of the Business Interface Types
    * 
    *  @return
    */
   @Override
   protected final Set<String> getBusinessInterfaceTypes()
   {
      return this.getMetadata().getBusinessRemotes();
   }

   /**
    * Returns the String representation of the Home Interface Type
    * @return
    */
   @Override
   protected final String getHomeType()
   {
      return this.getMetadata().getHome();
   }

   /**
    * Returns the String representation of the EJB2.x Interface Type
    * 
    *  @return
    */
   @Override
   protected final String getEjb2xInterfaceType()
   {
      return this.getMetadata().getRemote();
   }

   /**
    * Return the name of the interceptor stack to apply to 
    * proxies created by this proxy factory
    * 
    * @return
    */
   @Override
   protected String getInterceptorStackName()
   {
      // Initialize to default
      String stackName = StatelessSessionRemoteProxyFactory.DEFAULT_STACK_NAME_STATELESS_SESSION_CLIENT_INTERCEPTORS;

      // Override if specified
      String overrideName = this.interceptorStackName;
      if (overrideName != null && overrideName.trim().length() > 0)
      {
         stackName = overrideName;
      }

      // Return
      return stackName;
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   protected SessionProxyInvocationHandler createBusinessInterfaceSpecificInvocationHandler(String businessInterfaceName)
   {
      // Obtain properties
      String containerName = this.getContainerName();
      String containerGuid = this.getContainerGuid();
      String url = this.getUrl();

      // Get Interceptors
      Interceptor[] interceptors = this.getInterceptors();

      // Create
      SessionProxyInvocationHandler handler = new SessionRemoteProxyInvocationHandler(containerName, containerGuid,
            interceptors, businessInterfaceName, url);

      // Return
      return handler;
   }

   @Override
   protected SessionProxyInvocationHandler createBusinessDefaultInvocationHandler()
   {
      return this.createBusinessInterfaceSpecificInvocationHandler(null);
   }

   @Override
   protected SessionProxyInvocationHandler createEjb2xComponentInterfaceInvocationHandler()
   {
      return this.createBusinessDefaultInvocationHandler();
   }

   @Override
   protected SessionProxyInvocationHandler createHomeInvocationHandler()
   {
      return this.createBusinessDefaultInvocationHandler();
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public String getUrl()
   {
      return url;
   }

   public void setUrl(final String url)
   {
      this.url = url;
   }

   protected void setInterceptorStackName(String interceptorStackName)
   {
      this.interceptorStackName = interceptorStackName;
   }
}
