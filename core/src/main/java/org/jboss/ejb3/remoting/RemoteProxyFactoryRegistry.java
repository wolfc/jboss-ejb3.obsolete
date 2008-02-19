/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.remoting;

import org.jboss.ha.client.loadbalance.LoadBalancePolicy;

import java.util.Map;

/**
 * Registry for all configured Remote Proxy Factory implementations
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class RemoteProxyFactoryRegistry
{
   // Instance Members
   private Map<String, Class<? extends RemoteProxyFactory>> factories;
   
   private Map<String, Class<LoadBalancePolicy>> loadBalancePolicies;
  
   // Accessors / Mutators

   public Map<String, Class<? extends RemoteProxyFactory>> getFactories()
   {
      return factories;
   }

   public void setFactories(Map<String, Class<? extends RemoteProxyFactory>> factories)
   {
      this.factories = factories;
   }
   
   public Map<String, Class<LoadBalancePolicy>> getLoadBalancePolicies()
   {
      return loadBalancePolicies;
   }

   public void setLoadBalancePolicies(Map<String, Class<LoadBalancePolicy>> loadBalancePolicies)
   {
      this.loadBalancePolicies = loadBalancePolicies;
   }

   // Functional Methods

   /**
    * Obtains the Proxy Factory Class with the specified registered name
    * 
    * @param name The registered name of the proxy factory to retrieve
    * @return The Proxy Factory
    */
   public Class<? extends RemoteProxyFactory> getProxyFactoryClass(String name) throws ProxyFactoryNotRegisteredException
   {
      // Obtain cache factory
      Class<? extends RemoteProxyFactory> proxyFactory = this.factories.get(name);

      // Ensure registered
      if (proxyFactory == null)
      {
         throw new ProxyFactoryNotRegisteredException("Remoting Proxy Factory with name " + name
               + " is not registered.");
      }
      
      // Return 
      return proxyFactory;

   }
   
   public Class<LoadBalancePolicy> getLoadBalancePolicy(String name) throws LoadBalancePolicyNotRegisteredException
   {
      // Obtain cache factory
      Class<LoadBalancePolicy> loadBalancePolicy = this.loadBalancePolicies.get(name);

      // Ensure registered
      if (loadBalancePolicy == null)
      {
         throw new LoadBalancePolicyNotRegisteredException("LoadBalancePolicy with name " + name
               + " is not registered.");
      }
      
      // Return 
      return loadBalancePolicy;

   }
}
