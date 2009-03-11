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
package org.jboss.ejb3.proxy.handler.session.stateless;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.handler.session.SessionSpecProxyInvocationHandlerBase;
import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;

/**
 * StatelessProxyInvocationHandlerBase
 * 
 * Implementation of a SLSB Proxy Invocation Handler 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class StatelessProxyInvocationHandlerBase extends SessionSpecProxyInvocationHandlerBase
      implements
         SessionProxyInvocationHandler,
         Serializable
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(StatelessProxyInvocationHandlerBase.class);

   // ------------------------------------------------------------------------------||
   // Constructors -----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param containerName
    * @param containerGuid The globally-unique name of the container
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   public StatelessProxyInvocationHandlerBase(final String containerName, final String containerGuid,
         final Interceptor[] interceptors)
   {
      this(containerName, containerGuid, interceptors, null);
   }

   /**
    * Constructor
    * 
    * @param containerName The name of the target Container
    * @param containerGuid The globally-unique name of the container
    * @param businessInterfaceType The possibly null businessInterfaceType
    *   marking this invocation hander as specific to a given
    *   EJB3 Business Interface
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   public StatelessProxyInvocationHandlerBase(final String containerName, final String containerGuid,
         final Interceptor[] interceptors, final String businessInterfaceType)
   {
      super(containerName, containerGuid, interceptors, businessInterfaceType);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Handles invocation of "equals(Object)" upon a SLSB Proxy
    * 
    * EJB 3.0 Specification 3.4.5.2
    * 
    * @param proxy
    * @param args
    * @return
    */
   protected boolean invokeEquals(Object proxy, Object argument)
   {
      /*
       * EJB 3.0 Specification 3.4.5.2:
       * 
       * All business object references of the same interface type for the same 
       * stateless session bean have the same object identity, which is 
       * assigned by the container.
       *
       * The equals method always returns true when used to compare references 
       * to the same business interface type of the same session bean.
       * 
       * Session bean references to either different business interface types
       * or different session beans will not be equal."
       */

      // If the argument is not a proxy
      if (!Proxy.isProxyClass(argument.getClass()))
      {
         return false;
      }

      // Get the InvocationHandlers
      InvocationHandler proxyHandler = this.getInvocationHandler(proxy);
      InvocationHandler argumentHandler = Proxy.getInvocationHandler(argument);

      // If argument handler is not SLSB Handler
      if (!(argumentHandler instanceof StatelessProxyInvocationHandlerBase))
      {
         return false;
      }

      // Cast
      StatelessProxyInvocationHandlerBase proxySHandler = (StatelessProxyInvocationHandlerBase) proxyHandler;
      StatelessProxyInvocationHandlerBase argumentSHandler = (StatelessProxyInvocationHandlerBase) argumentHandler;

      // Ensure target containers are equal
      String proxyContainerName = proxySHandler.getContainerName();
      assert proxyContainerName != null : "Container Name for " + proxySHandler + " was not set and is required";
      if (!proxyContainerName.equals(argumentSHandler.getContainerName()))
      {
         return false;
      }

      // Obtain target business interfaces
      String proxyBusinessInterface = proxySHandler.getBusinessInterfaceType();
      String argumentBusinessInterface = argumentSHandler.getBusinessInterfaceType();

      // If no business interface is specified for the proxy, but is for the argument
      if (proxyBusinessInterface == null && argumentBusinessInterface != null)
      {
         return false;
      }

      // If the business interface of the proxy does not match that of the argument
      if (proxyBusinessInterface != null && !proxyBusinessInterface.equals(argumentBusinessInterface))
      {
         return false;
      }

      // All conditions passed, so true
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
      StatelessProxyInvocationHandlerBase handler = this.getInvocationHandler(proxy);

      // Generate unique String by value according to rules in "invokeEquals"; 
      // Destination Container and Business Interface
      String unique = handler.getContainerName() + handler.getBusinessInterfaceType();

      // Hash the String
      return unique.hashCode();
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   protected StatelessProxyInvocationHandlerBase getInvocationHandler(Object proxy)
   {
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof StatelessProxyInvocationHandlerBase : "Expected "
            + InvocationHandler.class.getSimpleName() + " of type "
            + StatelessProxyInvocationHandlerBase.class.getName() + ", but instead was " + handler;
      return (StatelessProxyInvocationHandlerBase) handler;
   }
}
