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
package org.jboss.ejb3.proxy.jndiregistrar;

import org.jboss.aop.Advisor;
import org.jboss.aop.Dispatcher;
import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateless.StatelessSessionLocalProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateless.StatelessSessionRemoteProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * JndiStatelessSessionRegistrar
 * 
 * Responsible for binding of ObjectFactories and
 * creation/registration of associated ProxyFactories, 
 * centralizing operations for SLSB Implementations
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JndiStatelessSessionRegistrar extends JndiSessionRegistrarBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(JndiStatelessSessionRegistrar.class);

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a JNDI Registrar from the specified configuration properties, none of
    * which may be null.
    * 
    * @param statelessSessionProxyObjectFactoryType String representation of the JNDI Object Factory to use for SLSBs
    */
   public JndiStatelessSessionRegistrar(String statelessSessionProxyObjectFactoryType)
   {
      super(statelessSessionProxyObjectFactoryType);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates and returns a new local proxy factory for this SLSB
    * 
    * @param name The unique name for the ProxyFactory
    * @param containerName The name of the Container upon which Proxies 
    *   from the returned ProxyFactory will invoke
    * @param containerGuid The globally-unique name of the container
    * @param containerGuid The globally-unique name of the container
    * @param smd The metadata representing this SLSB
    * @param cl The ClassLoader for this EJB Container
    * @param advisor The Advisor for proxies created by this factory
    */
   @Override
   protected ProxyFactory createLocalProxyFactory(final String name, final String containerName,
         final String containerGuid, final JBossEnterpriseBeanMetaData smd, final ClassLoader cl, final Advisor advisor)
   {
      assert (smd instanceof JBossSessionBeanMetaData) : "Specified metadata was not of expected type "
            + JBossSessionBeanMetaData.class.getSimpleName();
      JBossSessionBeanMetaData sessionMd = (JBossSessionBeanMetaData) smd;
      return new StatelessSessionLocalProxyFactory(name, containerName, containerGuid, sessionMd, cl, advisor);
   }

   /**
    * Creates and returns a new remote proxy factory for this Session Bean
    * 
    * @param name The unique name for the ProxyFactory
    * @param containerName The name of the Container upon which Proxies 
    *   from the returned ProxyFactory will invoke
    * @param containerGuid The globally-unique name of the container
    * @param smd The metadata representing this Session EJB
    * @param cl The ClassLoader for this EJB Container
    * @param url The URL to use for Remoting
    * @param advisor The Advisor for proxies created by this factory
    * @param interceptorStackName The name of the client-side interceptor stack to use.
    *       If null the default will apply.
    */
   @Override
   protected ProxyFactory createRemoteProxyFactory(final String name, final String containerName,
         final String containerGuid, final JBossEnterpriseBeanMetaData smd, final ClassLoader cl, final String url,
         final Advisor advisor, final String interceptorStackName)
   {
      // Ensure metadata is of expected type
      assert (smd instanceof JBossSessionBeanMetaData) : "Specified metadata was not of expected type "
            + JBossSessionBeanMetaData.class.getSimpleName();

      // Cast
      JBossSessionBeanMetaData sessionMd = (JBossSessionBeanMetaData) smd;

      // Create
      ProxyFactory factory = new StatelessSessionRemoteProxyFactory(name, containerName, containerGuid, sessionMd, cl,
            url, advisor, interceptorStackName);

      // Register with Remoting
      log.debug("Registering with Remoting Dispatcher under name \"" + factory.getName() + "\": " + factory);
      Dispatcher.singleton.registerTarget(factory.getName(), factory);

      // Return
      return factory;
   }

}
