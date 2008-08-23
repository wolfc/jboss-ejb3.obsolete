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

package org.jboss.ejb3.proxy.clustered.registry;

import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.framework.server.HATarget;

/**
 * Encapsulates clustering information about a particular container's
 * cluster family.
 * <p>
 * There will be one instance of this class for each combination of container
 * and InvokerLocator. So, a container that has two @RemoteBinding 
 * annotations with different <code>clientBindUrl</code> values will generate
 * two ProxyClusteringInfo objects.
 * 
 * @author Brian Stansberry
 *
 * @version $Revision: $
 */
public class ProxyClusteringInfo
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private final String containerName;
   private final String proxyFactoryName;
   private final FamilyWrapper familyWrapper;
   private final String partitionName;
   private final Class<? extends LoadBalancePolicy> loadBalancePolicy;
   private final Class<? extends LoadBalancePolicy> homeLoadBalancePolicy;
   private final HATarget haTarget;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   public ProxyClusteringInfo(String containerName, String proxyFactoryName, 
                             String partitionName, FamilyWrapper wrapper,
                             Class<? extends LoadBalancePolicy> loadBalancePolicy,
                             Class<? extends LoadBalancePolicy> homeLoadBalancePolicy, 
                             HATarget haTarget)
   {
      assert containerName != null : "containerName is null";
      assert proxyFactoryName != null : "proxyFactoryName is null";
      assert partitionName != null : "partitionName is null";
      assert wrapper != null : "wrapper is null";
      assert loadBalancePolicy != null : "loadBalancePolicy is null";
      assert homeLoadBalancePolicy != null : "homeLoadBalancePolicy is null";
      assert haTarget != null : "haTarget is null";
      
      this.containerName = containerName;
      this.proxyFactoryName = proxyFactoryName;
      this.partitionName = partitionName;
      this.familyWrapper = wrapper;
      this.loadBalancePolicy = loadBalancePolicy;
      this.homeLoadBalancePolicy = homeLoadBalancePolicy;
      this.haTarget = haTarget;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||   

   public String getContainerName()
   {
      return containerName;
   }

   public String getProxyFactoryName()
   {
      return proxyFactoryName;
   }

   public String getPartitionName()
   {
      return partitionName;
   }

   public FamilyWrapper getFamilyWrapper()
   {
      return familyWrapper;
   }  

   public Class<? extends LoadBalancePolicy> getLoadBalancePolicy()
   {
      return loadBalancePolicy;
   }

   public Class<? extends LoadBalancePolicy> getHomeLoadBalancePolicy()
   {
      return homeLoadBalancePolicy;
   }

   public HATarget getHaTarget()
   {
      return haTarget;
   }   
   
}