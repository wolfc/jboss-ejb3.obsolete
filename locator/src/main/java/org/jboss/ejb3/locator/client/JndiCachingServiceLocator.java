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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
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
   private List<JndiHost> jndiHosts = Collections.synchronizedList(new ArrayList<JndiHost>());

   /**
    * Mapping of Business Interface to the JNDI Host upon which it resides
    */
   private Map<Class<?>, JndiHost> serviceMappings = Collections.synchronizedMap(new HashMap<Class<?>, JndiHost>());

   // Constructor

   /**
    * Constructor
    */
   JndiCachingServiceLocator(List<JndiHost> jndiHosts)
   {
      super();
      this.jndiHosts = jndiHosts;
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
    *             hosts, or if the specified class is no an interface
    */
   public <T> T getObjectFromRemoteHost(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      throw new RuntimeException("IMPLEMENT");
   }

}
