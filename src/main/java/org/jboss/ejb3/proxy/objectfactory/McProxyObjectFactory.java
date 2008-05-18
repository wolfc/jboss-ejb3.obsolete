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

import org.jboss.ejb3.proxy.hack.Hack;
import org.jboss.ejb3.proxy.mc.MicrocontainerBindings;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryRegistry;
import org.jboss.logging.Logger;

/**
 * McProxyObjectFactory
 *
 * A Proxy Object Factory using an underlying 
 * Proxy Factory Registry intended to be obtained
 * as a managed object from the MicroContainer
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class McProxyObjectFactory extends ProxyObjectFactory implements ObjectFactory, Serializable
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(McProxyObjectFactory.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   //TODO
   // Inject from MC, must be configurable
   private ProxyFactoryRegistry proxyFactoryRegistry;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public McProxyObjectFactory()
   {
      // Set the ProxyFactoryRegistry as obtained from MC
      //TODO ProxyFactoryRegistry will be replaced by MC itself
      //TODO This hack will be replaced when we have some way of getting at MC Kernel
      this.setProxyFactoryRegistry((ProxyFactoryRegistry) Hack.BOOTSTRAP.getKernel().getController()
            .getInstalledContext(MicrocontainerBindings.MC_BEAN_NAME_PROXY_FACTORY_REGISTRY).getTarget());
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
