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
package org.jboss.ejb3.proxy.impl.jndiregistrar;

import org.jboss.aop.Advisor;
import org.jboss.aop.Dispatcher;
import org.jboss.ejb3.proxy.impl.factory.ProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.service.ServiceLocalProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.service.ServiceRemoteProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * JndiServiceRegistrar
 * 
 * Responsible for binding of ObjectFactories and
 * creation/registration of associated ProxyFactories, 
 * centralizing operations for @Service Implementations
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JndiServiceRegistrar extends JndiSessionRegistrarBase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(JndiServiceRegistrar.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a JNDI Registrar from the specified configuration properties, none of
    * which may be null.
    * 
    * @param serviceProxyObjectFactoryType String representation of the JNDI Object Factory to use for @Service
    */
   public JndiServiceRegistrar(String serviceProxyObjectFactoryType)
   {
      super(serviceProxyObjectFactoryType);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates and returns a new local proxy factory for this @Service
    * 
    * @param name The unique name for the ProxyFactory
    * @param containerName The name of the Container upon which Proxies 
    *   from the returned ProxyFactory will invoke
    * @param containerGuid The globally-unique name of the container
    * @param smd The metadata representing this SFSB
    * @param cl The ClassLoader for this EJB Container
    * @param advisor The Advisor for proxies created by this factory
    */
   @Override
   protected ProxyFactory createLocalProxyFactory(final String name, final String containerName,
         final String containerGuid, final JBossSessionBeanMetaData smd, final ClassLoader cl, final Advisor advisor)
   {
      assert (smd instanceof JBossServiceBeanMetaData) : "Specified metadata was not of expected type "
            + JBossServiceBeanMetaData.class.getSimpleName();
      JBossServiceBeanMetaData serviceMd = (JBossServiceBeanMetaData) smd;
      return new ServiceLocalProxyFactory(name, containerName, containerGuid, serviceMd, cl, advisor);
   }

   /**
    * Creates and returns a new remote proxy factory for this SFSB
    * 
    * @param name The unique name for the ProxyFactory
    * @param containerName The name of the Container upon which Proxies 
    *   from the returned ProxyFactory will invoke
    * @param containerGuid The globally-unique name of the container
    * @param smd The metadata representing this SFSB
    * @param cl The ClassLoader for this EJB Container
    * @param url The URL to use for Remoting
    * @param advisor The Advisor for proxies created by this factory
    * @param interceptorStackName The name of the client-side interceptor stack to use.
    *       If null the default will apply.
    */
   @Override
   protected ProxyFactory createRemoteProxyFactory(final String name, final String containerName,
         final String containerGuid, final JBossSessionBeanMetaData smd, final ClassLoader cl, final String url,
         final Advisor advisor, final String interceptorStackName)
   {
      // Ensure metadata is of expected type
      assert (smd instanceof JBossServiceBeanMetaData) : "Specified metadata was not of expected type "
            + JBossServiceBeanMetaData.class.getSimpleName();

      // Cast
      JBossServiceBeanMetaData serviceMd = (JBossServiceBeanMetaData) smd;

      // Create
      ProxyFactory factory = new ServiceRemoteProxyFactory(name, containerName, containerGuid, serviceMd, cl, url,
            advisor, interceptorStackName);

      // Register with Remoting
      log.debug("Registering with Remoting Dispatcher under name \"" + factory.getName() + "\": " + factory);
      Dispatcher.singleton.registerTarget(factory.getName(), factory);

      // Return
      return factory;
   }

}
