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
package org.jboss.ejb3.proxy.impl.objectfactory.session.service;

import java.util.List;
import java.util.Map;

import javax.naming.Name;

import org.jboss.ejb3.proxy.impl.factory.ProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.service.ServiceProxyFactory;
import org.jboss.ejb3.proxy.impl.objectfactory.Ejb3RegistrarProxyObjectFactory;
import org.jboss.ejb3.proxy.impl.objectfactory.ProxyFactoryReferenceAddressTypes;
import org.jboss.logging.Logger;

/**
 * ServiceProxyObjectFactory
 * 
 * A JNDI ObjectFactory responsible for returning the
 * appropriate @Service Proxy upon lookup
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ServiceProxyObjectFactory extends Ejb3RegistrarProxyObjectFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members  -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(ServiceProxyObjectFactory.class);

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * TODO
    * 
    * This implementation simply creates a new proxy upon each lookup.  We should
    * improve performance by providing a caching mechanism to cache:
    * 
    * Proxy per JNDI Address
    * 
    * Can be done via a static Map?  Not in an instance variable because
    * ObjectFactories are made upon each lookup
    */
   /**
    * @Service Object Factories must always create a new @Service Proxy if one is not
    * available in the internal cache, otherwise use the cached one and return
    * 
    * @param proxyFactory The ProxyFactory to use
    * @param name The JNDI name looked up
    * @param referenceAddresses
    */
   @Override
   protected Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses)
   {
      // Initialize
      Object proxy = null;

      // Determine if a business interface is defined here
      boolean hasBusiness = this.hasBusiness(name, referenceAddresses);

      // Cast
      ServiceProxyFactory sFactory = null;
      try
      {
         sFactory = this.getProxyFactoryClass().cast(proxyFactory);
      }
      catch (ClassCastException cce)
      {
         throw new RuntimeException(ProxyFactory.class.getSimpleName() + " used in "
               + ServiceProxyFactory.class.getSimpleName() + " must be of type " + ServiceProxyFactory.class.getName()
               + " but was instead " + proxyFactory, cce);
      }

      // If bound to business
      if (hasBusiness)
      {
         // Use a general-purpose proxy for all business interfaces
         proxy = sFactory.createProxyDefault();
         log.debug("Created Proxy of type " + proxy.getClass().getSimpleName() + ".");

      }
      // No valid type is bound here
      else
      {
         throw new RuntimeException(proxyFactory + " found associated with JNDI Binding " + name.toString()
               + " is not bound to create/return any valid @Service Business Interfaces");
      }

      // Obtain the target container name
      String containerName = this.getSingleRequiredReferenceAddressValue(name, referenceAddresses,
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_EJBCONTAINER_NAME);
      assert containerName != null && containerName.trim().length() > 0 : "Container Name must be specified via reference address + \""
            + ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_EJBCONTAINER_NAME + "\"";

      // Return
      return proxy;
   }

   /**
    * Obtains the type or supertype used by proxy factories for this Object Factory
    * @return
    */
   @Override
   protected Class<ServiceProxyFactory> getProxyFactoryClass()
   {
      return ServiceProxyFactory.class;
   }

}
