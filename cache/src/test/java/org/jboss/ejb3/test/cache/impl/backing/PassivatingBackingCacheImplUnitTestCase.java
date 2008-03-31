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
package org.jboss.ejb3.test.cache.impl.backing;

import java.util.HashMap;

import org.jboss.ejb3.cache.impl.TransactionalCache;
import org.jboss.ejb3.cache.impl.backing.PassivatingBackingCacheImpl;
import org.jboss.ejb3.cache.impl.backing.SerializationGroupMemberImpl;
import org.jboss.ejb3.test.cache.integrated.Ejb3CacheTestCaseBase;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockEjb3System;
import org.jboss.ejb3.test.cache.mock.MockXPC;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * 
 * @version $Revision: 65339 $
 */
public class PassivatingBackingCacheImplUnitTestCase extends Ejb3CacheTestCaseBase
{   
   private static final Logger log = Logger.getLogger(PassivatingBackingCacheImplUnitTestCase.class);
   
   /**
    * Peek of an active object should not prevent its passivation.
    */
   @SuppressWarnings("unchecked")
   public void testPassivatePeeked() throws Exception
   {
      log.info("testPassivatePeeked()");
      
      MockEjb3System system = new MockEjb3System(false, CacheType.SIMPLE);
      MockXPC sharedXPC = new MockXPC();
      MockCacheConfig config = new MockCacheConfig();
      config.setIdleTimeoutSeconds(100);
      MockBeanContainer container = system.deployBeanContainer("MockBeanContainer1", null, CacheType.SIMPLE, config, sharedXPC.getName());
      TransactionalCache cache = (TransactionalCache) container.getCache();
      PassivatingBackingCacheImpl<MockBeanContext, SerializationGroupMemberImpl<MockBeanContext>> backingCache = (PassivatingBackingCacheImpl<MockBeanContext, SerializationGroupMemberImpl<MockBeanContext>>) cache.getBackingCache();
      SerializationGroupMemberImpl<MockBeanContext> obj = backingCache.create(null, null, new HashMap<Object, Object>());
      Object key = obj.getId();
      
      backingCache.peek(key);
      
      try
      {
         backingCache.passivate(key);
      }
      catch (IllegalStateException bad)
      {
         fail("Should be able to passivate entry that has been peeked");
      }
      finally
      {
         backingCache.remove(key);
      }         
   }  
   
   /**
    * Get of an active object should prevent its passivation.
    */
   @SuppressWarnings("unchecked")
   public void testPassivateActive() throws Exception
   {
      log.info("testPassivateActive()");
      
      MockEjb3System system = new MockEjb3System(false, CacheType.SIMPLE);
      MockXPC sharedXPC = new MockXPC();
      MockCacheConfig config = new MockCacheConfig();
      config.setIdleTimeoutSeconds(100);
      MockBeanContainer container = system.deployBeanContainer("MockBeanContainer1", null, CacheType.SIMPLE, config, sharedXPC.getName());
      TransactionalCache cache = (TransactionalCache) container.getCache();
      PassivatingBackingCacheImpl<MockBeanContext, SerializationGroupMemberImpl<MockBeanContext>> backingCache = (PassivatingBackingCacheImpl<MockBeanContext, SerializationGroupMemberImpl<MockBeanContext>>) cache.getBackingCache();
      SerializationGroupMemberImpl<MockBeanContext> obj = backingCache.create(null, null, new HashMap<Object, Object>());
      Object key = obj.getId();
      
      backingCache.get(key);
      
      try
      {
         backingCache.passivate(key);
         fail("Should not be able to passivate entry that is active");
      }
      catch (IllegalStateException bad)
      {
         backingCache.release(key);
         backingCache.passivate(key);
      }
      finally
      {
         backingCache.remove(key);
      }         
   }
}
