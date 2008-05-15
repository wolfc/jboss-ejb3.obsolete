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
package org.jboss.ejb3.proxy.objectfactory.session;

import java.util.List;
import java.util.Map;

import javax.naming.Name;
import javax.naming.RefAddr;

import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.objectfactory.McProxyObjectFactory;
import org.jboss.ejb3.proxy.objectfactory.ProxyFactoryReferenceAddressTypes;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryNotRegisteredException;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryRegistry;
import org.jboss.logging.Logger;

/**
 * SessionProxyObjectFactory
 * 
 * A Base JNDI Object Factory responsible for parsing metadata
 * obtained from Reference Address information, and
 * returning the appropriate Session Proxy
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionProxyObjectFactory extends McProxyObjectFactory
{

   // --------------------------------------------------------------------------------||
   // Class Members  -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(SessionProxyObjectFactory.class);

   // --------------------------------------------------------------------------------||
   // Required Implementations  ------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   protected Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses)
   {
      // Initialize
      Object proxy = null;

      // Determine if a home is defined here
      boolean hasHome = this.hasHome(name, referenceAddresses);

      // Determine if a business interface is defined here
      boolean hasBusiness = this.hasBusiness(name, referenceAddresses);

      // Obtain Proxy Factory Registry
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();
      assert registry != null : ProxyFactoryRegistry.class.getSimpleName() + " is required but found null reference";

      // Obtain Proxy Factory
      SessionProxyFactory factory = null;
      Object pFactory = null;
      List<String> proxyFactoryRegistryKeys = referenceAddresses
            .get(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY);
      assert proxyFactoryRegistryKeys != null : "Required " + RefAddr.class.getSimpleName() + " of type "
            + ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY
            + " is required present in JNDI at " + name.toString() + " but was not found";
      assert proxyFactoryRegistryKeys.size() == 1 : "Exactly one " + RefAddr.class.getSimpleName() + " of type "
            + ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY
            + " should be defined but instead found " + proxyFactoryRegistryKeys;
      String proxyFactoryRegistryKey = proxyFactoryRegistryKeys.get(0);
      assert proxyFactoryRegistryKey != null && !proxyFactoryRegistryKey.equals("") : "Required "
            + RefAddr.class.getSimpleName() + " of type "
            + ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY
            + " is required present in JNDI at " + name.toString() + " but was not found";
      try
      {
         // Get the Factory
         pFactory = registry.getProxyFactory(proxyFactoryRegistryKey);
         // Cast into a SessionProxyFactory
         factory = SessionProxyFactory.class.cast(pFactory);
         log.debug("Using: " + factory + " as specified from JNDI reference " + name.toString());
      }
      catch (ProxyFactoryNotRegisteredException e)
      {
         throw new RuntimeException("Expected " + SessionProxyFactory.class.getName() + " in " + registry
               + " under key \"" + proxyFactoryRegistryKey + "\" but found none", e);
      }
      catch (ClassCastException cce)
      {
         throw new RuntimeException("Found Proxy Factory in " + registry + " under key \"" + proxyFactoryRegistryKey
               + "\", but was of type " + pFactory.getClass().getName() + " instead of expected "
               + SessionProxyFactory.class.getName(), cce);
      }

      // If home and business are bound together
      if (hasHome && hasBusiness)
      {
         proxy = factory.createProxyBusinessAndHome();
         log.debug("Created Proxy " + proxy + " for both EJB2.x and EJB3 Business Interfaces.");
      }
      // If bound to home only
      else if (hasHome)
      {
         proxy = factory.createProxyHome();
         log.debug("Created Proxy " + proxy + " for EJB2.x Home Interface(s)");
      }
      // If bound to business only
      else if (hasBusiness)
      {
         // Initialize
         String type = null;

         // If local
         if (this.hasLocalBusiness(referenceAddresses))
         {
            type = ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL;
         }
         // If remote
         else
         {
            type = ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE;
         }

         // Get all business interfaces to be used
         List<String> businessInterfaces = referenceAddresses.get(type);

         // If only one is defined
         if (businessInterfaces.size() == 1)
         {
            // Obtain a proxy specific to this business interface
            String businessInterface = businessInterfaces.get(0);
            proxy = factory.createProxyBusiness(businessInterface);
            log.debug("Created Proxy " + proxy + " for EJB3 Business Interface: " + businessInterface);
         }
         else
         {
            // Use a general-purpose proxy for all business interfaces
            proxy = factory.createProxyBusiness();
            log.debug("Created Proxy " + proxy + " for EJB3 Business Interfaces: " + businessInterfaces);
         }
      }
      // No valid type is bound here
      else
      {
         throw new RuntimeException(factory + " found associated with JNDI Binding " + name.toString()
               + " is not bound to create/return any valid EJB2.x Home or EJB3 Business Interfaces");
      }

      // Return
      return proxy;
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Looks to the metadata specified by the reference addresses to determine if
    * an EJB2.x home interface is defined here.  Additionally checks to ensure
    * that both remote and local homes are not bound to the same JNDI Address
    * 
    * @param name
    * @param referenceAddresses
    * @return
    */
   protected boolean hasHome(Name name, Map<String, List<String>> referenceAddresses)
   {
      // Initialize
      boolean hasHome = false;

      // Obtain metadata
      boolean hasLocalHome = referenceAddresses
            .containsKey(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_LOCAL);
      boolean hasRemoteHome = referenceAddresses
            .containsKey(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE);

      // Ensure both local and remote home are not specified here
      String errorMessage = "ObjectFactory at JNDI \"" + name.toString()
            + "\" contains references to both local and remote homes";
      assert !(hasLocalHome && hasRemoteHome) : errorMessage;
      if (hasLocalHome && hasRemoteHome)
      {
         throw new RuntimeException(errorMessage);
      }

      // Set if has home defined
      hasHome = hasLocalHome || hasRemoteHome;

      // Return
      return hasHome;
   }

   /**
    * Looks to the metadata specified by the reference addresses to determine if
    * an EJB3 Business View is defined here.  Additionally checks that both local 
    * and remote business interfaces are not bound to the same JNDI Address
    * 
    * @param name
    * @param referenceAddresses
    * @return
    */
   protected boolean hasBusiness(Name name, Map<String, List<String>> referenceAddresses)
   {
      // Initialize
      boolean hasBusiness = false;

      // Obtain metadata
      boolean hasLocalBusiness = this.hasLocalBusiness(referenceAddresses);
      boolean hasRemoteBusiness = this.hasRemoteBusiness(referenceAddresses);

      // Ensure both local and remote home are not specified here
      String errorMessage = "ObjectFactory at JNDI \"" + name.toString()
            + "\" contains references to both local and remote business interfaces";
      assert !(hasLocalBusiness && hasRemoteBusiness) : errorMessage;
      if (hasLocalBusiness && hasRemoteBusiness)
      {
         throw new RuntimeException(errorMessage);
      }

      // Set 
      hasBusiness = hasLocalBusiness || hasRemoteBusiness;

      // Return
      return hasBusiness;

   }

   /**
    * Determines if the specified metadata contains a type of local business
    * 
    * @param referenceAddresses
    * @return
    */
   protected boolean hasLocalBusiness(Map<String, List<String>> referenceAddresses)
   {
      return referenceAddresses
            .containsKey(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL);
   }

   /**
    * Determines if the specified metadata contains a type of remote business
    * 
    * @param referenceAddresses
    * @return
    */
   protected boolean hasRemoteBusiness(Map<String, List<String>> referenceAddresses)
   {
      return referenceAddresses
            .containsKey(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE);
   }

}
