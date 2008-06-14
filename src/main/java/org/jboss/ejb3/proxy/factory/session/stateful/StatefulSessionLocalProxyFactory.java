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
package org.jboss.ejb3.proxy.factory.session.stateful;

import java.util.Set;

import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulLocalProxyInvocationHandler;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatefulSessionLocalProxyFactory
 * 
 * A SFSB Proxy Factory for Local Views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulSessionLocalProxyFactory extends StatefulSessionProxyFactoryBase implements SessionProxyFactory
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
    * @param metadata The metadata representing this SLSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    */
   public StatefulSessionLocalProxyFactory(final String name, final String containerName,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader)
   {
      // Call Super
      super(name, containerName, metadata, classloader);
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
   protected SessionProxyInvocationHandler createInvocationHandler(String businessInterfaceName)
   {
      // Create
      SessionProxyInvocationHandler handler = new StatefulLocalProxyInvocationHandler(businessInterfaceName);

      // Return
      return handler;
   }

   /**
    * Obtains the Container used by this Proxy Factory
    * 
    * @return The Container for this Proxy Factory
    */
   @Override
   protected StatefulSessionInvokableContext<?> obtainContainer()
   {
      /*
       * Obtain the Container
       */
      StatefulSessionInvokableContext<?> container = null;
      String containerName = this.getContainerName();

      // Lookup from EJB3 Registrar
      try
      {
         Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(containerName);
         assert obj instanceof StatefulSessionInvokableContext : "Container retrieved from "
               + Ejb3Registrar.class.getSimpleName() + " was not of expected type "
               + StatefulSessionInvokableContext.class.getName() + " but was instead " + obj;
         container = (StatefulSessionInvokableContext<?>) obj;
      }
      catch (NotBoundException nbe)
      {
         throw new RuntimeException(StatefulSessionProxyFactory.class.getSimpleName() + " " + this
               + " has defined container name \"" + containerName + "\", but this could not be found in the "
               + Ejb3Registrar.class.getSimpleName());
      }

      // Return
      return container;
   }
}
