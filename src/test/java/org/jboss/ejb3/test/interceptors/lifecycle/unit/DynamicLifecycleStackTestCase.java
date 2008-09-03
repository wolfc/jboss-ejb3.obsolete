/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.interceptors.lifecycle.unit;

import static org.junit.Assert.assertEquals;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.proxy.ProxyContainer;
import org.jboss.ejb3.test.interceptors.common.aop.InvocationCounterInterceptor;
import org.jboss.ejb3.test.interceptors.proxy.MyInterface;
import org.jboss.ejb3.test.interceptors.proxy.ProxiedBean;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Make sure we can override the interceptor chain which gets invoked for
 * lifecycle callbacks.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DynamicLifecycleStackTestCase
{
   private static final Logger log = Logger.getLogger(DynamicLifecycleStackTestCase.class);
   
   private static AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      log.info(deployer.deploy());
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      log.info(deployer.undeploy());
   }

   @Before
   public void setUp() throws Exception
   {
      InvocationCounterInterceptor.counter = 0;
   }

   @After
   public void tearDown() throws Exception
   {
   }

   @Test
   public void test1() throws Throwable
   {
      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      ProxyContainer<ProxiedBean> container = new ProxyContainer<ProxiedBean>("ProxyTestCase", "InterceptorContainer", ProxiedBean.class);
      
      Class<?> interfaces[] = { MyInterface.class };
      MyInterface proxy = container.constructProxy(interfaces);
      
      String result = proxy.sayHi("Me");
      assertEquals("Hi Me", result);

      assertEquals(1, InvocationCounterInterceptor.counter);
   }
}
