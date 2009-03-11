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
package org.jboss.ejb3.proxy.handler.session.service;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;

/**
 * ServiceProxyInvocationHandlerBase
 * 
 * Abstract base from which all JBoss Service Proxy InvocationHandlers
 * may extend.
 * 
 * Implements the notion of equality based solely on the name 
 * of the target container
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ServiceProxyInvocationHandlerBase extends ProxyInvocationHandlerBase
      implements
         SessionProxyInvocationHandler,
         Serializable
{
   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(ServiceProxyInvocationHandlerBase.class);

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor 
    * 
    * @param containerName The name of the target Container
    * @param containerGuid The globally-unique name of the container
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   protected ServiceProxyInvocationHandlerBase(final String containerName, final String containerGuid,
         final Interceptor[] interceptors)
   {
      super(containerName, containerGuid, interceptors);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Required "invoke" as defined by InvocationHandler interface
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // Set the invoked method as a SerializableMethod
      SerializableMethod invokedMethod = new SerializableMethod(method);

      // Use the overloaded implementation
      return this.invoke(proxy, invokedMethod, args);
   }

   /**
    * Handles invocation of "equals(Object)" upon a @Service Proxy
    * 
    * Since this is a Singleton, if and only if the destination 
    * targets are to the same container, we'll consider 
    * the two Proxies equal
    * 
    * @param proxy
    * @param args
    * @return
    */
   protected boolean invokeEquals(Object proxy, Object argument)
   {

      // If the argument is not a proxy
      if (!Proxy.isProxyClass(argument.getClass()))
      {
         return false;
      }

      // Get the InvocationHandlers
      InvocationHandler proxyHandler = this.getInvocationHandler(proxy);
      InvocationHandler argumentHandler = Proxy.getInvocationHandler(argument);

      // If argument handler is not @Service Handler
      if (!(argumentHandler instanceof ServiceProxyInvocationHandlerBase))
      {
         return false;
      }

      // Cast
      ServiceProxyInvocationHandlerBase proxySHandler = (ServiceProxyInvocationHandlerBase) proxyHandler;
      ServiceProxyInvocationHandlerBase argumentSHandler = (ServiceProxyInvocationHandlerBase) argumentHandler;

      // Ensure target containers are equal
      String proxyContainerName = proxySHandler.getContainerName();
      assert proxyContainerName != null : "Container Name for " + proxySHandler + " was not set and is required";
      if (!proxyContainerName.equals(argumentSHandler.getContainerName()))
      {
         return false;
      }

      // Condition passed, so true
      return true;

   }

   /**
    * Handles invocation of "hashCode()" upon the proxy
    * 
    * @param proxy
    * @return
    */
   protected int invokeHashCode(Object proxy)
   {
      // Get the InvocationHandler
      ServiceProxyInvocationHandlerBase handler = this.getInvocationHandler(proxy);

      // Generate unique String by value according to rules in "invokeEquals"; 
      // Destination Container and Business Interface
      String unique = handler.getContainerName();

      // Hash the String
      return unique.hashCode();
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   protected ServiceProxyInvocationHandlerBase getInvocationHandler(Object proxy)
   {
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof ServiceProxyInvocationHandlerBase : "Expected "
            + InvocationHandler.class.getSimpleName() + " of type " + ServiceProxyInvocationHandlerBase.class.getName()
            + ", but instead was " + handler;
      return (ServiceProxyInvocationHandlerBase) handler;
   }
}
