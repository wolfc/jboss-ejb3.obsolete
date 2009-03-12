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
package org.jboss.ejb3.proxy.clustered.objectstore;

import org.jboss.ejb3.proxy.impl.objectstore.ObjectStoreBindings;

/**
 * Defines constants used in binding POJOs to the Object Store.
 * 
 * These values must match those either used to Register beans programmatically 
 * or via XML Configuration
 *
 * @author Brian Stansberry 
 * @version $Revision: $
 */
public interface ClusteredObjectStoreBindings extends ObjectStoreBindings
{
   // --------------------------------------------------------------------------------||
   // Constants ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Bind name for ProxyClusteringRegistry
    */
   String CLUSTERED_OBJECTSTORE_BEAN_NAME_PROXY_CLUSTERING_REGISTRY = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJB3
         + "ProxyClusteringRegistry";
   /**
    * Bind name for SLSB JNDI Registrar
    */
   String CLUSTERED_OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SLSB = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR_SESSION
         + "ClusteredSLSBJndiRegistrar";

   /**
    * Bind name for SFSB JNDI Registrar
    */
   String CLUSTERED_OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SFSB = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR_SESSION
         + "ClusteredSFSBJndiRegistrar";
   
   /**
    * Bind name for @Service JNDI Registrar
    */
   String CLUSTERED_OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SERVICE = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR_SESSION
         + "ClusteredServiceJndiRegistrar";

}
