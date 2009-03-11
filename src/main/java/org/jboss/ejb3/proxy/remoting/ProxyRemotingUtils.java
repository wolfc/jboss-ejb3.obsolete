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
package org.jboss.ejb3.proxy.remoting;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.invocation.InvokableContextStatefulRemoteProxyInvocationHack;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.transport.Connector;

/**
 * ProxyRemotingUtils
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyRemotingUtils
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ProxyRemotingUtils.class);

   /*
    * Default Remoting Client Bind URL Support
    */

   /**
    * The name under which the Remoting Connector is bound in MC
    */
   private static final String OBJECT_NAME_REMOTING_CONNECTOR = "org.jboss.ejb3.RemotingConnector";

   /**
    * The default URL for InvokerLocator in the case @RemoteBinding 
    * does not specify it
    * 
    * Synchronization policy on "this"
    */
   protected static String DEFAULT_CLIENT_BINDING;

   /**
    * The default URL for InvokerLocator if if cannot be read from the EJB3 Remoting Connector
    */
   protected static final String DEFAULT_CLIENT_BINDING_IF_CONNECTOR_NOT_FOUND = "socket://0.0.0.0:3873";

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the default client binding
    * 
    * Will return the value of the InvokerLocator
    * used by the EJB3 Remoting Connector
    * 
    * EJBTHREE-1419
    */
   public static synchronized String getDefaultClientBinding()
   {

      // If the binding has not yet been set
      if (DEFAULT_CLIENT_BINDING == null)
      {
         try
         {
            // Use the binding specified by the Connector
            try
            {
               DEFAULT_CLIENT_BINDING = getClientBinding(OBJECT_NAME_REMOTING_CONNECTOR);
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not obtain " + InvokerLocator.class.getSimpleName()
                     + " from EJB3 Remoting Connector", e);
            }
         }
         // The EJB3 Remoting Connector was not found in MC
         catch (NotBoundException nbe)
         {
            // Log a warning
            log.warn("Could not find the EJB3 Remoting Connector bound in the Object Store (MC) at the expected name: "
                  + OBJECT_NAME_REMOTING_CONNECTOR + ".  Defaulting a client bind URL to "
                  + DEFAULT_CLIENT_BINDING_IF_CONNECTOR_NOT_FOUND);

            // Set a default URL
            DEFAULT_CLIENT_BINDING = DEFAULT_CLIENT_BINDING_IF_CONNECTOR_NOT_FOUND;
         }
      }

      // Return
      return DEFAULT_CLIENT_BINDING;
   }

   /**
    * Obtains the client binding for the specified 
    * invokerName (supplied as the Object Store bind name in
    * MC)
    * 
    * @param invokerName
    * @return
    * @throws NotBoundException If the specified invokerName is not bound in MC
    */
   public static String getClientBinding(String invokerName) throws NotBoundException
   {
      // Initialize
      String url = null;
      Connector connector = null;

      // Lookup the Connector in MC
      try
      {
         connector = Ejb3RegistrarLocator.locateRegistrar().lookup(invokerName, Connector.class);
      }
      catch (NotBoundException nbe)
      {
         // Log and rethrow
         log.warn("Could not find the remoting connector for the specified invoker name, " + invokerName + " in MC");
         throw nbe;
      }

      // Use the binding specified by the Connector
      try
      {
         url = connector.getInvokerLocator();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not obtain " + InvokerLocator.class.getSimpleName()
               + " from EJB3 Remoting Connector", e);
      }

      // Return
      return url;
   }

   /**
    * Creates a remote proxy to the container using the specified
    * arguments.
    * 
    * This is here to centralize the hackiness of this approach.  Ideally
    * remoting should be an add-on capability, not built-in to the core
    * logic of having a proxy to a container.  Thus marked as FIXME.
    * 
    * @param containerName
    * @param containerGuid
    * @param url
    * @param interceptors
    * @param target
    * @return
    */
   //FIXME
   public static InvokableContext createRemoteProxyToContainer(String containerName, String containerGuid, String url,
         Interceptor[] interceptors, Object target)
   {
      // Default the remoting URL if necessary
      if (url == null || url.trim().length() == 0)
      {
         url = ProxyRemotingUtils.getDefaultClientBinding();
      }

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

      /*
       * Create a Proxy
       */

      // Create a POJI Proxy to the Container
      PojiProxy handler = new InvokableContextStatefulRemoteProxyInvocationHack(containerName, containerGuid, locator,
            interceptors, target);
      Class<?>[] interfaces = new Class<?>[]
      {InvokableContext.class};
      InvokableContext container = (InvokableContext) Proxy.newProxyInstance(InvokableContext.class.getClassLoader(),
            interfaces, handler);

      // Return
      return container;
   }

}
