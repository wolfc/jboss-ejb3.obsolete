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

import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
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
    * The default URL for InvokerLocator in the case @RemoteBinding does not specify it
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
            // Lookup the Connector in MC
            Connector connector = Ejb3RegistrarLocator.locateRegistrar().lookup(OBJECT_NAME_REMOTING_CONNECTOR,
                  Connector.class);

            // Use the binding specified by the Connector
            try
            {
               DEFAULT_CLIENT_BINDING = connector.getInvokerLocator();
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

}
