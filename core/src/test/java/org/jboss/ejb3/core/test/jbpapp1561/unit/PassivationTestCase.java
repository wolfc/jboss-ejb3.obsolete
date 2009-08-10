/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.jbpapp1561.unit;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.jbpapp1561.PassivatingStatefulBean;
import org.jboss.ejb3.core.test.jbpapp1561.PassivatingStatefulLocal;
import org.jboss.ejb3.core.test.jbpapp1561.TestCache;
import org.jboss.ejb3.core.test.jbpapp1561.TestCacheFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PassivationTestCase extends AbstractEJB3TestCase
{
   @After
   public void after()
   {
      PassivatingStatefulBean.barrier.reset();
      PassivatingStatefulBean.finalized = false;
   }
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      CacheFactoryRegistry cacheFactoryRegistry = Ejb3RegistrarLocator.locateRegistrar().lookup("EJB3CacheFactoryRegistry", CacheFactoryRegistry.class);
      cacheFactoryRegistry.getFactories().remove(TestCacheFactory.NAME);
      
      AbstractEJB3TestCase.afterClass();
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      // Add the Force Passivation Cache
      CacheFactoryRegistry cacheFactoryRegistry = Ejb3RegistrarLocator.locateRegistrar().lookup("EJB3CacheFactoryRegistry", CacheFactoryRegistry.class);
      cacheFactoryRegistry.getFactories().put(TestCacheFactory.NAME, TestCacheFactory.class);
      //log.info("Added " + forcePassivationCacheRegistryName);

      deploySessionEjb(PassivatingStatefulBean.class);
   }
   
   @Test
   public void test1() throws Exception
   {
      PassivatingStatefulLocal bean = lookup("PassivatingStatefulBean/local", PassivatingStatefulLocal.class);
      Assert.assertNotNull(bean);
      
      PassivatingStatefulBean.barrier.await(10, TimeUnit.SECONDS);
      
      synchronized (TestCache.passivationCompleteNotification)
      {
         TestCache.passivationCompleteNotification.wait(10000);
      }
      
      for(int i = 0; i < 3; i++)
      {
         System.gc();
         Runtime.getRuntime().runFinalization();
      }
      
      Assert.assertTrue("bean should have been purged from memory", PassivatingStatefulBean.finalized);
   }
}
