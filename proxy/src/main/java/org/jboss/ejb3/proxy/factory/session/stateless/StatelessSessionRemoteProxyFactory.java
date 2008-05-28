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
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatelessSessionRemoteProxyFactory
 * 
 * A SLSB Proxy Factory for Remote Views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessSessionRemoteProxyFactory extends StatelessSessionProxyFactoryBase implements SessionProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   private static final long serialVersionUID = 1L;

   private static final Logger logger = Logger.getLogger(StatelessSessionRemoteProxyFactory.class);

   private static final String STACK_NAME_STATELESS_SESSION_CLIENT_INTERCEPTORS = "StatelessSessionClientInterceptors";

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param metadata The metadata representing this SLSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    */
   public StatelessSessionRemoteProxyFactory(final JBossSessionBeanMetaData metadata, final ClassLoader classloader)
   {
      // Call Super
      super(metadata, classloader);
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
      return this.getMetadata().getBusinessRemotes();
   }

   /**
    * Returns the String representation of the Home Interface Type
    * @return
    */
   @Override
   protected final String getHomeType()
   {
      return this.getMetadata().getHome();
   }

   /**
    * Returns the String representation of the EJB2.x Interface Type
    * 
    *  @return
    */
   @Override
   protected final String getEjb2xInterfaceType()
   {
      return this.getMetadata().getRemote();
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
      return StatelessSessionRemoteProxyFactory.STACK_NAME_STATELESS_SESSION_CLIENT_INTERCEPTORS;
   }
}
