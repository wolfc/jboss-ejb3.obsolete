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
package org.jboss.ejb3.proxy.factory.stateful;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.RemoteHome;
import javax.naming.NamingException;

import org.jboss.aop.AspectManager;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.ClusterChooserInterceptor;
import org.jboss.aspects.remoting.ClusteredPojiProxy;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.defaults.ClusteredDefaults;
import org.jboss.ejb3.proxy.ProxyFactory;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactory;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactoryRegistry;
import org.jboss.ejb3.proxy.handler.stateful.StatefulClusteredInvocationHandler;
import org.jboss.ejb3.remoting.ClusteredIsLocalInterceptor;
import org.jboss.ejb3.remoting.LoadBalancePolicyNotRegisteredException;
import org.jboss.ejb3.session.ProxyAccessType;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.client.loadbalance.RoundRobin;
import org.jboss.ha.client.loadbalance.aop.FirstAvailable;
import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.HAPartitionLocator;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.util.naming.Util;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 *
 * @version $Revision$
 */
public class StatefulClusterProxyFactory extends BaseStatefulRemoteProxyFactory 
   implements RemoteProxyFactory, DistributedReplicantManager.ReplicantListener
{
   private static final Logger log = Logger.getLogger(StatefulClusterProxyFactory.class);
   
   private static String STACK_NAME_CLUSTERED_STATEFUL_SESSION_CLIENT_INTERCEPTORS = "ClusteredStatefulSessionClientInterceptors";
   
   private Clustered clustered;
   private DistributedReplicantManager drm;
   private HATarget hatarget;
   private String proxyFamilyName;
   private String partitionName;
   private HAPartition partition;
   private LoadBalancePolicy lbPolicy;
   private FamilyWrapper wrapper;

   public StatefulClusterProxyFactory(SessionSpecContainer container, RemoteBinding binding, Clustered clustered)
   {
      super(container, binding);
      
      assert clustered != null : "clustered is null";
      
      this.clustered = clustered;
   }
   
   /**
    * Returns the interface type for Home
    * 
    * @return
    */
   @Override
   protected Class<?> getHomeType()
   {
      // Not Supported
      return null;
   }
   
   /**
    * Defines the access type for this Proxies created by this Factory
    * 
    * @return
    */
   @Override
   protected ProxyAccessType getProxyAccessType(){
      return ProxyAccessType.REMOTE;
   }
   
   /**
    * Whether or not to bind the home and business interfaces together
    * 
    * @return
    */
   @Override
   protected boolean bindHomeAndBusinessTogether()
   {
      // Not Supported
      return false;
   }
   
   protected void validateEjb21Views()
   { 
      // Obtain Container
      SessionContainer container = this.getContainer();
      
      // Obtain @RemoteHome
      RemoteHome remoteHome = container.getAnnotation(RemoteHome.class);

      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.validateEjb21Views(remoteHome == null ? null : remoteHome.value(), ProxyFactoryHelper
            .getRemoteInterfaces(container));

   }

   public void start() throws Exception
   {
      this.init();
      
      InvokerLocator locator = this.getLocator();
      SessionContainer container = this.getContainer();
      partitionName = container.getPartitionName();
      proxyFamilyName = container.getDeploymentQualifiedName() + locator.getProtocol() + partitionName;
      partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(partitionName, container.getInitialContextProperties());
      drm = partition.getDistributedReplicantManager();
      hatarget = new HATarget(partition, proxyFamilyName, locator, HATarget.ENABLE_INVOCATIONS);
      ClusteringTargetsRepository.initTarget(proxyFamilyName, hatarget.getReplicants());
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
      
      drm.registerListener(proxyFamilyName, this);
      
      super.start();
      
      // Set up the proxy to ourself. Needs to be clustered so it can load 
      // balance requests (EJBTHREE-1375). We use the home load balance policy.
      
      LoadBalancePolicy factoryLBP = null;
      if (clustered.homeLoadBalancePolicy() == null || clustered.homeLoadBalancePolicy().equals(ClusteredDefaults.LOAD_BALANCE_POLICY_DEFAULT))
      {
         factoryLBP = new RoundRobin();
      }
      else
      {
         String policyClass = clustered.homeLoadBalancePolicy();
         try
         {
            RemoteProxyFactoryRegistry registry = container.getDeployment().getRemoteProxyFactoryRegistry();
            Class<LoadBalancePolicy> policy = registry.getLoadBalancePolicy(policyClass);
            policyClass = policy.getName();
         }
         catch (LoadBalancePolicyNotRegisteredException e){}
         
         factoryLBP = (LoadBalancePolicy)Thread.currentThread().getContextClassLoader().loadClass(policyClass)
               .newInstance();
      }
      
      Class<?>[] interfaces = {ProxyFactory.class};
      String targetId = getTargetId();
      Interceptor[] interceptors = { new ClusteredIsLocalInterceptor(), 
                                     new ClusterChooserInterceptor(), 
                                     InvokeRemoteInterceptor.singleton
      };
      
      // We can use the same FamilyWrapper as we use for the bean
      ClusteredPojiProxy proxy = new ClusteredPojiProxy(targetId, locator, interceptors, wrapper, 
                                                        factoryLBP, partitionName, null);
      Object factoryProxy =  Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, proxy);
      String jndiName = this.getJndiName();
      try
      {
         Util.rebind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME, factoryProxy);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful cluster proxy with ejb name "
               + getContainer().getEjbName() + " into JNDI under jndiName: "
               + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }
      assert !Dispatcher.singleton.isRegistered(targetId) : targetId + " is already registered";
      Dispatcher.singleton.registerTarget(targetId, this);

   }

   @Override
   String getStackNameInterceptors()
   {
      return StatefulClusterProxyFactory.STACK_NAME_CLUSTERED_STATEFUL_SESSION_CLIENT_INTERCEPTORS;
   }
   
   @Override
   protected Object createProxy(Object id,SpecificationInterfaceType type, String businessInterfaceType)
   {
      String stackName = this.getStackNameInterceptors();
      RemoteBinding binding = this.getBinding();
      if (binding.interceptorStack() != null && !binding.interceptorStack().trim().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
      StatefulClusteredInvocationHandler handler = new StatefulClusteredInvocationHandler(getContainer(), stack.createInterceptors(getContainer()
            .getAdvisor(), null), this.wrapper, this.lbPolicy, partitionName, getLocator(), id, businessInterfaceType);
      
      if(type.equals(SpecificationInterfaceType.EJB21))
      {
         return this.constructEjb21Proxy(handler);
      }
      else
      {
         return this.constructProxyBusiness(handler);
      }
   }
   
   public void stop() throws Exception
   {
      Dispatcher.singleton.unregisterTarget(getTargetId());
      hatarget.destroy();
      drm.unregisterListener(proxyFamilyName, this);
      this.getContainer().getClusterFamilies().remove(proxyFamilyName);
      String jndiName = this.getJndiName();
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      super.stop();
   }

   /**
    * @return unique name for this proxy factory
    */
   protected String getTargetId()
   {
      String jndiName = this.getJndiName();
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
