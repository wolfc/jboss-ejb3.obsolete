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
package org.jboss.ejb3.test.interceptors.direct.unit;

import java.net.URL;
import java.util.Arrays;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;
import org.jboss.ejb3.interceptors.direct.DirectContainer;
import org.jboss.ejb3.test.interceptors.direct.DirectBean;
import org.jboss.ejb3.test.interceptors.direct.DirectInterceptor;
import org.jboss.ejb3.test.interceptors.direct.DirectMethodInterceptor;
import org.jboss.logging.Logger;

/**
 * Test direct container advisement.
 * 
 * There is no special class loader needed, because all invocations
 * are routed through the direct container.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class DirectTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(DirectTestCase.class);
   
   private class MyContainer<T> extends DirectContainer<T>
   {
      public MyContainer(String name, String domainName, Class<? extends T> beanClass)
      {
         super(name, domainName, beanClass);
      }

      public void testAdvisor()
      {
         assertNotNull("container not set in managed object advisor", ((ManagedObjectAdvisor<T, DirectContainer<T>>) getAdvisor()).getContainer());
         assertTrue(((ManagedObjectAdvisor<T, DirectContainer<T>>) getAdvisor()).getContainer() == this);
      }
   }
   
   public void test() throws Throwable
   {
      AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(DirectBean.class.getClassLoader());
      
      // Bootstrap AOP
      // FIXME: use the right jboss-aop.xml
      URL url = Thread.currentThread().getContextClassLoader().getResource("proxy/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);
      
      assertEquals(0, DirectInterceptor.postConstructs);
      
      MyContainer<DirectBean> container = new MyContainer<DirectBean>("DirectBean", "Test", DirectBean.class);
      container.testAdvisor();
      
      BeanContext<DirectBean> bean = container.construct();
      
      assertEquals("DirectInterceptor postConstruct must have been called once", 1, DirectInterceptor.postConstructs);
      assertEquals("DirectBean postConstruct must have been called once", 1, DirectBean.postConstructs);
      
      System.out.println(bean.getClass() + " " + bean.getClass().getClassLoader());
      System.out.println("  " + Arrays.toString(bean.getClass().getInterfaces()));
      String result = container.invoke(bean, "sayHi", "Test");
      System.out.println(result);
      
      assertEquals("sayHi didn't invoke DirectInterceptor.aroundInvoke once", 1, DirectInterceptor.aroundInvokes);
      assertEquals("sayHi didn't invoke DirectBean.aroundInvoke once", 1, DirectBean.aroundInvokes);
      
      container.invoke(bean, "intercept");
      assertEquals("intercept didn't invoke DirectMethodInterceptor.aroundInvoke", 1, DirectMethodInterceptor.aroundInvokes);
      container.invoke(bean, "intercept");
      assertEquals("intercept didn't invoke DirectMethodInterceptor.aroundInvoke", 2, DirectMethodInterceptor.aroundInvokes);
      
      assertEquals("intercept didn't invoke DirectInterceptor.aroundInvoke", 3, DirectInterceptor.aroundInvokes);
      assertEquals("DirectInterceptor postConstruct must have been called once", 1, DirectInterceptor.postConstructs);
      // 12.7 footnote 57
      assertEquals("DirectMethodInterceptor.postConstruct must not have been called", 0, DirectMethodInterceptor.postConstructs);
      
      //((Destructable) bean)._preDestroy();
      bean = null;
   }
}
