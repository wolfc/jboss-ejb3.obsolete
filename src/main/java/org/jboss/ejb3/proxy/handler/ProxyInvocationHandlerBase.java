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
package org.jboss.ejb3.proxy.handler;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.intf.SessionProxy;
import org.jboss.logging.Logger;

/**
 * ProxyInvocationHandlerBase
 * 
 * Abstract base from which all Proxy InvocationHandlers
 * may extend
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ProxyInvocationHandlerBase implements ProxyInvocationHandler, Serializable
{
   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The interceptors to apply to inovcations upon this handler
    */
   private Interceptor[] interceptors;

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(ProxyInvocationHandlerBase.class);

   /*
    * Method Names
    */
   private static final String METHOD_NAME_TO_STRING = "toString";

   private static final String METHOD_NAME_EQUALS = "equals";

   private static final String METHOD_NAME_HASH_CODE = "hashCode";

   /*
    * Local Methods
    */
   private static final SerializableMethod METHOD_TO_STRING;

   private static final SerializableMethod METHOD_EQUALS;

   private static final SerializableMethod METHOD_HASH_CODE;

   static
   {
      try
      {
         METHOD_TO_STRING = new SerializableMethod(Object.class
               .getDeclaredMethod(ProxyInvocationHandlerBase.METHOD_NAME_TO_STRING), Object.class);
         METHOD_EQUALS = new SerializableMethod(Object.class.getDeclaredMethod(
               ProxyInvocationHandlerBase.METHOD_NAME_EQUALS, Object.class), Object.class);
         METHOD_HASH_CODE = new SerializableMethod(Object.class
               .getDeclaredMethod(ProxyInvocationHandlerBase.METHOD_NAME_HASH_CODE), Object.class);
      }
      catch (NoSuchMethodException nsme)
      {
         throw new RuntimeException(
               "Methods for handling directly by the InvocationHandler were not initialized correctly", nsme);
      }

   }

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The name under which the target container is registered
    */
   private String containerName;

   /**
    * The Globally-unique Container ID
    */
   private String containerGuid;

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param containerName The name of the target container
    * @param containerGuid The globally-unique name of the container
    * @param interceptors The interceptors to apply to invocations upon this handler
    */
   protected ProxyInvocationHandlerBase(final String containerName, final String containerGuid,
         final Interceptor[] interceptors)
   {
      this.setContainerName(containerName);
      this.setContainerGuid(containerGuid);
      this.setInterceptors(interceptors);
   }

   // ------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Handles the current invocation directly in this invocation handler.  Only 
    * a subset of method invocations are eligible for this treatment, else 
    * a NotEligibleForDirectInvocationException will be thrown
    * 
    * @param proxy
    * @param args Arguments of the current invocation
    * @param invokedMethod The method invoked
    * @return
    * @throws NotEligibleForDirectInvocationException
    */
   protected Object handleInvocationDirectly(Object proxy, Object[] args, Method invokedMethod)
         throws NotEligibleForDirectInvocationException
   {
      // Obtain the invoked method
      assert invokedMethod != null : "Invoked Method was not set upon invocation of " + this.getClass().getName();

      // equals
      if (invokedMethod.equals(ProxyInvocationHandlerBase.METHOD_EQUALS.toMethod()))
      {
         assert args.length == 1 : "Invocation for 'equals' should have exactly one argument, instead was: "
               + invokedMethod;
         Object argument = args[0];
         return this.invokeEquals(proxy, argument);
      }
      // toString
      if (invokedMethod.equals(ProxyInvocationHandlerBase.METHOD_TO_STRING.toMethod()))
      {
         // Perform assertions
         assert Proxy.isProxyClass(proxy.getClass()) : "Specified proxy invoked is not of type "
               + Proxy.class.getName();

         // Obtain implemented interfaces
         Class<?>[] implementedInterfaces = proxy.getClass().getInterfaces();
         Set<Class<?>> interfacesSet = new HashSet<Class<?>>();
         for (Class<?> interfaze : implementedInterfaces)
         {
            interfacesSet.add(interfaze);
         }

         // Construct a return value
         StringBuffer sb = new StringBuffer();
         sb.append("Proxy to ");
         sb.append(this.getContainerName());
         sb.append(" implementing ");
         sb.append(interfacesSet);

         // Return
         return sb.toString();
      }
      // hashCode
      if (invokedMethod.equals(ProxyInvocationHandlerBase.METHOD_HASH_CODE.toMethod()))
      {
         return this.invokeHashCode(proxy);
      }

      // If no eligible methods were invoked
      throw new NotEligibleForDirectInvocationException("Current invocation \"" + invokedMethod
            + "\" is not eligible for direct handling by " + this);
   }

   /**
    * Returns the container housed locally
    * 
    * @return
    */
   protected InvokableContext getContainerLocally()
   {
      // Lookup
      Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(this.getContainerName());

      // Ensure of correct type
      assert obj instanceof InvokableContext : "Container retrieved from " + Ejb3Registrar.class.getSimpleName()
            + " was not of expected type " + InvokableContext.class.getName() + " but was instead " + obj;

      // Return
      return (InvokableContext) obj;
   }

   /**
    * Overloaded "invoke" which takes into account a {@link SerializableMethod} 
    * view
    * 
    * @param proxy
    * @param method
    * @param args
    * @return
    * @throws Throwable
    */
   public Object invoke(SessionProxy proxy, SerializableMethod method, Object[] args) throws Throwable
   {
      // Attempt to handle directly
      try
      {
         return this.handleInvocationDirectly(proxy, args, method.toMethod());
      }
      // Ignore this, we just couldn't handle here
      catch (NotEligibleForDirectInvocationException nefdie)
      {
         log.debug("Couldn't handle invocation directly within " + this + ": " + nefdie.getMessage());
      }

      /*
       * Obtain the Container
       */
      InvokableContext container = this.getContainer();

      /*
       * Invoke
       */

      // Adjust args if null to empty array
      if (args == null)
      {
         args = new Object[]
         {};
      }

      // Invoke
      Object result = container.invoke(proxy, method, args);

      // Return
      return result;
   }

   // ------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Obtains the Container upon which this Proxy should invoke
    * 
    * @return
    */
   protected abstract InvokableContext getContainer();

   /**
    * Handles invocation of "equals(Object)" upon the Proxy 
    * 
    * @param proxy
    * @param args
    * @return
    */
   protected abstract boolean invokeEquals(Object proxy, Object argument);

   /**
    * Handles invocation of "hashCode()" upon the proxy
    * 
    * @param proxy
    * @return
    */
   protected abstract int invokeHashCode(Object proxy);

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   protected String getContainerName()
   {
      return containerName;
   }

   protected void setContainerName(final String containerName)
   {
      assert containerName != null && containerName.trim().length() > 0 : "Container Name must be specified";
      this.containerName = containerName;
   }

   protected Interceptor[] getInterceptors()
   {
      return interceptors;
   }

   private void setInterceptors(final Interceptor[] interceptors)
   {
      this.interceptors = interceptors;
   }

   protected String getContainerGuid()
   {
      return containerGuid;
   }

   private void setContainerGuid(final String containerGuid)
   {
      this.containerGuid = containerGuid;
   }
}
