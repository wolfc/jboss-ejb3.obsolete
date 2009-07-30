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
package org.jboss.ejb3.core.test.ejbthree1703.unit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1703.NoopBean;
import org.jboss.ejb3.core.test.ejbthree1703.NoopLocal;
import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * (created - removed) = (inUse + available)
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ThreadLocalPoolStatsTestCase extends AbstractEJB3TestCase
{
   private SessionContainer container;
   
   @After
   public void after()
   {
      undeployEjb(container);
   }
   
   @Before
   public void before() throws DeploymentException
   {
      container = deploySessionEjb(NoopBean.class);
   }
   
   /*
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      container = deploySessionEjb(NoopBean.class);
   }
   */
   
   @Test
   public void testCreateCount() throws Exception
   {
      NoopLocal bean = lookup("NoopBean/local", NoopLocal.class);
      bean.noop();
      assertEquals(1, container.getPool().getCreateCount());
      assertEquals("the pool should have grown", 1, container.getPool().getMaxSize());
      assertEquals("the instance should be available", 1, container.getPool().getAvailableCount());
   }
   
   /**
    * InUse is calculated as: inUse = maxSize - available
    */
   @Test
   public void testInUse() throws Exception
   {
      final NoopLocal bean = lookup("NoopBean/local", NoopLocal.class);
      ExecutorService service = Executors.newSingleThreadExecutor();
      // exercise the thread
      /*
      Future<Void> future = service.submit(new Callable<Void> () {
         public Void call() throws Exception
         {
            bean.noop();
            return null;
         }
      });
      future.get(5, SECONDS);
      assertEquals(1, container.getPool().getCreateCount());
      assertEquals("the pool should have grown", 1, container.getPool().getMaxSize());
      assertEquals("the instance should be available", 1, container.getPool().getAvailableCount());
      */
      
      final CyclicBarrier entree = new CyclicBarrier(2);
      final CyclicBarrier exit = new CyclicBarrier(2);
      Future<Void> future = service.submit(new Callable<Void> () {
         public Void call() throws Exception
         {
            bean.shoo(entree, exit);
            return null;
         }
      });
      
      entree.await(5, SECONDS);
      
      Pool pool = container.getPool();
      assertEquals(1, pool.getCreateCount());
      assertEquals("the pool should have grown", 1, pool.getMaxSize());
      int inUse = pool.getMaxSize() - pool.getAvailableCount();
      assertEquals("the instance should be in use", 1, inUse);
      
      exit.await(5, SECONDS);
      
      future.get(5, SECONDS);
      
      service.shutdown();
   }
   
   @Test
   public void testMaxSize()
   {
      Pool pool = container.getPool();
      int actual = pool.getMaxSize();
      assertEquals("a ThreadLocalPool starts empty and grows", 0, actual);
   }
   
   @Test
   public void testReentrant() throws Exception
   {
      final NoopLocal bean = lookup("NoopBean/local", NoopLocal.class);
      ExecutorService service = Executors.newSingleThreadExecutor();
      final CyclicBarrier entree = new CyclicBarrier(2);
      final CyclicBarrier exit = new CyclicBarrier(2);
      Future<Void> future = service.submit(new Callable<Void> () {
         public Void call() throws Exception
         {
            bean.reentrant(1, entree, exit);
            return null;
         }
      });
      
      entree.await(5, SECONDS);
      
      Pool pool = container.getPool();
      assertEquals(2, pool.getCreateCount());
      assertEquals("the pool should have grown", 2, pool.getMaxSize());
      int inUse = pool.getMaxSize() - pool.getAvailableCount();
      assertEquals("two instances should be in use", 2, inUse);
      
      exit.await(5, SECONDS);
      
      future.get(5, SECONDS);
      
      assertEquals("the pool should have shrunk", 1, pool.getMaxSize());
      assertEquals("the pool should have 1 available instance", 1, pool.getAvailableCount());
      
      service.shutdown();
   }
   
   @Test
   public void testThreeThreads() throws Exception
   {
      final NoopLocal bean = lookup("NoopBean/local", NoopLocal.class);
      ExecutorService service = Executors.newFixedThreadPool(2);
      
      final CyclicBarrier entree = new CyclicBarrier(3);
      final CyclicBarrier exit = new CyclicBarrier(3);
      Callable<Void> task = new Callable<Void> () {
         public Void call() throws Exception
         {
            bean.shoo(entree, exit);
            return null;
         }
      };
      Future<?> futures[] = new Future[2];
      futures[0] = service.submit(task);
      futures[1] = service.submit(task);
      
      entree.await(5, SECONDS);
      
      Pool pool = container.getPool();
      assertEquals(2, pool.getCreateCount());
      assertEquals("the pool should have grown", 2, pool.getMaxSize());
      int inUse = pool.getMaxSize() - pool.getAvailableCount();
      assertEquals("the instance should be in use", 2, inUse);
      
      exit.await(5, SECONDS);
      
      futures[0].get(5, SECONDS);
      futures[1].get(5, SECONDS);
      
      service.shutdown();

   }
}
