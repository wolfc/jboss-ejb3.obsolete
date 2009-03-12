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
package org.jboss.ejb3.proxy.impl.objectstore;

/**
 * ObjectStoreBindings
 * 
 * Defines constants used in binding POJOs to
 * the Object Store
 * 
 * These values must match those either used to 
 * Register beans programmatically or via
 * XML Configuration
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface ObjectStoreBindings
{
   // --------------------------------------------------------------------------------||
   // Constants ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * The following are Namespaces used by EJB3
    */

   /**
    * Namespace for all Beans for EJB3
    */
   String OBJECTSTORE_NAMESPACE_EJB3 = "org.jboss.ejb3.";

   /**
    * Namespace of all EJB Containers, should not be used directly but 
    * instead precedes namespaces for SLSB, SFSB, MDB, and @Service
    */
   String OBJECTSTORE_NAMESPACE_EJBCONTAINER = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJB3 + "EJBContainer.";

   /**
    * Namespace for SFSB Containers
    */
   String OBJECTSTORE_NAMESPACE_EJBCONTAINER_STATEFUL = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJBCONTAINER
         + "StatefulSession.";

   /**
    * Namespace for SLSB Containers
    */
   String OBJECTSTORE_NAMESPACE_EJBCONTAINER_STATELESS = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJBCONTAINER
         + "StatelessSession.";

   /**
    * Namespace for @Service Containers
    */
   String OBJECTSTORE_NAMESPACE_EJBCONTAINER_SERVICE = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJBCONTAINER
         + "Service.";

   /**
    * Namespace for MDB Containers
    */
   String OBJECTSTORE_NAMESPACE_EJBCONTAINER_MDB = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJBCONTAINER + "MDB.";

   /*
    * The following are Bindings used as Object Store Bean Names
    */

   /**
    * Namespace for all JNDI Registrars
    */
   String OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJB3 + "JndiRegistrar.";

   /**
    * Namespace for Session EJB JNDI Registrars
    */
   String OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR_SESSION = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR
         + "Session.";

   /**
    * Bind name for SLSB JNDI Registrar
    */
   String OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SLSB = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR_SESSION
         + "SLSBJndiRegistrar";

   /**
    * Bind name for SFSB JNDI Registrar
    */
   String OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SFSB = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR_SESSION
         + "SFSBJndiRegistrar";

   /**
    * Bind name for @Service JNDI Registrar
    */
   String OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SERVICE = ObjectStoreBindings.OBJECTSTORE_NAMESPACE_JNDI_REGISTRAR_SESSION
         + "ServiceJndiRegistrar";

}
