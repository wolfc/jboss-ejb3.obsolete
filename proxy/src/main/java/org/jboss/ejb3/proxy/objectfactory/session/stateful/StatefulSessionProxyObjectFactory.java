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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import javax.naming.Name;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.objectfactory.ProxyFactoryReferenceAddressTypes;
import org.jboss.ejb3.proxy.objectfactory.session.SessionProxyObjectFactory;
import org.jboss.ejb3.proxy.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.remoting.InvokerLocator;

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
    * SFSB Object Factories must always create a new SFSB Proxy
    * 
    * @param proxyFactory The ProxyFactory to use
    * @param name The JNDI name looked up
    * @param referenceAddresses
    */
   @Override
   protected Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses)
   {
      // Create a new Proxy Instance
      Object proxy = this.createProxy(proxyFactory, name, referenceAddresses);

      // Obtain the InvocationHandler
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof StatefulProxyInvocationHandlerBase : "SFSB Proxy must be of type "
            + StatefulProxyInvocationHandlerBase.class.getName();
      StatefulProxyInvocationHandlerBase sHandler = (StatefulProxyInvocationHandlerBase) handler;

      /*
       * Obtain the Container
       */
      StatefulSessionInvokableContext<?> container = null;
      String containerName = this.getSingleRequiredReferenceAddressValue(name, referenceAddresses,
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_EJBCONTAINER_NAME);

      // Attempt to obtain locally
      try
      {
         Object obj = Ejb3RegistrarLocator.locateRegistrar().lookup(containerName);
         assert obj instanceof StatefulSessionInvokableContext : "Container retrieved from "
               + Ejb3Registrar.class.getSimpleName() + " was not of expected type "
               + StatefulSessionInvokableContext.class.getName() + " but was instead " + obj;
         container = (StatefulSessionInvokableContext<?>) obj;
      }
      // Remote
      catch (NotBoundException nbe)
      {
         // Create a POJI Proxy to the Container
         String url = this.getSingleRequiredReferenceAddressValue(name, referenceAddresses,
               ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_INVOKER_LOCATOR_URL);
         InvokerLocator locator = null;
         try
         {
            locator = new InvokerLocator(url);
         }
         catch (MalformedURLException e)
         {
            throw new RuntimeException();
         }
         Interceptor[] interceptors =
         {IsLocalProxyFactoryInterceptor.singleton, InvokeRemoteInterceptor.singleton};
         PojiProxy proxyHandler = new PojiProxy(containerName, locator, interceptors);
         Class<?>[] interfaces = new Class<?>[]
         {StatefulSessionInvokableContext.class};
         container = (StatefulSessionInvokableContext<?>) Proxy.newProxyInstance(interfaces[0].getClassLoader(),
               interfaces, proxyHandler);
      }

      // Get a new Session ID from the Container
      Object sessionId = null;
      try
      {
         sessionId = container.createSession();
      }
      catch (NotBoundException e)
      {
         throw new RuntimeException("Could not obtain a new Session ID from SFSB Container with name \""
               + containerName + "\"", e);
      }

      // Set the Session ID on the Proxy
      sHandler.setSessionId(sessionId);

      // Return the Proxy
      return proxy;
   }

}
