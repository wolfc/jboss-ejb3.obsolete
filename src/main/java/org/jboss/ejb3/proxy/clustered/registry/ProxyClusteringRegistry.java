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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.ejb3.annotation.defaults.ClusteredDefaults;
import org.jboss.ejb3.proxy.clustered.familyname.ClusterFamilyNamePolicy;
import org.jboss.ejb3.proxy.clustered.familyname.InvokerLocatorProtocolClusterFamilyNamePolicy;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.client.loadbalance.RoundRobin;
import org.jboss.ha.client.loadbalance.aop.FirstAvailable;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager.ReplicantListener;
import org.jboss.ha.framework.server.HAPartitionLocator;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.ClusterConfigMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.remoting.InvokerLocator;
import org.jboss.util.StringPropertyReplacer;

/**
 * Registry for clustering information about clustered containers.
 * 
 * @author Brian Stansberry
 * @version $Revision: $
 *
 */
public class ProxyClusteringRegistry implements ReplicantListener
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   private static final Logger log = Logger.getLogger(ProxyClusteringRegistry.class);
   
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /** Event listeners */
   private List<ProxyClusteringRegistryListener> listeners = new ArrayList<ProxyClusteringRegistryListener>();
   
   /** Policy for creating names for FamilyClusterInfo */
   private ClusterFamilyNamePolicy<InvokerLocator> clusterFamilyNamePolicy;
   
   /** Map of String annotation values to classes */
   private Map<String, Class<LoadBalancePolicy>> loadBalancePolicies;
   
   /** Clustering data for each family */
   private Map<String, ProxyClusteringInfo> beanInfosByFamilyName = new ConcurrentHashMap<String, ProxyClusteringInfo>();
   /** Clustering data for each ProxyFactory */
   private Map<String, ProxyClusteringInfo> beanInfosByProxyFactory = new ConcurrentHashMap<String, ProxyClusteringInfo>();
   /** HATargets for each container, stored in a map keyed by family name */
   private ConcurrentMap<String, Map<String, HATarget>> haTargetsByContainerName = new ConcurrentHashMap<String, Map<String, HATarget>>();
   
   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   public void registerListener(ProxyClusteringRegistryListener listener)
   {
      synchronized (listeners)
      {
         listeners.add(listener);
      }
   }
   
   public void unregisterListener(ProxyClusteringRegistryListener listener)
   {
      synchronized (listeners)
      {
         listeners.remove(listener);
      }      
   }
   
   public ProxyClusteringInfo registerClusteredBean(String containerName, String proxyFactoryName, 
         JBossSessionBeanMetaData metadata, InvokerLocator locator)
      throws Exception
   {
      ClusterConfigMetaData clusterMetadata = metadata.getClusterConfig();
      String partitionName = getPartitionName(clusterMetadata);
      
      String familyInfoName = getClusterFamilyNamePolicy().getClusterFamilyName(containerName, locator, partitionName);
      
      HAPartition partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(partitionName, null);
      HATarget hatarget = new HATarget(partition, familyInfoName, locator, HATarget.ENABLE_INVOCATIONS);
      
      FamilyWrapper wrapper = new FamilyWrapper(familyInfoName, hatarget.getReplicants());
      
      String lbpClassKey = clusterMetadata.getLoadBalancePolicy();
      Class<? extends LoadBalancePolicy> lbPolicyClass = getLoadBalancePolicyClass(lbpClassKey, metadata.isStateful());         
     
      lbpClassKey = clusterMetadata.getHomeLoadBalancePolicy();
      Class<? extends LoadBalancePolicy> homeLBPolicyClass = getLoadBalancePolicyClass(lbpClassKey, false);
      
      ProxyClusteringInfo info = new ProxyClusteringInfo(containerName, proxyFactoryName, partitionName, wrapper, lbPolicyClass, homeLBPolicyClass, hatarget);
      
      // Place this data in the various maps
      registerBeanClusteringInfo(info);
      
      // IMPORTANT: This must be done after the HATarget is instantiated above
      // or we will get a notification from our own work instantiating the HATarget
      DistributedReplicantManager drm = partition.getDistributedReplicantManager();
      drm.registerListener(familyInfoName, this);
      
      // Notify listeners of the addition. We do this after we register
      // with the DRM so the listener won't know about the bean until our
      // locally initiated DRM events have finished
      List<ProxyClusteringRegistryListener> toNotify = null;
      synchronized (listeners)
      {
         toNotify = new ArrayList<ProxyClusteringRegistryListener>(listeners);
      }
      
      for (ProxyClusteringRegistryListener listener : toNotify)
      {
         listener.beanClusteringInfoAdded(info);
      }
      
      return info;
   }
   
   public void unregisterClusteredBean(ProxyClusteringInfo info)
   {
      String familyName = info.getFamilyWrapper().get().getFamilyName();
      
      // Destroy the HATarget and stop listening to DRM
      info.getHaTarget().destroy();
      HAPartition partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(info.getPartitionName(), null);
      partition.getDistributedReplicantManager().unregisterListener(familyName, this);
      
      String containerName = info.getContainerName();
      
      beanInfosByFamilyName.remove(familyName);
      beanInfosByProxyFactory.remove(info.getProxyFactoryName());
      
      Map<String, HATarget> haTargets = haTargetsByContainerName.get(containerName);
      if (haTargets != null)
      {
         haTargets.remove(familyName);
         if (haTargets.size() == 0)
         {
            haTargetsByContainerName.remove(containerName);
         }
      }
      
      /** Notify listeners of the removal */
      List<ProxyClusteringRegistryListener> toNotify = null;
      synchronized (listeners)
      {
         toNotify = new ArrayList<ProxyClusteringRegistryListener>(listeners);
      }
      
      for (ProxyClusteringRegistryListener listener : toNotify)
      {
         listener.beanClusteringInfoRemoved(info);
      }
   }
   
   public ProxyClusteringInfo getBeanClusteringInfo(String proxyFactoryKey)
   {
      return beanInfosByProxyFactory.get(proxyFactoryKey);
   }
   
   /**
    * Gets a map of all <code>HATarget</code>s associated with the given
    * container, keyed by the cluster family name associated with the 
    * target.
    * <p>
    * This method is exposed for use by ReplicantsManagerInterceptorFactory
    * in ejb3-core.
    * </p>
    * 
    * @param containerName the name of the container
    * @return Map<familyName, haTarget> or <code>null</code> if none are registered
    */
   public Map<String, HATarget> getHATargets(String containerName)
   {
      Map<String, HATarget> containerMap = new HashMap<String, HATarget>();
      Map<String, HATarget> existing = haTargetsByContainerName.putIfAbsent(containerName, containerMap);
      if (existing != null)
      {
         containerMap = existing;
      }
      return containerMap;
   }
   
   // --------------------------------------------------------------------------------||
   // ReplicantListener --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /**
    * Updates the FamilyWrapper identified by <code>key</code> and notifies
    * any {@link ProxyClusteringRegistryListener}s of a topology change.
    */
   @SuppressWarnings("unchecked")
   public void replicantsChanged(String key, List newReplicants, int newReplicantsViewId, boolean merge)
   {
      ProxyClusteringInfo info = beanInfosByFamilyName.get(key);
      if (info != null)
      {
         info.getFamilyWrapper().get().updateClusterInfo(newReplicants, newReplicantsViewId);
         
         List<ProxyClusteringRegistryListener> toNotify = null;
         synchronized (listeners)
         {
            toNotify = new ArrayList<ProxyClusteringRegistryListener>(listeners);
         }
         
         for (ProxyClusteringRegistryListener listener : toNotify)
         {
            listener.clusterTopologyChanged(info);
         }
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   public synchronized ClusterFamilyNamePolicy<InvokerLocator> getClusterFamilyNamePolicy()
   {
      if (clusterFamilyNamePolicy == null)
      {
         clusterFamilyNamePolicy = new InvokerLocatorProtocolClusterFamilyNamePolicy();
      }
      return clusterFamilyNamePolicy;
   }

   public void setClusterFamilyNamePolicy(ClusterFamilyNamePolicy<InvokerLocator> clusterFamilyNamePolicy)
   {
      this.clusterFamilyNamePolicy = clusterFamilyNamePolicy;
   }

   public Map<String, Class<LoadBalancePolicy>> getLoadBalancePolicies()
   {
      return loadBalancePolicies;
   }

   public void setLoadBalancePolicies(Map<String, Class<LoadBalancePolicy>> loadBalancePolicies)
   {
      this.loadBalancePolicies = loadBalancePolicies;
   }

   // --------------------------------------------------------------------------------||
   // Private ------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /**
    * Gets the <code>Class</code> that corresponds to the given key.
    * 
    * @param lbpClassKey   the key
    * @param defaultSticky if the key is <code>null</code>, empty or
    *                      {@link ClusteredDefaults.LOAD_BALANCE_POLICY_DEFAULT},
    *                      should the default value returned implement 
    *                      sticky-session behavior?
    *                      
    * @return the load balancy policy class
    */
   @SuppressWarnings("unchecked")
   private Class<? extends LoadBalancePolicy> getLoadBalancePolicyClass(String lbpClassKey, boolean defaultSticky)
   {
      if (lbpClassKey == null || lbpClassKey.length() == 0 || lbpClassKey.equals(ClusteredDefaults.LOAD_BALANCE_POLICY_DEFAULT))
      {
         return defaultSticky ? FirstAvailable.class : RoundRobin.class;
      }
      else
      {
         Class<LoadBalancePolicy> lbPolicyClass = loadBalancePolicies == null ? null : loadBalancePolicies.get(lbpClassKey);
         if (lbPolicyClass == null)
         {
            // Try to use the string as a classname
            String className = lbpClassKey;
            
            // If it's a simple string, prepend a standard package
            if (lbpClassKey.indexOf('.') < 0)
            {
               className = RoundRobin.class.getPackage().getName() + "." + lbpClassKey;
            }
            
            try
            {
               lbPolicyClass = (Class<LoadBalancePolicy>) Thread.currentThread().getContextClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
               // If it's a simple string, prepend a different standard package
               if (lbpClassKey.indexOf('.') < 0)
               {
                  className = FirstAvailable.class.getPackage().getName() + "." + lbpClassKey;
                  try
                  {
                     lbPolicyClass = (Class<LoadBalancePolicy>) Thread.currentThread().getContextClassLoader().loadClass(className);
                  }
                  catch (ClassNotFoundException ignored) {}
               }
               
               if (lbPolicyClass == null)
               {
                  throw new IllegalStateException("Cannot determine LoadBalancePolicy class for key " + lbpClassKey);
               }
            }
         }
         
         return lbPolicyClass;
      }      
   }
   
   private void registerBeanClusteringInfo(ProxyClusteringInfo info)
   {
      String containerName = info.getContainerName();
      String familyName = info.getFamilyWrapper().get().getFamilyName();
      
      beanInfosByFamilyName.put(familyName, info);
      
      beanInfosByProxyFactory.put(info.getProxyFactoryName(), info);
      
      Map<String, HATarget> containerMap = getHATargets(containerName);      
      containerMap.put(familyName, info.getHaTarget());
   }
   
   public static String getPartitionName(ClusterConfigMetaData metadata)
   {
      String value = metadata.getPartitionName();
      try
      {
         String replacedValue = StringPropertyReplacer.replaceProperties(value);
         if (value != replacedValue)
         {            
            log.debug("Replacing " + ClusterConfigMetaData.class.getSimpleName() + 
                  " partitionName property " + value + " with " + replacedValue);
            value = replacedValue;
         }
      }
      catch (Exception e)
      {
         log.warn("Unable to replace @Clustered partition attribute " + value + 
                  ". Caused by " + e.getClass() + " " + e.getMessage());         
      }
      
      return value;
   }
   
}
