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

import java.io.Serializable;

import junit.framework.TestCase;

import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.grouped.SerializationGroup;
import org.jboss.ejb3.cache.impl.FileObjectStore;
import org.jboss.ejb3.cache.impl.SerializationGroupContainer;
import org.jboss.ejb3.cache.impl.SimpleIntegratedObjectStore;
import org.jboss.ejb3.cache.impl.SimplePassivatingCache2;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author Brian Stansberry
 * @version $Revision: 65920 $
 */
public class GroupedPassivatingUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(GroupedPassivatingUnitTestCase.class);
   
   private static void sleep(long micros)
   {
      try
      {
         Thread.sleep(micros);
      }
      catch (InterruptedException e)
      {
         // ignore
      }
   }
   
   public void test1()
   {      
      SerializationGroupContainer container = new SerializationGroupContainer();
      StatefulObjectFactory<SerializationGroup> factory = container;
      PassivationManager<SerializationGroup> passivationManager = container;
      FileObjectStore<SerializationGroup> filestore = new FileObjectStore<SerializationGroup>();
      filestore.setStorageDirectory("./target/tmp/groups");
      filestore.start();
      SimpleIntegratedObjectStore<SerializationGroup> store = new SimpleIntegratedObjectStore<SerializationGroup>(filestore);
      SimplePassivatingCache2<SerializationGroup> groupCache = new SimplePassivatingCache2<SerializationGroup>(factory, passivationManager, store);
      store.setName("PassivationGroupContainer");
      MockBeanContainer container1 = new MockBeanContainer("MockBeanContainer1", 1, groupCache);
      MockBeanContainer container2 = new MockBeanContainer("MockBeanContainer2", 10, groupCache);
      
      try
      {
         groupCache.start();
         container1.start();
         container2.start();
         
         Object shared = new Serializable()
         {
            private static final long serialVersionUID = 1L;
         };
         MockBeanContext firstCtx1;
         MockBeanContext ctx1 = firstCtx1 = container1.getCache().create(null, null);
         Object key1 = ctx1.getId();
         ctx1.shared = shared;
         MockBeanContext ctx2 = container2.getCache().create(null, null);
         Object key2 = ctx2.getId();
         ctx2.shared = shared;
         
         // TODO: how will passivation groups be created?
         SerializationGroup group = groupCache.create(null, null);
         container1.getCache().setGroup(ctx1, group);
         container2.getCache().setGroup(ctx2, group);
         // TODO: currently we need to release the group
         // BES -- not any more
   //      groupCache.release(group);
         
         container1.getCache().release(ctx1);
         container2.getCache().release(ctx2);
         
         sleep(4000);
         
         assertEquals("ctx1 should have been passivated", 1, container1.passivations);
         assertEquals("ctx2 should have been passivated", 1, container2.passivations);
         
         ctx2 = container2.getCache().get(key2);
         
         log.info("ctx2 = " + ctx2);
         assertNotNull(ctx2);
         
         ctx1 = container1.getCache().get(key1);
         
         log.info("ctx1 = " + ctx1);
         
         assertTrue("ctx1 must be different than firstCtx1 (else no passivation has taken place)", ctx1 != firstCtx1);
         
         assertEquals(ctx1.shared, ctx2.shared);
      }
      finally
      {
         container1.stop();
         container2.stop();
         groupCache.stop();
      }
   }
}
