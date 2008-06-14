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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactoryBase;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.intf.StatefulSessionProxy;
import org.jboss.ejb3.proxy.invocation.StatefulSessionContainerMethodInvocation;
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
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The Container used by this SFSB Proxy Factory
    */
   private transient StatefulSessionInvokableContext<?> container;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param name The unique name for this ProxyFactory
    * @param containerName The name of the InvokableContext (container)
    *   upon which Proxies will invoke
    * @param metadata The metadata representing this SFSB
    * @param classloader The ClassLoader associated with the StatelessContainer
    *       for which this ProxyFactory is to generate Proxies
    */
   public StatefulSessionProxyFactoryBase(final String name, final String containerName,
         final JBossSessionBeanMetaData metadata, final ClassLoader classloader)
   {
      // Call Super
      super(name, containerName, metadata, classloader);
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
      return this.getReturnTypesFromCreateMethods(homeInterface, true);
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
    * Create an EJB2.x Home Proxy
    * 
    * @return
    */
   @Override
   public Object createProxyHome()
   {
      Object sessionId = this.getNewSessionId();
      return this.createProxyHome(sessionId);
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
   @Override
   public Object createProxyDefault()
   {
      Object sessionId = this.getNewSessionId();
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
      Object sessionId = this.getNewSessionId();
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
      Object sessionId = this.getNewSessionId();
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
   public Object createProxyHome(Object sessionId)
   {
      // Obtain Proxy using Super Implementation
      Object proxy = super.createProxyHome();

      // Associate with session
      this.associateProxyWithSession(proxy, sessionId);

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
   public Object createProxyDefault(Object sessionId)
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
   public Object createProxyBusiness(Object sessionId, String businessInterfaceName)
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
   public Object createProxyEjb2x(Object sessionId)
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
   protected void associateProxyWithSession(Object proxy, Object sessionId)
   {
      // Obtain the InvocationHandler
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof StatefulProxyInvocationHandlerBase : "SFSB Proxy must be of type "
            + StatefulProxyInvocationHandlerBase.class.getName();
      StatefulProxyInvocationHandlerBase sHandler = (StatefulProxyInvocationHandlerBase) handler;

      // Set the Session ID on the Proxy
      sHandler.setSessionId(sessionId);
   }

   /**
    * Creates a new Session on the container and returns the ID
    * representing this newly-created session
    * 
    * @return The ID of the new session
    */
   protected Object getNewSessionId()
   {
      // Obtain the Container
      StatefulSessionInvokableContext<?> container = this.getContainer();

      // Get a new Session ID from the Container
      Object sessionId = null;
      try
      {
         sessionId = container.createSession();
      }
      catch (NotBoundException e)
      {
         throw new RuntimeException("Could not obtain a new Session ID from SFSB Container \"" + container + "\"", e);
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
   protected abstract StatefulSessionInvokableContext<?> obtainContainer();

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public StatefulSessionInvokableContext<?> getContainer()
   {
      if (this.container == null)
      {
         this.setContainer(this.obtainContainer());
      }

      return this.container;
   }

   public void setContainer(
         StatefulSessionInvokableContext<? extends StatefulSessionContainerMethodInvocation> container)
   {
      this.container = container;
   }

}
