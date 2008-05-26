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
import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.NotEligibleForDirectInvocationException;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
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

      // Invoke
      //TODO This won't fly for remote, Object Store would be on another Process
      log.debug("Invoking on Bean with name \"" + this.getContainerName() + "\" method \""
            + InvokableContext.METHOD_NAME_INVOKE + "\" with arguments : " + invocationArguments);
      return Ejb3RegistrarLocator.locateRegistrar().invoke(this.getContainerName(),
            InvokableContext.METHOD_NAME_INVOKE, invocationArguments.toArray(new Object[]
            {}), InvokableContext.METHOD_SIGNATURE_INVOKE);

   }
}
