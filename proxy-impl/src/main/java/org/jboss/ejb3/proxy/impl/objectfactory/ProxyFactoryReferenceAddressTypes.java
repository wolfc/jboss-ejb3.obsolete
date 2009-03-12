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
package org.jboss.ejb3.proxy.impl.objectfactory;

/**
 * ProxyFactoryReferenceAddressTypes
 * 
 * This interface defines the key constants used as 
 * valid factory reference address types expected by the 
 * ProxyObjectFactory
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface ProxyFactoryReferenceAddressTypes
{
   // --------------------------------------------------------------------------------||
   // Constants ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * The following are Reference Address Types denoting the type of interface
    * represented by the contents, which should be the fully-qualified class
    * name of the interface
    */

   /**
    * Reference Address Type for EJB3 Remote Business Interfaces
    */
   String REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_REMOTE = "Remote Business Interface";

   /**
    * Reference Address Type for EJB3 Local Business Interfaces
    */
   String REF_ADDR_TYPE_PROXY_BUSINESS_INTERFACE_LOCAL = "Local Business Interface";

   /**
    * Reference Address Type for EJB2.x Remote Home Interfaces
    */
   String REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_REMOTE = "EJB 2.x Remote Home Interface";

   /**
    * Reference Address Type for EJB2.x Local Home Interfaces
    */
   String REF_ADDR_TYPE_PROXY_EJB2x_INTERFACE_HOME_LOCAL = "EJB 2.x Local Home Interface";

   /*
    * The following are Reference Address Types denoting metadata
    * used for interaction with the ProxyFactoryRegistry
    */

   /**
    * Reference Address Type for the key to which the desired ProxyFactory
    * is bound 
    */
   String REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY = "ProxyFactoryKey";

   /*
    * The following are ReferenceAddress types denoting the Name of the EJB Container associated 
    * with a Reference
    */
   
   String REF_ADDR_TYPE_EJBCONTAINER_NAME = "EJB Container Name";
   
   /*
    * The following are Reference Address Types used in Remoting
    */
   
   String REF_ADDR_TYPE_INVOKER_LOCATOR_URL = "Remoting Host URL";
   
   String REF_ADDR_TYPE_IS_LOCAL = "Proxy Factory is Local";

}
