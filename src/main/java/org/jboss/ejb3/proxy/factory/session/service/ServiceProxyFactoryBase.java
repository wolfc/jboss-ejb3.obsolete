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
package org.jboss.ejb3.proxy.factory.session.service;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.proxy.factory.ProxyFactoryBase;
import org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.intf.ServiceProxy;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;

/**
 * ServiceProxyFactoryBase
 * 
 * Base upon which @Service Proxy Factory implementations
 * may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ServiceProxyFactoryBase extends ProxyFactoryBase implements ServiceProxyFactory
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ServiceProxyFactoryBase.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private JBossServiceBeanMetaData metadata;

   /**
    * Proxy Constructor (All
    * business interfaces)
    */
   private Constructor<?> proxyConstructor;

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
    * @param metadata The metadata representing this @Service Bean
    * @param classloader The ClassLoader associated with the Container's Bean Class
    *       for which this ProxyFactory is to generate Proxies
    * @param advisor The Advisor for proxies created by this factory
    */
   public ServiceProxyFactoryBase(final String name, final String containerName, final String containerGuid,
         final JBossServiceBeanMetaData metadata, final ClassLoader classloader, final Advisor advisor)
   {
      // Call Super
      super(name, containerName, containerGuid, classloader, advisor);

      // Set Metadata
      this.setMetadata(metadata);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create an EJB3 Business proxy.  The 
    * returned proxy will implement all appropriate
    * business interfaces.
    * 
    * @return
    */
   public Object createProxyDefault()
   {
      // Obtain Constructor to Default Proxy
      Constructor<?> constructor = this.getProxyConstructor();
      assert constructor != null : "Constructor for Default Proxy was null; perhaps the "
            + ServiceProxyFactory.class.getSimpleName() + " was not properly started?";

      // Create a new InvocationHandler
      SessionProxyInvocationHandler handler = this.createInvocationHandler();

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
       * Make Default Proxy
       */

      // Create a Set to hold all relevant interfaces
      Set<Class<?>> defaultProxyInterfaces = new HashSet<Class<?>>();

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
               throw new RuntimeException("Could not find specified @Service Bean Business Interface \""
                     + businessInterfaceType + "\" in " + ClassLoader.class.getSimpleName() + " for EJB "
                     + this.getMetadata().getEjbName(), cnfe);
            }

            // Add business interface to classes
            businessInterfaceClasses.add(businessInterface);
         }
      }

      // Add all business interfaces
      defaultProxyInterfaces.addAll(businessInterfaceClasses);

      // Make the Default Business Interfaces Proxy Constructor
      Constructor<?> businessInterfacesConstructor = this.createProxyConstructor(defaultProxyInterfaces, this
            .getClassLoader());
      log.debug("Created @Service Bean Default EJB3 Business Proxy Constructor implementing " + defaultProxyInterfaces);

      // Set
      this.setProxyConstructor(businessInterfacesConstructor);

   }

   /**
    * Lifecycle callback to be invoked
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
      interfaces.add(ServiceProxy.class);

      // Return
      return interfaces;
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

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
    * Returns the ServiceProxyInvocationHandler to be used in 
    * Proxy Creation
    * 
    * @return
    */
   protected abstract SessionProxyInvocationHandler createInvocationHandler();

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public JBossServiceBeanMetaData getMetadata()
   {
      return metadata;
   }

   protected void setMetadata(final JBossServiceBeanMetaData metadata)
   {
      this.metadata = metadata;
   }

   protected Constructor<?> getProxyConstructor()
   {
      return proxyConstructor;
   }

   protected void setProxyConstructor(Constructor<?> proxyConstructor)
   {
      this.proxyConstructor = proxyConstructor;
   }
}
