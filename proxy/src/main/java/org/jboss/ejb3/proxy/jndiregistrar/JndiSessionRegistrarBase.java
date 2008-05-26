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

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.jboss.ejb3.common.registrar.spi.DuplicateBindException;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.objectfactory.ProxyFactoryReferenceAddressTypes;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.jboss.naming.Util;

/**
 * JndiSessionRegistrarBase
 * 
 * Responsible for binding of ObjectFactories and
 * creation/registration of associated ProxyFactories, 
 * centralizing operations common to that of all Session
 * EJB Implementations
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class JndiSessionRegistrarBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(JndiSessionRegistrarBase.class);

   /**
    * The value appended to the key used to bind proxy factories to the registry
    */
   private static final String KEY_SUFFIX_PROXY_FACTORY_REGISTRY = "/ProxyFactory";

   /**
    * The value appended to the key used to bind local proxy factories to the registry
    */
   private static final String KEY_SUFFIX_PROXY_FACTORY_REGISTRY_LOCAL = JndiSessionRegistrarBase.KEY_SUFFIX_PROXY_FACTORY_REGISTRY
         + "/local";

   /**
    * The value appended to the key used to bind local remote factories to the registry
    */
   private static final String KEY_SUFFIX_PROXY_FACTORY_REGISTRY_REMOTE = JndiSessionRegistrarBase.KEY_SUFFIX_PROXY_FACTORY_REGISTRY
         + "/remote";

   private static final String OBJECT_FACTORY_CLASSNAME_PREFIX = "Proxy for: ";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Context under which we'll bind to JNDI
    */
   private Context context;

   /**
    * Fully-qualified class name of the JNDI Object Factory to Reference
    */
   private String sessionProxyObjectFactoryType;

   //TODO @Service, SFSB

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a JNDI Registrar from the specified configuration properties, none of
    * which may be null.
    * 
    * @param context The JNDI Context into which Objects will be bound
    * @param sessionProxyObjectFactoryType String representation of the JNDI Object 
    *           Factory Class Name (fully-qualified) to use for this Session EJB
    */
   public JndiSessionRegistrarBase(final Context context, final String sessionProxyObjectFactoryType)
   {
      // Set the Context
      assert context != null : this + " may not be configured with null  " + Context.class.getName();
      this.setContext(context);
      log.debug("Using  " + Context.class.getName() + ": " + context);

      /*
       * Perform some assertions and logging
       */

      // Set the Proxy Object Factory Type
      assert sessionProxyObjectFactoryType != null && !sessionProxyObjectFactoryType.equals("") : "Session EJB Proxy "
            + ObjectFactory.class.getSimpleName() + " must be specified.";

      try
      {
         // See if the specified Session Proxy Object Factory is valid
         Class.forName(sessionProxyObjectFactoryType);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Specified " + ObjectFactory.class.getSimpleName() + " of "
               + sessionProxyObjectFactoryType + " could not be loaded.", e);
      }
      this.setSessionProxyObjectFactoryType(sessionProxyObjectFactoryType);
      log.debug("Using Session EJB JNDI " + ObjectFactory.class.getSimpleName() + ": "
            + this.getSessionProxyObjectFactoryType());
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Binds into JNDI all appropriate objects required 
    * by the EJB described by the specified metadata.  Additionally
    * responsible for creation and registration of any all ProxyFactory
    * implementations required by the EJB
    * 
    * @param md
    * @param cl The CL of the Container
    * @param containerName The name under which the target container is registered
    */
   public void bindEjb(final JBossEnterpriseBeanMetaData md, final ClassLoader cl, final String containerName)
   {
      // Assert castable
      assert (md instanceof JBossSessionBeanMetaData) : md + " claims to be a Session Bean but is not of type "
            + JBossSessionBeanMetaData.class.getName();
      JBossSessionBeanMetaData smd = null;
      try
      {
         smd = JBossSessionBeanMetaData.class.cast(md);
      }
      catch (ClassCastException cce)
      {
         throw new RuntimeException(md + " claiming to be Session Bean could not be cast to "
               + JBossSessionBeanMetaData.class.getName(), cce);
      }

      // Log 
      String ejbName = smd.getEjbName();
      log.debug("Found Session Bean: " + ejbName);

      // Get Business Locals
      BusinessLocalsMetaData businessLocals = smd.getBusinessLocals();

      // Get Business Remotes
      BusinessRemotesMetaData businessRemotes = smd.getBusinessRemotes();

      // Get Local Home
      String localHome = StringUtils.adjustWhitespaceStringToNull(smd.getLocalHome());

      // Get Remote Home
      String remoteHome = StringUtils.adjustWhitespaceStringToNull(smd.getHome());

      // Determine if there are local/remote views
      boolean hasLocalView = (localHome != null || businessLocals.size() < 1);
      boolean hasRemoteView = (remoteHome != null || businessRemotes.size() < 1);

      // If no local or remote views
      if (!hasLocalView && !hasRemoteView)
      {
         throw new RuntimeException("EJB " + smd.getEjbName() + " has no local or remote views defined.");
      }

      /*
       * Create and Register Proxy Factories
       */

      // If there's a local view
      String localProxyFactoryKey = null;
      if (hasLocalView)
      {
         // Create and register a local proxy factory
         ProxyFactory factory = this.createLocalProxyFactory(smd, cl);
         localProxyFactoryKey = this.registerProxyFactory(factory, smd, true);
      }

      // If there's a remote view
      String remoteProxyFactoryKey = null;
      if (hasRemoteView)
      {
         // Create and register a local proxy factory
         ProxyFactory factory = this.createRemoteProxyFactory(smd, cl);
         remoteProxyFactoryKey = this.registerProxyFactory(factory, smd, false);
      }

      /*
       * Bind Remote ObjectFactories to JNDI
       */

      if (hasRemoteView)
      {

         // Initialize Reference Addresses to attach to default remote JNDI Reference
         List<RefAddr> refAddrsForDefaultRemote = new ArrayList<RefAddr>();

         // For each of the remote business interfaces, make a Reference Address
         for (String businessRemote : businessRemotes)
         {
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE, businessRemote);
            refAddrsForDefaultRemote.add(refAddr);
         }

         // Determine if remote home and business remotes are bound to same JNDI Address
         boolean bindRemoteAndHomeTogether = this.isHomeAndBusinessBoundTogether(smd, false);
         if (bindRemoteAndHomeTogether)
         {
            // Add a Reference Address for the Remote Home
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE, smd.getHome());
            refAddrsForDefaultRemote.add(refAddr);
         }
         // Bind Home (not bound together) if exists
         else if (smd.getHome() != null && !smd.getHome().equals(""))
         {
            String homeType = smd.getHome();
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE, homeType);
            Reference homeRef = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX + homeType, this
                  .getSessionProxyObjectFactoryType(), null);
            homeRef.add(refAddr);
            String homeAddress = smd.determineResolvedJndiName(homeType);
            log.debug("Remote Home View for EJB " + smd.getEjbName() + " to be bound into JNDI at \"" + homeAddress
                  + "\"");
            this.bind(homeRef, homeAddress, remoteProxyFactoryKey, containerName);
         }

         /*
          * Bind ObjectFactory for default remote businesses (and home if bound together)
          */

         // Get Classname to set for Reference
         String defaultRemoteClassName = this.getHumanReadableListOfInterfacesInRefAddrs(refAddrsForDefaultRemote);

         // Create a Reference
         Reference defaultRemoteRef = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
               + defaultRemoteClassName, this.getSessionProxyObjectFactoryType(), null);

         // Add all Reference Addresses for Default Remote Reference
         for (RefAddr refAddr : refAddrsForDefaultRemote)
         {
            log.debug("Adding " + RefAddr.class.getSimpleName() + " to Default Remote "
                  + Reference.class.getSimpleName() + ": Type \"" + refAddr.getType() + "\", Content \""
                  + refAddr.getContent() + "\"");
            defaultRemoteRef.add(refAddr);
         }

         // Bind the Default Remote Reference to JNDI
         String defaultRemoteAddress = smd.determineJndiName();
         log.debug("Default Remote View for EJB " + smd.getEjbName() + " to be bound into JNDI at \""
               + defaultRemoteAddress + "\"");
         this.bind(defaultRemoteRef, defaultRemoteAddress, remoteProxyFactoryKey, containerName);

         // Bind ObjectFactory specific to each Remote Business Interface
         for (String businessRemote : businessRemotes)
         {
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE, businessRemote);
            Reference ref = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX + businessRemote,
                  this.getSessionProxyObjectFactoryType(), null);
            ref.add(refAddr);
            String address = smd.determineResolvedJndiName(businessRemote);
            log.debug("Remote Business View for " + businessRemote + " of EJB " + smd.getEjbName()
                  + " to be bound into JNDI at \"" + address + "\"");
            this.bind(ref, address, remoteProxyFactoryKey, containerName);

         }
      }

      if (hasLocalView)
      {
         // Initialize Reference Addresses to attach to default local JNDI Reference
         List<RefAddr> refAddrsForDefaultLocal = new ArrayList<RefAddr>();

         // For each of the local business interfaces, make a Reference Address
         for (String businessLocal : businessLocals)
         {
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL, businessLocal);
            refAddrsForDefaultLocal.add(refAddr);
         }

         // Determine if local home and business locals are bound to same JNDI Address
         boolean bindLocalAndLocalHomeTogether = this.isHomeAndBusinessBoundTogether(smd, true);
         if (bindLocalAndLocalHomeTogether)
         {
            // Add a Reference Address for the Local Home
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_LOCAL, smd.getLocalHome());
            refAddrsForDefaultLocal.add(refAddr);
         }
         // Bind Local Home (not bound together) if exists
         else if (smd.getLocalHome() != null && !smd.getLocalHome().equals(""))
         {
            String localHomeType = smd.getLocalHome();
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_LOCAL, localHomeType);
            Reference localHomeRef = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
                  + localHomeType, this.getSessionProxyObjectFactoryType(), null);
            localHomeRef.add(refAddr);
            String localHomeAddress = smd.determineResolvedJndiName(localHomeType);
            log.debug("Local Home View for EJB " + smd.getEjbName() + " to be bound into JNDI at \"" + localHomeAddress
                  + "\"");
            this.bind(localHomeRef, localHomeAddress, localProxyFactoryKey, containerName);
         }

         /*
          * Bind ObjectFactory for default local businesses (and LocalHome if bound together)
          */

         // Get Classname to set for Reference
         String defaultLocalClassName = this.getHumanReadableListOfInterfacesInRefAddrs(refAddrsForDefaultLocal);

         // Create a Reference
         Reference defaultLocalRef = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
               + defaultLocalClassName, this.getSessionProxyObjectFactoryType(), null);

         // Add all Reference Addresses for Default Local Reference
         for (RefAddr refAddr : refAddrsForDefaultLocal)
         {
            log.debug("Adding " + RefAddr.class.getSimpleName() + " to Default Local "
                  + Reference.class.getSimpleName() + ": Type \"" + refAddr.getType() + "\", Content \""
                  + refAddr.getContent() + "\"");
            defaultLocalRef.add(refAddr);
         }

         // Bind the Default Local Reference to JNDI
         String defaultLocalAddress = smd.determineLocalJndiName();
         log.debug("Default Local View for EJB " + smd.getEjbName() + " to be bound into JNDI at \""
               + defaultLocalAddress + "\"");
         this.bind(defaultLocalRef, defaultLocalAddress, localProxyFactoryKey, containerName);

         // Bind ObjectFactory specific to each Local Business Interface
         for (String businessLocal : businessLocals)
         {
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL, businessLocal);
            Reference ref = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX + businessLocal,
                  this.getSessionProxyObjectFactoryType(), null);
            ref.add(refAddr);
            String address = smd.determineResolvedJndiName(businessLocal);
            log.debug("Local Business View for " + businessLocal + " of EJB " + smd.getEjbName()
                  + " to be bound into JNDI at \"" + address + "\"");
            this.bind(ref, address, localProxyFactoryKey, containerName);

         }
      }
   }

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates and returns a new local proxy factory for this Session Bean
    * 
    *  @param smd The metadata representing this Session EJB
    *  @param cl The ClassLoader for this EJB Container
    */
   protected abstract SessionProxyFactory createLocalProxyFactory(final JBossSessionBeanMetaData smd,
         final ClassLoader cl);

   /**
    * Creates and returns a new remote proxy factory for this Session Bean
    * 
    *  @param smd The metadata representing this Session EJB
    *  @param cl The ClassLoader for this EJB Container
    */
   protected abstract SessionProxyFactory createRemoteProxyFactory(final JBossSessionBeanMetaData smd,
         final ClassLoader cl);

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Binds the specified Reference into JNDI at the specified address, adding 
    * the requisite key for the ProxyFactory within the Registry and the requisite
    * target EJB Container Name as ReferenceAddresses
    * 
    * @param ref
    * @param address
    * @param proxyFactoryRegistryKey The key under which the proxy factory 
    *   for this reference is stored in the proxy factory registry
    * @param containerName The target container to be used in invocations from Proxies obtained from this address
    */
   protected void bind(Reference ref, String address, String proxyFactoryRegistryKey, String containerName)
   {
      // Add the Proxy Factory Registry key for this Reference
      RefAddr proxyFactoryRefAddr = new StringRefAddr(
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY, proxyFactoryRegistryKey);
      ref.add(proxyFactoryRefAddr);

      // Add the Container name for this Reference
      RefAddr containerRefAddr = new StringRefAddr(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_EJBCONTAINER_NAME,
            containerName);
      ref.add(containerRefAddr);

      // Bind
      try
      {
         Util.rebind(this.getContext(), address, ref);
         log.debug("Bound " + ref.getClass().getName() + " into JNDI at \"" + address + "\"");
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not bind " + ref + " into JNDI at \"" + address + "\"", e);
      }
   }

   /**
    * Returns whether the business interfaces and EJB2.x Home should be bound to 
    * the same JNDI Name 
    * 
    * @param smd
    * @param isLocal
    * @return
    */
   protected boolean isHomeAndBusinessBoundTogether(JBossSessionBeanMetaData smd, boolean isLocal)
   {
      // Initialize
      boolean bindTogether = false;

      // If local
      if (isLocal)
      {
         // Bind together if Local Default JNDI Name == Local Home JNDI Name
         bindTogether = smd.determineLocalJndiName().equals(smd.getLocalHomeJndiName());
      }
      // If Remote
      else
      {
         // Bind together if Local Default JNDI Name == Local Home JNDI Name
         bindTogether = smd.determineJndiName().equals(smd.getHomeJndiName());
      }

      // Return
      return bindTogether;
   }

   /**
    * Returns the name of the unique key under which a Proxy Factory will 
    * be registered.  Will follow form:
    * 
    * ejbName/ProxyFactory/(local|remote)
    * 
    * ...depending upon the specified "isLocal" flag
    * 
    * @param md
    * @param isLocal
    */
   protected String getProxyFactoryRegistryKey(JBossEnterpriseBeanMetaData md, boolean isLocal)
   {
      // Initialize
      String suffix = null;

      // Set Suffix
      if (isLocal)
      {
         suffix = JndiSessionRegistrarBase.KEY_SUFFIX_PROXY_FACTORY_REGISTRY_LOCAL;
      }
      else
      {
         suffix = JndiSessionRegistrarBase.KEY_SUFFIX_PROXY_FACTORY_REGISTRY_REMOTE;
      }

      // Assemble and return
      String key = md.getEjbName() + suffix;
      return key;
   }

   /**
    * Makes a comma-delimited list of interfaces bound for setting the 
    * Classname of the Reference.  This will show up in JNDIView and make 
    * it clear to application developers what will be castable from the lookup result
    * 
    * @param refAddrs
    * @return
    */
   protected String getHumanReadableListOfInterfacesInRefAddrs(List<RefAddr> refAddrs)
   {
      // Make a Comma-delimited list of interfaces bound for setting the Classname of the Reference
      // This will show up in JNDIView and make it clear to application developers
      // what will be castable from the lookup result
      StringBuffer defaultRemotes = new StringBuffer();
      int remotesCount = 0;
      for (RefAddr refAddr : refAddrs)
      {
         remotesCount++;
         defaultRemotes.append(refAddr.getContent());
         if (remotesCount < refAddrs.size())
         {
            defaultRemotes.append(", ");
         }
      }
      return defaultRemotes.toString();
   }

   /**
    * Registers the specified proxy factory into the registry 
    * 
    * @param factory
    * @param smd Metadata describing the EJB
    * @param isLocal
    * @return The key under which the ProxyFactory was registered
    */
   protected String registerProxyFactory(ProxyFactory factory, JBossEnterpriseBeanMetaData smd, boolean isLocal)
   {
      // Get a unique key
      String key = this.getProxyFactoryRegistryKey(smd, isLocal);

      // Register
      log.debug("Registering " + factory + " under key \"" + key + "\"...");
      try
      {
         Ejb3RegistrarLocator.locateRegistrar().bind(key, factory);
      }
      catch (DuplicateBindException e)
      {
         /*
          * Note on registry key collisions:
          * 
          * Indicates that either the keys created are not unique or that we're attempting to redeploy 
          * an EJB that was not properly deregistered.  Either way, this is a programmatic problem
          * and not the fault of the bean provider/developer/deployer
          */

         throw new RuntimeException("Could not register " + factory + " under an already registered key, \"" + key
               + "\"", e);
      }

      // Return the key
      return key;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public Context getContext()
   {
      return context;
   }

   public void setContext(Context context)
   {
      this.context = context;
   }

   public String getSessionProxyObjectFactoryType()
   {
      return sessionProxyObjectFactoryType;
   }

   public void setSessionProxyObjectFactoryType(String sessionProxyObjectFactoryType)
   {
      this.sessionProxyObjectFactoryType = sessionProxyObjectFactoryType;
   }

}
