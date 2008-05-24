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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryNotRegisteredException;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryRegistry;
import org.jboss.logging.Logger;

/**
 * ProxyObjectFactory
 *
 * Base upon which Proxy Object Factories may build.  Defines 
 * abstractions to:
 * 
 * <ul>
 *  <li>Obtain a proxy based on metadata received 
 *  from Reference Address information associated with the bound 
 *  Reference</li> 
 *  <li>Use a pluggable ProxyFactoryRegistry</li>
 * </ul>
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ProxyObjectFactory implements ObjectFactory, Serializable
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(ProxyObjectFactory.class.getName());

   // --------------------------------------------------------------------------------||
   // Required Implementations  ------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns an appropriate Proxy based on the Reference Address information
    * associated with the Reference (obj) bound at name in the specified nameCtx with 
    * specified environment.
    * 
    * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, 
    *       javax.naming.Name, javax.naming.Context, java.util.Hashtable)
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
         throws Exception
   {
      // Log
      log.debug(ProxyObjectFactory.class.getName() + " servicing request for " + name.toString());

      // Cast Reference
      assert (Reference.class.isAssignableFrom(obj.getClass())) : "Object bound at " + name.toString()
            + " was not of expected type " + Reference.class.getName();
      Reference ref = (Reference) obj;

      // Get a useful object for handling Reference Addresses
      Map<String, List<String>> refAddrs = this.getReferenceAddresses(ref);

      // Obtain the key used for looking up the appropriate ProxyFactory in the Registry
      List<String> proxyFactoryRegistryKeys = refAddrs
            .get(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY);
      String assertionErrorMessage = "Exactly one Reference Address of type \""
            + ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY + "\" is required, found "
            + proxyFactoryRegistryKeys;
      assert proxyFactoryRegistryKeys != null : assertionErrorMessage;
      assert proxyFactoryRegistryKeys.size() == 1 : assertionErrorMessage;
      String proxyFactoryRegistryKey = proxyFactoryRegistryKeys.get(0);

      // Obtain Proxy Factory Registry
      ProxyFactoryRegistry registry = this.getProxyFactoryRegistry();
      assert registry != null : "Returned null " + ProxyFactoryRegistry.class.getName();

      // Obtain Proxy Factory
      ProxyFactory proxyFactory = null;
      try
      {
         proxyFactory = registry.getProxyFactory(proxyFactoryRegistryKey);
      }
      catch (ProxyFactoryNotRegisteredException pfnre)
      {
         throw new RuntimeException(pfnre);
      }
      assert proxyFactory != null : ProxyFactory.class.getName() + " returned from " + registry + " at key "
            + proxyFactoryRegistryKey + " was null";

      // Return the proxy returned from the ProxyFactory
      Object proxy = this.getProxy(proxyFactory, name, refAddrs);
      assert proxy != null : "Proxy returned from " + proxyFactory + " was null.";
      return proxy;
   }

   /**
    * Obtains the container name bound as a reference address to the JNDI Name specified
    * 
    * @param name
    * @param referenceAddresses
    * @return
    */
   protected String getContainerName(Name name, Map<String, List<String>> referenceAddresses)
   {
      // Get the Container Name
      String refAddrType = ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_EJBCONTAINER_NAME;
      List<String> containerNames = referenceAddresses.get(refAddrType);
      assert containerNames != null : RefAddr.class.getSimpleName() + " type of " + refAddrType
            + " is required to find the EJB Container associated with the " + Reference.class.getSimpleName()
            + " for JNDI Name " + name;
      assert containerNames.size() == 1 : "Only one " + RefAddr.class.getSimpleName() + " of type " + refAddrType
            + " may be defined, instead found: " + containerNames;
      String containerName = containerNames.get(0);

      // Return
      return containerName;
   }

   // --------------------------------------------------------------------------------||
   // Specifications -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected abstract Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses);

   protected abstract ProxyFactoryRegistry getProxyFactoryRegistry();

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Underlying Enumeration for handling Reference Addresses is clumsy (though ordered properly);
    * iterate through and put in a useful form for this implementation
    * 
    * @param ref
    * @return A Map consisting of keys holding reference types, and values of
    *   Lists containing their contents
    */
   private Map<String, List<String>> getReferenceAddresses(Reference ref)
   {

      // Initialize and instanciate a more reasonable object for handling Reference Addresses
      Map<String, List<String>> referenceAddresses = new HashMap<String, List<String>>();

      // For all Reference Addresses
      int count = 0;
      Enumeration<RefAddr> refAddrs = ref.getAll();
      while (refAddrs.hasMoreElements())
      {
         // Get the current Reference Address information
         RefAddr refAddr = refAddrs.nextElement();
         String type = refAddr.getType();
         Class<?> expectedContentsType = String.class;
         assert (expectedContentsType.isAssignableFrom(refAddr.getContent().getClass())) : "Content of Reference Address of type \""
               + type + "\" at index " + count + " was not of expected Java type " + expectedContentsType.getName();
         String content = (String) refAddr.getContent();

         // If our map doesn't yet contain an entry for this type
         if (!referenceAddresses.containsKey(type))
         {
            // Create an entry in the Map to hold the reference addresses
            referenceAddresses.put(type, new ArrayList<String>());
         }

         // Place an entry for the contents at index "type"
         referenceAddresses.get(type).add(content);
         log.trace("Found reference type \"" + type + "\" with content \"" + content + "\"");

         // Increase the internal counter
         count++;
      }

      // Return
      return referenceAddresses;

   }

}
