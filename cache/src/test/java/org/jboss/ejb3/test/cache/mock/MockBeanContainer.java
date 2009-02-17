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

package org.jboss.ejb3.test.cache.mock;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulCacheFactory;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.logging.Logger;

/**
 * @author Brian Stansberry
 *
 */
public class MockBeanContainer
   implements StatefulObjectFactory<MockBeanContext>, PassivationManager<MockBeanContext>
{
   private static final Logger log = Logger.getLogger(MockBeanContainer.class);   
   
   private final String containerName;
   private final String cacheFactoryName;
   private final StatefulCacheFactoryRegistry<MockBeanContext> cacheFactoryRegistry;
   private final MockCacheConfig cacheConfig;
   private final MockStatefulObjectFactory objectFactory;
   private final MockPassivationManager passivationManager;
   
   private Cache<MockBeanContext> cache;
   private Set<MockBeanContainer> children;
   
   private String xpcName;
   
   public MockBeanContainer(String containerName, String cacheFactoryName,
         StatefulCacheFactoryRegistry<MockBeanContext> cacheFactoryRegistry,
         MockCacheConfig cacheConfig)
   {
      this.containerName = containerName;
      this.cacheFactoryName = cacheFactoryName;
      this.cacheFactoryRegistry = cacheFactoryRegistry;
      this.cacheConfig = cacheConfig;
      
      this.objectFactory = new MockStatefulObjectFactory(this);
      this.passivationManager = new MockPassivationManager();
      this.children = new HashSet<MockBeanContainer>();
      
      MockRegistry.put(this.containerName, this);
   }
   
   public void start() throws Exception
   {      
      log.debug("Starting container " + containerName);
      
      StatefulCacheFactory<MockBeanContext> cacheFactory = cacheFactoryRegistry.getCacheFactory(cacheFactoryName);
      cache = cacheFactory.createCache(containerName, this, this, cacheConfig);
      cache.start();
      
      log.debug("Started container " + containerName);
   }
   
   public void stop()
   {      
      log.debug("Stopping container " + containerName);     
      
      cache.stop();
      MockRegistry.remove(containerName);
      
      log.debug("Stopped container " + containerName);
   }
   
   public String getName()
   {
      return containerName;
   }
   
   public Cache<MockBeanContext> getCache()
   {
      return cache;
   }
   
   public Set<MockBeanContainer> getChildren()
   {
      return children;
   }
   
   public void addChild(MockBeanContainer child)
   {
      children.add(child);
   }
   
   public boolean hasChild(MockBeanContainer child)
   {
      return children.contains(child);
   }

   public String getXPCName()
   {
      return xpcName;
   }
   
   public void setXPCName(String name)
   {
      this.xpcName = name;
   }
   
   public StatefulObjectFactory<MockBeanContext> getStatefulObjectFactory()
   {
      return objectFactory;
   }

   public PassivationManager<MockBeanContext> getPassivationManager()
   {
      return passivationManager;
   }
   
   // --------------------------------------------------  StatefulObjectFactory


   public MockBeanContext create(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      return objectFactory.create(initTypes, initValues, sharedState);
   }

   public void destroy(MockBeanContext obj)
   {
      objectFactory.destroy(obj);      
   }
   
   // --------------------------------------------------  PassivationManager

   public void postActivate(MockBeanContext ctx)
   {
      passivationManager.postActivate(ctx);
   }

   public void postReplicate(MockBeanContext ctx)
   {
      passivationManager.postReplicate(ctx); 
   }

   public void prePassivate(MockBeanContext ctx)
   {
      passivationManager.prePassivate(ctx);
   }

   public void preReplicate(MockBeanContext ctx)
   {
      passivationManager.preReplicate(ctx);
   }
   
   
   
}
