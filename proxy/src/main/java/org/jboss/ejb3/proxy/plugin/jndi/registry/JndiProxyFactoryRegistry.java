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
package org.jboss.ejb3.proxy.plugin.jndi.registry;

import java.util.HashSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.proxy.factory.ProxyFactory;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryAlreadyRegisteredException;
import org.jboss.ejb3.proxy.spi.registry.ProxyFactoryNotRegisteredException;
import org.jboss.ejb3.proxy.spi.registry.SetBackedProxyFactoryRegistry;
import org.jboss.logging.Logger;
import org.jboss.naming.Util;

/**
 * JndiProxyFactoryRegistry
 * 
 * A Proxy Factory Registry implementation that 
 * binds the proxy factory into JNDI at the name
 * specified by the factory's key.  To speed lookups, 
 * a Set of keys for registered factories is maintained, but
 * the factories themselves are bound only in JNDI and
 * are not necessarily available in RAM.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class JndiProxyFactoryRegistry extends SetBackedProxyFactoryRegistry<HashSet<String>>
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(JndiProxyFactoryRegistry.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   //TODO Inject from MC
   private Context context;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public JndiProxyFactoryRegistry()
   {
      // Call super
      super(new HashSet<String>());

      // Create Context
      try
      {
         // Use default environment
         //TODO Inject from MC
         context = new InitialContext();
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Register the specified Proxy Factory with the 
    * specified key.  Is additionally responsible for calling the
    * ProxyFactory.start()
    * 
    * @param key
    * @param value
    * @throws ProxyFactoryAlreadyRegisteredException When a proxy factory is already registered under the
    *       specified key
    */
   @Override
   public void registerProxyFactory(String key, ProxyFactory value) throws ProxyFactoryAlreadyRegisteredException
   {
      // Register
      super.registerProxyFactory(key, value);

      // Bind to JNDI at the specified key
      try
      {
         Util.rebind(context, key, value);
      }
      catch (NamingException ne)
      {
         // Deregister as the factory was not bound
         try
         {
            this.deregisterProxyFactory(key);
         }
         catch (ProxyFactoryNotRegisteredException pfnre)
         {
            log.error("Could not deregister " + ProxyFactory.class.getSimpleName() + " with key \"" + key
                  + "\" after binding to JNDI failed with: " + ne.getMessage(), pfnre);
         }

         // Rethrow
         throw new RuntimeException(ne);
      }

   }

   /**
    * Register the specified Proxy Factory with the 
    * specified key.  Is additionally responsible for calling the
    * ProxyFactory.stop()
    * 
    * @param key
    * @throws ProxyFactoryNotRegisteredException If no Proxy Factory is registered under the specified key
    */
   public void deregisterProxyFactory(String key) throws ProxyFactoryNotRegisteredException
   {
      // Deregister
      super.deregisterProxyFactoryFromBackingSet(key);

      // Obtain
      ProxyFactory factory = this.getProxyFactory(key);
      assert factory != null : ProxyFactory.class.getSimpleName() + " at key \"" + key
            + "\" found as registered, but was null when obtained";

      // Remove from JNDI
      try
      {
         Util.unbind(this.getContext(), key);
      }
      catch (NamingException e)
      {
         // Log an error only if the JNDI location could not be unbound
         log.error("Deregistered " + ProxyFactory.class.getSimpleName() + " bound to key \"" + key
               + "\", but could not unbind from JNDI", e);
      }

      // Stop
      try
      {
         factory.stop();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error in stopping " + factory, e);
      }
   }

   /**
    * Obtains the ProxyFactory registered with the specified key
    * 
    * @param key
    * @return
    * @throws ProxyFactoryNotRegisteredException If no Proxy Factory is registered under the specified key
    */
   public ProxyFactory getProxyFactory(String key) throws ProxyFactoryNotRegisteredException
   {
      // Ensure exists
      if (!this.isRegistered(key))
      {
         throw new ProxyFactoryNotRegisteredException(ProxyFactory.class.getSimpleName() + " under key \"" + key
               + "\" is not registered");
      }

      // Obtain and return
      Object fromJndi = null;
      try
      {
         fromJndi = Util.lookup(key, ProxyFactory.class);
         assert fromJndi != null : ProxyFactory.class.getSimpleName() + " under key \"" + key
               + "\" is registered but returned null on lookup";
         return ProxyFactory.class.cast(fromJndi);
      }
      catch (ClassCastException cce)
      {
         throw new RuntimeException("Expected " + ProxyFactory.class.getName() + " in JNDI at address \"" + key
               + "\", instead found " + fromJndi, cce);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected Context getContext()
   {
      return context;
   }
}
