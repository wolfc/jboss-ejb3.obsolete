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
package org.jboss.ejb3.stateful;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.jboss.aop.AspectManager;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.aspects.remoting.Remoting;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.defaults.ClusteredDefaults;
import org.jboss.ejb3.remoting.LoadBalancePolicyNotRegisteredException;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.ejb3.remoting.RemoteProxyFactoryRegistry;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ha.client.loadbalance.FirstAvailable;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.remoting.InvokerLocator;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 *
 * @version $Revision$
 */
public class StatefulClusterProxyFactory extends BaseStatefulProxyFactory 
   implements RemoteProxyFactory, DistributedReplicantManager.ReplicantListener
{
   private static final Logger log = Logger.getLogger(StatefulClusterProxyFactory.class);
   
//   public static final String FACTORY_ATTRIBUTE = ",element=ProxyFactory,partition=";
   
   private RemoteBinding binding;
   private Clustered clustered;
   private InvokerLocator locator;
   private DistributedReplicantManager drm;
   private HATarget hatarget;
   private String proxyFamilyName;
   private LoadBalancePolicy lbPolicy;
   private FamilyWrapper wrapper;

   public StatefulClusterProxyFactory(SessionContainer container, RemoteBinding binding, Clustered clustered)
   {
      super(container, binding.jndiBinding());
      
      assert clustered != null : "clustered is null";
      
      this.binding = binding;
      this.clustered = clustered;
   }

   protected Class[] getInterfaces()
   {
      Class[] remoteInterfaces = ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(getContainer());
      Class[] interfaces = new Class[remoteInterfaces.length + 1];
      System.arraycopy(remoteInterfaces, 0, interfaces, 0, remoteInterfaces.length);
      interfaces[remoteInterfaces.length] = JBossProxy.class;
      return interfaces;
   }

   public void start() throws Exception
   {
      String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(binding);
      locator = new InvokerLocator(clientBindUrl);
      String partitionName = ((SessionContainer) getContainer()).getPartitionName();
      proxyFamilyName = ((SessionContainer) getContainer()).getDeploymentQualifiedName() + locator.getProtocol() + partitionName;
      HAPartition partition = (HAPartition) getContainer().getInitialContext().lookup("/HAPartition/" + partitionName);
      hatarget = new HATarget(partition, proxyFamilyName, locator, HATarget.ENABLE_INVOCATIONS);
      ClusteringTargetsRepository.initTarget(proxyFamilyName, hatarget.getReplicants());
      SessionContainer container = (SessionContainer) getContainer();
      container.getClusterFamilies().put(proxyFamilyName, hatarget);
      
      if (clustered.loadBalancePolicy() == null || clustered.loadBalancePolicy().equals(ClusteredDefaults.LOAD_BALANCE_POLICY_DEFAULT))
      {
         lbPolicy = new FirstAvailable();
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
      
      Class[] interfaces = {ProxyFactory.class};
      String targetId = getTargetId();
      Object factoryProxy = Remoting.createPojiProxy(targetId, interfaces, ProxyFactoryHelper.getClientBindUrl(binding));
      try
      {
         Util.rebind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME, factoryProxy);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful cluster proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }
      assert !Dispatcher.singleton.isRegistered(targetId) : targetId + " is already registered";
      Dispatcher.singleton.registerTarget(targetId, this);

   }

   public Object createProxy()
   {
      String stackName = "ClusteredStatefulSessionClientInterceptors";
      if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      String partitionName = ((SessionContainer) getContainer()).getPartitionName();
      return constructProxy(new StatefulClusteredProxy(getContainer(), stack.createInterceptors(getContainer().getAdvisor(), null), 
            wrapper, lbPolicy, partitionName));
   }

   public Object createProxy(Object id)
   {
      throw new RuntimeException("NYI");
   }
   
   public void stop() throws Exception
   {
      Dispatcher.singleton.unregisterTarget(getTargetId());
      hatarget.destroy();
      drm.unregisterListener(proxyFamilyName, this);
      ((SessionContainer) getContainer()).getClusterFamilies().remove(proxyFamilyName);
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      super.stop();
   }
   
   protected StatefulHandleImpl getHandle()
   {
      StatefulHandleImpl handle = new StatefulHandleImpl();
      RemoteBinding remoteBinding = (RemoteBinding) getContainer().resolveAnnotation(RemoteBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding();
 
      return handle;
   }
   
   /**
    * @return unique name for this proxy factory
    */
   protected String getTargetId()
   {
      assert jndiName != null : "jndiName is null";      
      String partition = ((SessionContainer) getContainer()).getPartitionName();
      return jndiName + PROXY_FACTORY_NAME + "@" + partition;
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
      }
      catch (Exception e)
      {
         log.error(e);
      }
   }

}
