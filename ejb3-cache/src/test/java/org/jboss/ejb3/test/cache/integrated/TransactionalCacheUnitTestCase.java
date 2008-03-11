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
package org.jboss.ejb3.test.cache.integrated;

import javax.ejb.NoSuchEJBException;

import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.test.cache.mock.CacheType;
import org.jboss.ejb3.test.cache.mock.MockBeanContainer;
import org.jboss.ejb3.test.cache.mock.MockBeanContext;
import org.jboss.ejb3.test.cache.mock.MockEjb3System;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * @version $Revision: $
 */
public class TransactionalCacheUnitTestCase extends Ejb3CacheTestCaseBase
{   
   protected Cache<MockBeanContext> createCache() throws Exception
   {
      MockEjb3System system = new MockEjb3System(false, CacheType.NON_PASSIVATING);
      MockBeanContainer ejb = system.deployBeanContainer("test", null, CacheType.NON_PASSIVATING);
      return ejb.getCache();
   }
   
   public void testNonExistingGet() throws Exception
   {      
      Cache<MockBeanContext> cache = createCache();
      
      try
      {
         cache.get(1);
         fail("Object 1 should not be in cache");
      }
      catch(NoSuchEJBException e)
      {
         // good
      }
   }
   
   public void testSimpleLifeCycle() throws Exception
   {
      Cache<MockBeanContext> cache = createCache();
      
      Object key = cache.create(null, null).getId();
      MockBeanContext object = cache.get(key);
      
      assertNotNull(object);
      
      cache.remove(key);
      
      try
      {
         cache.get(key);
         fail("Object should not be in cache");
      }
      catch(NoSuchEJBException e)
      {
         // good
      }
   }
   
   public void testSequentialGetCalls() throws Exception
   {
      Cache<MockBeanContext> cache = createCache();
      
      Object key = cache.create(null, null).getId();
      MockBeanContext object = cache.get(key);
      
      assertNotNull(object);
      
      try
      {
         cache.get(key);
         fail("Two sequential get calls should throw ISE");
      }
      catch(IllegalStateException e)
      {
         // good
      }
      finally {
         cache.remove(key);
      }
   }
   
   public void testSequentialFinishedCalls() throws Exception
   {
      Cache<MockBeanContext> cache = createCache();
      
      Object key = cache.create(null, null).getId();
      MockBeanContext object = cache.get(key);
      
      assertNotNull(object);
      
      cache.finished(object);
      
      try
      {
         cache.finished(object);
         fail("Two sequential finished calls should throw ISE");
      }
      catch(IllegalStateException e)
      {
         // good
      }
      finally {
         cache.remove(key);
      }
   }
}
