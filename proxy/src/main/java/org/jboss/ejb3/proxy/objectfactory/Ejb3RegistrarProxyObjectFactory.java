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
package org.jboss.ejb3.proxy.objectfactory;

import java.io.Serializable;

import javax.naming.spi.ObjectFactory;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.objectstore.ObjectStoreBindings;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryRegistry;
import org.jboss.logging.Logger;

/**
 * Ejb3RegistrarProxyObjectFactory
 *
 * A Proxy Object Factory using an underlying 
 * Proxy Factory Registry intended to be obtained
 * as a managed object from the Object Store
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class Ejb3RegistrarProxyObjectFactory extends ProxyObjectFactory implements ObjectFactory, Serializable
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(Ejb3RegistrarProxyObjectFactory.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   //TODO
   // Inject via IoC, must be configurable
   private ProxyFactoryRegistry proxyFactoryRegistry;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public Ejb3RegistrarProxyObjectFactory()
   {
      // Set the ProxyFactoryRegistry as obtained from the EJB3 Registrar
      //TODO ProxyFactoryRegistry will be replaced by IoC itself
      ProxyFactoryRegistry registry = null;
      try
      {
         registry = (ProxyFactoryRegistry) Ejb3RegistrarLocator.locateRegistrar().lookup(
               ObjectStoreBindings.OBJECTSTORE_BEAN_NAME_PROXY_FACTORY_REGISTRY);
      }
      catch (NotBoundException e)
      {
         throw new RuntimeException(ProxyFactoryRegistry.class.getSimpleName()
               + " is required to be bound in the Object Store, but was not", e);
      }
      this.setProxyFactoryRegistry(registry);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   protected ProxyFactoryRegistry getProxyFactoryRegistry()
   {
      return this.proxyFactoryRegistry;
   }

   public void setProxyFactoryRegistry(ProxyFactoryRegistry proxyFactoryRegistry)
   {
      this.proxyFactoryRegistry = proxyFactoryRegistry;
   }

}
