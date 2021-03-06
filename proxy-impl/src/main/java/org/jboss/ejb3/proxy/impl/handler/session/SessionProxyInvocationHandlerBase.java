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
package org.jboss.ejb3.proxy.impl.handler.session;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.impl.remoting.ProxyRemotingUtils;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.logging.Logger;

/**
 * SessionProxyInvocationHandlerBase
 * 
 * Abstract base from which all JBoss Session Proxy InvocationHandlers
 * may extend
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionProxyInvocationHandlerBase implements SessionProxyInvocationHandler, Serializable
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(SessionProxyInvocationHandlerBase.class);

   /*
    * Method Names
    */
   private static final String METHOD_NAME_TO_STRING = "toString";

   private static final String METHOD_NAME_EQUALS = "equals";

   private static final String METHOD_NAME_HASH_CODE = "hashCode";

   /*
    * Local Methods
    */
   private static final Method METHOD_TO_STRING;

   private static final Method METHOD_EQUALS;

   private static final Method METHOD_HASH_CODE;

   static
   {
      try
      {
         METHOD_TO_STRING = Object.class.getMethod(METHOD_NAME_TO_STRING);
         METHOD_EQUALS = Object.class.getMethod(METHOD_NAME_EQUALS, Object.class);
         METHOD_HASH_CODE = Object.class.getMethod(METHOD_NAME_HASH_CODE);
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
    * The target for this Invocation (for instance, Session ID)
    */
   private Object target;

   /**
    * The interceptors to apply to inovcations upon this handler
    */
   private Interceptor[] interceptors;

   /**
    * The name under which the target container is registered
    */
   private String containerName;

   /**
    * The Globally-unique Container ID
    */
   private String containerGuid;

   /**
    * Fully-qualified name of the class targeted either for injection
    * or casting to support getInvokedBusinessInterface.  May be
    * null to denote non-deterministic invocation
    */
   private String businessInterfaceType;

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor 
    * 
    * @param containerName The name of the target Container
    * @param containerGuid The globally-unique name of the container
    * @param interceptors The interceptors to apply to invocations upon this handler
    * @param businessInterfaceType Possibly null FQN of business interface 
    * @param target The target object (Session ID)
    */
   protected SessionProxyInvocationHandlerBase(final String containerName, final String containerGuid,
         final Interceptor[] interceptors, final String businessInterfaceType, final Object target)
   {
      this.setContainerName(containerName);
      this.setContainerGuid(containerGuid);
      this.setInterceptors(interceptors);
      this.setBusinessInterfaceType(businessInterfaceType);
      this.setTarget(target);
   }

   // ------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
   {
      // Obtain an explicitly-specified actual class
      final String actualClass = this.getBusinessInterfaceType();

      // If we can service this invocation in the handler itself
      if (this.canBeHandledDirectly(method))
      {
         // Handle right here
         return this.handleInvocationDirectly(proxy, args, method);
      }

      /*
       * Obtain the Container
       */
      final InvokableContext container = this.getContainer();

      /*
       * Invoke
       */

      // Adjust args if null to empty array
      Object[] usedArgs = args;
      if (usedArgs == null)
      {
         usedArgs = new Object[]
         {};
      }

      // Set the invoked method
      final SerializableMethod invokedMethod = new SerializableMethod(method, actualClass);

      // Invoke
      final Object result = container.invoke(proxy, invokedMethod, usedArgs);

      // Return
      return result;
   }

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
      if (invokedMethod.equals(METHOD_EQUALS))
      {
         assert args.length == 1 : "Invocation for 'equals' should have exactly one argument, instead was: "
               + invokedMethod;
         Object argument = args[0];
         return this.invokeEquals(proxy, argument);
      }

      // toString
      if (invokedMethod.equals(METHOD_TO_STRING))
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
      if (invokedMethod.equals(METHOD_HASH_CODE))
      {
         return this.invokeHashCode(proxy);
      }

      // If no eligible methods were invoked
      throw new NotEligibleForDirectInvocationException("Current invocation \"" + invokedMethod
            + "\" is not eligible for direct handling by " + this);
   }

   /**
    * Returns whether or not this handler is capable of handling the invoked 
    * method request directly, without passing along to the container
    * @param invokedMethod The invoked Method
    * @return
    */
   protected boolean canBeHandledDirectly(final Method invokedMethod)
   {
      // We handle: equals, hashCode, toString
      return (invokedMethod.equals(METHOD_EQUALS) || invokedMethod.equals(METHOD_TO_STRING) || invokedMethod
            .equals(METHOD_HASH_CODE));
   }

   /**
    * Handles invocation of "equals(Object)" upon a Session Proxy
    * 
    * EJB 3.0 Specification 3.4.5.1, 3.4.5.2
    * 
    * @param proxy
    * @param args
    * @return
    */
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

      // Ensure we've got j.l.r.Proxies
      assert Proxy.isProxyClass(proxy.getClass()) && Proxy.isProxyClass(argument.getClass()) : "invokeEquals handles only "
            + Proxy.class.getName();

      // Get the InvocationHandlers
      InvocationHandler proxyHandler = Proxy.getInvocationHandler(proxy);
      InvocationHandler argumentHandler = Proxy.getInvocationHandler(argument);

      // If argument handler is not a SessionProxyInvocationHandler
      if (!(argumentHandler instanceof SessionProxyInvocationHandler))
      {
         if (log.isTraceEnabled())
         {
            log.trace(argument + " and " + this + " are not equal; handler of argument is not a "
                  + SessionProxyInvocationHandler.class.getName());
         }
         return false;
      }

      // Cast
      SessionProxyInvocationHandler proxySHandler = (SessionProxyInvocationHandler) proxyHandler;
      SessionProxyInvocationHandler argumentSHandler = (SessionProxyInvocationHandler) argumentHandler;

      // Ensure target containers are equal
      String proxyContainerName = proxySHandler.getContainerName();
      assert proxyContainerName != null : "Container Name for " + proxySHandler + " was not set and is required";
      if (!proxyContainerName.equals(argumentSHandler.getContainerName()))
      {
         return false;
      }

      // If target (Session ID) is specified, ensure equal
      Object proxyTarget = proxySHandler.getTarget();
      if (proxyTarget != null)
      {
         Object argumentTarget = argumentSHandler.getTarget();
         if (!proxyTarget.equals(argumentTarget))
         {
            if (log.isTraceEnabled())
            {
               log.trace("Not equal; different Session IDs - proxy== " + proxyTarget + ", argument==" + argumentTarget);
            }
            return false;
         }
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
      assert Proxy.isProxyClass(proxy.getClass()) : "Unexpected proxy implementation";
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof SessionProxyInvocationHandler;
      SessionProxyInvocationHandler sHandler = (SessionProxyInvocationHandler) handler;

      int hash = 0;

      // Generate unique String by value according to rules in "invokeEquals"; 
      // Destination Container
      StringBuffer sb = new StringBuffer();
      sb.append(sHandler.getContainerName());
      sb.append(sHandler.getContainerGuid());
      String compositionString = sb.toString();

      // Make the hash
      hash = compositionString.hashCode();

      // If there's a target in play, take that into account
      Object target = sHandler.getTarget();
      if (target != null)
      {
         hash += target.hashCode();
      }

      // Return the hash
      return hash;
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
    * Creates and returns a Remoting Proxy to invoke upon the container
    * 
    * This implementation is marked as FIXME as remoting should be an add-on
    * capability atop ejb3-proxy
    * 
    * @param url The location of the remote host holding the Container
    * @return
    */
   //FIXME
   protected InvokableContext createRemoteProxyToContainer(String url)
   {
      InvokableContext container = ProxyRemotingUtils.createRemoteProxyToContainer(this.getContainerName(), this
            .getContainerGuid(), url, this.getInterceptors(), this.getTarget());
      return container;
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

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public Object getTarget()
   {
      return target;
   }

   public void setTarget(final Object target)
   {
      this.target = target;
   }

   public String getContainerName()
   {
      return containerName;
   }

   public void setContainerName(final String containerName)
   {
      assert containerName != null && containerName.trim().length() > 0 : "Container Name must be specified";
      this.containerName = containerName;
   }

   public Interceptor[] getInterceptors()
   {
      return interceptors;
   }

   public void setInterceptors(final Interceptor[] interceptors)
   {
      this.interceptors = interceptors;
   }

   public String getContainerGuid()
   {
      return containerGuid;
   }

   public void setContainerGuid(final String containerGuid)
   {
      this.containerGuid = containerGuid;
   }

   public String getBusinessInterfaceType()
   {
      return businessInterfaceType;
   }

   public void setBusinessInterfaceType(String businessInterfaceType)
   {
      this.businessInterfaceType = businessInterfaceType;
   }

}
