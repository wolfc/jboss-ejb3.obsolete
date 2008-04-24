/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.proxy.factory.stateless;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.defaults.ClusteredDefaults;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactory;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactoryRegistry;
import org.jboss.ejb3.proxy.handler.stateless.StatelessClusteredInvocationHandler;
import org.jboss.ejb3.remoting.LoadBalancePolicyNotRegisteredException;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.client.loadbalance.RandomRobin;
import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.HAPartitionLocator;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessClusterProxyFactory extends BaseStatelessRemoteProxyFactory  
   implements RemoteProxyFactory, DistributedReplicantManager.ReplicantListener
{
   private static final Logger log = Logger.getLogger(StatelessClusterProxyFactory.class);

   private static final String STACK_NAME_CLUSTERED_STATELESS_SESSION_CLIENT_INTERCEPTORS = "ClusteredStatelessSessionClientInterceptors";
   
   private RemoteBinding binding;
   private Clustered clustered;
   private InvokerLocator locator;
   private DistributedReplicantManager drm;
   private HATarget hatarget;
   private String proxyFamilyName;
   private LoadBalancePolicy lbPolicy;
   private FamilyWrapper wrapper;
   private Object proxy;

   public StatelessClusterProxyFactory(SessionSpecContainer container, RemoteBinding binding, Clustered clustered)
   {
      super(container, binding);
      
      assert clustered != null : "clustered is null";
      
      this.binding = binding;
      this.clustered = clustered;
   }

   public void start() throws Exception
   {
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(binding);
      locator = new InvokerLocator(clientBindUrl);
      StatelessContainer container = (StatelessContainer) getContainer();
      String partitionName = container.getPartitionName();
      proxyFamilyName = container.getDeploymentQualifiedName() + locator.getProtocol() + partitionName;
      HAPartition partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(partitionName, container.getInitialContextProperties());
      hatarget = new HATarget(partition, proxyFamilyName, locator, HATarget.ENABLE_INVOCATIONS);
      ClusteringTargetsRepository.initTarget(proxyFamilyName, hatarget.getReplicants());
      
      container.getClusterFamilies().put(proxyFamilyName, hatarget);
      
      if (clustered.loadBalancePolicy() == null || clustered.loadBalancePolicy().equals(ClusteredDefaults.LOAD_BALANCE_POLICY_DEFAULT))
      {
         lbPolicy = new RandomRobin();
      }
      else
      {
         String policyClass = clustered.loadBalancePolicy();
         try
         {
            RemoteProxyFactoryRegistry registry = container.getDeployment().getRemoteProxyFactoryRegistry();
            Class<LoadBalancePolicy> policy = registry.getLoadBalancePolicy(policyClass);
            policyClass = policy.getName();
         }
         catch (LoadBalancePolicyNotRegisteredException e){}
         
         lbPolicy = (LoadBalancePolicy)Thread.currentThread().getContextClassLoader().loadClass(policyClass)
               .newInstance();
      }
      wrapper = new FamilyWrapper(proxyFamilyName, hatarget.getReplicants());
      
      this.drm = partition.getDistributedReplicantManager();
      drm.registerListener(proxyFamilyName, this);
      
      super.start();
   }

   public void stop() throws Exception
   {
      super.stop();
      proxy = null;
      hatarget.destroy();
      drm.unregisterListener(proxyFamilyName, this);
      ((StatelessContainer) getContainer()).getClusterFamilies().remove(proxyFamilyName);
   }

   public Object createProxyBusiness(String businessInterfaceClassName)
   {
      Object containerId = getContainer().getObjectName().getCanonicalName();
      String stackName = this.getStackNameInterceptors();
      if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      /*
      Object[] args = {new StatelessClusteredProxy(containerId, stack.createInterceptors((Advisor) container, null), wrapper, lbPolicy)};
      return proxyConstructor.newInstance(args);
      */
      String partitionName = ((StatelessContainer) getContainer()).getPartitionName();

      proxy = constructProxy(new StatelessClusteredInvocationHandler(getContainer(), stack.createInterceptors(
            getContainer().getAdvisor(), null), wrapper, lbPolicy, partitionName, businessInterfaceClassName),
            SpecificationInterfaceType.EJB30_BUSINESS);
      return proxy;
   }
   
   /**
    * Whether or not to bind the home and business interfaces together
    * 
    * @return
    */
   @Override
   protected boolean bindHomeAndBusinessTogether(){
      // Not Supported
      return false;
   }
   
   public synchronized void replicantsChanged (String key, 
         List newReplicants, 
         int newReplicantsViewId,
         boolean merge)
   {
      try
      {
         // Update the FamilyClusterInfo with the new targets
         ArrayList targets = new ArrayList(newReplicants);
         wrapper.get().updateClusterInfo(targets, newReplicantsViewId);
         
         // Rebind the proxy as the old one has been serialized
         if (proxy != null)
            bindProxy(proxy);
      }
      catch (Exception e)
      {
         log.error(e);
      }
   }
   
   /**
    * Returns the interface type for Home
    * 
    * @param container
    * @return
    */
   @Override
   protected Class<?> getHomeType()
   {
      // Not Supported
      return null;
   }

   @Override
   String getStackNameInterceptors()
   {
      return StatelessClusterProxyFactory.STACK_NAME_CLUSTERED_STATELESS_SESSION_CLIENT_INTERCEPTORS;
   }
}
