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
package org.jboss.ejb3.proxy.factory.session.stateless;

import java.util.Set;

import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactoryBase;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatelessSessionProxyFactoryBase
 * 
 * Base upon which SLSB Proxy Factories may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class StatelessSessionProxyFactoryBase extends SessionProxyFactoryBase implements SessionProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param metadata The metadata representing this SLSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    * @param containerName The name under which the target container is registered
    */
   public StatelessSessionProxyFactoryBase(final JBossSessionBeanMetaData metadata, final ClassLoader classloader,
         final String containerName)
   {
      // Call Super
      super(metadata, classloader, containerName);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the return types declared by the "create" methods for the specified home interface.
    *  
    * @param homeInterface
    * @return
    * @deprecated http://jira.jboss.com/jira/browse/JBMETA-41
    */
   @Deprecated
   @Override
   protected Set<Class<?>> getReturnTypesFromCreateMethods(Class<?> homeInterface)
   {
      return this.getReturnTypesFromCreateMethods(homeInterface, true);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle callback to be invoked by the ProxyFactoryDeployer
    * before the ProxyFactory is able to service requests
    * 
    *  @throws Exception
    */
   @Override
   public void start() throws Exception
   {
      super.start();
      //TODO
   }

   /**
    * Lifecycle callback to be invoked by the ProxyFactoryDeployer
    * before the ProxyFactory is taken out of service, 
    * possibly GC'd
    * 
    * @throws Exception
    */
   @Override
   public void stop() throws Exception
   {
      super.stop();
      //TODO
   }
}
