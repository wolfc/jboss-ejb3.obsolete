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
package org.jboss.ejb3.core.test.common;

import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.jacc.PolicyConfiguration;
import javax.transaction.TransactionManager;

import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.Ejb3CacheFactory;
import org.jboss.ejb3.cache.StatefulCacheFactory;
import org.jboss.ejb3.cache.impl.factory.GroupAwareCacheFactory;
import org.jboss.ejb3.cache.impl.factory.NonClusteredBackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.impl.factory.NonPassivatingCacheFactory;
import org.jboss.ejb3.cache.spi.BackingCacheEntryStoreSource;
import org.jboss.ejb3.cache.spi.impl.AbstractStatefulCacheFactory;
import org.jboss.ejb3.cache.spi.impl.JndiTransactionSynchronizationRegistrySource;
import org.jboss.ejb3.cache.spi.impl.SynchronizationCoordinatorImpl;
import org.jboss.ejb3.cache.spi.impl.TransactionSynchronizationRegistrySource;
//import org.jboss.ejb3.cache.NoPassivationCacheFactory;
//import org.jboss.ejb3.cache.simple.SimpleStatefulCacheFactory;
//import org.jboss.ejb3.cache.tree.StatefulTreeCacheFactory;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.common.resolvers.plugins.FirstMatchEjbReferenceResolver;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.pool.PoolFactory;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.ejb3.pool.StrictMaxPoolFactory;
import org.jboss.ejb3.pool.ThreadlocalPoolFactory;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.ejb3.test.cache.distributed.MockBackingCacheEntryStoreSource;
import org.jboss.ejb3.test.cache.distributed.UnmarshallingMap;
import org.jboss.ejb3.test.cache.mock.tm.MockTransactionManager;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockEjb3Deployment extends Ejb3Deployment
{
   public MockEjb3Deployment(DeploymentUnit unit)
   {
      // TODO This should be replaced w/ a MockDeploymentUnit when completed, 
      // to support nested deployments, @see ejb3-test MockDeploymentUnit
      this(unit, new AbstractDeploymentUnit());
   }

   public MockEjb3Deployment(DeploymentUnit unit, org.jboss.deployers.structure.spi.DeploymentUnit du)
   {
      this(unit, du, null);
   }

   public MockEjb3Deployment(DeploymentUnit unit, org.jboss.deployers.structure.spi.DeploymentUnit du,
         DeploymentScope scope)
   {
      super(du, unit, scope, null);

      // Replace the scope if we haven't been given one (hacky, but there's
      // a chicken/egg thing here as MockDeploymentScope requires the instance
      // currently under construction, and DeploymentScope is @Deprecated anyway
      // in favor of a pluggable resolver architecture, so this is a stop-gap
      if (scope == null)
      {
         EjbReferenceResolver resolver = new FirstMatchEjbReferenceResolver();
         this.deploymentScope = new MockDeploymentScope(this, du, resolver);
      }

      PoolFactoryRegistry poolRegistry = new PoolFactoryRegistry();
      HashMap<String, Class<? extends PoolFactory>> poolFactories = new HashMap<String, Class<? extends PoolFactory>>();
      poolFactories.put("ThreadlocalPool", ThreadlocalPoolFactory.class);
      poolFactories.put("StrictMaxPool", StrictMaxPoolFactory.class);
      poolRegistry.setFactories(poolFactories);
      setPoolFactoryRegistry(poolRegistry);
      CacheFactoryRegistry cacheRegistry = new CacheFactoryRegistry();
      HashMap<String, StatefulCacheFactory<StatefulBeanContext>> cacheFactories = new HashMap<String, StatefulCacheFactory<StatefulBeanContext>>();
      cacheFactories.put("NoPassivationCache", buildNonPassivatingCacheFactory());
      cacheFactories.put("SimpleStatefulCache", buildSimpleCacheFactory());
      cacheFactories.put("StatefulTreeCache", buildDistributedCacheFactory());
      cacheRegistry.setFactories(cacheFactories);
      setCacheFactoryRegistry(cacheRegistry);
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
   
   private StatefulCacheFactory<StatefulBeanContext> buildNonPassivatingCacheFactory()
   {
      AbstractStatefulCacheFactory<StatefulBeanContext> factory = new NonPassivatingCacheFactory<StatefulBeanContext>();
      configureStatefulCacheFactory(factory);
      return factory;
   }
   
   private StatefulCacheFactory<StatefulBeanContext> buildSimpleCacheFactory()
   {
      NonClusteredBackingCacheEntryStoreSource<StatefulBeanContext> source = 
         new NonClusteredBackingCacheEntryStoreSource<StatefulBeanContext>();
      AbstractStatefulCacheFactory<StatefulBeanContext> factory =  new GroupAwareCacheFactory<StatefulBeanContext>(source);
      configureStatefulCacheFactory(factory);
      return factory;
   }
   
   private StatefulCacheFactory<StatefulBeanContext> buildDistributedCacheFactory()
   {
     BackingCacheEntryStoreSource<StatefulBeanContext> storeSource = getDistributedStoreSource();
     AbstractStatefulCacheFactory<StatefulBeanContext> factory =  new GroupAwareCacheFactory<StatefulBeanContext>(storeSource);
     configureStatefulCacheFactory(factory);
     return factory;
   }
   
   private void configureStatefulCacheFactory(AbstractStatefulCacheFactory<StatefulBeanContext> factory)
   {
      TransactionManager tm = null;
      try
      {
         tm = (TransactionManager) Ejb3RegistrarLocator.locateRegistrar().lookup("RealTransactionManager");
      }
      catch (NotBoundException e)
      {
         // fall back on JNDI
         try
         {
            InitialContext ctx = new InitialContext();
            tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
         }
         catch (NamingException e1)
         {
            throw new RuntimeException("cannot resolve transaction manager", e1);
         }         
      }
      
      factory.setTransactionManager(tm);
      
      //factory.setPassivationExpirationCoordinator(coordinator);
      // Process passivation/expiration as quickly as possible so tests run fast
      factory.setDefaultPassivationExpirationInterval(10);
      SynchronizationCoordinatorImpl sci = new SynchronizationCoordinatorImpl();
      if (tm instanceof TransactionSynchronizationRegistrySource)
      {
         sci.setTransactionSynchronizationRegistrySource((TransactionSynchronizationRegistrySource) tm);
      }
      else
      {
         sci.setTransactionSynchronizationRegistrySource(new JndiTransactionSynchronizationRegistrySource());
      }
      factory.setSynchronizationCoordinator(sci);      
      factory.start();
   }

   private BackingCacheEntryStoreSource<StatefulBeanContext> getDistributedStoreSource()
   {
      return new MockBackingCacheEntryStoreSource<StatefulBeanContext>(new UnmarshallingMap(), new UnmarshallingMap());
   }

}
