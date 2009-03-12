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
package org.jboss.ejb3.proxy.impl.factory.session.stateful;

import java.util.Set;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.proxy.impl.factory.session.SessionSpecProxyFactory;
import org.jboss.ejb3.proxy.impl.handler.session.SessionLocalProxyInvocationHandler;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatefulSessionLocalProxyFactory
 * 
 * A SFSB Proxy Factory for Local Views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulSessionLocalProxyFactory extends StatefulSessionProxyFactoryBase
      implements
         SessionSpecProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

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
    * @param advisor The Advisor for proxies created by this factory
    */
   public StatefulSessionLocalProxyFactory(final String name, final String containerName, final String containerGuid,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader, final Advisor advisor)
   {
      // Call Super
      super(name, containerName, containerGuid, metadata, classloader, advisor);
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
      return this.getMetadata().getBusinessLocals();
   }

   /**
    * Returns the String representation of the Home Interface Type
    * @return
    */
   @Override
   protected final String getHomeType()
   {
      return this.getMetadata().getLocalHome();
   }

   /**
    * Returns the String representation of the EJB.2x Interface Type
    * 
    *  @return
    */
   @Override
   protected final String getEjb2xInterfaceType()
   {
      return this.getMetadata().getLocal();
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   protected SessionProxyInvocationHandler createBusinessInterfaceSpecificInvocationHandler(String businessInterfaceName)
   {
      // Obtain target container name
      String containerName = this.getContainerName();
      String containerGuid = this.getContainerGuid();

      // Obtain interceptors
      Interceptor[] interceptors = this.getInterceptors();

      // Create
      SessionProxyInvocationHandler handler = new SessionLocalProxyInvocationHandler(containerName, containerGuid,
            interceptors, businessInterfaceName);

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
}
