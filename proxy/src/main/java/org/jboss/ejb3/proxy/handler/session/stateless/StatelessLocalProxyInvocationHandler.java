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

import java.lang.reflect.Method;

import org.jboss.ejb3.proxy.handler.NotEligibleForDirectInvocationException;
import org.jboss.ejb3.proxy.handler.session.SessionSpecProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;

/**
 * StatelessLocalProxyInvocationHandler
 * 
 * Implementation of a SLSB Local Proxy Invocation Handler 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessLocalProxyInvocationHandler extends SessionSpecProxyInvocationHandlerBase
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(StatelessLocalProxyInvocationHandler.class);

   // ------------------------------------------------------------------------------||
   // Constructors -----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param containerName The name under which the target container is registered
    * @param businessInterfaceType The possibly null businessInterfaceType
    *   marking this invocation hander as specific to a given
    *   EJB3 Business Interface
    */
   public StatelessLocalProxyInvocationHandler(String containerName, String businessInterfaceType)
   {
      super(containerName, businessInterfaceType);
   }

   // ------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------||
   // ------------------------------------------------------------------------------||

   //TODO

   // ------------------------------------------------------------------------------||
   // TO BE IMPLEMENTED ------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // Set the invoked method
      this.setInvokedMethod(new SerializableMethod(method));

      // Attempt to handle directly
      try
      {
         return this.handleInvocationDirectly(proxy, args);
      }
      // Ignore this, we just couldn't handle here
      catch (NotEligibleForDirectInvocationException nefdie)
      {
         log.debug(nefdie.getMessage());
      }

      //TODO
      throw new NotImplementedException("ALR");

   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.handler.ProxyInvocationHandler#getAsynchronousProxy(java.lang.Object)
    */
   public Object getAsynchronousProxy(Object proxy)
   {
      throw new NotImplementedException("ALR");
   }

}
