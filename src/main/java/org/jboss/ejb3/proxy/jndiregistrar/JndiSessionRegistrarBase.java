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
import java.util.Set;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.jboss.aop.Advisor;
import org.jboss.aop.Dispatcher;
import org.jboss.ejb3.common.registrar.spi.DuplicateBindException;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
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
import org.jboss.remoting.transport.Connector;

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

   /**
    * The name under which the Remoting Connector is bound in MC
    */
   private static final String OBJECT_NAME_REMOTING_CONNECTOR = "org.jboss.ejb3.RemotingConnector";

   /**
    * The default URL for InvokerLocator in the case @RemoteBinding does not specify it
    */
   protected static String DEFAULT_CLIENT_BINDING;

   /**
    * The default URL for InvokerLocator if if cannot be read from the EJB3 Remoting Connector
    */
   protected static final String DEFAULT_CLIENT_BINDING_IF_CONNECTOR_NOT_FOUND = "0.0.0.0:3873";

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
    * @param smd the Container's metadata
    * @param cl The CL of the Container
    * @param containerName The name under which the target container is registered
    * @param containerGuid The globally-unique name of the container
    * @param advisor The advisor to use for generated proxies
    */
   public void bindEjb(final Context context, final JBossSessionBeanMetaData smd, final ClassLoader cl,
         final String containerName, final String containerGuid, final Advisor advisor)
   {
      JndiReferenceBindingSet bindingSet = createJndiReferenceBindingSet(context, smd, cl, containerName,
            containerGuid, advisor);

      bind(context, bindingSet, false, true);
   }

   /**
    * Creates all of the <code>Reference</code> objects that should be bound
    * in JNDI for the EJB, and determines the correct JNDI name for each.
    * Additionally responsible for creation and registration of any all 
    * ProxyFactory implementations required by the EJB.
    *
    * @param smd the Container's metadata
    * @param cl The CL of the Container
    * @param containerName The name under which the target container is registered
    * @param containerGuid The globally-unique name of the container
    * @param advisor The advisor to use for generated proxies
    * 
    * @return data object encapsulating the references and their JNDI names
    */
   protected JndiReferenceBindingSet createJndiReferenceBindingSet(final Context context,
         final JBossSessionBeanMetaData smd, final ClassLoader cl, final String containerName,
         final String containerGuid, final Advisor advisor)
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

      /*
       * Create and Register Proxy Factories
       */

      JndiReferenceBindingSet bindingSet = new JndiReferenceBindingSet(context);

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

         // If no explicit Client Bind URL is specified
         if (url == null || url.trim().equals(""))
         {
            // Use the binding on the EJB3 Remoting Connector
            url = this.getDefaultClientBinding();
            remoteBinding.setClientBindUrl(url);
         }
         // Create and register a remote proxy factory
         String remoteProxyFactoryKey = this.getProxyFactoryRegistryKey(smd, false);
         SessionProxyFactory factory = this.createRemoteProxyFactory(remoteProxyFactoryKey, containerName,
               containerGuid, smd, cl, url, advisor);
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
            Reference homeRef = createStandardReference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
                  + homeType, remoteProxyFactoryKey, containerName);
            homeRef.add(refAddrHomeInterface);
            homeRef.add(refAddrRemoting);

            String homeAddress = smd.getHomeJndiName();
            assert homeAddress != null && !homeAddress.equals("") : "JNDI Address for Remote Home must be defined";
            log.debug("Remote Home View for EJB " + smd.getEjbName() + " to be bound into JNDI at \"" + homeAddress
                  + "\"");

            bindingSet.addHomeRemoteBinding(new JndiReferenceBinding(homeAddress, homeRef));
         }

         // Add a Reference Address for the Remoting URL
         refAddrsForDefaultRemote.add(this.createRemotingRefAddr(smd));

         /*
          * Bind ObjectFactory for default remote businesses (and home if bound together)
          */

         // Get Classname to set for Reference
         String defaultRemoteClassName = this.getHumanReadableListOfInterfacesInRefAddrs(refAddrsForDefaultRemote);

         // Create a Reference
         Reference defaultRemoteRef = createStandardReference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
               + defaultRemoteClassName, remoteProxyFactoryKey, containerName);

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

         bindingSet.addDefaultRemoteBinding(new JndiReferenceBinding(defaultRemoteAddress, defaultRemoteRef));

         // Bind ObjectFactory specific to each Remote Business Interface
         if (businessRemotes != null)
         {
            for (String businessRemote : businessRemotes)
            {
               RefAddr refAddrBusinessInterface = new StringRefAddr(
                     ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE, businessRemote);
               RefAddr refAddrRemoting = this.createRemotingRefAddr(smd);
               Reference ref = createStandardReference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
                     + businessRemote, remoteProxyFactoryKey, containerName);
               ref.add(refAddrBusinessInterface);
               ref.add(refAddrRemoting);
               String address = JbossSessionBeanJndiNameResolver.resolveJndiName(smd, businessRemote);
               log.debug("Remote Business View for " + businessRemote + " of EJB " + smd.getEjbName()
                     + " to be bound into JNDI at \"" + address + "\"");

               bindingSet.addBusinessRemoteBinding(businessRemote, new JndiReferenceBinding(address, ref));
            }
         }
      }
      // If there's a local view
      if (hasLocalView)
      {
         // Create and register a local proxy factory
         String localProxyFactoryKey = this.getProxyFactoryRegistryKey(smd, true);
         SessionProxyFactory factory = this.createLocalProxyFactory(localProxyFactoryKey, containerName, containerGuid,
               smd, cl, advisor);
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
            Reference localHomeRef = createStandardReference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
                  + localHomeType, localProxyFactoryKey, containerName);
            localHomeRef.add(refAddr);
            String localHomeAddress = smd.getLocalHomeJndiName();
            log.debug("Local Home View for EJB " + smd.getEjbName() + " to be bound into JNDI at \"" + localHomeAddress
                  + "\"");

            bindingSet.addHomeLocalBinding(new JndiReferenceBinding(localHomeAddress, localHomeRef));
         }

         /*
          * Bind ObjectFactory for default local businesses (and LocalHome if bound together)
          */

         // Get Classname to set for Reference
         String defaultLocalClassName = this.getHumanReadableListOfInterfacesInRefAddrs(refAddrsForDefaultLocal);

         // Create a Reference
         Reference defaultLocalRef = createStandardReference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
               + defaultLocalClassName, localProxyFactoryKey, containerName);

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

         bindingSet.addDefaultLocalBinding(new JndiReferenceBinding(defaultLocalAddress, defaultLocalRef));

         // Bind ObjectFactory specific to each Local Business Interface
         if (businessLocals != null)
         {
            for (String businessLocal : businessLocals)
            {
               RefAddr refAddr = new StringRefAddr(
                     ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL, businessLocal);
               Reference ref = createStandardReference(JndiSessionRegistrarBase.OBJECT_FACTORY_CLASSNAME_PREFIX
                     + businessLocal, localProxyFactoryKey, containerName);
               ref.add(refAddr);
               String address = JbossSessionBeanJndiNameResolver.resolveJndiName(smd, businessLocal);
               log.debug("Local Business View for " + businessLocal + " of EJB " + smd.getEjbName()
                     + " to be bound into JNDI at \"" + address + "\"");

               bindingSet.addBusinessLocalBinding(businessLocal, new JndiReferenceBinding(address, ref));
            }
         }
      }
      return bindingSet;
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
    * @param containerGuid The globally-unique name of the container
    * @param smd The metadata representing this Session EJB
    * @param cl The ClassLoader for this EJB Container
    * @param advisor The Advisor for proxies created by this factory
    */
   protected abstract SessionProxyFactory createLocalProxyFactory(final String name, final String containerName,
         final String containerGuid, final JBossSessionBeanMetaData smd, final ClassLoader cl, final Advisor advisor);

   /**
    * Creates and returns a new remote proxy factory for this Session Bean
    * 
    * @param name The unique name for the ProxyFactory
    * @param containerName The name of the Container upon which Proxies 
    *   from the returned ProxyFactory will invoke
    * @param containerGuid The globally-unique name of the container
    * @param smd The metadata representing this Session EJB
    * @param cl The ClassLoader for this EJB Container
    * @param url The URL to use for Remoting
    * @param advisor The Advisor for proxies created by this factory
    */
   protected abstract SessionProxyFactory createRemoteProxyFactory(final String name, final String containerName,
         final String containerGuid, final JBossSessionBeanMetaData smd, final ClassLoader cl, final String url,
         final Advisor advisor);

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a new <code>Reference</code> whose <code>classname</code> is
    * the given <code>referenceName</code> and whose <code>classFactory</code> 
    * is {@link #getSessionProxyObjectFactoryType()}, adding 
    * the requisite Registry key for the ProxyFactory and the requisite
    * target EJB Container Name as ReferenceAddresses.
    */
   protected Reference createStandardReference(String referenceName, String proxyFactoryRegistryKey,
         String containerName)
   {
      Reference ref = new Reference(referenceName, this.getSessionProxyObjectFactoryType(), null);

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

      return ref;
   }

   protected void bind(final Context context, final JndiReferenceBindingSet bindings, final boolean useRebind,
         final boolean bindLocals)
   {
      for (JndiReferenceBinding binding : bindings.getDefaultRemoteBindings())
      {
         bind(context, binding, useRebind);
      }

      for (JndiReferenceBinding binding : bindings.getHomeRemoteBindings())
      {
         bind(context, binding, useRebind);
      }

      for (Set<JndiReferenceBinding> businessBindings : bindings.getBusinessRemoteBindings().values())
      {
         for (JndiReferenceBinding binding : businessBindings)
         {
            bind(context, binding, useRebind);
         }
      }

      if (bindLocals)
      {
         for (JndiReferenceBinding binding : bindings.getDefaultLocalBindings())
         {
            bind(context, binding, useRebind);
         }

         for (JndiReferenceBinding binding : bindings.getHomeLocalBindings())
         {
            bind(context, binding, useRebind);
         }

         for (Set<JndiReferenceBinding> businessBindings : bindings.getBusinessLocalBindings().values())
         {
            for (JndiReferenceBinding binding : businessBindings)
            {
               bind(context, binding, useRebind);
            }
         }
      }
   }

   protected void bind(Context context, JndiReferenceBinding binding, boolean useRebind)
   {
      if (binding != null)
      {
         if (useRebind)
            rebind(context, binding.getJndiName(), binding.getReference());
         else
            bind(context, binding.getJndiName(), binding.getReference());
      }
   }

   /**
    * Binds the specified Reference into JNDI at the specified address
    * 
    * @param context The JNDI Context to use
    * @param address the address
    * @param ref the reference to bind
    */
   protected void bind(Context context, String address, Reference ref)
   {
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
    * Re-binds the specified Reference into JNDI at the specified address
    * 
    * @param context The JNDI Context to use
    * @param address the address
    * @param object the object to bind
    */
   protected void rebind(Context context, String address, Reference ref)
   {
      try
      {
         Util.rebind(context, address, ref);
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
    * ProxyFactory/(jndiName)
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
            + " key prefix for binding to registry is not specified";

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
      return refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL)
            || refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE)
            || refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_LOCAL)
            || refAddrType.equals(ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE);
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

      // EJBTHREE-1473
      // Deregister with AOP if registered
      //TODO This should probably be in a cleaner location, ie.
      // implement a callback for AOP Registration/Deregistration
      // that decouples JNDI Registration and abstracts whether 
      // a Proxy Factory is Remote or not
      if (Dispatcher.singleton.isRegistered(name))
      {
         Dispatcher.singleton.unregisterTarget(name);
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the default client binding
    * 
    * Will return the value of the InvokerLocator
    * used by the EJB3 Remoting Connector
    * 
    * EJBTHREE-1419
    */
   protected synchronized String getDefaultClientBinding()
   {

      // If the binding has not yet been set
      if (DEFAULT_CLIENT_BINDING == null)
      {

         try
         {
            // Lookup the Connector in MC
            Connector connector = Ejb3RegistrarLocator.locateRegistrar().lookup(OBJECT_NAME_REMOTING_CONNECTOR,
                  Connector.class);

            // Use the binding specified by the Connector
            try
            {
               DEFAULT_CLIENT_BINDING = connector.getInvokerLocator();
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not obtain " + InvokerLocator.class.getSimpleName()
                     + " from EJB3 Remoting Connector", e);
            }
         }
         // The EJB3 Remoting Connector was not found in MC
         catch (NotBoundException nbe)
         {
            // Log a warning
            log.warn("Could not find the EJB3 Remoting Connector bound in the Object Store (MC) at the expected name: "
                  + OBJECT_NAME_REMOTING_CONNECTOR + ".  Defaulting a client bind URL to "
                  + DEFAULT_CLIENT_BINDING_IF_CONNECTOR_NOT_FOUND);

            // Set a default URL
            DEFAULT_CLIENT_BINDING = DEFAULT_CLIENT_BINDING_IF_CONNECTOR_NOT_FOUND;
         }
      }

      // Return
      return DEFAULT_CLIENT_BINDING;
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
