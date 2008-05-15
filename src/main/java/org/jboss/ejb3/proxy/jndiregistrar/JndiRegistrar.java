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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.naming.Context;
import javax.naming.spi.ObjectFactory;

import org.jboss.ejb3.common.lang.ClassHelper;
import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateless.StatelessSessionLocalProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateless.StatelessSessionRemoteProxyFactory;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryAlreadyRegisteredException;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryRegistry;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;

/**
 * JndiRegistrar
 * 
 * Responsible for binding of ObjectFactories and
 * creation/registration of associated ProxyFactories 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JndiRegistrar
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(JndiRegistrar.class);

   /**
    * The value appended to the key used to bind proxy factories to the registry
    */
   private static final String KEY_SUFFIX_PROXY_FACTORY_REGISTRY = "/ProxyFactory";

   /**
    * The value appended to the key used to bind local proxy factories to the registry
    */
   private static final String KEY_SUFFIX_PROXY_FACTORY_REGISTRY_LOCAL = JndiRegistrar.KEY_SUFFIX_PROXY_FACTORY_REGISTRY
         + "/local";

   /**
    * The value appended to the key used to bind local remote factories to the registry
    */
   private static final String KEY_SUFFIX_PROXY_FACTORY_REGISTRY_REMOTE = JndiRegistrar.KEY_SUFFIX_PROXY_FACTORY_REGISTRY
         + "/remote";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Context under which we'll bind to JNDI
    */
   private Context context;

   /**
    * Implementation of ProxyFactoryRegistry to use
    */
   private ProxyFactoryRegistry registry;

   /**
    * Fully-qualified class name of the JNDI Object Factory to Reference for SLSBs
    */
   private String statelessSessionProxyObjectFactoryType;

   //TODO MDB, @Service, SFSB

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a JNDI Deployer from the specified configuration properties, none of
    * which may be null.
    * 
    * @param context The JNDI Context into which Objects will be bound
    * @param registry The ProxyFactoryRegistry with which ProxyFactories will be registered
    * @param statelessSessionProxyObjectFactoryType String representation of the JNDI Object Factory to use for SLSBs
    */
   public JndiRegistrar(final Context context, final ProxyFactoryRegistry registry,
         String statelessSessionProxyObjectFactoryType)
   {
      // Set the Context
      assert context != null : this + " may not be configured with null  " + Context.class.getName();
      this.setContext(context);
      log.debug("Using  " + Context.class.getName() + ": " + context);

      // Set the ProxyFactoryRegistry
      assert registry != null : this + " may not be configured with null  " + ProxyFactoryRegistry.class.getName();
      this.setRegistry(registry);
      log.debug("Using " + ProxyFactoryRegistry.class.getSimpleName() + ": " + registry);

      /*
       * Perform some assertions and logging
       */

      // Set the SLSB Proxy Object Factory Type
      assert statelessSessionProxyObjectFactoryType != null && !statelessSessionProxyObjectFactoryType.equals("") : "SLSB Proxy "
            + ObjectFactory.class.getSimpleName() + " must be specified.";
      this.setStatelessSessionProxyObjectFactoryType(statelessSessionProxyObjectFactoryType);
      log.debug(this + " has configured SLSB JNDI " + ObjectFactory.class.getSimpleName() + ": "
            + this.getStatelessSessionProxyObjectFactoryType());
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
    */
   public void bindEjb(JBossEnterpriseBeanMetaData md, ClassLoader cl)
   {
      // If we've got a SessionBean
      if (md.isSession())
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

         // Delegate out to session-specific handling
         log.debug("Found Session Bean: " + smd.getEjbName());
         this.bindSessionEjb(smd, cl);
      }

      // If this is a MDB
      else if (md.isMessageDriven())
      {
         //TODO MDB Impl
         return;
      }

      //TODO If this is @Service
      else if (md.isService())
      {
         //TODO @Service Impl
         return;
      }
      // Does not report itself as a supported bean type
      else
      {
         // Should not be reached
         throw new RuntimeException(md
               + " reports that it is not Session, Service, or Message-Driven; it must be one of these.");
      }
   }

   /**
    * Binds into JNDI all objects appropriate for the Session Bean
    * described by the specified metadata
    * 
    * @param smd
    * @param cl The classloader associated with the Container 
    *   described by the specified metadata 
    */
   protected void bindSessionEjb(JBossSessionBeanMetaData smd, ClassLoader cl)
   {
      // If Stateful
      if (smd.isStateful())
      {
         //TODO Implement SFSB
      }
      // If Stateless
      else if (smd.isStateless())
      {
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
          * Create and register Proxy Factories
          */

         // If there's a local view
         if (hasLocalView)
         {
            // Create and register a local proxy factory
            ProxyFactory factory = new StatelessSessionLocalProxyFactory(smd, cl);
            this.registerProxyFactory(factory, smd, true);
         }

         // If there's a remote view
         if (hasRemoteView)
         {
            // Create and register a local proxy factory
            ProxyFactory factory = new StatelessSessionRemoteProxyFactory(smd, cl);
            this.registerProxyFactory(factory, smd, false);
         }

         // Bind OF to remote default (and possibly home)

         // Bind OF to home (if not bound together)

         // Bind OF to each remote business

         // Bind OF to local default (and possibly localHome)

         // Bind OF to localHome (if not bound together)

         // Bind OF to each local business

      }
      // Not SLSB or SFSB, error
      else
      {
         // Should not be reached
         throw new RuntimeException(smd
               + " reports that it is neither Stateful nor Stateless, but Session Bean must be one of these.");
      }

   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

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
         suffix = JndiRegistrar.KEY_SUFFIX_PROXY_FACTORY_REGISTRY_LOCAL;
      }
      else
      {
         suffix = JndiRegistrar.KEY_SUFFIX_PROXY_FACTORY_REGISTRY_REMOTE;
      }

      // Assemble and return
      String key = md.getEjbName() + suffix;
      return key;
   }

   /**
    * Obtains the return types declared by the "create" methods for the specified home interface.
    *  
    * @param homeInterface
    * @param isStateless Flag to indicate whether this is for a Stateful or Stateless container
    * @return
    */
   protected Set<Class<?>> getReturnTypesFromCreateMethods(Class<?> homeInterface, boolean isStateless)
   {
      // Ensure we've been passed a Home or LocalHome interface
      assert (EJBHome.class.isAssignableFrom(homeInterface) || EJBLocalHome.class.isAssignableFrom(homeInterface));
      if (!EJBHome.class.isAssignableFrom(homeInterface) && !EJBLocalHome.class.isAssignableFrom(homeInterface))
      {
         throw new RuntimeException("Declared EJB 2.1 Home Interface " + homeInterface.getName() + " does not extend "
               + EJBHome.class.getName() + " or " + EJBLocalHome.class.getName()
               + " as required by EJB 3.0 Core Specification 4.6.8 and 4.6.10");
      }

      // Initialize
      Set<Class<?>> types = new HashSet<Class<?>>();
      List<Method> createMethods = null;

      // If for a Stateless Container
      if (isStateless)
      {
         // Initialize error message
         String specViolationErrorMessage = "EJB 3.0 Specification Violation (4.6.8 Bullet 4, 4.6.10 Bullet 4): \""
               + "A stateless session bean must define exactly one create method with no arguments." + "\"; found in "
               + homeInterface.getName();

         // Get all methods with signature "create"
         createMethods = new ArrayList<Method>();
         try
         {
            createMethods.add(homeInterface.getMethod("create", new Class<?>[]
            {}));
         }
         // EJB 3.0 Specification 4.6.8 Bullet 4 Violation
         // EJBTHREE-1156
         catch (NoSuchMethodException e)
         {
            throw new RuntimeException(specViolationErrorMessage);
         }

         // Ensure only one create method is defined
         // EJB 3.0 Specification 4.6.8 Bullet 4 Violation
         // EJBTHREE-1156
         if (createMethods.size() > 1)
         {
            throw new RuntimeException(specViolationErrorMessage);
         }
      }
      else
      {
         // Obtain all "create<METHOD>" methods
         createMethods = ClassHelper.getAllMethodsByPrefix(homeInterface, "create");
      }
      if (createMethods.size() == 0)
      {
         throw new RuntimeException("EJB 3.0 Core Specification Violation (4.6.8 Bullet 5): EJB2.1 Home Interface "
               + homeInterface + " does not declare a \'create<METHOD>\' method");
      }

      // Add all return types
      for (Method method : createMethods)
      {
         types.add(method.getReturnType());
      }

      // Return
      return types;
   }

   /**
    * Registers the specified proxy factory into the registry 
    * 
    * @param factory
    * @param smd Metadata describing the EJB
    * @param isLocal
    */
   protected void registerProxyFactory(ProxyFactory factory, JBossEnterpriseBeanMetaData smd, boolean isLocal)
   {
      // Get a unique key
      String key = this.getProxyFactoryRegistryKey(smd, isLocal);
      assert !this.getRegistry().isRegistered(key) : "Attempting to register " + factory + " with "
            + this.getRegistry() + " an already registered key, \"" + key + "\"";

      /*
       * Note on registry key collisions:
       * 
       * Indicates that either the keys created are not unique or that we're attempting to redeploy 
       * an EJB that was not properly deregistered.  Either way, this is a programmatic problem
       * and not the fault of the application developer/deployer
       */

      // Log
      log.debug("Registering " + factory + " into " + ProxyFactoryRegistry.class.getSimpleName() + " under key \""
            + key + "\"...");

      // Register
      try
      {
         this.getRegistry().registerProxyFactory(key, factory);
      }
      catch (ProxyFactoryAlreadyRegisteredException e)
      {
         throw new RuntimeException("Could not register " + factory + " under an already registered key, \"" + key
               + "\", with " + this.getRegistry(), e);
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ProxyFactoryRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(ProxyFactoryRegistry registry)
   {
      this.registry = registry;
   }

   public Context getContext()
   {
      return context;
   }

   public void setContext(Context context)
   {
      this.context = context;
   }

   public String getStatelessSessionProxyObjectFactoryType()
   {
      return statelessSessionProxyObjectFactoryType;
   }

   public void setStatelessSessionProxyObjectFactoryType(String statelessSessionProxyObjectFactoryType)
   {
      this.statelessSessionProxyObjectFactoryType = statelessSessionProxyObjectFactoryType;
   }

}
