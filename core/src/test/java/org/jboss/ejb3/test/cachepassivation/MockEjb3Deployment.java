/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.cachepassivation;

import java.util.HashMap;

import javax.security.jacc.PolicyConfiguration;

import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.Ejb3CacheFactory;
import org.jboss.ejb3.cache.NoPassivationCacheFactory;
import org.jboss.ejb3.cache.simple.SimpleStatefulCacheFactory;
import org.jboss.ejb3.cache.tree.StatefulTreeCacheFactory;
import org.jboss.ejb3.deployers.Ejb3Deployer;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.pool.PoolFactory;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.ejb3.pool.StrictMaxPoolFactory;
import org.jboss.ejb3.pool.ThreadlocalPoolFactory;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockEjb3Deployment extends Ejb3Deployment
{
   public MockEjb3Deployment(DeploymentUnit unit, DeploymentScope deploymentScope)
   {
      super(unit, deploymentScope, null, null, new Ejb3Deployer());
      PoolFactoryRegistry poolRegistry = new PoolFactoryRegistry();
      HashMap<String, Class<? extends PoolFactory>> poolFactories = new HashMap<String, Class<? extends PoolFactory>>();
      poolFactories.put("ThreadlocalPool", ThreadlocalPoolFactory.class);
      poolFactories.put("StrictMaxPool", StrictMaxPoolFactory.class);
      poolRegistry.setFactories(poolFactories);
      deployer.setPoolFactoryRegistry(poolRegistry);
      CacheFactoryRegistry cacheRegistry = new CacheFactoryRegistry();
      HashMap<String, Class<? extends Ejb3CacheFactory>> cacheFactories = new HashMap<String, Class<? extends Ejb3CacheFactory>>();
      cacheFactories.put("NoPassivationCache", NoPassivationCacheFactory.class);
      cacheFactories.put("SimpleStatefulCache", SimpleStatefulCacheFactory.class);
      cacheFactories.put("StatefulTreeCache", StatefulTreeCacheFactory.class);
      cacheRegistry.setFactories(cacheFactories);
      deployer.setCacheFactoryRegistry(cacheRegistry);
   }

   @Override
   public DependencyPolicy createDependencyPolicy(JavaEEComponent component)
   {
      return new JBoss5DependencyPolicy(component);
   }

   @Override
   protected PolicyConfiguration createPolicyConfiguration() throws Exception
   {
      throw new RuntimeException("mock");
   }

   @Override
   protected void putJaccInService(PolicyConfiguration pc, DeploymentUnit unit)
   {
      throw new RuntimeException("mock");
   }

}
