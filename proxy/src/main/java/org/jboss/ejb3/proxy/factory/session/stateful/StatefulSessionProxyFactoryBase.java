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

import java.lang.reflect.Constructor;
import java.util.Set;

import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactoryBase;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulProxyInvocationHandler;
import org.jboss.ejb3.proxy.intf.StatefulSessionProxy;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatefulSessionProxyFactoryBase
 * 
 * Base upon which SFSB Proxy Factories may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class StatefulSessionProxyFactoryBase extends SessionProxyFactoryBase implements SessionProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param metadata The metadata representing this SFSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    */
   public StatefulSessionProxyFactoryBase(final JBossSessionBeanMetaData metadata, final ClassLoader classloader)
   {
      // Call Super
      super(metadata, classloader);
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

   /**
    * Returns the Constructor of the SessionProxyInvocationHandler to be used in 
    * instanciating new handlers to specify in Proxy Creation
    * 
    * @return
    */
   @Override
   protected final Constructor<StatefulProxyInvocationHandler> getInvocationHandlerConstructor()
   {
      try
      {
         return StatefulProxyInvocationHandler.class.getConstructor(new Class[]
         {String.class});
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException("Could not find Constructor with one String argument for "
               + StatefulProxyInvocationHandler.class.getName(), e);
      }
   }

   /**
    * Returns Proxy interfaces common to all Proxies generated
    * by this ProxyFactory
    * 
    * @return
    */
   @Override
   protected Set<Class<?>> getProxyInterfaces()
   {
      // Initialize
      Set<Class<?>> interfaces = super.getProxyInterfaces();

      // Add
      interfaces.add(StatefulSessionProxy.class);

      // Return
      return interfaces;
   }
}
