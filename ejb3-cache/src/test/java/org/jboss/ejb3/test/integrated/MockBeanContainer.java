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
package org.jboss.ejb3.test.integrated;

import org.jboss.ejb3.cache.PassivatingCache;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.grouped.GroupedPassivatingCache;
import org.jboss.ejb3.cache.grouped.SerializationGroup;
import org.jboss.ejb3.cache.impl.FileObjectStore;
import org.jboss.ejb3.cache.impl.GroupedPassivatingCacheImpl2;
import org.jboss.ejb3.cache.impl.SerializationGroupMemberImpl;
import org.jboss.ejb3.cache.impl.SimpleIntegratedObjectStore;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author Brian Stansberry
 * @version $Revision: 65920 $
 */
public class MockBeanContainer implements StatefulObjectFactory<MockBeanContext>, PassivationManager<MockBeanContext>
{
   private static final Logger log = Logger.getLogger(MockBeanContainer.class);
   
   protected int activations = 0;
   protected int passivations = 0;
   
   private GroupedPassivatingCache<MockBeanContext> cache;
   
   public MockBeanContainer(String name, int sessionTimeout, PassivatingCache<SerializationGroup> groupCache)
   {
      FileObjectStore<SerializationGroupMemberImpl<MockBeanContext>> filestore = new FileObjectStore<SerializationGroupMemberImpl<MockBeanContext>>();
      filestore.setStorageDirectory("./target/tmp/" + name);
      filestore.start();
      SimpleIntegratedObjectStore<SerializationGroupMemberImpl<MockBeanContext>> store = 
         new SimpleIntegratedObjectStore<SerializationGroupMemberImpl<MockBeanContext>>(filestore);
      store.setName(name);
      store.setIdleTimeSeconds(sessionTimeout);
      store.setInterval(1);
      this.cache = new GroupedPassivatingCacheImpl2<MockBeanContext>(this, this, store, groupCache);
   }
   
   public MockBeanContext create(Class<?>[] initTypes, Object[] initValues)
   {
      return new MockBeanContext();
   }
   
   public GroupedPassivatingCache<MockBeanContext> getCache()
   {
      return cache;
   }
   
   public void destroy(MockBeanContext obj)
   {
   }

   public void postActivate(MockBeanContext obj)
   {
      if(obj == null) throw new IllegalArgumentException("obj is null");
      
      log.info("postActivate " + obj);
      activations++;
      synchronized(this)
      {
         notifyAll();
      }
   }

   public void prePassivate(MockBeanContext obj)
   {
      if(obj == null) throw new IllegalArgumentException("obj is null");
      
      log.info("prePassivate " + obj);
      passivations++;
      synchronized(this)
      {
         notifyAll();
      }
   }
   public boolean isClustered()
   {
      // TODO Auto-generated method stub
      return false;
}
   public void postReplicate(MockBeanContext obj)
   {
      throw new UnsupportedOperationException("Clustering not supported");      
   }

   public void preReplicate(MockBeanContext obj)
   {
      throw new UnsupportedOperationException("Clustering not supported"); 
   }
   
   public void start()
   {
      cache.start();
   }
   
   public void stop()
   {
      cache.stop();
   }
   
}
