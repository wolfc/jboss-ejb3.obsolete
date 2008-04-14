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
package org.jboss.ejb3.session;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.naming.NamingException;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.proxy.EJBMetaDataImpl;
import org.jboss.ejb3.proxy.handle.HomeHandleImpl;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public abstract class BaseSessionProxyFactory implements ProxyFactory, Externalizable
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(BaseSessionProxyFactory.class);
   
   private SessionSpecContainer container;
   protected String containerGuid;
   protected String containerClusterUid;
   protected boolean isClustered = false;
   protected String jndiName;
   
   /**
    * Proxy Constructor for the Business Interfaces' Proxy
    */
   protected Constructor<?> businessProxyConstructor;
   
   /**
    * Proxy Constructor for the EJBObject/EJBLocalObject Proxy
    */
   protected Constructor<?> ejb21ProxyConstructor; 
   
   private static final String METHOD_PREFIX_EJB21_CREATE = "create";
   
   public BaseSessionProxyFactory()
   {
   }
   
   protected BaseSessionProxyFactory(SessionSpecContainer container)
   {
      assert container != null : "container is null";
      
      setContainer(container);
   }
   
   public Object createHomeProxy()
   {
      throw new RuntimeException("NYI");
   }
   
   /**
    * Creates the Proxy constructors
    */
   protected void createProxyConstructors() throws Exception
   {
      // Obtain this bean class' CL
      ClassLoader cl = this.getContainer().getBeanClass().getClassLoader();
      
      // Create business proxy constructor
      Class<?>[] businessInterfaces = this.getInterfacesForBusinessProxy();
      this.businessProxyConstructor = ProxyFactoryHelper.createProxyConstructor(businessInterfaces, cl);
      
      // Create EJB21 proxy constructor
      Class<?>[] ejb21Interfaces = this.getInterfacesForEjb21Proxy();
      if (ejb21Interfaces != null)
      {
         this.ejb21ProxyConstructor = ProxyFactoryHelper.createProxyConstructor(ejb21Interfaces, cl);
      }
         
      
      /* plain jdk 
      Class<?> proxyClass = java.lang.reflect.Proxy.getProxyClass(getContainer().getBeanClass().getClassLoader(), interfaces);
      final Class<?>[] constructorParams =
              {InvocationHandler.class};
      businessProxyConstructor = proxyClass.getConstructor(constructorParams);
      
      */
      
      /* javassist */
      /*
      proxyFactory = new javassist.util.proxy.ProxyFactory()
      {
         @Override
         protected ClassLoader getClassLoader()
         {
            return container.getBeanClass().getClassLoader();
         }
      };
      proxyFactory.setInterfaces(interfaces);
      proxyFactory.setSuperclass(JavassistProxy.class);
      proxyClass = proxyFactory.createClass();
      proxyConstructor = proxyClass.getConstructor((Class[]) null);
      */
      
      /* cglib */
      /*
      proxyClass = net.sf.cglib.proxy.Proxy.getProxyClass(container.getBeanClass().getClassLoader(), interfaces);
      final Class[] constructorParams = {net.sf.cglib.proxy.InvocationHandler.class};
      proxyConstructor = proxyClass.getConstructor(constructorParams);
      */
   }
   
   protected void bindProxy(Object proxy) throws NamingException
   {
      try
      {
         log.debug("Binding proxy for " + getContainer().getEjbName() + " in JNDI at " + this.getJndiName());
         Util.rebind(getContainer().getInitialContext(), this.getJndiName(), proxy);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateless proxy with ejb name "
               + getContainer().getEjbName() + " into JNDI under jndiName: "
               + getContainer().getInitialContext().getNameInNamespace() + "/" + this.getJndiName());
         namingException.setRootCause(e);
         throw namingException;
      }
   }
   
   /**
    * Whether or not to bind the home and business interfaces together
    * 
    * @return
    */
   protected abstract boolean bindHomeAndBusinessTogether(); 
   
   protected Object constructProxyBusiness(InvocationHandler handler)
   {
      // Return
      return this.constructProxy(handler, SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   protected Object constructEjb21Proxy(InvocationHandler handler)
   {
      // Return
      return this.constructProxy(handler, SpecificationInterfaceType.EJB21);
   }
   
   /**
    * Construct a new Proxy of the specified type using the 
    * specified handler as argument to the Constructor
    * 
    * @param handler
    * @param specType
    * @return
    */
   protected Object constructProxy(final InvocationHandler handler, SpecificationInterfaceType specType)
   {
      // Initialize
      Object obj = null;

      try
      {
         // Business Proxy
         if (specType.equals(SpecificationInterfaceType.EJB30_BUSINESS))
         {
            obj = this.businessProxyConstructor.newInstance(handler);
         }
         // EJBObject/EJBLocalObject
         else if (specType.equals(SpecificationInterfaceType.EJB21))
         {
            // If there's no EJB21 View
            if (this.ejb21ProxyConstructor == null)
            {
               throw new IllegalStateException(
                     "EJB3 Specification Violation Section 4.3.3: \""
                           + "Only session beans with a remote EJBObject / local EJBLocalObject interface can call this method.");
            }

            obj = this.ejb21ProxyConstructor.newInstance(handler);
         }
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if (t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw new RuntimeException(t);
      }

      // Ensure Proxy object was created
      assert obj != null : "Proxy Object must not be null";

      // Return
      return obj;
   }

   
   protected void setContainer(SessionSpecContainer container)
   {
      this.container = container;
      this.containerGuid = Ejb3Registry.guid(container);
      this.containerClusterUid = Ejb3Registry.clusterUid(container);
      this.isClustered = container.isClustered();
   }
   
   protected SessionSpecContainer getContainer()
   {
      if (container == null)
      {
         container = (SessionSpecContainer)Ejb3Registry.findContainer(containerGuid);
         
         if (container == null && isClustered)
            container = (SessionSpecContainer)Ejb3Registry.getClusterContainer(containerClusterUid);
      }
      
      return container;
   }
   
   /**
    * Obtains interfaces to be used in the business proxy
    * 
    * @return
    */
   protected Class<?>[] getInterfacesForBusinessProxy()
   {
      return this.getInterfacesForProxy(this.getProxyAccessType(), SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   /**
    * Obtains interfaces to be used in the EJB21 proxy.  Returns null if none defined
    * 
    * @return
    */
   protected Class<?>[] getInterfacesForEjb21Proxy()
   {
      return this.getInterfacesForProxy(this.getProxyAccessType(), SpecificationInterfaceType.EJB21);
   }
   
   /**
    * Returns an array of interfaces to be used for the proxy;
    * will return null if none are defined.
    * 
    * @param accessType
    * @param specType
    * @return
    */
   private Class<?>[] getInterfacesForProxy(ProxyAccessType accessType, SpecificationInterfaceType specType)
   {

      // Initialize
      Set<Class<?>> interfaces = new HashSet<Class<?>>();
      SessionContainer container = this.getContainer();

      // Initialize array of interfaces
      Set<Class<?>> intfs = new HashSet<Class<?>>();

      // If Local
      if (accessType.equals(ProxyAccessType.LOCAL))
      {

         // If business
         if (specType.equals(SpecificationInterfaceType.EJB30_BUSINESS))
         {
            intfs.addAll(Arrays.asList(ProxyFactoryHelper.getLocalBusinessInterfaces(container)));  
            
            // If binding home with local business 
            if(this.bindHomeAndBusinessTogether())
            {
               Class<?> home = this.getHomeType();
               if (home != null)
               {
                  intfs.add(home);
               }
            }
         }
         // If EJBLocalObject
         else
         {
            // Add local interfaces
            intfs.addAll(Arrays.asList(ProxyFactoryHelper.getLocalInterfaces(container)));
            
            // If no interfaces
            if (intfs.size() == 0)
            {
               return null;
            }
            
            // Add EJBLocalObject
            intfs.add(EJBLocalObject.class);
         }
      }
      // If remote
      else
      {
         // If business
         if (specType.equals(SpecificationInterfaceType.EJB30_BUSINESS))
         {
            intfs.addAll(Arrays.asList(ProxyFactoryHelper.getRemoteBusinessInterfaces(container)));   
            
            // If binding home with remote business
            if(this.bindHomeAndBusinessTogether())
            {
               Class<?> home = this.getHomeType();
               if (home != null)
               {
                  intfs.add(home);
               }
            }
            
         }
         // If EJBObject
         else
         {
            // Add remote interfaces
            intfs.addAll(Arrays.asList(ProxyFactoryHelper.getRemoteInterfaces(container)));
            
            // If no interfaces
            if (intfs.size() == 0)
            {
               return null;
            }
            
            // Add EJBObject
            intfs.add(EJBObject.class);
         }
      }

      // Add all interfaces
      for (Class<?> interfaze : intfs)
      {
         interfaces.add(interfaze);
      }

      // Add JBossProxy
      interfaces.add(JBossProxy.class);

      // Return
      return interfaces.toArray(new Class[]
      {});
   }
   
   /**
    * Defines the access type for this Proxies created by this Factory
    * 
    * @return
    */
   protected abstract ProxyAccessType getProxyAccessType();
   
   protected void setEjb21Objects(BaseSessionRemoteProxy proxy)
   {
      proxy.setHandle(this.createHandle());
      proxy.setHomeHandle(getHomeHandle());
      proxy.setEjbMetaData(getEjbMetaData());
   }
   
   abstract protected Handle createHandle();
   
   protected HomeHandle getHomeHandle()
   {
      EJBContainer ejbContainer = (EJBContainer)container;
      
      HomeHandleImpl homeHandle = null;
      
      RemoteBinding remoteBindingAnnotation = ejbContainer.getAnnotation(RemoteBinding.class);
      if (remoteBindingAnnotation != null)
         homeHandle = new HomeHandleImpl(ProxyFactoryHelper.getHomeJndiName(container));
      
      return homeHandle;
   }
   
   /**
    * Returns the interface type for Home
    * 
    * @return
    */
   protected abstract Class<?> getHomeType();
   
   protected final String getJndiName()
   {
      return this.jndiName;
   }
   
   protected EJBMetaData getEjbMetaData()
   {
      Class<?> remote = null;
      Class<?> home = null;
      Class<?> pkClass = Object.class;
      HomeHandleImpl homeHandle = null;
      
      EJBContainer ejbContainer = (EJBContainer)container;
      
      Class<?>[] remotes = ProxyFactoryHelper.getRemoteInterfaces(this.getContainer());
      if (remotes != null && remotes.length > 0)
      {
         remote = remotes[0];
      }
      RemoteHome homeAnnotation = ejbContainer.getAnnotation(RemoteHome.class);
      if (homeAnnotation != null)
         home = homeAnnotation.value();
      RemoteBinding remoteBindingAnnotation = ejbContainer.getAnnotation(RemoteBinding.class);
      if (remoteBindingAnnotation != null)
         homeHandle = new HomeHandleImpl(remoteBindingAnnotation.jndiBinding());
      
      EJBMetaDataImpl metadata = new EJBMetaDataImpl(remote, home, pkClass, true, false, homeHandle);
      
      return metadata;
   }   
   
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      containerGuid = in.readUTF();
      containerClusterUid = in.readUTF();
      isClustered = in.readBoolean();
      
      if (getContainer() == null)
         throw new EJBException("Invalid (i.e. remote) invocation of local interface (null container) for " + containerGuid);
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(containerGuid);
      out.writeUTF(containerClusterUid);
      out.writeBoolean(isClustered);
   }
   
   /**
    * Ensures that an EJB 2.1 view is complete; the following rules apply:
    * 
    * 1) If EJBHome/EJBLocalHome is defined, at least one EJBObject/EJBLocalObject is defined.  
    * 2) If one EJBObject/EJBLocalObject is defined, an EJBHome/EJBLocalHome is defined.
    * 
    * @param home
    * @param localOrRemoteInterfaces
    * @throws RuntimeException
    */
   protected void validateCompleteEjb21View(Class<?> home, Class<?>[] localOrRemoteInterfaces) throws RuntimeException
   {
      // Ensure specified home is EJBHome or EJBLocalHome
      assert (home == null || (EJBHome.class.isAssignableFrom(home) || EJBLocalHome.class.isAssignableFrom(home)));

      // Ensure all interfaces passed are either EJBObject or EJBLocalObject
      for (Class<?> localOrRemoteInterface : localOrRemoteInterfaces)
      {
         assert (EJBObject.class.isAssignableFrom(localOrRemoteInterface) || EJBLocalObject.class
               .isAssignableFrom(localOrRemoteInterface));
      }

      // If home is defined and there are no local/remote interfaces
      if (home != null && localOrRemoteInterfaces.length == 0)
      {
         throw new RuntimeException("EJBTHREE-1075: " + container.getBeanClassName() + " defines home"
               + " but provides no local/remote interfaces extending " + EJBLocalObject.class.getName() + "/"
               + EJBObject.class.getName() + "; EJB 2.1 view cannot be realized");
      }

      // If local/remote interfaces are defined, but no remote home
      if (home == null && localOrRemoteInterfaces.length != 0)
      {
         throw new RuntimeException("EJBTHREE-1075: " + container.getBeanClassName()
               + " defines local/remote interfaces" + " but provides no home; EJB 2.1 view cannot be realized");
      }
   }
   
   /**
    * Validates that the specified EJB2.1 Home interface returns only
    * valid remote/local interfaces from "create<METHOD>" methods.  If no
    * home is defined, the method will return without further checks
    * 
    * @param home
    */
   protected void validateHomeReturnsNoBusinessInterfaces(Class<?> home)
   {
      // Only perform if home is defined; otherwise no EJB2.1 view
      if(home==null)
      {
         return;
      }
      
      // Sanity checks
      assert EJBHome.class.isAssignableFrom(home) || EJBLocalHome.class.isAssignableFrom(home) : "Specified home interface, "
            + home.getName() + ", must be of type " + EJBHome.class.getName() + " or " + EJBLocalHome.class.getName();
      assert home.isInterface() : "Specified home interface, " + home.getName() + " is not an interface.";

      // Initialize
      Set<Method> creates = new HashSet<Method>();

      // Obtain all "create<METHOD>" methods
      Method[] all = home.getDeclaredMethods();

      // For each method
      for (Method method : all)
      {
         // If a "create<METHOD>" method
         if (method.getName().startsWith(BaseSessionProxyFactory.METHOD_PREFIX_EJB21_CREATE))
         {
            // Add to the Set of Creates
            creates.add(method);
         }
      }

      // For all "create<METHOD>" methods
      for (Method create : creates)
      {
         // Init
         boolean isLocal = true;

         // Set as remote if applicable
         if (EJBHome.class.isAssignableFrom(home))
         {
            isLocal = false;
         }

         // If local (EJBLocalHome)
         if (isLocal)
         {
            // Validate return type is local interface
            if (!EJBLocalObject.class.isAssignableFrom(create.getReturnType()))
            {
               throw new RuntimeException("EJB 3 Core Specification Section 4.6.10: "
                     + "The return type for a create<METHOD> method must be"
                     + " the session bean's local interface type.  " + home.getName() + " has method "
                     + create.getName() + " which returns " + create.getReturnType().getName() + ". [EJBTHREE-1059]");
            }
         }
         // If remote (EJBHome)
         else
         {
            // Validate return type is remote interface
            if (!EJBObject.class.isAssignableFrom(create.getReturnType()))
            {
               throw new RuntimeException("EJB 3 Core Specification Section 4.6.8: "
                     + "The return type for a create<METHOD> method "
                     + "must be the session bean’s remote interface type.  " + home.getName() + " has method "
                     + create.getName() + " which returns " + create.getReturnType().getName() + ". [EJBTHREE-1059]");
            }
         }
      }
   }
   
   /**
    * Validates that any EJB2.1 Views associated with this ProxyFactory 
    * are valid
    * 
    * @param home
    * @param localOrRemoteInterfaces
    * @throws RuntimeException
    */
   protected void validateEjb21Views(Class<?> home,Class<?>[] localOrRemoteInterfaces) throws RuntimeException
   {
      // Ensure EJB2.1 Views are complete (EJBTHREE-1075)
      this.validateCompleteEjb21View(home, localOrRemoteInterfaces);
      
      // Ensure EJB2.1 Home returns only local/remote interfaces
      this.validateHomeReturnsNoBusinessInterfaces(home);
   }
     
   /**
    * Validates that any EJB2.1 Views associated with this ProxyFactory 
    * are valid
    */
   protected abstract void validateEjb21Views();
}
