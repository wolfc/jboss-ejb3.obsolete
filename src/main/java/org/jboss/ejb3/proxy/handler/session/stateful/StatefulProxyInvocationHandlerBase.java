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
package org.jboss.ejb3.proxy.handler.session.stateful;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.session.SessionSpecProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.intf.StatefulSessionProxy;
import org.jboss.ejb3.proxy.invocation.InvokableContextStatefulRemoteProxyInvocationHack;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.util.NotImplementedException;

/**
 * StatefulProxyInvocationHandlerBase
 * 
 * Implementation of a SFSB Proxy Invocation Handler 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class StatefulProxyInvocationHandlerBase extends SessionSpecProxyInvocationHandlerBase
      implements
         StatefulSessionProxy,
         Serializable
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(StatefulProxyInvocationHandlerBase.class);

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The Session ID of the SFSB Instance to which this ProxyHandler will delegate
    */
   private Serializable sessionId;

   // ------------------------------------------------------------------------------||
   // Constructors -----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param containerName The name of the target container
    * @param containerGuid The globally-unique name of the container
    * @param businessInterfaceType The possibly null businessInterfaceType
    *   marking this invocation hander as specific to a given
    *   EJB3 Business Interface
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   public StatefulProxyInvocationHandlerBase(final String containerName, final String containerGuid,
         final Interceptor[] interceptors, final String businessInterfaceType)
   {
      super(containerName, containerGuid, interceptors, businessInterfaceType);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains the Session ID for this SFSB instance
    * 
    * @return
    */
   public Serializable getSessionId()
   {
      return this.sessionId;
   }

   /**
    * Sets the Session ID for this SFSB instance
    * 
    * @param sessionId
    */
   public void setSessionId(Serializable sessionId)
   {
      this.sessionId = sessionId;
   }

   /**
    * EJB 3.0 Core Specification 3.4.5.1
    * 
    * Determines Equality for SFSB Proxies
    * 
    * @param proxy
    * @param argument
    */
   @Override
   protected boolean invokeEquals(Object proxy, Object argument)
   {
      /*
       * EJB 3.0 Core Specification 3.4.5.1: 
       * 
       * A stateful session object has a unique identity that is assigned by the 
       * container at the time the object is created. A client of the stateful 
       * session bean business interface can determine if two business interface 
       * references refer to the same session object by use of the equals method.
       * 
       * All stateful session bean references to the same business interface for 
       * the same stateful session bean instance will be equal. Stateful session 
       * bean references to different interface types or to different session bean 
       * instances will not have the same identity.
       */

      // If these objects are not of the same type
      if (!argument.getClass().equals(proxy.getClass()))
      {
         // Not equal
         if (log.isTraceEnabled())
         {
            log.trace(argument + " is not equal to " + proxy + " as they are different types");
         }
         return false;
      }

      // Get Invocation Handlers
      InvocationHandler proxyHandler = this.getInvocationHandler(proxy);
      InvocationHandler argumentHandler = Proxy.getInvocationHandler(argument);

      // If argument handler is not SLSB Handler
      if (!(argumentHandler instanceof StatefulProxyInvocationHandlerBase))
      {
         return false;
      }

      // Cast
      StatefulProxyInvocationHandlerBase sHandler = (StatefulProxyInvocationHandlerBase) proxyHandler;
      StatefulProxyInvocationHandlerBase sArgument = (StatefulProxyInvocationHandlerBase) argumentHandler;

      // Ensure target containers are equal
      String proxyContainerName = sHandler.getContainerName();
      assert proxyContainerName != null : "Container Name for " + sHandler + " was not set and is required";
      if (!proxyContainerName.equals(sArgument.getContainerName()))
      {
         return false;
      }

      // Equal if Session IDs are equal
      Object sessionId = sHandler.getSessionId();
      assert sessionId != null : "Required Session ID is not present in " + proxy;
      boolean equal = sessionId.equals(sArgument.getSessionId());

      // Return
      log.debug("SFSB Equality Check for " + sHandler.getSessionId() + " and " + sArgument.getSessionId() + " = "
            + equal);
      return equal;
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
      StatefulProxyInvocationHandlerBase handler = this.getInvocationHandler(proxy);

      // Generate unique String by value according to rules in "invokeEquals"; 
      // Destination Container, Session ID, and Business Interface
      String unique = handler.getContainerName() + handler.getBusinessInterfaceType() + handler.getSessionId();

      // Hash the String
      return unique.hashCode();
   }

   /**
    * Creates and returns a Remoting Proxy to invoke upon the container
    * 
    * @param url The location of the remote host holding the Container
    * @return
    */
   protected InvokableContext createRemoteProxyToContainer(String url)
   {
      // Create an InvokerLocator
      InvokerLocator locator = null;
      try
      {
         locator = new InvokerLocator(url);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException("Could not create " + InvokerLocator.class.getSimpleName() + " to url \"" + url
               + "\"", e);
      }

      /*
       * Define interceptors
       */

      // Get interceptors from the stack
      Interceptor[] interceptors = this.getInterceptors();

      /*
       * Create Proxy
       */

      // Create a POJI Proxy to the Container
      String containerName = this.getContainerName();
      assert containerName != null && containerName.trim().length() > 0 : "Container Name must be set";
      PojiProxy handler = new InvokableContextStatefulRemoteProxyInvocationHack(this.getContainerName(), this
            .getContainerGuid(), locator, interceptors, this.getSessionId());
      Class<?>[] interfaces = new Class<?>[]
      {InvokableContext.class};
      InvokableContext container = (InvokableContext) Proxy.newProxyInstance(InvokableContext.class.getClassLoader(),
            interfaces, handler);

      // Return
      return container;
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   protected StatefulProxyInvocationHandlerBase getInvocationHandler(Object proxy)
   {
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof StatefulProxyInvocationHandlerBase : "Expected "
            + InvocationHandler.class.getSimpleName() + " of type "
            + StatefulProxyInvocationHandlerBase.class.getName() + ", but instead was " + handler;
      return (StatefulProxyInvocationHandlerBase) handler;
   }

   // ------------------------------------------------------------------------------||
   // TO BE IMPLEMENTED ------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.handler.ProxyInvocationHandler#getAsynchronousProxy(java.lang.Object)
    */
   public Object getAsynchronousProxy(Object proxy)
   {
      throw new NotImplementedException("ALR");
   }
}
