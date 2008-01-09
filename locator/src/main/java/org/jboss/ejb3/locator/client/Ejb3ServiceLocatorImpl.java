/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.jboss.ejb3.locator.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ejb3ServiceLocatorImpl
 * 
 * Implementation of the Service Locator; will attempt to obtain
 * services from one of a set of configured JNDI Directories (Hosts).
 * 
 * @version $Revision $&
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 */
public final class Ejb3ServiceLocatorImpl extends Ejb3ServiceLocator
{

   // Class Members
   private static final Log logger = LogFactory.getLog(Ejb3ServiceLocatorImpl.class);

   // Instance Members

   /**
    * JNDI Hosts on which Services may be bound
    */
   private final Map<String, JndiHost> jndiHosts = Collections.synchronizedMap(new HashMap<String, JndiHost>());

   /**
    * Mapping of JNDI Hosts to Naming Contexts
    */
   private final Map<String, Context> contexts = Collections.synchronizedMap(new HashMap<String, Context>());

   /**
    * Mapping of Business Interface to the JNDI Name on which its mapped
    */
   private final Map<Class<?>, String> serviceMappings = Collections.synchronizedMap(new HashMap<Class<?>, String>());

   // Constructor

   /**
    * Constructor
    */
   Ejb3ServiceLocatorImpl(Map<String, JndiHost> jndiHosts)
   {
      super();
      this.jndiHosts.putAll(jndiHosts);
      this.contexts.putAll(this.createNamingContextsFromJndiHosts());
   }

   // Contracts

   /**
    * Obtains the object associated with the specified business interface from
    * one of the configured remote hosts.
    * 
    * @param <T>
    * @param clazz
    *            The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException
    *             If no services implementing the specified business interface
    *             could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *             If the specified class is a business interface implemented by
    *             more than one service across the configured local/remote
    *             hosts, or if the specified class is not an interface
    */
   public <T> T getObject(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      // Initialize
      T obj = null;

      // For all configured hosts
      for (JndiHost host : this.jndiHosts.values())
      {
         T retrievedObj = null;
         try
         {
            retrievedObj = this.getObject(host, clazz);
         }
         // Ignore, simply not present on this one host
         catch (Ejb3NotFoundException enfe)
         {
            continue;
         }

         // Ensure that an EJB with this business interface has not already
         // been found on another host
         if (obj != null)
         {
            throw new IllegalArgumentException("EJB3 with business interface " + clazz.getName()
                  + " is not unique across all configured hosts and may not be looked up by interface alone.");
         }

         // Set retrieved object to obj and continue searching through other
         // configured hosts to ensure uniqueness
         obj = retrievedObj;
      }

      // If not found on any hosts
      if (obj == null)
      {
         throw new Ejb3NotFoundException("Could not find EJB3 with business interface " + clazz.getName()
               + " on any configured hosts.");
      }

      // Return
      return obj;
   }

   /**
    * Obtains the object associated with the specified business interface from
    * the host with the specified ID.
    * 
    * @param <T>
    * @param hostId
    *            The ID of the host from which to obtain the object with the
    *            specified business interface
    * @param clazz
    *            The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException
    *             If no services implementing the specified business interface
    *             could be found on the specified host
    * @throws IllegalArgumentException
    *             If the specified class is a business interface implemented by
    *             more than one service across the specified host, if the
    *             specified class is not an interface, or if the specified host
    *             ID is not valid for one of the configured hosts
    */
   public <T> T getObject(String hostId, Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      // Obtain configured host by ID
      JndiHost host = this.jndiHosts.get(hostId);

      // Ensure ID is valid
      if (host == null)
      {
         throw new IllegalArgumentException("There are no configured hosts with ID of \"" + hostId + "\"");
      }

      // Return
      return this.getObject(host, clazz);

   }

   /**
    * Fetches the object bound at the specified JNDI Address from the JNDI Host
    * with the specified ID
    * 
    * @param hostId
    * @param jndiName
    * @return
    * @throws NameNotFoundException
    *             If the specified JNDI Address is not a valid binding for the
    *             specified host
    */
   public Object getObject(String hostId, String jndiName) throws NameNotFoundException
   {

      // Initialize
      Context context = null;

      // Obtain context
      context = this.contexts.get(hostId);

      // Obtain JNDI Host
      JndiHost host = null;
      try
      {
         host = this.getJndiHost(hostId);
      }
      catch (JndiHostNotFoundException jhnfe)
      {
         throw new IllegalStateException(jhnfe);
      }

      // Obtain JNP URL for logging
      String jnpUrl = this.constructJnpUrl(host);

      // Ensure defined
      if (context == null)
      {
         throw new ServiceLocatorException("A JNDI Host with ID \"" + hostId
               + "\" has not been defined in configuration; cannot lookup \"" + jndiName + "\" from " + jnpUrl);
      }

      // Lookup
      try
      {
         logger.debug("Performing JNDI Lookup of \"" + jndiName + "\" on " + jnpUrl);
         return context.lookup(jndiName);
      }
      catch (NamingException e)
      {
         // Wrap as runtime error
         throw new ServiceLocatorException(e);
      }

   }

   // Convenience Methods

   /**
    * Obtains the JNDI Host with the specified ID;
    * may only be used after JNDI Hosts have been
    * initialized
    */
   protected JndiHost getJndiHost(String id) throws JndiHostNotFoundException
   {
      // Initialize
      JndiHost host = null;

      // Obtain
      host = this.jndiHosts.get(id);

      // Ensure found
      if (host == null)
      {
         throw new JndiHostNotFoundException("JNDI Host with ID " + id + " has not been properly initialized");
      }

      // Return
      return host;
   }

   // Internal Helper Methods

   /**
    * Obtains a Map of JNDI Hosts to Naming Contexts from the currently-defined 
    * JNDI Hosts
    */
   private Map<String, Context> createNamingContextsFromJndiHosts()
   {
      // Initialize
      Map<String, Context> contexts = new HashMap<String, Context>();

      assert (this.jndiHosts != null);
      if (this.jndiHosts == null)
      {
         throw new IllegalStateException(
               "createNamingContextsFromJndiHosts cannot be called until JNDI Hosts have been initialized");
      }

      // For each JNDI Host
      for (JndiHost host : this.jndiHosts.values())
      {
         // Populate Properties for naming context
         Properties props = this.getVendorNamingContextProperties();
         this.setNamingContextProviderUrl(props, host);

         Context context = null;
         try
         {
            context = new InitialContext(props);
         }
         catch (NamingException e)
         {
            throw new ServiceLocatorException(e);
         }

         // Add to Map
         contexts.put(host.getId(), context);
      }

      return contexts;
   }

   /**
    * Return vendor-specific properties for the naming context
    * 
    * @return
    */
   // TODO Externalize to allow for other vendor implementations
   private Properties getVendorNamingContextProperties()
   {
      Properties props = new Properties();
      props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      return props;
   }

   private void setNamingContextProviderUrl(Properties props, JndiHost host)
   {
      // Place into properties
      props.put(Context.PROVIDER_URL, this.constructJnpUrl(host));
   }

   private String constructJnpUrl(JndiHost host)
   {
      // Initialize
      StringBuffer sb = new StringBuffer();

      // Construct URL
      sb.append("jnp://");
      sb.append(host.getAddress());
      sb.append(":");
      sb.append(host.getPort());

      // Return
      return sb.toString();
   }

   /**
    * 
    */
   @SuppressWarnings("unchecked")
   private <T> T getObject(JndiHost host, Class<T> clazz) throws Ejb3NotFoundException
   {
      // Determine JNDI Host providing type
      String jndiName = this.getJndiNameForClass(clazz);

      // Look up
      try
      {
         return (T) this.getObject(host.getId(), jndiName);
      }
      catch (NameNotFoundException e)
      {
         throw new ServiceLocatorException(e);
      }

   }

   /**
    * Determines the JNDI Name for the specified class
    * 
    * @param clazz
    * @return
    * @throws Ejb3NotFoundException
    */
   private String getJndiNameForClass(Class<?> clazz) throws Ejb3NotFoundException
   {
      throw new RuntimeException("IMPLEMENT");
   }

}
