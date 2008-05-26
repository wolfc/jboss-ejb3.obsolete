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

import javax.naming.Context;

import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateful.StatefulSessionLocalProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateful.StatefulSessionRemoteProxyFactory;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * JndiStatefulSessionRegistrar
 * 
 * Responsible for binding of ObjectFactories and
 * creation/registration of associated ProxyFactories, 
 * centralizing operations for SFSB Implementations
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JndiStatefulSessionRegistrar extends JndiSessionRegistrarBase
{

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a JNDI Registrar from the specified configuration properties, none of
    * which may be null.
    * 
    * @param context The JNDI Context into which Objects will be bound
    * @param statelessSessionProxyObjectFactoryType String representation of the JNDI Object Factory to use for SLSBs
    */
   public JndiStatefulSessionRegistrar(Context context, String statelessSessionProxyObjectFactoryType)
   {
      super(context, statelessSessionProxyObjectFactoryType);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates and returns a new local proxy factory for this SFSB
    * 
    *  @param smd The metadata representing this SFSB
    *  @param cl The ClassLoader for this EJB Container
    */
   @Override
   protected SessionProxyFactory createLocalProxyFactory(JBossSessionBeanMetaData smd, ClassLoader cl)
   {
      return new StatefulSessionLocalProxyFactory(smd, cl);
   }

   /**
    * Creates and returns a new remote proxy factory for this SFSB
    * 
    *  @param smd The metadata representing this SFSB
    *  @param cl The ClassLoader for this EJB Container
    */
   @Override
   protected SessionProxyFactory createRemoteProxyFactory(final JBossSessionBeanMetaData smd, final ClassLoader cl)
   {
      return new StatefulSessionRemoteProxyFactory(smd, cl);
   }

}
