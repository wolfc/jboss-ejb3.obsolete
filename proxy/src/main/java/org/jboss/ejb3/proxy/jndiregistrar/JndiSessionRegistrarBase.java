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
import javax.naming.NameNotFoundException;
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
import org.jboss.metadata.ejb.jboss.RemoteBindingMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.JbossSessionBeanJndiNameResolver;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.jboss.naming.Util;
import org.jboss.remoting.InvokerLocator;

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
   private static final String KEY_PREFIX_PROXY_FACTORY_REGISTRY = "ProxyFactory/";

   private static final String OBJECT_FACTORY_CLASSNAME_PREFIX = "Proxy for: ";

   //TODO Remove
   // EJBTHREE-1419
   /**
    * The default URL for InvokerLocator in the case @RemoteBinding does not specify it
    */
   public static final String DEFAULT_CLIENT_BINDING = "socket://0.0.0.0:3873";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Fully-qualified class name of the JNDI Object Factory to Reference
    */
   private String sessionProxyObjectFactoryType;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a JNDI Registrar from the specified configuration properties, none of
    * which may be null.
    * 
    * @param sessionProxyObjectFactoryType String representation of the JNDI Object 
    *           Factory Class Name (fully-qualified) to use for this Session EJB
    */
   public JndiSessionRegistrarBase(final String sessionProxyObjectFactoryType)
   {
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
    * @param context The JNDI Context to use for binding
    * @param smd
    * @param cl The CL of the Container
    * @param containerName The name under which the target container is registered
    */
   public void bindEjb(final Context context, final JBossSessionBeanMetaData smd, final ClassLoader cl,
         final String containerName)
   {
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
      boolean hasLocalView = (localHome != null || (businessLocals != null && businessLocals.size() > 0));
      boolean hasRemoteView = (remoteHome != null || (businessRemotes != null && businessRemotes.size() > 0));

      // If no local or remote views
      if (!hasLocalView && !hasRemoteView)
      {
         throw new RuntimeException("EJB " + smd.getEjbName() + " has no local or remote views defined.");
      }

      /*
       * Create and Register Proxy Factories
       */

      // If there's a remote view
      /*
       * Bind Remote ObjectFactories to JNDI
       */

      if (hasRemoteView)
      {
         // Obtain RemoteBinding URL
         List<RemoteBindingMetaData> bindings = smd.getRemoteBindings();
         assert bindings != null && bindings.size() > 0 : "Remote Bindings are required and none are present";
         RemoteBindingMetaData remoteBinding = bindings.get(0);
         String url = remoteBinding.getClientBindUrl();
         //TODO
         // EJBTHREE-1419 Provide more intelligent mechanism for defaults when clientBindUrl is unspecified
         if (url == null || url.trim().equals(""))
         {
            url = JndiSessionRegistrarBase.DEFAULT_CLIENT_BINDING;
            remoteBinding.setClientBindUrl(url);
         }
         // Create and register a remote proxy factory
         String remoteProxyFactoryKey = this.getProxyFactoryRegistryKey(smd, false);
         SessionProxyFactory factory = this
               .createRemoteProxyFactory(remoteProxyFactoryKey, containerName, smd, cl, url);
         this.registerProxyFactory(remoteProxyFactoryKey, factory, smd);

         // Initialize Reference Addresses to attach to default remote JNDI Reference
         List<RefAddr> refAddrsForDefaultRemote = new ArrayList<RefAddr>();

         // For each of the remote business interfaces, make a Reference Address
         if (businessRemotes != null)
         {
            for (String businessRemote : businessRemotes)
            {
               RefAddr refAddr = new StringRefAddr(
                     ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE, businessRemote);
               refAddrsForDefaultRemote.add(refAddr);
            }
         }

         // Determine if remote home and business remotes are bound to same JNDI Address
         boolean bindRemoteAndHomeTogether = this.isHomeAndBusinessBoundTogether(smd, false);
         if (bindRemoteAndHomeTogether)
         {
            // Add a Reference Address for the Remote Home
            String home = smd.getHome();
            assert home != null : "Home and Business set to be bound together, yet no home is defined";
            RefAddr refAddr = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE, home);
            refAddrsForDefaultRemote.add(refAddr);
         }
         // Bind Home (not bound together) if exists
         else if (smd.getHome() != null && !smd.getHome().equals(""))
         {
            String homeType = smd.getHome();
            RefAddr refAddrHomeInterface = new StringRefAddr(
                  ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE, homeType);
            RefAddr refAddrRemoting = this.createRemotingRefAddr(smd);
            Reference homeRef = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX + homeType, this
                  .getSessionProxyObjectFactoryType(), null);
            homeRef.add(refAddrHomeInterface);
            homeRef.add(refAddrRemoting);
            String homeAddress = smd.getHomeJndiName();
            assert homeAddress != null && !homeAddress.equals("") : "JNDI Address for Remote Home must be defined";
            log.debug("Remote Home View for EJB " + smd.getEjbName() + " to be bound into JNDI at \"" + homeAddress
                  + "\"");
            this.bind(context, homeRef, homeAddress, remoteProxyFactoryKey, containerName);
         }

         // Add a Reference Address for the Remoting URL
         refAddrsForDefaultRemote.add(this.createRemotingRefAddr(smd));

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
         String defaultRemoteAddress = smd.getJndiName();
         log.debug("Default Remote Business View for EJB " + smd.getEjbName() + " to be bound into JNDI at \""
               + defaultRemoteAddress + "\"");
         this.bind(context, defaultRemoteRef, defaultRemoteAddress, remoteProxyFactoryKey, containerName);

         // Bind ObjectFactory specific to each Remote Business Interface
         if (businessRemotes != null)
         {
            for (String businessRemote : businessRemotes)
            {
               RefAddr refAddrBusinessInterface = new StringRefAddr(
                     ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE, businessRemote);
               RefAddr refAddrRemoting = this.createRemotingRefAddr(smd);
               Reference ref = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX + businessRemote,
                     this.getSessionProxyObjectFactoryType(), null);
               ref.add(refAddrBusinessInterface);
               ref.add(refAddrRemoting);
               String address = JbossSessionBeanJndiNameResolver.resolveJndiName(smd, businessRemote);
               log.debug("Remote Business View for " + businessRemote + " of EJB " + smd.getEjbName()
                     + " to be bound into JNDI at \"" + address + "\"");
               this.bind(context, ref, address, remoteProxyFactoryKey, containerName);
            }
         }
      }
      // If there's a local view
      if (hasLocalView)
      {
         // Create and register a local proxy factory
         String localProxyFactoryKey = this.getProxyFactoryRegistryKey(smd, true);
         SessionProxyFactory factory = this.createLocalProxyFactory(localProxyFactoryKey, containerName, smd, cl);
         this.registerProxyFactory(localProxyFactoryKey, factory, smd);

         // Initialize Reference Addresses to attach to default local JNDI Reference
         List<RefAddr> refAddrsForDefaultLocal = new ArrayList<RefAddr>();

         // For each of the local business interfaces, make a Reference Address
         if (businessLocals != null)
         {
            for (String businessLocal : businessLocals)
            {
               RefAddr refAddr = new StringRefAddr(
                     ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL, businessLocal);
               refAddrsForDefaultLocal.add(refAddr);
            }
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
            String localHomeAddress = smd.getLocalHomeJndiName();
            log.debug("Local Home View for EJB " + smd.getEjbName() + " to be bound into JNDI at \"" + localHomeAddress
                  + "\"");
            this.bind(context, localHomeRef, localHomeAddress, localProxyFactoryKey, containerName);
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
         String defaultLocalAddress = smd.getLocalJndiName();
         log.debug("Default Local Business View for EJB " + smd.getEjbName() + " to be bound into JNDI at \""
               + defaultLocalAddress + "\"");
         this.bind(context, defaultLocalRef, defaultLocalAddress, localProxyFactoryKey, containerName);

         // Bind ObjectFactory specific to each Local Business Interface
         if (businessLocals != null)
         {
            for (String businessLocal : businessLocals)
            {
               RefAddr refAddr = new StringRefAddr(
                     ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL, businessLocal);
               Reference ref = new Reference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX + businessLocal,
                     this.getSessionProxyObjectFactoryType(), null);
               ref.add(refAddr);
               String address = JbossSessionBeanJndiNameResolver.resolveJndiName(smd, businessLocal);
               log.debug("Local Business View for " + businessLocal + " of EJB " + smd.getEjbName()
                     + " to be bound into JNDI at \"" + address + "\"");
               this.bind(context, ref, address, localProxyFactoryKey, containerName);

            }
         }
      }
   }

   /**
    * Unbinds from JNDI all appropriate objects registered 
    * by the EJB described by the specified metadata.  Additionally
    * responsible for destruction and deregistration of any all ProxyFactory
    * implementations required by the EJB
    * 
    * @param context The JNDI Context to use for unbinding
    * @param smd
    */
   public void unbindEjb(final Context context, final JBossSessionBeanMetaData smd)
   {
      // Log 
      String ejbName = smd.getEjbName();
      log.debug("Unbinding JNDI References for Session Bean: " + ejbName);

      // Get Business Locals
      BusinessLocalsMetaData businessLocals = smd.getBusinessLocals();

      // Get Business Remotes
      BusinessRemotesMetaData businessRemotes = smd.getBusinessRemotes();

      // Get Local Home
      String localHome = StringUtils.adjustWhitespaceStringToNull(smd.getLocalHome());

      // Get Remote Home
      String remoteHome = StringUtils.adjustWhitespaceStringToNull(smd.getHome());

      // Determine if there are local/remote views
      boolean hasLocalView = (localHome != null || (businessLocals != null && businessLocals.size() > 0));
      boolean hasRemoteView = (remoteHome != null || (businessRemotes != null && businessRemotes.size() > 0));

      /*
       * Remove Proxy Factories
       */

      // If there's a remote view
      /*
       * Remove Remote ObjectFactories to JNDI
       */

      if (hasRemoteView)
      {
         // Obtain RemoteBinding URL
         List<RemoteBindingMetaData> bindings = smd.getRemoteBindings();
         assert bindings != null && bindings.size() > 0 : "Remote Bindings are required and none are present";

         // Create and register a remote proxy factory
         String remoteProxyFactoryKey = this.getProxyFactoryRegistryKey(smd, false);
         this.deregisterProxyFactory(remoteProxyFactoryKey);

         // Determine if remote home and business remotes are bound to same JNDI Address
         boolean bindRemoteAndHomeTogether = this.isHomeAndBusinessBoundTogether(smd, false);
         // Bind Home (not bound together) if exists
         if ((smd.getHome() != null && !smd.getHome().equals("")) && !bindRemoteAndHomeTogether)
         {
            String homeAddress = smd.getHomeJndiName();
            log.debug("Remote Home View for EJB " + smd.getEjbName() + " to be unbound from JNDI at \"" + homeAddress
                  + "\"");
            this.unbind(context, homeAddress);
         }

         /*
          * Unbind ObjectFactory for default remote businesses (and home if bound together)
          */

         // Bind the Default Remote Reference to JNDI
         String defaultRemoteAddress = smd.getJndiName();
         log.debug("Default Remote Business View for EJB " + smd.getEjbName() + " to be unbound from JNDI at \""
               + defaultRemoteAddress + "\"");
         this.unbind(context, defaultRemoteAddress);

         // Unbind ObjectFactory specific to each Remote Business Interface
         if (businessRemotes != null)
         {
            for (String businessRemote : businessRemotes)
            {
               String address = JbossSessionBeanJndiNameResolver.resolveJndiName(smd, businessRemote);
               log.debug("Remote Business View for " + businessRemote + " of EJB " + smd.getEjbName()
                     + " to be unbound from JNDI at \"" + address + "\"");
               this.unbind(context, address);
            }
         }
      }
      // If there's a local view
      if (hasLocalView)
      {
         // Remove local proxy factory
         String localProxyFactoryKey = this.getProxyFactoryRegistryKey(smd, true);
         this.deregisterProxyFactory(localProxyFactoryKey);

         // Determine if local home and business locals are bound to same JNDI Address
         boolean bindLocalAndLocalHomeTogether = this.isHomeAndBusinessBoundTogether(smd, true);

         // Unbind Local Home (not bound together) if exists
         if ((smd.getLocalHome() != null && !smd.getLocalHome().equals("")) && !bindLocalAndLocalHomeTogether)
         {
            String localHomeAddress = smd.getLocalHomeJndiName();
            log.debug("Local Home View for EJB " + smd.getEjbName() + " to be unbound from JNDI at \""
                  + localHomeAddress + "\"");
            this.unbind(context, localHomeAddress);
         }

         /*
          * Unbind ObjectFactory for default local businesses (and LocalHome if bound together)
          */

         // Unbind the Default Local Reference to JNDI
         String defaultLocalAddress = smd.getLocalJndiName();
         log.debug("Default Local Business View for EJB " + smd.getEjbName() + " to be unbound from JNDI at \""
               + defaultLocalAddress + "\"");
         this.unbind(context, defaultLocalAddress);

         // Unbind ObjectFactory specific to each Local Business Interface
         if (businessLocals != null)
         {
            for (String businessLocal : businessLocals)
            {
               String address = JbossSessionBeanJndiNameResolver.resolveJndiName(smd, businessLocal);
               log.debug("Local Business View for " + businessLocal + " of EJB " + smd.getEjbName()
                     + " to be unbound from JNDI at \"" + address + "\"");
               this.unbind(context, address);
            }
         }
      }
   }

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates and returns a new local proxy factory for this Session Bean
    * 
    * @param name The unique name for the ProxyFactory
    * @param containerName The name of the Container upon which Proxies 
    *   from the returned ProxyFactory will invoke
    * @param smd The metadata representing this Session EJB
    * @param cl The ClassLoader for this EJB Container
    */
   protected abstract SessionProxyFactory createLocalProxyFactory(final String name, final String containerName,
         final JBossSessionBeanMetaData smd, final ClassLoader cl);

   /**
    * Creates and returns a new remote proxy factory for this Session Bean
    * 
    * @param name The unique name for the ProxyFactory
    * @param containerName The name of the Container upon which Proxies 
    *   from the returned ProxyFactory will invoke
    * @param smd The metadata representing this Session EJB
    * @param cl The ClassLoader for this EJB Container
    * @param url The URL to use for Remoting
    */
   protected abstract SessionProxyFactory createRemoteProxyFactory(final String name, final String containerName,
         final JBossSessionBeanMetaData smd, final ClassLoader cl, final String url);

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Binds the specified Reference into JNDI at the specified address, adding 
    * the requisite key for the ProxyFactory within the Registry and the requisite
    * target EJB Container Name as ReferenceAddresses
    * 
    * @param context The JNDI Context to use
    * @param ref
    * @param address
    * @param proxyFactoryRegistryKey The key under which the proxy factory 
    *   for this reference is stored in the proxy factory registry
    * @param containerName The target container to be used in invocations from Proxies obtained from this address
    */
   protected void bind(Context context, Reference ref, String address, String proxyFactoryRegistryKey,
         String containerName)
   {
      // Add the Proxy Factory Registry key for this Reference
      assert proxyFactoryRegistryKey != null && !proxyFactoryRegistryKey.trim().equals("") : "Proxy Factory Registry key is required but not supplied";
      String proxyFactoryRefType = ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY;
      RefAddr proxyFactoryRefAddr = new StringRefAddr(proxyFactoryRefType, proxyFactoryRegistryKey);
      ref.add(proxyFactoryRefAddr);
      log.debug("Adding " + RefAddr.class.getSimpleName() + " to " + Reference.class.getSimpleName() + ": Type \""
            + proxyFactoryRefType + "\", Content \"" + proxyFactoryRegistryKey + "\"");

      // Add the Container name for this Reference
      assert containerName != null && !containerName.trim().equals("") : "Container Name is required but not supplied";
      String ejbContainerRefType = ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_EJBCONTAINER_NAME;
      RefAddr containerRefAddr = new StringRefAddr(ejbContainerRefType, containerName);
      ref.add(containerRefAddr);
      log.debug("Adding " + RefAddr.class.getSimpleName() + " to " + Reference.class.getSimpleName() + ": Type \""
            + ejbContainerRefType + "\", Content \"" + containerName + "\"");

      // Bind
      try
      {
         Util.bind(context, address, ref);
         log.debug("Bound " + ref.getClass().getName() + " into JNDI at \"" + address + "\"");
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not bind " + ref + " into JNDI at \"" + address + "\"", e);
      }
   }

   /**
    * Unbinds the specified address from JNDI
    * 
    * @param context The JNDI Context to use
    * @param address
    */
   protected void unbind(Context context, String address)
   {
      // Unbind
      try
      {
         Util.unbind(context, address);
      }
      catch (NameNotFoundException nnfe)
      {
         // Swallow, who cares? :)
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not unbind \"" + address + "\" from JNDI", e);
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
         // If no local home defined
         if (smd.getLocalHome() == null)
         {
            // Not bound together
            return false;
         }

         // Bind together if Local Default JNDI Name == Local Home JNDI Name
         bindTogether = smd.getLocalJndiName().equals(smd.getLocalHomeJndiName());
      }
      // If Remote
      else
      {
         // If no home defined
         if (smd.getHome() == null)
         {
            // Not bound together
            return false;
         }
         // Bind together if Local Default JNDI Name == Local Home JNDI Name
         bindTogether = smd.getJndiName().equals(smd.getHomeJndiName());
      }

      // Return
      return bindTogether;
   }

   /**
    * Creates and returns a new RefAddr to flag the proper
    * InvokerLocator URL used by remoting for the EJB represented
    * by the specified metadata 
    * 
    * @param smd
    * @return
    */
   protected RefAddr createRemotingRefAddr(JBossSessionBeanMetaData smd)
   {
      // Obtain RemoteBinding
      List<RemoteBindingMetaData> bindings = smd.getRemoteBindings();
      assert bindings != null && bindings.size() > 0 : "Remote Bindings are required and none are present";
      RemoteBindingMetaData remoteBinding = smd.getRemoteBindings().get(0);

      // Create RefAddr
      String url = remoteBinding.getClientBindUrl();
      assert url != null && url.trim().toString().length() != 0 : InvokerLocator.class.getSimpleName()
            + " URL must be defined, and is unspecified";
      RefAddr refAddr = new StringRefAddr(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_INVOKER_LOCATOR_URL, url);

      // Return
      return refAddr;
   }

   /**
    * Returns the name of the unique key under which a Proxy Factory will 
    * be registered.  Will follow form:
    * 
    * (jndiName)/ProxyFactory/
    * 
    * ...depending upon the specified "isLocal" flag
    * 
    * @param md
    * @param isLocal
    */
   public String getProxyFactoryRegistryKey(JBossSessionBeanMetaData md, boolean isLocal)
   {
      // Initialize
      String suffix = null;

      // Set Suffix
      if (isLocal)
      {
         suffix = md.getLocalJndiName();
      }
      else
      {
         suffix = md.getJndiName();
      }

      // Ensure suffix is specified
      assert suffix != null && !suffix.equals("") : ProxyFactory.class.getSimpleName()
            + " key prefix for binding to registry is noy specified";

      // Assemble and return
      String key = JndiSessionRegistrarBase.KEY_PREFIX_PROXY_FACTORY_REGISTRY + suffix;
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
      int interfaceCount = 0;
      for (RefAddr refAddr : refAddrs)
      {
         remotesCount++;
         String refAddrType = refAddr.getType();
         if (isRefAddrTypeEjbInterface(refAddrType))
         {
            if (interfaceCount > 0)
            {
               defaultRemotes.append(", ");
            }
            defaultRemotes.append(refAddr.getContent());
            interfaceCount++;
         }
      }
      return defaultRemotes.toString();
   }
   
   /**
    * Returns whether the specified RefAddr type denotes an EJB Interface 
    * 
    * @param refAddrType
    * @return
    */
   private boolean isRefAddrTypeEjbInterface(String refAddrType)
   {
      return refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL) ||
      refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE) ||
      refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_LOCAL) ||
      refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE);
   }

   /**
    * Registers the specified proxy factory into the registry 
    * 
    * @param name The unique name for the ProxyFactory
    * @param factory
    * @param smd Metadata describing the EJB
    */
   protected void registerProxyFactory(String name, ProxyFactory factory, JBossEnterpriseBeanMetaData smd)
   {
      // Register
      log.debug("Registering " + factory + " under key \"" + name + "\"...");
      try
      {
         Ejb3RegistrarLocator.locateRegistrar().bind(name, factory);
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

         throw new RuntimeException("Could not register " + factory + " under an already registered key, \"" + name
               + "\"", e);
      }
   }

   /** 
    * Deregisters the proxy factory with the specified name from the registry
    * 
    * @param name
    */
   protected void deregisterProxyFactory(String name)
   {
      // Log
      log.debug("Deregistering " + ProxyFactory.class.getSimpleName() + " under name \"" + name + "\"");

      // Obtain
      Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(name);
      assert (obj != null) : ProxyFactory.class.getSimpleName() + " was expected registered under name \"" + name
            + "\", but sws not found.";
      assert obj instanceof ProxyFactory : "Expected " + ProxyFactory.class.getName() + " bound under name \"" + name
            + "\", but was instead: " + obj;

      // Deregister
      Ejb3RegistrarLocator.locateRegistrar().unbind(name);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public String getSessionProxyObjectFactoryType()
   {
      return sessionProxyObjectFactoryType;
   }

   public void setSessionProxyObjectFactoryType(String sessionProxyObjectFactoryType)
   {
      this.sessionProxyObjectFactoryType = sessionProxyObjectFactoryType;
   }

}
