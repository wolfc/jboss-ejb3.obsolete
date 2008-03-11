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

import junit.framework.TestCase;

import org.jboss.ejb3.cache.impl.TransactionalCache;
import org.jboss.ejb3.cache.impl.backing.PassivatingBackingCacheImpl;
import org.jboss.ejb3.cache.spi.impl.SerializationGroupMember;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockCacheConfig;
import org.jboss.ejb3.test.cache.mock.MockEjb3System;
import org.jboss.ejb3.test.cache.mock.MockXPC;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 65339 $
 */
public class PassivatingBackingCacheImplUnitTestCase extends TestCase
{
   
   /**
    * Peek of an active object should not change it state.
    */
   @SuppressWarnings("unchecked")
   public void testPeekActive() throws Exception
   {
      MockEjb3System system = new MockEjb3System(false, CacheType.SIMPLE);
      MockXPC sharedXPC = new MockXPC();
      MockCacheConfig config = new MockCacheConfig();
      config.setIdleTimeoutSeconds(4);
      MockBeanContainer container = system.deployBeanContainer("MockBeanContainer1", null, CacheType.SIMPLE, config, sharedXPC);
      TransactionalCache cache = (TransactionalCache) container.getCache();
      PassivatingBackingCacheImpl<MockBeanContext, SerializationGroupMember<MockBeanContext>> backingCache = (PassivatingBackingCacheImpl<MockBeanContext, SerializationGroupMember<MockBeanContext>>) cache.getBackingCache();
      SerializationGroupMember<MockBeanContext> obj = backingCache.create(null, null);
      Object key = obj.getId();
      
      backingCache.peek(key);
      
      try
      {
         backingCache.release(key);
         fail("Should not be able to release entry that has not been gotten");
      }
      catch (IllegalStateException good)
      {
         backingCache.get(key);
         backingCache.release(key);      
      }
      finally
      {
         backingCache.remove(key);
      }
         
   }
}
