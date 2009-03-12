/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.statefulproxyfactoryoverride;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.proxy.impl.factory.session.stateful.StatefulSessionRemoteProxyFactory;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * NoOpStatefulRemoteProxyFactory
 * 
 * A Proxy Factory that dispatches Proxies which will
 * throw NoOpException upon every invocation
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class NoOpStatefulRemoteProxyFactory extends StatefulSessionRemoteProxyFactory
{

   //----------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   //----------------------------------------------------------------------------||

   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(NoOpStatefulRemoteProxyFactory.class);

   /**
    * Handler to be used, always
    */
   private static final SessionProxyInvocationHandler HANDLER = new NoOpSessionProxyInvocationHandler();

   //----------------------------------------------------------------------------||
   // Constructors --------------------------------------------------------------||
   //----------------------------------------------------------------------------||

   /**
    * Sole Constructor
    */
   public NoOpStatefulRemoteProxyFactory(String name, String containerName, String containerGuid,
         JBossSessionBeanMetaData metadata, ClassLoader classloader, String url, Advisor advisor,
         String interceptorStackName)
   {
      super(name, containerName, containerGuid, metadata, classloader, url, advisor, interceptorStackName);
   }

   //----------------------------------------------------------------------------||
   // Overridden Implementations ------------------------------------------------||
   //----------------------------------------------------------------------------||

   @Override
   protected SessionProxyInvocationHandler createBusinessDefaultInvocationHandler()
   {
      return HANDLER;
   }

   @Override
   protected SessionProxyInvocationHandler createBusinessInterfaceSpecificInvocationHandler(String businessInterfaceName)
   {
      return HANDLER;
   }

   @Override
   protected SessionProxyInvocationHandler createEjb2xComponentInterfaceInvocationHandler()
   {
      return HANDLER;
   }

   @Override
   protected SessionProxyInvocationHandler createHomeInvocationHandler()
   {
      return HANDLER;
   }

}
