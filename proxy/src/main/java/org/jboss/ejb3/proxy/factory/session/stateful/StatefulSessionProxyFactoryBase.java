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
package org.jboss.ejb3.proxy.factory.session.stateful;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.container.StatefulSessionFactory;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactoryBase;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.intf.StatefulSessionProxy;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatefulSessionProxyFactoryBase
 * 
 * Base upon which SFSB Proxy Factories may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class StatefulSessionProxyFactoryBase extends SessionProxyFactoryBase
      implements
         StatefulSessionProxyFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(StatefulSessionProxyFactoryBase.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The Container used by this SFSB Proxy Factory
    */
   private transient StatefulSessionFactory container;

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
    * @param metadata The metadata representing this SFSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    * @param advisor The Advisor for proxies created by this factory
    */
   public StatefulSessionProxyFactoryBase(final String name, final String containerName, final String containerGuid,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader, final Advisor advisor)
   {
      // Call Super
      super(name, containerName, containerGuid, metadata, classloader, advisor);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the return types declared by the "create" methods for the specified home interface.
    *  
    * @param homeInterface
    * @return
    * @deprecated http://jira.jboss.com/jira/browse/JBMETA-41
    */
   @Deprecated
   @Override
   protected Set<Class<?>> getReturnTypesFromCreateMethods(Class<?> homeInterface)
   {
      return this.getReturnTypesFromCreateMethods(homeInterface, false);
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
      interfaces.add(StatefulSessionProxy.class);

      // Return
      return interfaces;
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

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
   @Override
   public Object createProxyDefault()
   {
      Serializable sessionId = this.getNewSessionId();
      return this.createProxyDefault(sessionId);
   }

   /**
    * Create an EJB3 Business Proxy specific to the specified
    * target business interface name (expressed as 
    * a fully-qualified class name)
    * 
    * @param businessInterfaceName
    * @return
    */
   @Override
   public Object createProxyBusiness(final String businessInterfaceName)
   {
      Serializable sessionId = this.getNewSessionId();
      return this.createProxyBusiness(sessionId, businessInterfaceName);
   }

   /**
    * Create an EJB2.x Proxy 
    * 
    * @return
    */
   @Override
   public Object createProxyEjb2x()
   {
      Serializable sessionId = this.getNewSessionId();
      return this.createProxyEjb2x(sessionId);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create an EJB2.x Home Proxy
    * 
    * @param sessionId
    * @return
    */
   public Object createProxyHome()
   {
      // Obtain Proxy using Super Implementation
      Object proxy = super.createProxyHome();

      // Return
      return proxy;
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
    * @param sessionId
    * @return
    */
   public Object createProxyDefault(Serializable sessionId)
   {
      // Obtain Proxy using Super Implementation
      Object proxy = super.createProxyDefault();

      // Associate with session
      this.associateProxyWithSession(proxy, sessionId);

      // Return
      return proxy;
   }

   /**
    * Create an EJB3 Business Proxy specific to the specified
    * target business interface name (expressed as 
    * a fully-qualified class name)
    * 
    * @param sessionId
    * @param businessInterfaceName
    * @return
    */
   public Object createProxyBusiness(Serializable sessionId, String businessInterfaceName)
   {
      // Obtain Proxy using Super Implementation
      Object proxy = super.createProxyBusiness(businessInterfaceName);

      // Associate with session
      this.associateProxyWithSession(proxy, sessionId);

      // Return
      return proxy;
   }

   /**
    * Create an EJB2.x Proxy 
    * 
    * @param sessionId
    * @return
    */
   public Object createProxyEjb2x(Serializable sessionId)
   {
      // Obtain Proxy using Super Implementation
      Object proxy = super.createProxyEjb2x();

      // Associate with session
      this.associateProxyWithSession(proxy, sessionId);

      // Return
      return proxy;
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Associates the specified Proxy with the session denoted by 
    * the specified sessionId
    * 
    * @param proxy
    * @param sessionId
    */
   protected void associateProxyWithSession(Object proxy, Serializable sessionId)
   {
      // Obtain the InvocationHandler
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof StatefulProxyInvocationHandlerBase : "SFSB Proxy must be of type "
            + StatefulProxyInvocationHandlerBase.class.getName();
      StatefulProxyInvocationHandlerBase sHandler = (StatefulProxyInvocationHandlerBase) handler;

      // Set the Session ID on the Proxy
      sHandler.setTarget(sessionId);
   }

   /**
    * Creates a new Session on the container and returns the ID
    * representing this newly-created session
    * 
    * @return The ID of the new session
    */
   protected Serializable getNewSessionId()
   {
      // Obtain the Container
      StatefulSessionFactory container = this.getContainer();

      // Get a new Session ID from the Container
      Serializable sessionId = null;
      try
      {
         sessionId = container.createSession();
      }
      catch (NotBoundException e)
      {
         throw new RuntimeException("Could not obtain a new Session ID from SFSB Container \"" + container + "\"", e);
      }
      catch (RuntimeException re)
      {
         log.error("Could not obtain new Session ID from SFSB Container", re);
         throw re;
      }

      // Return the new ID
      return sessionId;
   }

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Container used by this Proxy Factory
    * 
    * @return The Container for this Proxy Factory
    */
   protected StatefulSessionFactory obtainContainer()
   {
      /*
       * Obtain the Container
       */
      StatefulSessionFactory container = null;
      String containerName = this.getContainerName();

      // Lookup from EJB3 Registrar
      try
      {
         Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(containerName);
         assert obj instanceof StatefulSessionFactory : "Container retrieved from "
               + Ejb3Registrar.class.getSimpleName() + " was not of expected type "
               + StatefulSessionFactory.class.getName() + " but was instead " + obj;
         container = (StatefulSessionFactory) obj;
      }
      catch (NotBoundException nbe)
      {
         throw new RuntimeException(StatefulSessionProxyFactory.class.getSimpleName() + " " + this
               + " has defined container name \"" + containerName + "\", but this could not be found in the "
               + Ejb3Registrar.class.getSimpleName());
      }

      // Return
      return container;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public StatefulSessionFactory getContainer()
   {
      if (this.container == null)
      {
         this.setContainer(this.obtainContainer());
      }

      return this.container;
   }

   public void setContainer(StatefulSessionFactory container)
   {
      this.container = container;
   }

}
