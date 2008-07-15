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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.NotEligibleForDirectInvocationException;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandlerBase;
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
         log.debug("Couldn't handle invocation directly within " + this + ": "
               + nefdie.getMessage());
      }
      
      /*
       * Obtain the Container
       */
      InvokableContext<?> container = this.getContainer();

      /*
       * Invoke
       */

      // Invoke
      log.debug("Invoking: " + invokedMethod + " with arguments " + args + "...");
      Object result = container.invoke(proxy, invokedMethod, args);

      // Return
      return result;

   }

   /**
    * Returns the container housed locally
    * 
    * @return
    */
   protected InvokableContext<? extends ContainerMethodInvocation> getContainerLocally()
   {
      // Lookup
      Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(this.getContainerName());

      // Ensure of correct type
      assert obj instanceof InvokableContext : "Container retrieved from " + Ejb3Registrar.class.getSimpleName()
            + " was not of expected type " + InvokableContext.class.getName() + " but was instead " + obj;

      // Return
      return (InvokableContext<?>) obj;
   }

   /**
    * Creates and returns a Remoting Proxy to invoke upon the container
    * 
    * @param url The location of the remote host holding the Container
    * @return
    */
   protected InvokableContext<? extends ContainerMethodInvocation> createRemoteProxyToContainer(String url)
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

      // Create a POJI Proxy to the Container
      Interceptor[] interceptors =
      {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};
      PojiProxy handler = new PojiProxy(this.getContainerName(), locator, interceptors);
      Class<?>[] interfaces = new Class<?>[]
      {InvokableContext.class};
      InvokableContext<? extends ContainerMethodInvocation> container = (InvokableContext<?>) Proxy.newProxyInstance(
            interfaces[0].getClassLoader(), interfaces, handler);

      // Return
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
   protected abstract InvokableContext<? extends ContainerMethodInvocation> getContainer();
}
