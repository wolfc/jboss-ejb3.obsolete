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
package org.jboss.ejb3.core.test.ejbthree1846.unit;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1846.MyStateful;
import org.jboss.ejb3.core.test.ejbthree1846.MyStatefulBean;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PassivateContextTestCase extends AbstractEJB3TestCase
{
   private static StatefulContainer container;
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      container = (StatefulContainer) deploySessionEjb(MyStatefulBean.class);
   }
   
   @Before
   public void before()
   {
      MyStatefulBean.activations = 0;
      MyStatefulBean.passivations = 0;
   }
   
   @Test
   public void test1() throws Throwable
   {
      Serializable id = container.getSessionFactory().createSession(null, null);
      
      Method method = MyStateful.class.getMethod("getBusinessObject");
      MyStateful bean = (MyStateful) container.invoke(id, MyStateful.class, method, null);
      
      MyStatefulBean.barrier.await(5000, TimeUnit.MILLISECONDS);
      
      assertEquals(0, MyStatefulBean.activations);
      assertEquals(1, MyStatefulBean.passivations);
      
      MyStateful bean2 = bean.getBusinessObject();
      
      assertEquals(1, MyStatefulBean.activations);
      
      assertEquals(bean, bean2);
   }
}
