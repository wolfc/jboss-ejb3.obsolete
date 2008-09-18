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
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
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

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;

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
      String proxyFactoryRegistryKey = this.getSingleRequiredReferenceAddressValue(name, refAddrs,
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY);

      /*
       * Obtain the Proxy Factory either
       * locally via the Ejb3Registry, or
       * via the remote Dispatcher
       */

      // Obtain Proxy Factory
      ProxyFactory proxyFactory = null;

      try
      {
         // Attempt to get local EJB3 Registrar
         //TODO EJBTHREE-1403
         // This is the wrong way to determine local/remote 
         Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();

         // Local lookup succeeded, so use it
         // BES 2008/08/22 -- a NotBoundException doesn't mean failure, just
         // means the container isn't deployed in this server. So don't catch it
         // in an inner try/catch; let it propagate to the outer catch.
         // This may or may not be the proper fix for EJBTHREE-1403, but it allows
         // clustering unit tests to pass.
//         try
//         {
            Object pfObj = registrar.lookup(proxyFactoryRegistryKey);
            assert pfObj != null : ProxyFactory.class.getName() + " from key " + proxyFactoryRegistryKey + " was null";
            assert pfObj instanceof ProxyFactory : " Object obtained from key " + proxyFactoryRegistryKey
                  + " was expected to be of type " + ProxyFactory.class.getName() + " but was instead " + pfObj;
            proxyFactory = (ProxyFactory) pfObj;
//         }
//         catch (NotBoundException nbe)
//         {
//            throw new RuntimeException("Could not obtain " + ProxyFactory.class.getSimpleName()
//                  + " from expected key \"" + proxyFactoryRegistryKey + "\"", nbe);
//         }
      }
      // Registrar is not local, so use Remoting to Obtain Proxy Factory
      catch (NotBoundException nbe)
      {
         proxyFactory = createProxyFactoryProxy(name, refAddrs, proxyFactoryRegistryKey);
      }

      // Get the proxy returned from the ProxyFactory
      Object proxy = this.getProxy(proxyFactory, name, refAddrs);
      assert proxy != null : "Proxy returned from " + proxyFactory + " was null.";

      // Return the Proxy
      return proxy;
   }

   /**
    * Creates a remoting proxy to the proxy factory.
    * 
    * @param name
    * @param refAddrs
    * @param proxyFactoryRegistryKey
    * @return
    * @throws Exception
    */
   protected ProxyFactory createProxyFactoryProxy(Name name, Map<String, List<String>> refAddrs,
         String proxyFactoryRegistryKey) throws Exception
   {
      // Obtain the URL for invoking upon the Registry
      String url = this.getSingleRequiredReferenceAddressValue(name, refAddrs,
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_INVOKER_LOCATOR_URL);

      // Create an InvokerLocator
      assert url != null && !url.trim().equals("") : InvokerLocator.class.getSimpleName()
            + " URL is required, but is not specified; improperly bound reference in JNDI";
      InvokerLocator locator = new InvokerLocator(url);

      // Create a POJI Proxy to the Registrar
      Interceptor[] interceptors =
      {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};
      PojiProxy handler = new PojiProxy(proxyFactoryRegistryKey, locator, interceptors);
      Class<?>[] interfaces = new Class<?>[]{this.getProxyFactoryClass()};
      
      return (ProxyFactory) Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
   }

   /**
    * Obtains the single value of the specified type as obtained from the specified reference 
    * addresses bound at the specified Name.  Asserts that the value exists and is the only one
    * for the specified type. 
    * 
    * @param name
    * @param referenceAddresses
    * @param refAddrType
    * @return
    */
   protected String getSingleRequiredReferenceAddressValue(Name name, Map<String, List<String>> referenceAddresses,
         String refAddrType)
   {
      // Get the value
      String value = this.getSingleReferenceAddressValue(name, referenceAddresses, refAddrType);
      assert (value != null && !value.trim().equals("")) : "Exactly one " + RefAddr.class.getSimpleName() + " of type "
            + refAddrType + " must be defined for Name " + name.toString() +", none found";

      // Return
      return value;
   }

   /**
    * Obtains the single value of the specified type as obtained from the specified reference 
    * addresses bound at the specified Name.  Asserts that the value exists and is the only one
    * for the specified type. 
    * 
    * @param name
    * @param referenceAddresses
    * @param refAddrType
    * @return
    */
   protected String getSingleReferenceAddressValue(Name name, Map<String, List<String>> referenceAddresses,
         String refAddrType)
   {
      // Get the values
      List<String> values = referenceAddresses.get(refAddrType);
      assert values == null || values.size() == 1 : "Only one " + RefAddr.class.getSimpleName() + " of type "
            + refAddrType + " may be defined, instead found: " + values;
      String value = null;
      if (values != null)
      {
         value = values.get(0).trim();
      }

      // Return
      return value;
   }

   // --------------------------------------------------------------------------------||
   // Specifications -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected abstract Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses);

   /**
    * Obtains the type or supertype used by proxy factories for this Object Factory
    * @return
    */
   protected abstract Class<?> getProxyFactoryClass();

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
         Object refAddrContent = refAddr.getContent();
         assert (refAddrContent != null) : "Encountered Reference Address of type " + type + " but with null Content";
         assert (expectedContentsType.isAssignableFrom(refAddrContent.getClass())) : "Content of Reference Address of type \""
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
