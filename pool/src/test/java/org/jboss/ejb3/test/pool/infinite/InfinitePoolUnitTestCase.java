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
package org.jboss.ejb3.test.pool.infinite;

import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.pool.StatelessObjectFactory;
import org.jboss.ejb3.pool.inifinite.InfinitePool;
import org.jboss.ejb3.test.pool.common.MockBean;
import org.jboss.ejb3.test.pool.common.MockFactory;

import junit.framework.TestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InfinitePoolUnitTestCase extends TestCase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      MockBean.reset();
   }
   
   public void test1()
   {
      StatelessObjectFactory<MockBean> factory = new MockFactory();
      Pool<MockBean> pool = new InfinitePool<MockBean>(factory);
      MockBean bean = pool.get();
      
      assertEquals(1, MockBean.getPostConstructs());
      
      pool.release(bean);
      
      assertEquals(1, MockBean.getPreDestroys());
   }

   public void test100()
   {
      StatelessObjectFactory<MockBean> factory = new MockFactory();
      Pool<MockBean> pool = new InfinitePool<MockBean>(factory);
      
      for(int i = 1; i <= 100; i++)
      {
         MockBean bean = pool.get();
         
         assertEquals(i, MockBean.getPostConstructs());
         
         pool.release(bean);
         
         assertEquals(i, MockBean.getPreDestroys());
      }
   }
}
