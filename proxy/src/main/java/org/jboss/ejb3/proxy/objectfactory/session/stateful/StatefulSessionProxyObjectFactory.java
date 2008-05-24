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
package org.jboss.ejb3.proxy.objectfactory.session.stateful;

import java.util.List;
import java.util.Map;

import javax.naming.Name;
import javax.naming.RefAddr;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.hack.Hack;
import org.jboss.ejb3.proxy.intf.StatefulSessionProxy;
import org.jboss.ejb3.proxy.objectfactory.session.SessionProxyObjectFactory;
import org.jboss.kernel.Kernel;

/**
 * StatefulSessionProxyObjectFactory
 * 
 * A JNDI ObjectFactory responsible for returning the
 * appropriate SFSB Proxy upon lookup
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulSessionProxyObjectFactory extends SessionProxyObjectFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members  -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * SFSB Object Factories must always create a new SFSB Proxy with every lookup, 
    * set a new Session ID as obtained by the SFSB Container, and return.
    * 
    * @param proxyFactory The ProxyFactory to use
    * @param name The JNDI name looked up
    * @param referenceAddresses
    */
   @Override
   protected Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses)
   {
      // Get the Proxy from the Super Implementation
      Object proxy = this.createProxy(proxyFactory, name, referenceAddresses);

      // Get the Container Name
      String containerName = this.getContainerName(name, referenceAddresses);

      // Get the Container
      Kernel kernel = Hack.BOOTSTRAP.getKernel();
      ControllerContext context = kernel.getController().getInstalledContext(containerName);
      assert context != null && context.getTarget() != null : "EJB Container could not be found at " + containerName
            + "; perhaps it has not been properly registered or the " + RefAddr.class.getSimpleName()
            + " is incorrect?";
      Object obj = context.getTarget();
      assert obj instanceof StatefulSessionInvokableContext : "Object found registered under name " + containerName
            + " must be of type " + StatefulSessionInvokableContext.class.getName() + " but was instead " + obj;
      StatefulSessionInvokableContext<?> container = (StatefulSessionInvokableContext<?>) obj;

      // Create a Session ID from the Container
      Object sessionId = container.createSession();

      // Ensure Proxy is of expected type
      assert proxy instanceof StatefulSessionProxy : "Proxy " + proxy + " must be of type "
            + StatefulSessionProxy.class.getName();

      // Cast
      StatefulSessionProxy sProxy = (StatefulSessionProxy) proxy;

      // Set the Session ID
      sProxy.setSessionId(sessionId);

      // Return
      return proxy;
   }
}
