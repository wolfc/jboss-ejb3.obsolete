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
import org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.handler.session.stateless.StatelessProxyInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatelessSessionLocalProxyFactory
 * 
 * A SLSB Proxy Factory for Local Views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessSessionLocalProxyFactory extends StatelessSessionProxyFactoryBase implements SessionProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger logger = Logger.getLogger(StatelessSessionLocalProxyFactory.class);

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
   public StatelessSessionLocalProxyFactory(final JBossSessionBeanMetaData metadata, final ClassLoader classloader,
         final String containerName)
   {
      // Call Super
      super(metadata, classloader, containerName);
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
   protected Set<String> getBusinessInterfaceTypes()
   {
      return this.getMetadata().getBusinessLocals();
   }

   /**
    * Returns the String representation of the Home Interface Type
    * @return
    */
   @Override
   protected String getHomeType()
   {
      return this.getMetadata().getLocalHome();
   }

   /**
    * Returns the String representation of the EJB.2x Interface Type
    * 
    *  @return
    */
   @Override
   protected String getEjb2xInterfaceType()
   {
      return this.getMetadata().getLocal();
   }

   /**
    * Returns the Constructor of the SessionProxyInvocationHandler to be used in 
    * instanciating new handlers to specify in Proxy Creation
    * 
    * @return
    */
   @Override
   protected Constructor<? extends SessionProxyInvocationHandler> getInvocationHandlerConstructor()
   {
      try
      {
         return StatelessProxyInvocationHandler.class.getConstructor(new Class[]
         {String.class, String.class});
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException("Could not find Constructor with two String arguments for "
               + StatelessProxyInvocationHandler.class.getName(), e);
      }
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
      // No client-side interceptors for remote
      return null;
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
