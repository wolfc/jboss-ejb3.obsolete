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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.NotEligibleForDirectInvocationException;
import org.jboss.ejb3.proxy.handler.session.SessionSpecProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelBus;
import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;

/**
 * StatelessProxyInvocationHandler
 * 
 * Implementation of a SLSB Proxy Invocation Handler 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessProxyInvocationHandler extends SessionSpecProxyInvocationHandlerBase
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(StatelessProxyInvocationHandler.class);

   // ------------------------------------------------------------------------------||
   // Constructors -----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    */
   public StatelessProxyInvocationHandler()
   {
      this(null);
   }

   /**
    * Constructor
    * 
    * @param businessInterfaceType The possibly null businessInterfaceType
    *   marking this invocation hander as specific to a given
    *   EJB3 Business Interface
    */
   public StatelessProxyInvocationHandler(String businessInterfaceType)
   {
      super(businessInterfaceType);
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

      // Obtain the correct container from MC
      //TODO This won't fly for remote, MC would be on another Process
      //TODO This breaks contract, so provide mechanism to invoke over commons Ejb3Registry
      Kernel kernel = (Kernel)Ejb3RegistrarLocator.locateRegistrar().getProvider();
      KernelBus bus = kernel.getBus();

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

      // Invoke
      log.debug("Invoking on MC Bean with name \"" + this.getContainerName() + "\" method \""
            + InvokableContext.METHOD_NAME_INVOKE + "\" with arguments : " + invocationArguments);
      return bus.invoke(this.getContainerName(), InvokableContext.METHOD_NAME_INVOKE, invocationArguments
            .toArray(new Object[]
            {}), InvokableContext.METHOD_SIGNATURE_INVOKE);

   }

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

      // If these are not of the same type
      if (!proxy.getClass().equals(argument.getClass()))
      {
         // Return false
         return false;
      }

      // If the argument is not a proxy
      if (!Proxy.isProxyClass(argument.getClass()))
      {
         return false;
      }

      // Get the InvocationHandlers
      InvocationHandler proxyHandler = this.getInvocationHandler(proxy);
      InvocationHandler argumentHandler = Proxy.getInvocationHandler(argument);

      // If argument handler is not SLSB Handler
      if (!(argumentHandler instanceof StatelessProxyInvocationHandler))
      {
         return false;
      }

      // Cast
      StatelessProxyInvocationHandler proxySHandler = (StatelessProxyInvocationHandler) proxyHandler;
      StatelessProxyInvocationHandler argumentSHandler = (StatelessProxyInvocationHandler) argumentHandler;

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
      StatelessProxyInvocationHandler handler = this.getInvocationHandler(proxy);

      // Generate unique String by value according to rules in "invokeEquals"; 
      // Destination Container and Business Interface
      String unique = handler.getContainerName() + handler.getBusinessInterfaceType();

      // Hash the String
      return unique.hashCode();
   }

   // ------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   protected StatelessProxyInvocationHandler getInvocationHandler(Object proxy)
   {
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof StatelessProxyInvocationHandler : "Expected " + InvocationHandler.class.getSimpleName()
            + " of type " + StatelessProxyInvocationHandler.class.getName() + ", but instead was " + handler;
      return (StatelessProxyInvocationHandler) handler;
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
