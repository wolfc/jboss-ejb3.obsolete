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

import java.lang.reflect.Constructor;
import java.util.Set;

import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactoryBase;
import org.jboss.ejb3.proxy.handler.session.stateless.StatelessProxyInvocationHandler;
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
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private Constructor<StatelessProxyInvocationHandler> invocationHandlerConstructor;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param name The unique name for this ProxyFactory
    * @param metadata The metadata representing this SLSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    */
   public StatelessSessionProxyFactoryBase(final String name, final JBossSessionBeanMetaData metadata,
         final ClassLoader classloader)
   {
      // Call Super
      super(name, metadata, classloader);
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
   protected final Constructor<StatelessProxyInvocationHandler> getInvocationHandlerConstructor()
   {
      // If not created
      if (this.invocationHandlerConstructor == null)
      {
         // Initialize
         Constructor<StatelessProxyInvocationHandler> ctor = null;
         try
         {
            // Create
            Class<?>[] args = new Class[]
            {String.class};
            log.debug("Creating " + Constructor.class.getSimpleName() + " to "
                  + StatelessProxyInvocationHandler.class.getName() + " with arguments: " + args);
            ctor = StatelessProxyInvocationHandler.class.getConstructor(args);
         }
         catch (NoSuchMethodException e)
         {
            throw new RuntimeException("Could not find Constructor with one String argument for "
                  + StatelessProxyInvocationHandler.class.getName(), e);
         }
         this.setInvocationHandlerConstructor(ctor);
      }

      // Return
      return this.invocationHandlerConstructor;
   }

   private void setInvocationHandlerConstructor(
         Constructor<StatelessProxyInvocationHandler> invocationHandlerConstructor)
   {
      this.invocationHandlerConstructor = invocationHandlerConstructor;
   }
}
