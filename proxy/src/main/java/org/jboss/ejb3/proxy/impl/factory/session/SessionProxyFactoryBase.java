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
package org.jboss.ejb3.proxy.impl.factory.session;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.common.lang.ClassHelper;
import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.ejb3.proxy.impl.factory.ProxyFactoryBase;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.spi.intf.SessionProxy;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * SessionProxyFactoryBase
 * 
 * Base upon which Session Proxy Factory implementations
 * may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionProxyFactoryBase extends ProxyFactoryBase implements SessionSpecProxyFactory
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SessionProxyFactoryBase.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private JBossSessionBeanMetaData metadata;

   /**
    * Constructor for the default Proxy Constructor (All
    * business interfaces and, if bound together, the EJB2.x Home)
    */
   private Constructor<?> constructorProxyDefault;

   /**
    * Map of Proxy Constructors, one per business interface, with key 
    * as the fully-qualified class name of the interface, and value
    * of the constructor to use
    */
   private Map<String, Constructor<?>> constructorsProxySpecificBusinessInterface;

   /**
    * Constructor for the EJB2.x Home Proxy
    */
   private Constructor<?> constructorProxyHome;

   /**
    * Constructor for the EJB2.x View Proxy
    */
   private Constructor<?> constructorProxyEjb2x;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param name The unique name for this ProxyFactory
    * @param containerName The name of the InvokableContext (container)
    *   upon which Proxies will invoke
    * @param containerGuid The globally-unique name of the container
    * @param metadata The metadata representing this Session Bean
    * @param classloader The ClassLoader associated with the Container's Bean Class
    *       for which this ProxyFactory is to generate Proxies
    * @param advisor The Advisor for proxies created by this factory
    */
   public SessionProxyFactoryBase(final String name, final String containerName, final String containerGuid,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader, final Advisor advisor)
   {
      // Call Super
      super(name, containerName, containerGuid, classloader, advisor);

      // Set Metadata
      this.setMetadata(metadata);

      // Instanciate backing Map for interface-specific business proxies
      this.setConstructorsProxySpecificBusinessInterface(new HashMap<String, Constructor<?>>());
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create an EJB2.x Home Proxy
    * 
    * @return
    */
   public Object createProxyHome()
   {
      // Create a new InvocationHandler
      SessionProxyInvocationHandler handler = this.createHomeInvocationHandler();

      try
      {
         // Create a new Proxy instance, and return
         return this.getConstructorProxyHome().newInstance(handler);
      }
      catch (Throwable t)
      {
         // Throw a descriptive error message along with the originating Throwable 
         throw new RuntimeException("Could not create Home Proxy for " + this.getMetadata().getEjbName(), t);
      }
   }

   /**
    * Create an EJB3 Business proxy with no 
    * specific target business interface.  The 
    * returned proxy will implement all appropriate
    * business interfaces.  Additionally, if
    * the Home interface is bound alongside 
    * the Default (same JNDI Name), this 
    * Proxy will implement the Home interface as well.
    * 
    * @return
    */
   public Object createProxyDefault()
   {
      // Obtain Constructor to Default Proxy
      Constructor<?> constructor = this.getConstructorProxyDefault();
      assert constructor != null : "Constructor for Default Proxy was null; perhaps the "
            + SessionProxyFactory.class.getSimpleName() + " was not properly started?";

      // Create a new InvocationHandler
      SessionProxyInvocationHandler handler = this.createBusinessDefaultInvocationHandler();

      try
      {
         // Create a new Proxy instance, and return
         return constructor.newInstance(handler);
      }
      catch (Throwable t)
      {
         // Throw a descriptive error message along with the originating Throwable 
         throw new RuntimeException("Could not create the Default Proxy for " + this.getMetadata().getEjbName(), t);
      }
   }

   /**
    * Create an EJB3 Business Proxy specific to the specified
    * target business interface name (expressed as 
    * a fully-qualified class name)
    * 
    * @param businessInterfaceName
    * @return
    */
   public Object createProxyBusiness(final String businessInterfaceName)
   {
      // Ensure businessInterfaceName is specified
      assert businessInterfaceName != null && businessInterfaceName.trim().length() > 0 : "Required business interface type name was not specified";

      try
      {

         // Obtain the correct business proxy constructor
         Constructor<?> constructor = this.getConstructorsProxySpecificBusinessInterface().get(
               businessInterfaceName.trim());

         // Ensure the constructor was found
         assert constructor != null : "No business proxy constructor for \"" + businessInterfaceName
               + "\" was found; not created at start() properly?  Bad value bound as RefAddr in JNDI?";

         // Create a new InvocationHandler
         SessionProxyInvocationHandler handler = this
               .createBusinessInterfaceSpecificInvocationHandler(businessInterfaceName);

         // Create a new Proxy instance
         Object proxy = constructor.newInstance(handler);

         // Return
         return proxy;
      }
      catch (Throwable t)
      {
         // Throw a descriptive error message along with the originating Throwable 
         throw new RuntimeException("Could not create the EJB3 Business Proxy implementing \"" + businessInterfaceName
               + "\" for " + this.getMetadata().getEjbName(), t);
      }
   }

   /**
    * Create an EJB2.x Proxy 
    * 
    * @return
    */
   public Object createProxyEjb2x()
   {
      // Create a new InvocationHandler
      SessionProxyInvocationHandler handler = this.createEjb2xComponentInterfaceInvocationHandler();

      try
      {
         // Create a new Proxy instance, and return
         return this.getConstructorProxyEjb2x().newInstance(handler);
      }
      catch (Throwable t)
      {
         // Throw a descriptive error message along with the originating Throwable 
         throw new RuntimeException("Could not create the EJB2.x Proxy for " + this.getMetadata().getEjbName(), t);
      }
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle callback to be invoked 
    * before the ProxyFactory is able to service requests
    * 
    *  @throws Exception
    */
   @Override
   public void start() throws Exception
   {
      // Call Super
      super.start();

      /*
       * TODO:
       * 
       * Yet another method that should be broken apart
       * and analyzed for re-use.
       */

      /*
       * Make Proxies for EJB3 Business Interfaces
       */

      // Obtain Business Interface Types
      Set<String> businessInterfaceTypes = this.getBusinessInterfaceTypes();

      // Initialize
      Set<Class<?>> businessInterfaceClasses = new HashSet<Class<?>>();

      // Determine if business interfaces are defined
      boolean hasBusinessInterfaces = businessInterfaceTypes != null && businessInterfaceTypes.size() > 0;

      // If there are business interfaces
      if (hasBusinessInterfaces)
      {
         // For all business interface types
         for (String businessInterfaceType : businessInterfaceTypes)
         {
            Class<?> businessInterface = null;
            try
            {
               // Load
               businessInterface = this.getClassLoader().loadClass(businessInterfaceType);
            }
            catch (ClassNotFoundException cnfe)
            {
               throw new RuntimeException("Could not find specified Session Bean Business Interface \""
                     + businessInterfaceType + "\" in " + ClassLoader.class.getSimpleName() + " for EJB "
                     + this.getMetadata().getEjbName(), cnfe);
            }

            // Add business interface to classes
            businessInterfaceClasses.add(businessInterface);

            // Make Proxy specific to the business interface
            Set<Class<?>> businessInterfaces = new HashSet<Class<?>>();
            businessInterfaces.add(businessInterface);
            Constructor<?> businessInterfaceConstructor = this.createProxyConstructor(businessInterfaces, this
                  .getClassLoader());
            log.debug("Created Session Bean Business Interface-Specific Proxy Constructor implementing \""
                  + businessInterfaceType + "\"");

            // Set
            this.getConstructorsProxySpecificBusinessInterface().put(businessInterfaceType,
                  businessInterfaceConstructor);

         }
      }

      /*
       * Make Constructor for Home
       */

      // Obtain Home Interface Type
      String homeInterfaceType = this.getHomeType();

      // Determine if Home is defined
      boolean hasHomeInterface = homeInterfaceType != null && !homeInterfaceType.equals("");

      // Ensure Home ad/or Business is defined
      if (!hasHomeInterface && !hasBusinessInterfaces)
      {
         // Throw bean provider descriptive error
         throw new RuntimeException("Cannot deploy EJB " + this.getMetadata().getEjbName()
               + " as it has no EJB3 (Business Interface) or EJB2.x (Home Interface) Views Defined.");
      }

      // Initialize Home Interface Class
      Class<?> homeInterfaceClass = null;

      // If Home is defined
      if (hasHomeInterface)
      {
         try
         {
            // Load the Home class
            homeInterfaceClass = this.getClassLoader().loadClass(homeInterfaceType);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new RuntimeException("Could not find specified Session Bean Home Interface \"" + homeInterfaceType
                  + "\" in " + ClassLoader.class.getSimpleName() + " for EJB " + this.getMetadata().getEjbName(), cnfe);
         }

         // Make the Home Proxy Constructor
         Set<Class<?>> homeInterfaces = new HashSet<Class<?>>();
         homeInterfaces.add(homeInterfaceClass);
         Constructor<?> homeConstructor = this.createProxyConstructor(homeInterfaces, this.getClassLoader());
         log.debug("Created Session Bean Home Proxy Constructor implementing \"" + homeInterfaceType + "\"");

         // Set the Home Proxy Constructor
         this.setConstructorProxyHome(homeConstructor);
      }

      /*
       * Make Default Proxy
       */

      // Create a Set to hold all relevant interfaces
      Set<Class<?>> defaultProxyInterfaces = new HashSet<Class<?>>();

      // Add all business interfaces
      if (hasBusinessInterfaces)
      {
         defaultProxyInterfaces.addAll(businessInterfaceClasses);
      }

      // If there's a home defined and its bound to the same binding as the default
      if (hasHomeInterface && this.getMetadata().getJndiName().equals(this.getMetadata().getHomeJndiName()))
      {
         defaultProxyInterfaces.add(homeInterfaceClass);
      }

      // Make the Default Business Interfaces Proxy Constructor
      Constructor<?> businessInterfacesConstructor = this.createProxyConstructor(defaultProxyInterfaces, this
            .getClassLoader());
      log.debug("Created Session Bean Default EJB3 Business Proxy Constructor implementing " + defaultProxyInterfaces);

      // Set
      this.setConstructorProxyDefault(businessInterfacesConstructor);

      /*
       * Make Constructor for EJB2.x Views
       */

      // EJB2.x Views may exist only if there is a Home
      if (hasHomeInterface)
      {
         // Initialize Set of EJB2.x Interfaces 
         Set<Class<?>> ejb2xInterfaces = new HashSet<Class<?>>();

         // Get return types of create methods (EJB3 Core Specification 3.6.2.1)
         //TODO Should be handled by metadata, see @Deprecated on the method below
         Set<Class<?>> homeReturnTypes = this.getReturnTypesFromCreateMethods(homeInterfaceClass);
         if (homeReturnTypes != null)
         {
            ejb2xInterfaces.addAll(homeReturnTypes);

         }

         // Get explicitly-defined EJB2x interface type
         String ejb2xDeclaredType = StringUtils.adjustWhitespaceStringToNull(this.getEjb2xInterfaceType());

         // If there is an explicitly-defined EJB2.x interface
         if (ejb2xDeclaredType != null)
         {
            Class<?> ejb2xInterface = null;
            try
            {
               // Load
               ejb2xInterface = this.getClassLoader().loadClass(ejb2xDeclaredType);
            }
            catch (ClassNotFoundException cnfe)
            {
               throw new RuntimeException("Could not find specified Session Bean EJB2.x Interface \""
                     + ejb2xDeclaredType + "\" in " + ClassLoader.class.getSimpleName() + " for EJB "
                     + this.getMetadata().getEjbName(), cnfe);
            }

            // Add
            ejb2xInterfaces.add(ejb2xInterface);
         }

         // Make the EJB2.x Proxy Constructor
         Constructor<?> ejb2xConstructor = this.createProxyConstructor(ejb2xInterfaces, this.getClassLoader());
         log.debug("Created Session Bean EJB2x Proxy Constructor implementing " + ejb2xInterfaces);

         // Set the EJB2.x Proxy Constructor
         this.setConstructorProxyEjb2x(ejb2xConstructor);
      }

   }

   /**
    * Lifecycle callback to be invoked by the ProxyFactoryDeployer
    * before the ProxyFactory is taken out of service, 
    * possibly GC'd
    * 
    * @throws Exception
    */
   @Override
   public void stop() throws Exception
   {
      super.stop();
      //TODO
   }

   /**
    * Returns Proxy interfaces common to all Proxies generated
    * by this ProxyFactory
    * 
    * @return
    */
   @Override
   protected Set<Class<?>> getCommonProxyInterfaces()
   {
      // Initialize
      Set<Class<?>> interfaces = super.getCommonProxyInterfaces();

      // Add
      interfaces.add(SessionProxy.class);

      // Return
      return interfaces;
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the return types declared by the "create" methods for the specified home interface.
    * 
    * EJB3 Core Specification 3.6.2.1
    * JIRA: EJBTHREE-1127
    *  
    * @param homeInterface
    * @param isStateless Flag to indicate whether this is for a Stateful or Stateless container
    * @return
    * @deprecated http://jira.jboss.com/jira/browse/JBMETA-41
    */
   @Deprecated
   protected Set<Class<?>> getReturnTypesFromCreateMethods(Class<?> homeInterface, boolean isStateless)
   {
      /*
       * TODO
       * 
       * Kill the "isStateless" argument, centralize logic as appropriate, 
       * and use polymorphsm to properly implement this method.
       * 
       * The current structure with the "isStateless" was born out of
       * this function originating as a static utility from EJB3 Core.
       * 
       * Note: Perhaps this is a moot point, given the @Deprecated tag 
       * and that jboss-metadata should be implementing this.
       */

      // Ensure we've been passed a Home or LocalHome interface (Developers only)
      assert (EJBHome.class.isAssignableFrom(homeInterface) || EJBLocalHome.class.isAssignableFrom(homeInterface));

      // Ensure we've been passed a Home or LocalHome interface (End-User)
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

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns the a Set of String representations of the Business Interface Types
    * 
    *  @return
    */
   protected abstract Set<String> getBusinessInterfaceTypes();

   /**
    * Returns the String representation of the Home Interface Type
    * @return
    */
   protected abstract String getHomeType();

   /**
    * Returns the String representation of the EJB2.x Interface Types
    * 
    *  @return
    */
   protected abstract String getEjb2xInterfaceType();

   /**
    * Obtains the return types declared by the "create" methods for the specified home interface.
    *  
    * @param homeInterface
    * @return
    */
   protected abstract Set<Class<?>> getReturnTypesFromCreateMethods(Class<?> homeInterface);

   /**
    * Returns the Constructor of the SessionProxyInvocationHandler to be used in 
    * instanciating new handlers to specify in Proxy Creation
    * 
    * Used for creating a Handler for a Business Interface-specific proxy
    * 
    * @return
    */
   protected abstract SessionProxyInvocationHandler createBusinessInterfaceSpecificInvocationHandler(
         String businessInterfaceName);

   /**
    * Returns the Constructor of the SessionProxyInvocationHandler to be used in 
    * instanciating new handlers to specify in Proxy Creation
    * 
    * Used for creating a Handler for a Business Default proxy
    * 
    * @return
    */
   protected abstract SessionProxyInvocationHandler createBusinessDefaultInvocationHandler();

   /**
    * Returns the Constructor of the SessionProxyInvocationHandler to be used in 
    * instanciating new handlers to specify in Proxy Creation
    * 
    * Used for creating a Handler for an EJB2.x Component Interface proxy
    * 
    * @return
    */
   protected abstract SessionProxyInvocationHandler createEjb2xComponentInterfaceInvocationHandler();

   /**
    * Returns the Constructor of the SessionProxyInvocationHandler to be used in 
    * instanciating new handlers to specify in Proxy Creation
    * 
    * Used for creating a Handler for am EJB2.x Home proxy
    * 
    * @return
    */
   protected abstract SessionProxyInvocationHandler createHomeInvocationHandler();

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public JBossSessionBeanMetaData getMetadata()
   {
      return metadata;
   }

   protected void setMetadata(final JBossSessionBeanMetaData metadata)
   {
      this.metadata = metadata;
   }

   protected Constructor<?> getConstructorProxyDefault()
   {
      return constructorProxyDefault;
   }

   protected void setConstructorProxyDefault(Constructor<?> constructorProxyDefault)
   {
      this.constructorProxyDefault = constructorProxyDefault;
   }

   protected Map<String, Constructor<?>> getConstructorsProxySpecificBusinessInterface()
   {
      return constructorsProxySpecificBusinessInterface;
   }

   protected void setConstructorsProxySpecificBusinessInterface(
         Map<String, Constructor<?>> constructorsProxySpecificBusinessInterface)
   {
      this.constructorsProxySpecificBusinessInterface = constructorsProxySpecificBusinessInterface;
   }

   protected Constructor<?> getConstructorProxyHome()
   {
      return constructorProxyHome;
   }

   protected void setConstructorProxyHome(Constructor<?> constructorProxyHome)
   {
      this.constructorProxyHome = constructorProxyHome;
   }

   protected Constructor<?> getConstructorProxyEjb2x()
   {
      return constructorProxyEjb2x;
   }

   protected void setConstructorProxyEjb2x(Constructor<?> constructorProxyEjb2x)
   {
      this.constructorProxyEjb2x = constructorProxyEjb2x;
   }

}
