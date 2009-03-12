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

package org.jboss.ejb3.proxy.clustered.jndiregistrar;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Context;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.proxy.clustered.objectfactory.ClusteredProxyFactoryReferenceAddressTypes;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringInfo;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringRegistry;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringRegistryListener;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiReferenceBinding;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiReferenceBindingSet;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.impl.objectfactory.ProxyFactoryReferenceAddressTypes;
import org.jboss.ha.framework.interfaces.FamilyClusterInfo;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.ClusterConfigMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.remoting.InvokerLocator;



/**
 * Responsible for binding of ObjectFactories and creation/registration of 
 * associated ProxyFactories, centralizing operations common to that of all 
 * clustered Session EJB Implementations.
 * 
 * @author Brian Stansberry
 */
public abstract class JndiClusteredSessionRegistrarBase 
   extends JndiSessionRegistrarBase
   implements ProxyClusteringRegistryListener
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   private static final Logger log = Logger.getLogger(JndiClusteredSessionRegistrarBase.class);
   
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   private final ProxyClusteringRegistry registry;
   private final Map<String, BeanClusteringRegistryInfo> bindingsByContainer = new ConcurrentHashMap<String, BeanClusteringRegistryInfo>(); 

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create a new JndiClusteredSessionRegistrarBase.
    * 
    * @param sessionProxyObjectFactoryType
    * @param registry registry of clustering information about deployed containers
    */
   public JndiClusteredSessionRegistrarBase(String sessionProxyObjectFactoryType, 
                                            ProxyClusteringRegistry registry)
   {
      super(sessionProxyObjectFactoryType);
      
      assert registry != null : "registry is null";
      this.registry = registry;
      this.registry.registerListener(this);
   }

   // --------------------------------------------------------------------------------||
   // ProxyClusteringRegistryListener -------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Finds any {@link JndiReferenceBindingSet} associated with the 
    * <code>beanClusteringInfo</code>'s container, updates any <code>Reference</code>s
    * associated with the <code>beanClusteringInfo</code>'s <code>FamilyWrapper</code> to
    * reflect the new cluster topoloyg, and rebinds the reference in JNDI.
    */
   public void clusterTopologyChanged(ProxyClusteringInfo beanClusteringInfo)
   {
      JndiReferenceBindingSet bindings = null;
      BeanClusteringRegistryInfo registryEntry = bindingsByContainer.get(beanClusteringInfo.getContainerName());
      if (registryEntry != null)
      {
         bindings= registryEntry.bindings;
      }
      
      if (bindings == null)
      {
         // We aren't handling this bean
         return;
      }
      
      Context context = bindings.getContext();
      
      FamilyClusterInfo fci = beanClusteringInfo.getFamilyWrapper().get();     
      String familyName = fci.getFamilyName();
      
      for (JndiReferenceBinding binding : bindings.getDefaultRemoteBindings())
      {
         RefAddr refAddr = getFirstRefAddr(binding.getReference(), ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME);
         if (refAddr != null && familyName.equals(refAddr.getContent()))
         {
            redecorateReferenceForClusteringTargets(binding.getReference(), fci);
            rebind(context, binding.getJndiName(), binding.getReference());
         }
      }
      
      for (JndiReferenceBinding binding : bindings.getHomeRemoteBindings())
      {
         RefAddr refAddr = getFirstRefAddr(binding.getReference(), ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME);
         if (refAddr != null && familyName.equals(refAddr.getContent()))
         {
            redecorateReferenceForClusteringTargets(binding.getReference(), fci);
            rebind(context, binding.getJndiName(), binding.getReference());
         }
      }
      
      for (Set<JndiReferenceBinding> businessBindings : bindings.getBusinessRemoteBindings().values())
      {         
         for (JndiReferenceBinding binding : businessBindings)
         {
            RefAddr refAddr = getFirstRefAddr(binding.getReference(), ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME);
            if (refAddr != null && familyName.equals(refAddr.getContent()))
            {
               redecorateReferenceForClusteringTargets(binding.getReference(), fci);
               rebind(context, binding.getJndiName(), binding.getReference());
            }
         }
      }
   }

   public void beanClusteringInfoAdded(ProxyClusteringInfo beanClusteringInfo)
   {
      BeanClusteringRegistryInfo info = getBeanClusteringRegistryInfo(beanClusteringInfo.getContainerName());
      info.familyCount.incrementAndGet();
   }

   public void beanClusteringInfoRemoved(ProxyClusteringInfo beanClusteringInfo)
   {
      String containerName = beanClusteringInfo.getContainerName();
      BeanClusteringRegistryInfo info = getBeanClusteringRegistryInfo(containerName);
      if (info.familyCount.decrementAndGet() == 0)
      {
         bindingsByContainer.remove(containerName);
      }
   }   

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ProxyClusteringRegistry getRegistry()
   {
      return registry;
   }

   // --------------------------------------------------------------------------------||
   // Overrides ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public String getProxyFactoryRegistryKey(String jndiName, JBossSessionBeanMetaData smd, boolean isLocal)
   {
      String key = super.getProxyFactoryRegistryKey(jndiName, smd, isLocal);
      if (!isLocal)
      {
         ClusterConfigMetaData ccmd = smd.getClusterConfig();
         assert ccmd != null : ClusterConfigMetaData.class.getSimpleName()
               + " not found in metadata, specified only in XML? [EJBTHREE-1539]";
         key += "/" + ProxyClusteringRegistry.getPartitionName(smd.getClusterConfig());
      }
      return key;
   }
   
   /**
    * Overrides the superclass version to add clustering related {@link RefAddr}s
    * to the binding references.
    */
   @Override
   protected JndiReferenceBindingSet createJndiReferenceBindingSet(final Context context, 
         final JBossSessionBeanMetaData smd, final ClassLoader cl,
         final String containerName, final String containerGuid, final Advisor advisor)
   {
      JndiReferenceBindingSet bindings = super.createJndiReferenceBindingSet(context, smd, cl, containerName, containerGuid, advisor);
      
      decorateReferencesForClustering(bindings);
      
      // Store ref to bindings so we can rebind upon topology changes
      BeanClusteringRegistryInfo registryInfo = getBeanClusteringRegistryInfo(containerName);      
      registryInfo.bindings = bindings;
      
      return bindings;
   }

   // --------------------------------------------------------------------------------||
   // Private ------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private BeanClusteringRegistryInfo getBeanClusteringRegistryInfo(final String containerName)
   {
      BeanClusteringRegistryInfo registryInfo = this.bindingsByContainer.get(containerName);
      if (registryInfo == null)
      {
         registryInfo = new BeanClusteringRegistryInfo();
         this.bindingsByContainer.put(containerName, registryInfo);
      }
      return registryInfo;
   }

   /**
    * Add clustering related <code>RefAddr</code>s to the <code>Reference</code>s
    * in the given binding set.
    * 
    * @param bindings the binding set
    */
   private void decorateReferencesForClustering(JndiReferenceBindingSet bindings)
   {
      for (JndiReferenceBinding binding : bindings.getDefaultRemoteBindings())
      {
         decorateReferenceForClustering(binding.getReference());
      }
      
      for (JndiReferenceBinding binding : bindings.getHomeRemoteBindings())
      {
         decorateReferenceForClustering(binding.getReference());
      }
      
      for (Set<JndiReferenceBinding> businessBindings : bindings.getBusinessRemoteBindings().values())
      {
         for (JndiReferenceBinding binding : businessBindings)
         {
            decorateReferenceForClustering(binding.getReference());
         }
      }
   }
   
   /**
    * Add clustering related <code>RefAddr</code>s to <code>Reference</code>
    * 
    * @param ref           the reference
    */
   private void decorateReferenceForClustering(Reference ref)
   {
      String proxyFactoryKey = getSingleRequiredRefAddr(ref, ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY);
      ProxyClusteringInfo bci = registry.getBeanClusteringInfo(proxyFactoryKey);
      
      if (bci == null)
      {
         throw new IllegalStateException("Cannot find " + ProxyClusteringInfo.class.getSimpleName() + 
                                         " for proxyFactoryKey " + proxyFactoryKey);
      }
      
      RefAddr partitionRef = new StringRefAddr(ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_PARTITION_NAME, bci.getPartitionName());
      addRefAddrToReference(ref, partitionRef);
      RefAddr lbpRef = new StringRefAddr(ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_PROXY_FACTORY_LOAD_BALANCE_POLICY, bci.getHomeLoadBalancePolicy().getName());
      addRefAddrToReference(ref, lbpRef);
      FamilyClusterInfo fci = bci.getFamilyWrapper().get();
      RefAddr familyNameRef = new StringRefAddr(ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME, 
                                                fci.getFamilyName());
      addRefAddrToReference(ref, familyNameRef);
      
      decorateReferenceForClusteringTargets(ref, fci);
   }

   /**
    * Removes from <code>ref</code> all existing <code>RefAddr</code>s of type 
    * {@link ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL},
    * and then calls {@link #decorateReferenceForClusteringTargets(Reference, FamilyClusterInfo).
    *   
    * @param ref the Reference
    * @param fci the source of the targets
    */
   private void redecorateReferenceForClusteringTargets(Reference ref, FamilyClusterInfo fci)
   {
      for (int i = 0; i < ref.size(); i++)
      {
         RefAddr refAddr = ref.get(i);
         if (ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL.equals(refAddr.getType()))
         {
            ref.remove(i);
            i--;
         }
      }
      decorateReferenceForClusteringTargets(ref, fci);
   }
   
   /**
    * Adds a <code>RefAddr</code> of type {@link ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL}
    * to <code>ref</code> for each target associated with <code>fci</code>.
    * 
    * @param ref the Reference
    * @param fci the source of the targets
    */
   @SuppressWarnings("unchecked")
   private void decorateReferenceForClusteringTargets(Reference ref, FamilyClusterInfo fci)
   {
      List<Object> targets = fci.getTargets();
      for (Object target : targets)
      {
         // Assert correct target type. Fail with assertion error if enabled,
         // otherwise with an ISE
         boolean correctType = (target instanceof InvokerLocator);
         assert correctType : target + " is not an instance of InvokerLocator";
         if (!correctType)
            throw new IllegalStateException(target + " is not an instance of InvokerLocator");
         
         String url = ((InvokerLocator) target).getOriginalURI();
         RefAddr targetRef = new StringRefAddr(ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL, url);
         addRefAddrToReference(ref, targetRef);
      }
   }
   
   private void addRefAddrToReference(Reference ref, RefAddr refAddr)
   {
      log.debug("Adding " + RefAddr.class.getSimpleName() + " to "
            + Reference.class.getSimpleName() + ": Type \"" + refAddr.getType() + "\", Content \""
            + refAddr.getContent() + "\"");
      ref.add(refAddr);
   }

   private String getSingleRequiredRefAddr(Reference ref, String refAddrType)
   {
      RefAddr result = null;
      for (int i = 0; i < ref.size(); i++)
      {
         RefAddr refAddr = ref.get(i);
         if (refAddr.getType().equals(refAddrType))
         {
            if (result == null)
            {
               result = refAddr;
            }
            else
            {
               throw new IllegalStateException(ref + " has multiple RefAddr objects of type " + refAddrType);
            }
         }
      }
      
      if (result == null)
      {
         throw new IllegalStateException(ref + " has no RefAddr object of type " + refAddrType);
      }
      
      return (String) result.getContent();
   }

   private RefAddr getFirstRefAddr(Reference ref, String refAddrType)
   {
      for (int i = 0; i < ref.size(); i++)
      {
         RefAddr refAddr = ref.get(i);
         if (refAddr.getType().equals(refAddrType))
         {
            return refAddr;
         }
      }
      return null;
   }
   
   private static class BeanClusteringRegistryInfo
   {
      private final AtomicInteger familyCount = new AtomicInteger();
      private JndiReferenceBindingSet bindings;
   }   

}
