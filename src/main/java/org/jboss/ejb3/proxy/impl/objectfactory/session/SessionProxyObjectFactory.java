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
package org.jboss.ejb3.proxy.impl.objectfactory.session;

import java.util.List;
import java.util.Map;

import javax.naming.Name;

import org.jboss.ejb3.proxy.impl.factory.ProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.SessionSpecProxyFactory;
import org.jboss.ejb3.proxy.impl.objectfactory.Ejb3RegistrarProxyObjectFactory;
import org.jboss.ejb3.proxy.impl.objectfactory.ProxyFactoryReferenceAddressTypes;
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
public abstract class SessionProxyObjectFactory extends Ejb3RegistrarProxyObjectFactory
{

   // --------------------------------------------------------------------------------||
   // Class Members  -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(SessionProxyObjectFactory.class);

   // --------------------------------------------------------------------------------||
   // Required Implementations  ------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates an proxy from the appropriate ProxyFactory as determined by 
    * metadata in the specified reference addresses
    * 
    * @param name The JNDI Name looked up
    * @param referenceAddresses A Map of RefAddr instances in form key = type and 
    *       value = List of values for this type
    */
   protected Object createProxy(ProxyFactory factory, Name name, Map<String, List<String>> referenceAddresses)
   {
      // Initialize
      Object proxy = null;

      // Determine if a home is defined here
      boolean hasHome = this.hasHome(name, referenceAddresses);

      // Determine if a business interface is defined here
      boolean hasBusiness = this.hasBusiness(name, referenceAddresses);

      // Cast
      SessionSpecProxyFactory sFactory = null;
      try
      {
         sFactory = (SessionSpecProxyFactory) this.getProxyFactoryClass().cast(factory);
      }
      catch (ClassCastException cce)
      {
         throw new RuntimeException(ProxyFactory.class.getSimpleName() + " used in "
               + SessionProxyObjectFactory.class.getSimpleName() + " must be of type "
               + SessionSpecProxyFactory.class.getName() + " but was instead " + factory, cce);
      }

      // If home and business are bound together
      if (hasHome && hasBusiness)
      {
         proxy = sFactory.createProxyDefault();
         log.debug("Created Proxy for both EJB2.x Home and EJB3 Business Interfaces.");
      }
      // If bound to home only
      else if (hasHome)
      {
         proxy = sFactory.createProxyHome();
         log.debug("Created Proxy for EJB2.x Home Interface(s)");
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
            proxy = sFactory.createProxyBusiness(businessInterface);
            log.debug("Created Proxy of type " + proxy.getClass().getSimpleName() + " for EJB3 Business Interface: "
                  + businessInterface);

            // Ensure the proxy is visible to the TCL
            proxy = this.redefineProxyInTcl(proxy);
         }
         else
         {
            // Use a general-purpose proxy for all business interfaces
            proxy = sFactory.createProxyDefault();
            log.debug("Created Proxy of type " + proxy.getClass().getSimpleName() + " for EJB3 Business Interfaces: "
                  + businessInterfaces);
         }
      }
      // No valid type is bound here
      else
      {
         throw new RuntimeException(factory + " found associated with JNDI Binding " + name.toString()
               + " is not bound to create/return any valid EJB2.x Home or EJB3 Business Interfaces");
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
   protected Class<SessionSpecProxyFactory> getProxyFactoryClass()
   {
      return SessionSpecProxyFactory.class;
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

}
