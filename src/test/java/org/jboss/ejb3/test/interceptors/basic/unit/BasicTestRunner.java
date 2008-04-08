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
package org.jboss.ejb3.test.interceptors.basic.unit;

import java.util.Arrays;

import junit.framework.TestCase;

import org.jboss.aop.InstanceAdvised;
import org.jboss.ejb3.test.interceptors.basic.BasicBean;
import org.jboss.ejb3.test.interceptors.basic.BasicInterceptor;
import org.jboss.ejb3.test.interceptors.basic.BasicMethodInterceptor;

/**
 * This one must run within a domain class loader.
 * 
 * Named TestRunner, so surefire does not pick it up.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class BasicTestRunner extends TestCase
{
   public void test2() throws Exception
   {
      assertEquals(0, BasicInterceptor.postConstructs);
      
      BasicBean bean = new BasicBean();
      System.out.println("instanceAdvisor = " + ((InstanceAdvised) bean)._getInstanceAdvisor());
      
      assertEquals("BasicInterceptor postConstruct must have been called once", 1, BasicInterceptor.postConstructs);
      
      System.out.println(bean.getClass() + " " + bean.getClass().getClassLoader());
      System.out.println("  " + Arrays.toString(bean.getClass().getInterfaces()));
      String result = bean.sayHi("Test");
      System.out.println(result);
      
      assertEquals("sayHi didn't invoke BasicInterceptor.aroundInvoke once", 1, BasicInterceptor.aroundInvokes);
      assertEquals("sayHi didn't invoke BasicBean.aroundInvoke once", 1, BasicBean.aroundInvokes);
      
      bean.intercept();
      assertEquals("intercept didn't invoke BasicMethodInterceptor.aroundInvoke", 1, BasicMethodInterceptor.aroundInvokes);
      bean.intercept();
      assertEquals("intercept didn't invoke BasicMethodInterceptor.aroundInvoke", 2, BasicMethodInterceptor.aroundInvokes);
      
      assertEquals("intercept didn't invoke BasicInterceptor.aroundInvoke", 3, BasicInterceptor.aroundInvokes);
      assertEquals("BasicInterceptor postConstruct must have been called once", 1, BasicInterceptor.postConstructs);
      // 12.7 footnote 57
      assertEquals("BasicMethodInterceptor.postConstruct must not have been called", 0, BasicMethodInterceptor.postConstructs);
      
      //((Destructable) bean)._preDestroy();
      bean = null;
   }
   
   public void testInstances() throws Exception
   {
      BasicBean bean1 = new BasicBean();
      BasicBean bean2 = new BasicBean();
      
      bean1.setState(1);
      bean2.setState(2);
      
      assertEquals(1, bean1.getState());
      assertEquals(2, bean2.getState());
   }
}
