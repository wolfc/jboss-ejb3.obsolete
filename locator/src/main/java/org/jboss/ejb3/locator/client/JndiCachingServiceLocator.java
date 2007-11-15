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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JndiCachingServiceLocator
 * 
 * JNDI-based implementation of the Service Locator; will attempt to obtain
 * services from one of a set of configured JNDI Directories (Hosts).
 * 
 * @version $Revision $
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 */
public final class JndiCachingServiceLocator extends CachingServiceLocator
{

   // Class Members
   private static final Log logger = LogFactory.getLog(JndiCachingServiceLocator.class);

   private static JndiCachingServiceLocator instance = null;

   // Instance Members

   /**
    * List of JNDI Hosts on which Services may be bound
    */
   private final Map<String, JndiHost> jndiHosts = Collections.synchronizedMap(new HashMap<String, JndiHost>());

   /**
    * Mapping of Business Interface to the JNDI Host upon which it resides
    */
   private final Map<Class<?>, JndiHost> serviceMappings = Collections
         .synchronizedMap(new HashMap<Class<?>, JndiHost>());

   // Constructor

   /**
    * Constructor
    */
   JndiCachingServiceLocator(Map<String, JndiHost> jndiHosts)
   {
      super();
      this.jndiHosts.putAll(jndiHosts);
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

         // Ensure that an EJB with this business interface has not already been found on another host
         if (obj != null)
         {
            throw new IllegalArgumentException("EJB3 with business interface " + clazz.getName()
                  + " is not unique across all configured hosts and may not be looked up by interface alone.");
         }

         // Set retrieved object to obj and continue searching through other configured hosts to ensure uniqueness
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
    * Obtains the object associated with the specified business interface 
    * from the host with the specified ID.
    * 
    * @param <T>
    * @param hostId The ID of the host from which to obtain the 
    *   object with the specified business interface
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on the specified host
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the specified host, if the
    *   specified class is not an interface, or if the specified host ID is not 
    *   valid for one of the configured hosts 
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

   // Internal Helper Methods

   /**
    * 
    */
   private <T> T getObject(JndiHost host, Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      throw new RuntimeException("IMPLEMENT");
   }

}
