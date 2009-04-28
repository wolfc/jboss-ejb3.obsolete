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
package org.jboss.ejb3.core.test.ejbthree1358.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.ejb.EJBException;

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.core.test.ejbthree1358.MockContainer;
import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.pool.StrictMaxPool;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StrictMaxPoolUsageTestCase
{
   private static MockContainer container;
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      container = new MockContainer();
   }
   
   @Test
   public void test1()
   {
      Pool pool = new StrictMaxPool();
      int maxSize = 10;
      long timeout = 100;
      pool.initialize(container, maxSize, timeout);
      
      BeanContext<?> ctx = pool.get();
      pool.remove(ctx);
      
      assertEquals(0, pool.getCurrentSize());
   }
   
   @Test
   public void test2()
   {
      Pool pool = new StrictMaxPool();
      int maxSize = 10;
      long timeout = 100;
      pool.initialize(container, maxSize, timeout);
      
      for(int i = 0; i < maxSize; i++)
         pool.get();
      
      assertEquals(maxSize, pool.getCurrentSize());
      
      try
      {
         pool.get();
         fail("should have thrown an EJBException");
      }
      catch(EJBException e)
      {
         // good
      }
   }
   
   @Test
   public void test3()
   {
      Pool pool = new StrictMaxPool();
      int maxSize = 10;
      long timeout = 100;
      pool.initialize(container, maxSize, timeout);
      
      BeanContext<?> ctxs[] = new BeanContext[maxSize];
      for(int i = 0; i < maxSize; i++)
         ctxs[i] = pool.get();
      
      pool.remove(ctxs[0]);
      
      assertEquals(maxSize - 1, pool.getCurrentSize());
      assertEquals(1, pool.getAvailableCount());
      
      try
      {
         pool.get();
      }
      catch(EJBException e)
      {
         fail(e.getMessage());
      }
   }
}
