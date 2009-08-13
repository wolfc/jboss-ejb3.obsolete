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

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.ClusterChooserInterceptor;
import org.jboss.aspects.remoting.ClusteredPojiProxy;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.proxy.clustered.objectfactory.ClusteredProxyFactoryReferenceAddressTypes;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringInfo;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringRegistry;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringRegistryListener;
import org.jboss.ejb3.proxy.impl.factory.ProxyFactory;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiReferenceBinding;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiReferenceBindingSet;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.impl.objectfactory.ProxyFactoryReferenceAddressTypes;
import org.jboss.ejb3.proxy.impl.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.FamilyClusterInfo;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.ClusterConfigMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.naming.Util;
import org.jboss.remoting.InvokerLocator;

/**
 * Responsible for binding of ObjectFactories and creation/registration of 
 * associated ProxyFactories, centralizing operations common to that of all 
 * clustered Session EJB Implementations.
 * 
 * @author Brian Stansberry
 */
public abstract class JndiClusteredSessionRegistrarBase extends JndiSessionRegistrarBase
      implements
         ProxyClusteringRegistryListener
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
   public JndiClusteredSessionRegistrarBase(String sessionProxyObjectFactoryType, ProxyClusteringRegistry registry)
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
         bindings = registryEntry.bindings;
      }

      if (bindings == null)
      {
         // We aren't handling this bean
         return;
      }

      Context context = bindings.getContext();

      FamilyClusterInfo fci = beanClusteringInfo.getFamilyWrapper().get();
      String familyName = fci.getFamilyName();
      log.debug("Cluster topology changed for family " + familyName + " new view id " + fci.getCurrentViewId()
            + " - Updating JNDI bindings for container " + beanClusteringInfo.getContainerName());

      for (JndiReferenceBinding binding : bindings.getDefaultRemoteBindings())
      {
         RefAddr refAddr = getFirstRefAddr(binding.getReference(),
               ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME);
         if (refAddr != null && familyName.equals(refAddr.getContent()))
         {
            redecorateReferenceForClusteringTargets(binding.getReference(), fci);
            rebind(context, binding.getJndiName(), binding.getReference());
         }

         // The remote proxyfactory in JNDI too needs to be updated with the changes in the
         // clustering family. This involves unbinding the remote proxyfactory from JNDI,
         // creating a new proxy for the proxyfactory with this new FamilyCluster info
         // and finally binding this new proxy for the proxyfactory to the JNDI
         String proxyFactoryKey = this.getSingleRequiredRefAddr(binding.getReference(),
               ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY);
         // first create a new proxy. if we run into problems creating a new proxy,
         // let's NOT unbind the existing one since a change in topology should not
         // result in loss of proxy factory
         ProxyFactory existingProxyFactoryInJNDI = null;
         try
         {
            existingProxyFactoryInJNDI = (ProxyFactory) context.lookup(proxyFactoryKey);

         }
         catch (NamingException ne)
         {
            // ignore and skip. If there is not proxyfactory bound or if there is some other
            // issue related to naming, let's not try to "update" the proxy factory.
            log.debug("Could not update the cluster topology changes to proxyfactory at key " + proxyFactoryKey);
            continue;
         }
         // create a new proxy to proxyfactory with the available information in JNDI Reference,
         // the previously bound proxy to the proxyfactory and the beanClusteringInfo which has
         // contains the updated information of the cluster topology
         ProxyFactory updatedProxyToProxyFactory = this.updateProxyForRemoteProxyFactory(proxyFactoryKey, binding
               .getReference(), existingProxyFactoryInJNDI, beanClusteringInfo);
         try
         {
            Util.rebind(context, proxyFactoryKey, updatedProxyToProxyFactory);
            log.debug("Bound an updated proxyfactory at key " + proxyFactoryKey);
         }
         catch (NamingException ne)
         {
            // let's just log a WARN since we don't want the other operations to fail because of this
            log.warn("Exception while rebinding a new proxyfactory at key " + proxyFactoryKey
                  + " with updated clustered topology", ne);
         }

      }

      for (JndiReferenceBinding binding : bindings.getHomeRemoteBindings())
      {
         RefAddr refAddr = getFirstRefAddr(binding.getReference(),
               ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME);
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
            RefAddr refAddr = getFirstRefAddr(binding.getReference(),
                  ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME);
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
         final JBossSessionBeanMetaData smd, final ClassLoader cl, final String containerName,
         final String containerGuid, final Advisor advisor)
   {
      JndiReferenceBindingSet bindings = super.createJndiReferenceBindingSet(context, smd, cl, containerName,
            containerGuid, advisor);

      decorateReferencesForClustering(bindings);

      // Store ref to bindings so we can rebind upon topology changes
      BeanClusteringRegistryInfo registryInfo = getBeanClusteringRegistryInfo(containerName);
      registryInfo.bindings = bindings;

      return bindings;
   }

   @Override
   protected ProxyFactory createProxyToProxyFactory(String proxyFactoryKey, String remotingUrl,
         ProxyFactory proxyFactory, ClassLoader cl, JBossEnterpriseBeanMetaData smd)
   {
      InvokerLocator locator = null;
      try
      {
         locator = new InvokerLocator(remotingUrl);
      }
      catch (MalformedURLException mue)
      {
         throw new RuntimeException("Unable to create a remoting proxy for ProxyFactory " + proxyFactoryKey
               + " with remoting url " + remotingUrl, mue);

      }

      ProxyClusteringInfo bci = registry.getBeanClusteringInfo(proxyFactoryKey);

      if (bci == null)
      {
         throw new IllegalStateException("Cannot find " + ProxyClusteringInfo.class.getSimpleName()
               + " for proxyFactoryKey " + proxyFactoryKey);
      }

      String partitionName = bci.getPartitionName();

      assert partitionName != null && !partitionName.trim().equals("") : " Partition name is required, but is not available in ProxyClusteringInfo";

      String lbpClass = bci.getHomeLoadBalancePolicy().getName();

      assert lbpClass != null && !lbpClass.trim().equals("") : LoadBalancePolicy.class.getSimpleName()
            + " class name is required, but is not available in ProxyClusteringInfo";

      LoadBalancePolicy loadBalancePolicy;
      try
      {
         log.debug("Instantiating loadbalancer policy " + lbpClass + " for remote proxyfactory bound at key "
               + proxyFactoryKey);
         loadBalancePolicy = (LoadBalancePolicy) cl.loadClass(lbpClass).newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not load loadbalancer policy " + lbpClass
               + " while creating a proxy to remote proxyfactory", e);
      }

      FamilyWrapper wrapper = bci.getFamilyWrapper();
      FamilyClusterInfo familyClusterInfo = wrapper.get();
      log.debug("Remote proxyfactory for key " + proxyFactoryKey + " will be associated with family name "
            + familyClusterInfo.getFamilyName() + " view id " + familyClusterInfo.getCurrentViewId()
            + " with available targets " + familyClusterInfo.getTargets());

      Class<ProxyFactory>[] interfaces = this.getAllProxyFactoryInterfaces((Class<ProxyFactory>) proxyFactory
            .getClass());
      Interceptor[] interceptors =
      {IsLocalProxyFactoryInterceptor.singleton, ClusterChooserInterceptor.singleton, InvokeRemoteInterceptor.singleton};

      ClusteredPojiProxy handler = new ClusteredPojiProxy(proxyFactoryKey, locator, interceptors, wrapper,
            loadBalancePolicy, partitionName, null);
      // register the handler

      return (ProxyFactory) Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
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
      String proxyFactoryKey = getSingleRequiredRefAddr(ref,
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_PROXY_FACTORY_REGISTRY_KEY);
      ProxyClusteringInfo bci = registry.getBeanClusteringInfo(proxyFactoryKey);

      if (bci == null)
      {
         throw new IllegalStateException("Cannot find " + ProxyClusteringInfo.class.getSimpleName()
               + " for proxyFactoryKey " + proxyFactoryKey);
      }

      RefAddr partitionRef = new StringRefAddr(
            ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_PARTITION_NAME, bci.getPartitionName());
      addRefAddrToReference(ref, partitionRef);
      RefAddr lbpRef = new StringRefAddr(
            ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_PROXY_FACTORY_LOAD_BALANCE_POLICY, bci
                  .getHomeLoadBalancePolicy().getName());
      addRefAddrToReference(ref, lbpRef);
      FamilyClusterInfo fci = bci.getFamilyWrapper().get();
      RefAddr familyNameRef = new StringRefAddr(
            ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME, fci.getFamilyName());
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
         if (ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL.equals(refAddr
               .getType()))
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
         RefAddr targetRef = new StringRefAddr(
               ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL, url);
         addRefAddrToReference(ref, targetRef);
      }
   }

   private void addRefAddrToReference(Reference ref, RefAddr refAddr)
   {
      log.debug("Adding " + RefAddr.class.getSimpleName() + " to " + Reference.class.getSimpleName() + ": Type \""
            + refAddr.getType() + "\", Content \"" + refAddr.getContent() + "\"");
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

   /**
    * Utility method to create an updated proxy for the remote proxyfactory.
    * <br>
    * Note that this method is expected to be used at runtime (i.e. when the EJB container bindings are
    * available in JNDI)  for recreation of an existing proxy to the proxyfactory. This method must 
    * not be used during deployment (when the JNDI bindings are not yet available). For deployment time
    * creation of proxy for the proxyFactory is handled by the other method 
    * {@link JndiClusteredSessionRegistrarBase#createProxyToProxyFactory(String, String, ProxyFactory, ClassLoader, JBossEnterpriseBeanMetaData)}
    * 
    * <p>
    *   Also see {@link JndiClusteredSessionRegistrarBase#clusterTopologyChanged(ProxyClusteringInfo)} method
    *   where this is used. Internally this method uses a combination of information available in JNDI and also
    *   the latest clustering topology (that is available in the passed <code>clusteringInfo</code> parameter),
    *   to (re)create an updated proxy for the proxyfactory.
    * </p>
    *   
    * @param proxyFactoryKey
    * @param reference
    * @param proxyFactory
    * @param clusteringInfo
    * @return
    */
   private ProxyFactory updateProxyForRemoteProxyFactory(String proxyFactoryKey, Reference reference,
         ProxyFactory proxyFactory, ProxyClusteringInfo clusteringInfo)
   {
      // Obtain the URL for invoking upon the Registry
      String url = this.getSingleRequiredRefAddr(reference,
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_INVOKER_LOCATOR_URL);

      // Create an InvokerLocator
      assert url != null && !url.trim().equals("") : InvokerLocator.class.getSimpleName()
            + " URL is required, but is not specified; improperly bound reference in JNDI";
      InvokerLocator locator;
      try
      {
         locator = new InvokerLocator(url);
      }
      catch (MalformedURLException mue)
      {
         throw new RuntimeException("Unable to create a remoting proxy for ProxyFactory " + proxyFactoryKey
               + " with remoting url " + url, mue);
      }

      // get the partition name
      String partitionName = clusteringInfo.getPartitionName();

      assert partitionName != null && !partitionName.trim().equals("") : " Partition name is required, but was not available in ProxyClusteringInfo "
            + clusteringInfo;

      // load balancer policy - Note we are creating a proxyfactory so we use the *home* loadbalance policy
      // and not the loadbalance policy
      Class<? extends LoadBalancePolicy> lbpClass = clusteringInfo.getHomeLoadBalancePolicy();

      assert lbpClass != null : " LoadBalancePolicy is required, but is not available in the ProxyClusteringInfo "
            + clusteringInfo;

      LoadBalancePolicy loadBalancePolicyInstance = null;

      try
      {
         // create a loadbalancer policy instance using the classloader associated with the
         // previous proxy
         loadBalancePolicyInstance = lbpClass.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create loadbalancer policy instance for " + lbpClass
               + " while creating a proxy to remote proxyfactory", e);
      }
      // family wrapper (which contains the latest information of the cluster topology)
      FamilyWrapper wrapper = clusteringInfo.getFamilyWrapper();
      // the interfaces to be exposed by the proxy for the proxyfactory
      Class<?>[] interfaces = this.getAllProxyFactoryInterfaces((Class<ProxyFactory>) proxyFactory.getClass());
      // interceptors to the proxy
      Interceptor[] interceptors =
      {IsLocalProxyFactoryInterceptor.singleton, ClusterChooserInterceptor.singleton, InvokeRemoteInterceptor.singleton};
      // an invocation handler which internally will apply the interceptors and do other magic :)
      ClusteredPojiProxy handler = new ClusteredPojiProxy(proxyFactoryKey, locator, interceptors, wrapper,
            loadBalancePolicyInstance, partitionName, null);

      // finally the proxy for the proxyfactory
      return (ProxyFactory) Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
   }

}
