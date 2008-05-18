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

import org.jboss.ejb3.common.string.StringUtils;
import org.jboss.ejb3.proxy.hack.Hack;
import org.jboss.ejb3.proxy.handler.session.SessionSpecProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.kernel.spi.registry.KernelBus;
import org.jboss.util.NotImplementedException;

/**
 * StatelessRemoteProxyInvocationHandler
 * 
 * Implementation of a SLSB Remote Proxy Invocation Handler 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatelessRemoteProxyInvocationHandler extends SessionSpecProxyInvocationHandlerBase
{

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
   public StatelessRemoteProxyInvocationHandler(String containerName, String businessInterfaceType)
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

      // Obtain the correct container from MC
      //TODO This won't fly for remote, MC would be on another Process
      KernelBus bus = Hack.BOOTSTRAP.getKernel().getBus();

      // Obtain container name
      String containerName = StringUtils.adjustWhitespaceStringToNull(this.getContainerName());
      assert containerName != null : "Container name for invocation must be specified";

      // Inoke
      return bus.invoke(this.getContainerName(), this.getInvokedMethod().getName(), args, new String[]
      {});
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.proxy.handler.ProxyInvocationHandler#getAsynchronousProxy(java.lang.Object)
    */
   public Object getAsynchronousProxy(Object proxy)
   {
      throw new NotImplementedException("ALR");
   }

}
