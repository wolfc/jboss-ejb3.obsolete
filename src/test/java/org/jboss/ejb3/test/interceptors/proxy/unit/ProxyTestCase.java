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
package org.jboss.ejb3.test.interceptors.proxy.unit;

import java.net.URL;

import junit.framework.TestCase;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.interceptors.proxy.ProxyContainer;
import org.jboss.ejb3.test.interceptors.proxy.MyInterface;
import org.jboss.ejb3.test.interceptors.proxy.ProxiedBean;
import org.jboss.ejb3.test.interceptors.proxy.ProxiedInterceptor;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class ProxyTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(ProxyTestCase.class);

   public void test1() throws Throwable
   {
      //AspectManager.verbose = true;
      
      // Bootstrap AOP
      URL url = Thread.currentThread().getContextClassLoader().getResource("proxyinstanceadvisor/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);

      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      ProxyContainer<ProxiedBean> container = new ProxyContainer<ProxiedBean>("ProxyTestCase", "InterceptorContainer", ProxiedBean.class);
      
      assertEquals(0, ProxiedInterceptor.postConstructs);
      
      Class<?> interfaces[] = { MyInterface.class };
      MyInterface proxy = container.constructProxy(interfaces);
      
      assertEquals("ProxiedInterceptor postConstruct must have been called once", 1, ProxiedInterceptor.postConstructs);
      
      String result = proxy.sayHi("Me");
      assertEquals("Hi Me", result);
      
      assertEquals("sayHi didn't invoke ProxiedInterceptor.aroundInvoke once", 1, ProxiedInterceptor.aroundInvokes);
      assertEquals("sayHi didn't invoke ProxiedBean.aroundInvoke once", 1, ProxiedBean.aroundInvokes);
   }
}
