/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.proxy.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.jws.WebService;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.KernelAbstraction;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;
import org.jboss.ejb3.annotation.RemoteHomeBinding;
import org.jboss.ejb3.annotation.impl.LocalImpl;
import org.jboss.ejb3.annotation.impl.RemoteImpl;
import org.jboss.ejb3.common.lang.ClassHelper;
import org.jboss.ejb3.common.spi.ErrorCodes;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ProxyFactoryHelper
{
   private static final Logger log = Logger.getLogger(ProxyFactoryHelper.class);

   private static String getEndpointInterface(Container container)
   {
      WebService ws = (javax.jws.WebService) ((EJBContainer) container).resolveAnnotation(javax.jws.WebService.class);
      if (ws != null)
      {
         return ws.endpointInterface();
      }
      return null;
   }

   /**
    *
    * @param container
    * @return       the local interfaces of the container or an empty array
    */
   public static Class<?>[] getLocalAndBusinessLocalInterfaces(Container container)
   {
      // Initialize
      Set<Class<?>> localAndBusinessLocalInterfaces = new HashSet<Class<?>>();

      // Obtain Bean Class
      Class<?> beanClass = container.getBeanClass();

      // Obtain @Local
      Local localAnnotation = ((EJBContainer) container).getAnnotation(Local.class);

      // Obtain @LocalHome
      LocalHome localHomeAnnotation = ((EJBContainer) container).getAnnotation(LocalHome.class);

      // Obtain @Remote
      Remote remoteAnnotation = ((EJBContainer) container).getAnnotation(Remote.class);

      // Obtain Remote and Business Remote interfaces
      Class<?>[] remoteAndBusinessRemoteInterfaces = ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(container);

      // Obtain all business interfaces from the bean class
      Set<Class<?>> businessInterfacesImplementedByBeanClass = ProxyFactoryHelper.getBusinessInterfaces(beanClass);

      // Obtain all business interfaces directly implemented by the bean class (not including supers)
      Set<Class<?>> businessInterfacesDirectlyImplementedByBeanClass = ProxyFactoryHelper.getBusinessInterfaces(
            beanClass, false);

      // Determine whether Stateful or Stateless
      boolean isStateless = (container instanceof StatelessContainer) ? true : false;

      // EJBTHREE-1127
      // Determine local interface from return value of "create" in Local Home
      if (localHomeAnnotation != null)
      {
         localAndBusinessLocalInterfaces.addAll(ProxyFactoryHelper.getReturnTypesFromCreateMethods(localHomeAnnotation
               .value(), isStateless));
      }

      // For each of the business interfaces implemented by the bean class
      for (Class<?> clazz : businessInterfacesImplementedByBeanClass)
      {
         // If @Local is on the interface
         if (clazz.isAnnotationPresent(Local.class))
         {
            // Add to the list of locals
            localAndBusinessLocalInterfaces.add(clazz);
         }
      }

      // EJBTHREE-1062
      // EJB 3 Core Specification 4.6.6
      // If bean class implements a single interface, that interface is assumed to be the 
      // business interface of the bean. This business interface will be a local interface unless the
      // interface is designated as a remote business interface by use of the Remote 
      // annotation on the bean class or interface or by means of the deployment descriptor. 
      if (businessInterfacesDirectlyImplementedByBeanClass.size() == 1 && localAndBusinessLocalInterfaces.size() == 0)
      {
         // Obtain the implemented interface
         Class<?> singleInterface = businessInterfacesDirectlyImplementedByBeanClass.iterator().next();

         // If not explicitly marked as @Remote, and is a valid business interface
         if (remoteAnnotation == null && singleInterface.getAnnotation(Remote.class) == null)
         {
            // Return the implemented interface, adding to the container  
            Class<?>[] returnValue = new Class[]
            {singleInterface};
            Local li = new LocalImpl(returnValue);
            ((EJBContainer) container).getAnnotations().addClassAnnotation(Local.class, li);
            return returnValue;
         }
      }

      // @Local was defined
      if (localAnnotation != null)
      {
         // If @Local has no value or empty value
         if (localAnnotation.value() == null || localAnnotation.value().length == 0)
         {
            // If @Local is defined with no value and there are no business interfaces
            if (businessInterfacesImplementedByBeanClass.size() == 0)
            {
               throw new RuntimeException("Use of empty @Local on bean " + container.getEjbName()
                     + " and there are no valid business interfaces");
            }
            // If more than one business interface is directly implemented by the bean class
            else if (businessInterfacesImplementedByBeanClass.size() > 1)
            {
               throw new RuntimeException("Use of empty @Local on bean " + container.getEjbName()
                     + " with more than one default interface " + businessInterfacesImplementedByBeanClass);
            }
            // JIRA EJBTHREE-1062
            // EJB 3 4.6.6
            // If the bean class implements only one business interface, that 
            //interface is exposed as local business if not denoted as @Remote
            else
            {
               // If not explicitly marked as @Remote
               if (remoteAnnotation == null)
               {
                  // Return the implemented interface and add to container
                  Class<?>[] returnValue = businessInterfacesImplementedByBeanClass.toArray(new Class<?>[]
                  {});
                  Local li = new LocalImpl(returnValue);
                  ((EJBContainer) container).getAnnotations().addClassAnnotation(Local.class, li);
                  return returnValue;
               }
            }
         }
         // @Local has value 
         else
         {
            // For each of the interfaces in @Local.value
            for (Class<?> clazz : localAnnotation.value())
            {
               // Add to the list of locals
               localAndBusinessLocalInterfaces.add(clazz);
            }

            // For each of the business interfaces implemented by the bean class
            for (Class<?> clazz : businessInterfacesImplementedByBeanClass)
            {
               // If @Local is on the interface
               if (clazz.isAnnotationPresent(Local.class))
               {
                  // Add to the list of locals
                  localAndBusinessLocalInterfaces.add(clazz);
               }
            }
         }
      }

      // If local interfaces have been defined/discovered
      if (localAndBusinessLocalInterfaces.size() > 0)
      {
         // Check to ensure @Local and @Remote are not defined on the same interface
         // EJBTHREE-751
         for (Class<?> remoteInterface : remoteAndBusinessRemoteInterfaces)
         {
            for (Class<?> localInterface : localAndBusinessLocalInterfaces)
            {
               if (localInterface.equals(remoteInterface))
               {
                  /*
                   * The error code in this message is checked by the ejbthree751 integration test
                   */
                  throw new RuntimeException("@Remote and @Local may not both be specified on the same interface \""
                        + remoteInterface.toString() + "\" for EJB \"" + container.getEjbName()
                        + "\" per EJB3 Spec 4.6.6, Bullet 5.4 [" + ErrorCodes.ERROR_CODE_EJBTHREE751 + "]");
               }
            }
         }

         // Return local interfaces, first adding to the container
         Class<?>[] rtn = localAndBusinessLocalInterfaces.toArray(new Class<?>[]
         {});
         localAnnotation = new LocalImpl(rtn);
         ((EJBContainer) container).getAnnotations().addClassAnnotation(Local.class, localAnnotation);
         return rtn;
      }
      // If no local interfaces have been defined/discovered
      else
      {
         // Obtain WS Endpoint
         String endpoint = ProxyFactoryHelper.getEndpointInterface(container);

         // If neither WS Endpoint or remotes are defined
         if (remoteAndBusinessRemoteInterfaces.length == 0 && endpoint == null)
            throw new RuntimeException(
                  "Bean Class "
                        + beanClass.getName()
                        + " has no local, webservice, or remote interfaces defined and does not implement at least one business interface: "
                        + container.getEjbName());

      }

      // No local or business local interfaces discovered
      return new Class<?>[]
      {};
   }

   /**
    * Resolve the potential business interfaces on an enterprise bean.
    * Returns all interfaces implemented by this class and its supers which
    * are potentially a business interface.
    *
    * Note: for normal operation call container.getBusinessInterfaces().
    *
    * @param    beanClass   the EJB implementation class
    * @return   a list of potential business interfaces
    * @see      org.jboss.ejb3.EJBContainer#getBusinessInterfaces()
    */
   public static Set<Class<?>> getBusinessInterfaces(Class<?> beanClass)
   {
      // Obtain all business interfaces implemented by this bean class and its superclasses
      return ProxyFactoryHelper.getBusinessInterfaces(beanClass, new HashSet<Class<?>>());
   }

   /**
    * Resolve the potential business interfaces on an enterprise bean.
    * Returns all interfaces implemented by this class and, optionally, its supers which
    * are potentially a business interface.
    *
    * Note: for normal operation call container.getBusinessInterfaces().
    *
    * @param    beanClass   the EJB implementation class
    * @param    includeSupers Whether or not to include superclasses of the specified beanClass in this check
    * @return   a list of potential business interfaces
    * @see      org.jboss.ejb3.EJBContainer#getBusinessInterfaces()
    */
   public static Set<Class<?>> getBusinessInterfaces(Class<?> beanClass, boolean includeSupers)
   {
      // Obtain all business interfaces implemented by this bean class and optionally, its superclass
      return ProxyFactoryHelper.getBusinessInterfaces(beanClass, new HashSet<Class<?>>(), includeSupers);
   }

   private static Set<Class<?>> getBusinessInterfaces(Class<?> beanClass, Set<Class<?>> interfaces)
   {
      return ProxyFactoryHelper.getBusinessInterfaces(beanClass, interfaces, true);
   }

   private static Set<Class<?>> getBusinessInterfaces(Class<?> beanClass, Set<Class<?>> interfaces,
         boolean includeSupers)
   {
      /*
       * 4.6.6:
       * The following interfaces are excluded when determining whether the bean class has
       * more than one interface: java.io.Serializable; java.io.Externalizable; 
       * any of the interfaces defined by the javax.ejb package.
       */
      for (Class<?> intf : beanClass.getInterfaces())
      {
         if (intf.equals(java.io.Externalizable.class))
            continue;
         if (intf.equals(java.io.Serializable.class))
            continue;
         if (intf.getName().startsWith("javax.ejb"))
            continue;

         // FIXME Other aop frameworks might add other interfaces, this should really be configurable
         if (intf.getName().startsWith("org.jboss.aop"))
            continue;

         interfaces.add(intf);
      }

      // If there's no superclass, or we shouldn't check the superclass, return
      if (!includeSupers || beanClass.getSuperclass() == null)
      {
         return interfaces;
      }
      else
      {
         // Include any superclasses' interfaces
         return getBusinessInterfaces(beanClass.getSuperclass(), interfaces);
      }
   }

   public static Class<?> getLocalHomeInterface(Container container)
   {
      LocalHome li = ((EJBContainer) container).getAnnotation(javax.ejb.LocalHome.class);
      if (li != null)
         return li.value();
      return null;
   }

   public static Class<?> getRemoteHomeInterface(Container container)
   {
      RemoteHome li = ((EJBContainer) container).getAnnotation(javax.ejb.RemoteHome.class);
      if (li != null)
         return li.value();
      return null;
   }

   public static boolean publishesInterface(Container container, Class<?> businessInterface)
   {
      if (!(container instanceof SessionContainer))
         return false;
      Class<?>[] remotes = getRemoteAndBusinessRemoteInterfaces(container);
      for (Class<?> intf : remotes)
      {
         if (intf.getName().equals(businessInterface.getName()))
            return true;
      }

      Class<?> remoteHome = getRemoteHomeInterface(container);
      if (remoteHome != null)
      {
         if (businessInterface.getName().equals(remoteHome.getName()))
         {
            return true;
         }
      }
      Class<?>[] locals = getLocalAndBusinessLocalInterfaces(container);
      for (Class<?> clazz : locals)
      {
         if (clazz.getName().equals(businessInterface.getName()))
         {
            return true;
         }
      }
      Class<?> localHome = getLocalHomeInterface(container);
      if (localHome != null)
      {
         if (businessInterface.getName().equals(localHome.getName()))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Obtains the JNDI name for the specified container; may either be explicitly-defined by 
    * annotation / XML or will otherwise default to the configured JNDI Binding Policy
    * 
    * @param container
    * @param businessInterface
    * @return
    */
   public static String getJndiName(EJBContainer container, Class<?> businessInterface)
   {
      assert container != null : "container is null";
      assert businessInterface != null : "businessInterface is null";

      // Initialize to defaults of remote and not home
      String jndiName = null;
      boolean isHome = false;
      boolean isLocal = false;

      // Determine if remote
      Class<?>[] remotes = ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(container);
      for (Class<?> clazz : remotes)
      {
         if (clazz.getName().equals(businessInterface.getName()))
         {
            // Check for declared @RemoteBindings
            RemoteBindings bindings = ((EJBContainer) container).getAnnotation(RemoteBindings.class);
            if (bindings != null)
            {
               // Encountered, return
               return bindings.value()[0].jndiBinding();
            }
            // Check for declared @RemoteBinding 
            RemoteBinding binding = ((EJBContainer) container).getAnnotation(RemoteBinding.class);
            if (binding != null)
            {
               // Encountered, return
               return binding.jndiBinding();
            }
         }
      }

      // Determine if remote home
      Class<?> remoteHome = getRemoteHomeInterface(container);
      if (remoteHome != null)
      {
         if (businessInterface.getName().equals(remoteHome.getName()))
         {
            // Check for declared @RemoteHomeBinding 
            RemoteHomeBinding binding = ((EJBContainer) container).getAnnotation(RemoteHomeBinding.class);
            if (binding != null)
            {
               // Encountered, return
               return binding.jndiBinding();
            }

            // Set home for policy
            isHome = true;
         }
      }

      // Determine if local and home
      Class<?> localHome = getLocalHomeInterface(container);
      if (localHome != null)
      {
         if (businessInterface.getName().equals(localHome.getName()))
         {
            // Check for declared @LocalHomeBinding 
            LocalHomeBinding binding = ((EJBContainer) container).getAnnotation(LocalHomeBinding.class);
            if (binding != null)
            {
               // Encountered, return
               return binding.jndiBinding();
            }

            // Set local and home for policy
            isHome = true;
            isLocal = true;
         }
      }

      // Determine if local
      Class<?>[] locals = getLocalAndBusinessLocalInterfaces(container);
      for (Class<?> clazz : locals)
      {
         if (clazz.getName().equals(businessInterface.getName()))
         {
            // Check for declared @LocalBinding 
            LocalBinding binding = ((EJBContainer) container).getAnnotation(LocalBinding.class);
            if (binding != null)
            {
               // Encountered, return
               return binding.jndiBinding();
            }

            // Set local for policy
            isLocal = true;
         }
      }

      // If JNDI Name has not been explicitly specified, use policy
      if (jndiName == null)
      {
         // Log 
         log.debug("JNDI name has not been explicitly set for EJB " + container.getEjbName() + ", interface "
               + businessInterface.getName());

         // Set JNDI name
         JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData) container.getXml();
         jndiName = smd.getJndiName();
      }

      // Return
      return jndiName;
   }

   /**
    * Returns all local interfaces in the specified container; interfaces
    * marked as "local" via either annotation or XML and extending EJBLocalObject
    * 
    * @param container
    * @return
    */
   public static Class<?>[] getLocalInterfaces(Container container)
   {
      return ProxyFactoryHelper.getInterfacesAssignableFromClass(ProxyFactoryHelper
            .getLocalAndBusinessLocalInterfaces(container), EJBLocalObject.class, true);
   }

   /**
    * Returns all remote interfaces in the specified container; interfaces
    * marked as "remote" via either annotation or XML and extending EJBObject
    * 
    * @param container
    * @return
    */
   public static Class<?>[] getRemoteInterfaces(Container container)
   {
      return ProxyFactoryHelper.getInterfacesAssignableFromClass(ProxyFactoryHelper
            .getRemoteAndBusinessRemoteInterfaces(container), EJBObject.class, true);
   }

   /**
    * Returns all local business interfaces in the specified container; interfaces
    * marked as "local" via either annotation or XML and not extending EJBLocalObject
    * 
    * @param container
    * @return
    */
   public static Class<?>[] getLocalBusinessInterfaces(Container container)
   {
      return ProxyFactoryHelper.getInterfacesAssignableFromClass(ProxyFactoryHelper
            .getLocalAndBusinessLocalInterfaces(container), EJBLocalObject.class, false);
   }

   /**
    * Returns all remote business interfaces in the specified container; interfaces
    * marked as "remote" via either annotation or XML and not extending EJBObject
    * 
    * @param container
    * @return
    */
   public static Class<?>[] getRemoteBusinessInterfaces(Container container)
   {
      return ProxyFactoryHelper.getInterfacesAssignableFromClass(ProxyFactoryHelper
            .getRemoteAndBusinessRemoteInterfaces(container), EJBObject.class, false);
   }

   /**
    * Returns an subset of the specified array of interfaces either 
    * assignable to or not assignable to the specified class, depending 
    * upon the flag "assignable"
    * 
    * @param interfaces
    * @param clazz
    * @param assignable
    * @return
    */
   private static Class<?>[] getInterfacesAssignableFromClass(Class<?>[] interfaces, Class<?> clazz, boolean assignable)
   {
      // Initialize
      List<Class<?>> subset = new ArrayList<Class<?>>();

      // For all interfaces  
      for (Class<?> interfaze : interfaces)
      {
         // If we want assignable classes only
         if (assignable && clazz.isAssignableFrom(interfaze))
         {
            subset.add(interfaze);
         }

         // If we want classes not assignable only
         if (!assignable && !clazz.isAssignableFrom(interfaze))
         {
            subset.add(interfaze);
         }
      }

      // Return
      return subset.toArray(new Class<?>[]
      {});
   }

   /**
    * Returns all remote and remote business interfaces in the specified container, 
    * designated by @Remote or in ejb-jar.xml as "remote" or "business-remote"
    *
    * @param container
    * @return   the remote interfaces of the container or an empty array
    */
   public static Class<?>[] getRemoteAndBusinessRemoteInterfaces(Container container)
   {
      // Initialize
      Remote remoteAnnotation = ((EJBContainer) container).getAnnotation(Remote.class);
      RemoteHome remoteHomeAnnotation = ((EJBContainer) container).getAnnotation(RemoteHome.class);
      Set<Class<?>> remoteAndRemoteBusinessInterfaces = new HashSet<Class<?>>();
      Class<?> beanClass = container.getBeanClass();
      boolean isStateless = (container instanceof StatelessContainer) ? true : false;

      // Obtain business interfaces
      Class<?>[] businessInterfaces = ProxyFactoryHelper.getBusinessInterfaces(beanClass).toArray(new Class[]
      {});

      // EJBTHREE-1127
      // Determine remote interface from return value of "create" in Remote Home
      if (remoteHomeAnnotation != null)
      {
         remoteAndRemoteBusinessInterfaces.addAll(ProxyFactoryHelper.getReturnTypesFromCreateMethods(
               remoteHomeAnnotation.value(), isStateless));
      }

      // If @Remote is not defined
      if (remoteAnnotation == null)
      {
         // For each of the business interfaces
         for (Class<?> clazz : businessInterfaces)
         {
            // If @Remote is on the business interface
            if (clazz.isAnnotationPresent(Remote.class))
            {
               // Add to the list of remotes
               remoteAndRemoteBusinessInterfaces.add(clazz);
            }
         }
      }
      // @Remote was defined
      else
      {
         // @Remote declares interfaces, add these
         if (remoteAnnotation.value().length > 0)
         {
            for (Class<?> clazz : remoteAnnotation.value())
            {
               remoteAndRemoteBusinessInterfaces.add(clazz);
            }
         }
         // @Remote is empty
         else
         {
            // No business interfaces were defined on the bean
            if (businessInterfaces.length == 0)
            {
               throw new RuntimeException("Use of empty @Remote on bean " + container.getEjbName()
                     + " and there are no valid business interfaces");
            }

            // More than one default interface, cannot be marked as @Remote
            else if (businessInterfaces.length > 1)
            {
               throw new RuntimeException("Use of empty @Remote on bean " + container.getEjbName()
                     + " with more than one default interface " + businessInterfaces);
            }
            // Only one default interface, mark as @Remote and return
            else
            {
               Class<?>[] rtn =
               {(Class<?>) businessInterfaces[0]};
               remoteAnnotation = new RemoteImpl(rtn);
               ((EJBContainer) container).getAnnotations().addClassAnnotation(javax.ejb.Remote.class, remoteAnnotation);
               return rtn;
            }
         }
      }

      // If remotes were found
      if (remoteAndRemoteBusinessInterfaces.size() > 0)
      {
         // Set interfaces and return
         Class<?>[] remotesArray = remoteAndRemoteBusinessInterfaces
               .toArray(new Class[remoteAndRemoteBusinessInterfaces.size()]);
         remoteAnnotation = new RemoteImpl(remotesArray);
         ((EJBContainer) container).getAnnotations().addClassAnnotation(Remote.class, remoteAnnotation);
         return remoteAnnotation.value();
      }
      // No remotes were found
      else
      {
         return new Class<?>[]
         {};
      }
   }

   /**
    * Obtains the return types declared by the "create" methods for the specified home interface.
    *  
    * @param homeInterface
    * @param isStateless Flag to indicate whether this is for a Stateful or Stateless container
    * @return
    */
   private static Set<Class<?>> getReturnTypesFromCreateMethods(Class<?> homeInterface, boolean isStateless)
   {
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
   
//   public static String getClientBindUrl(RemoteBinding binding) throws Exception
//   {
//      String clientBindUrl = binding.clientBindUrl();
//      if (clientBindUrl.trim().length() == 0)
//      {
//         if (binding.invokerName()!=null && binding.invokerName().trim().length() != 0)
//         {
//            try
//            {
//               ObjectName connectionON = new ObjectName(binding.invokerName());
//               KernelAbstraction kernelAbstraction = KernelAbstractionFactory.getInstance();
//               clientBindUrl = (String)kernelAbstraction.getAttribute(connectionON, "InvokerLocator");
//            }
//            catch (Exception e)
//            {
//               log.warn("Unable to find InvokerLocator " + binding.invokerName() + ". Using default. " + e);
//               clientBindUrl = RemoteProxyFactory.DEFAULT_CLIENT_BINDING;
//            }
//         }
//         else
//         {
//            try
//            {
//               ObjectName connectionON = new ObjectName("jboss.remoting:type=Connector,name=DefaultEjb3Connector,handler=ejb3");
//               KernelAbstraction kernelAbstraction = KernelAbstractionFactory.getInstance();
//               clientBindUrl = (String)kernelAbstraction.getAttribute(connectionON, "InvokerLocator");
//            }
//            catch (Exception e)
//            {
//               log.warn("Unable to find default InvokerLocator. Using default. " + e);
//               clientBindUrl = RemoteProxyFactory.DEFAULT_CLIENT_BINDING;
//            }
//         }
//      }
//      else if (clientBindUrl.indexOf("0.0.0.0") != -1)
//      {
//         KernelAbstraction kernelAbstraction = KernelAbstractionFactory.getInstance();
//         ObjectName query = new ObjectName("jboss.remoting:type=Connector,handler=ejb3,*");
//         Set mbeanSet = kernelAbstraction.getMBeans(query);
//         
//         URI targetUri = new URI(clientBindUrl);
//         Iterator mbeans = mbeanSet.iterator();
//         while (mbeans.hasNext())
//         {
//            ObjectInstance invokerInstance = (ObjectInstance)mbeans.next();
//            ObjectName invokerName = invokerInstance.getObjectName();
//            String invokerLocator = (String)kernelAbstraction.getAttribute(invokerName, "InvokerLocator");
//            URI uri = new URI(invokerLocator);
//          
//            if (uri.getScheme().equals(targetUri.getScheme()) && uri.getPort() == targetUri.getPort())
//            {
//               return invokerLocator;
//            }
//         }
//      }
//      
//      if (clientBindUrl == null)
//         clientBindUrl = RemoteProxyFactory.DEFAULT_CLIENT_BINDING;
//      
//      return clientBindUrl;
//   }
   
   /**
    * Create a Proxy Constructor for the specified interfaces, using the specified CL
    * 
    * @param interfaces
    * @param cl
    * @return
    * @throws Exception
    */
   public static Constructor<?> createProxyConstructor(Class<?>[] interfaces, ClassLoader cl) throws Exception
   {
      Class<?> proxyClass = java.lang.reflect.Proxy.getProxyClass(cl, interfaces);
      return proxyClass.getConstructor(InvocationHandler.class);
   }

   public static String getHomeJndiName(EJBContainer container)
   {
      // Use explicitly-specified binding, if defined
      RemoteHomeBinding binding = container.getAnnotation(RemoteHomeBinding.class);
      if (binding != null)
         return binding.jndiBinding();

      JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData)container.getXml();
      return smd.getHomeJndiName();
   }

   public static String getLocalHomeJndiName(EJBContainer container)
   {
      // Use explicitly-specified binding, if defined
      LocalHomeBinding binding = container.getAnnotation(LocalHomeBinding.class);
      if (binding != null)
         return binding.jndiBinding();

      // Use Default JNDI Binding Policy
      JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData)container.getXml();
      return smd.getLocalHomeJndiName();
   }

   public static String getLocalJndiName(EJBContainer container)
   {
      return getLocalJndiName(container, true);
   }

   private static String getLocalJndiName(EJBContainer container, boolean conflictCheck)
   {
      // See if local binding is explicitly-defined
      LocalBinding localBinding = container.getAnnotation(LocalBinding.class);

      // If none specified
      if (localBinding == null || (localBinding.jndiBinding() != null && localBinding.jndiBinding().trim().length() == 0))
      {
         JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData)container.getXml();
         String name = smd.getLocalJndiName();

         // If we should check for naming conflict
         if (conflictCheck){
            // Check
            ProxyFactoryHelper.checkForJndiNamingConflict(container);
         }

         // Return
         return name;
      }
      // Local Binding was explicitly-specified, use it
      else
      {
         return localBinding.jndiBinding();
      }
   }

   public static String getRemoteBusinessJndiName(EJBContainer container)
   {
      return ProxyFactoryHelper.getRemoteBusinessJndiName(container, true);
   }

   public static String getRemoteBusinessJndiName(EJBContainer container, boolean check)
   {
      RemoteBinding binding = container.getAnnotation(RemoteBinding.class);

      return ProxyFactoryHelper.getRemoteBusinessJndiName(container, binding);
   }

   private static void checkForJndiNamingConflict(EJBContainer container)
   {
      if (container.getAnnotation(Local.class) != null)
      {
         JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData)container.getXml();
         String localJndiName = smd.getLocalJndiName();
         String remoteJndiName = smd.getJndiName();
         String ejbName = container.getEjbName();
         if (localJndiName != null)
         {
            if (localJndiName.equals(remoteJndiName) || localJndiName.startsWith(remoteJndiName + "/"))
               throw new javax.ejb.EJBException("Conflict between default jndi name " + remoteJndiName
                     + " for both remote and local for ejb-name:" + ejbName + ", bean class="
                     + container.getBeanClass() + "\nLocal JNDI Name: " + localJndiName + "\nRemote JNDI Name: "
                     + remoteJndiName);
         }
      }
   }

   private static String getRemoteBusinessJndiName(EJBContainer container, RemoteBinding binding)
   {
      return ProxyFactoryHelper.getRemoteBusinessJndiName(container, binding, true);
   }

   public static String getRemoteBusinessJndiName(EJBContainer container, RemoteBinding binding, boolean conflictCheck)
   {
      // Initialize
      String jndiName = null;

      // If binding is not defined
      if (binding == null || binding.jndiBinding() == null || binding.jndiBinding().trim().equals(""))
      {
         // Use the default
         jndiName = getDefaultRemoteBusinessJndiName(container);

         // If we should check for a naming conflict
         if (conflictCheck)
         {
            // Check
            ProxyFactoryHelper.checkForJndiNamingConflict(container);
         }

      }
      // Binding is explicitly-defined
      else
      {
         // use it
         jndiName = binding.jndiBinding();
      }

      // Return
      return jndiName;
   }

   public static String getDefaultRemoteBusinessJndiName(EJBContainer container)
   {
      JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData)container.getXml();
      String jndiName = smd.getJndiName();
      return jndiName;
   }
}