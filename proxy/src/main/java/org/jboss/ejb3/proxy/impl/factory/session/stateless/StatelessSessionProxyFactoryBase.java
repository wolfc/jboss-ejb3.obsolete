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
import org.jboss.ejb3.proxy.impl.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.SessionProxyFactoryBase;
import org.jboss.logging.Logger;
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
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(StatelessSessionProxyFactoryBase.class);

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
   public StatelessSessionProxyFactoryBase(final String name, final String containerName, final String containerGuid,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader, final Advisor advisor)
   {
      // Call Super
      super(name, containerName, containerGuid, metadata, classloader, advisor);
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
}
