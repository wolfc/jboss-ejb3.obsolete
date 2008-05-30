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
package org.jboss.ejb3.proxy.handler.session;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.NotEligibleForDirectInvocationException;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandlerMetadata;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.ejb3.proxy.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;

/**
 * SessionProxyInvocationHandlerBase
 * 
 * Abstract base from which all JBoss Session Proxy InvocationHandlers
 * may extend
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionProxyInvocationHandlerBase extends ProxyInvocationHandlerBase
      implements
         SessionProxyInvocationHandler
{
   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SessionProxyInvocationHandlerBase.class);

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor 
    */
   protected SessionProxyInvocationHandlerBase()
   {
      super();
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // Set the invoked method
      SerializableMethod invokedMethod = new SerializableMethod(method);
      this.setInvokedMethod(invokedMethod);

      // Attempt to handle directly
      try
      {
         return this.handleInvocationDirectly(proxy, args);
      }
      // Ignore this, we just couldn't handle here
      catch (NotEligibleForDirectInvocationException nefdie)
      {
         log.debug("Couldn't handle invocation directly within Proxy " + InvocationHandler.class.getName() + ": "
               + nefdie.getMessage());
      }

      // Obtain container name
      String containerName = StringUtils.adjustWhitespaceStringToNull(this.getContainerName());
      assert containerName != null : "Container name for invocation must be specified";

      // Assemble arguments for invocation
      List<Object> invocationArguments = new ArrayList<Object>();
      // Add proxy as argument
      invocationArguments.add(proxy);
      // Add invoked method as argument
      invocationArguments.add(invokedMethod);
      // Add rest of arguments
      invocationArguments.add(args);

      /*
       * Obtain the Container
       */
      InvokableContext<?> container = null;

      // Attempt to obtain locally
      try
      {
         Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(this.getContainerName());
         assert obj instanceof InvokableContext : "Container retrieved from " + Ejb3Registrar.class.getSimpleName()
               + " was not of expected type " + InvokableContext.class.getName() + " but was instead " + obj;
         container = (InvokableContext<?>) obj;
      }
      // Remote
      catch (NotBoundException nbe)
      {
         // Create a POJI Proxy to the Container
         InvokerLocator locator = ProxyInvocationHandlerMetadata.INVOKER_LOCATOR.get();
         Interceptor[] interceptors =
         {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};
         PojiProxy handler = new PojiProxy(this.getContainerName(), locator, interceptors);
         Class<?>[] interfaces = new Class<?>[]
         {InvokableContext.class};
         container = (InvokableContext<?>) Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
      }

      /*
       * Invoke
       */

      // Invoke
      SerializableMethod methodToInvoke = new SerializableMethod(method);
      log.debug("Invoking: " + methodToInvoke + " with arguments " + args + "...");
      Object result = container.invoke(proxy, methodToInvoke, args);

      // Remove the Invoker Locator from this Thread, we've invoked already
      //TODO Revisit/rethink this pattern; the InvokerLocator is set in the ObjectFactory, 
      // yet removed after invocation in the handler here? 
      ProxyInvocationHandlerMetadata.INVOKER_LOCATOR.set(null);

      // Return
      return result;

   }
}
