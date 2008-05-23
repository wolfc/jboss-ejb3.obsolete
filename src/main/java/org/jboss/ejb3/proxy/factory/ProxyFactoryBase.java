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
package org.jboss.ejb3.proxy.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.jboss.logging.Logger;

/**
 * ProxyFactoryBase
 * 
 * A Base upon which Proxy Factory Implementations may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ProxyFactoryBase implements ProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ProxyFactoryBase.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private ClassLoader classloader;

   private String containerName;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param classloader The ClassLoader associated with the EJBContainer
    *       for which this ProxyFactory is to generate Proxies
    * @param containerName The name under which the target container is registered
    */
   public ProxyFactoryBase(final ClassLoader classloader, final String containerName)
   {
      // Set properties
      this.setClassLoader(classloader);
      this.setContainerName(containerName);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create a Proxy Constructor for the specified interfaces, using the specified CL
    * 
    * @param interfaces
    * @param cl
    * @return
    * @throws Exception
    */
   protected Constructor<?> createProxyConstructor(Class<?>[] interfaces, ClassLoader cl)
   {
      Class<?> proxyClass = Proxy.getProxyClass(cl, interfaces);
      Constructor<?> proxyConstructor = null;
      try
      {
         proxyConstructor = proxyClass.getConstructor(InvocationHandler.class);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
      return proxyConstructor;
   }

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Return the name of the interceptor stack to apply to 
    * proxies created by this proxy factory, or null 
    * if no interceptors should be applied
    * 
    * @return
    */
   protected String getInterceptorStackName()
   {
      // Apply no interceptors by default
      return null;
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle callback to be invoked by the ProxyFactoryDeployer
    * before the ProxyFactory is able to service requests
    * 
    *  @throws Exception
    */
   public void start() throws Exception
   {
      // Log
      log.debug("Started: " + this);

      //TODO
   }

   /**
    * Lifecycle callback to be invoked by the ProxyFactoryDeployer
    * before the ProxyFactory is taken out of service, 
    * possibly GC'd
    * 
    * @throws Exception
    */
   public void stop() throws Exception
   {
      // Log
      log.debug("Stopped: " + this);

      //TODO
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ClassLoader getClassLoader()
   {
      return classloader;
   }

   protected void setClassLoader(final ClassLoader classloader)
   {
      this.classloader = classloader;
   }

   public String getContainerName()
   {
      return containerName;
   }

   protected void setContainerName(String containerName)
   {
      this.containerName = containerName;
   }

}
