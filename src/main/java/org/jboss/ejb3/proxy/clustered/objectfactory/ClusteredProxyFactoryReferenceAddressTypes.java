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
package org.jboss.ejb3.proxy.clustered.objectfactory;

/**
 * This interface defines the key constants used as 
 * valid factory reference address types expected by a cluster-aware 
 * ProxyObjectFactory
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface ClusteredProxyFactoryReferenceAddressTypes
{
   // --------------------------------------------------------------------------------||
   // Constants ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /*
    * The following are Reference Address Types used in Remoting
    */
   
   String REF_ADDR_TYPE_CLUSTER_PARTITION_NAME = "Cluster Partition Name";
   
   String REF_ADDR_TYPE_CLUSTER_PROXY_FACTORY_LOAD_BALANCE_POLICY = "Cluster Proxy Factory Load Balance Policy";
   
   String REF_ADDR_TYPE_CLUSTER_TARGET_ID = "Cluster Target ID";
   
   String REF_ADDR_TYPE_CLUSTER_FAMILY_NAME = "Cluster Family Name";
   
   String REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL = "Cluster Target Remoting Host URL";

}
