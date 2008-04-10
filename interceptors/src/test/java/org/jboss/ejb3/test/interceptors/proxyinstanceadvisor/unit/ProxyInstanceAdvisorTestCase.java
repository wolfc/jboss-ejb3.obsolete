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
package org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.unit;

import java.net.URL;

import junit.framework.TestCase;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.interceptors.proxy.ProxyContainer;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.MyInterface;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.PerInstanceInterceptor;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.PerJoinpointInterceptor;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.ProxiedBean;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.ProxiedInterceptor;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.ProxyContainerWithPool;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.SimplePoolInterceptor;
import org.jboss.logging.Logger;

/**
 * Make sure that PER_INSTANCE and PER_JOINPOINT container interceptors work with EJB 3 
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ProxyInstanceAdvisorTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(ProxyInstanceAdvisorTestCase.class);

   public void test1() throws Throwable
   {
      //AspectManager.verbose = true;
      
      // Bootstrap AOP
      URL url = Thread.currentThread().getContextClassLoader().getResource("proxyinstanceadvisor/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);

      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      ProxyContainerWithPool<ProxiedBean> container = new ProxyContainerWithPool<ProxiedBean>("ProxyTestCase", "InterceptorContainer", ProxiedBean.class);
      
      
      Class<?> interfaces[] = { MyInterface.class };
      MyInterface proxy = container.constructProxy(interfaces);
      
      reset(true);
      String result = proxy.sayHi("Me");
      assertEquals("Hi Me", result);
      ProxiedBean bean1hi = ProxiedBean.instance;
      PerInstanceInterceptor pi1hi = PerInstanceInterceptor.instance;
      PerJoinpointInterceptor pj1hi = PerJoinpointInterceptor.instance;
      
      reset(false);
      result = proxy.sayBye("Me");
      assertEquals("Bye Me", result);
      ProxiedBean bean1bye = ProxiedBean.instance;
      PerInstanceInterceptor pi1bye = PerInstanceInterceptor.instance;
      PerJoinpointInterceptor pj1bye = PerJoinpointInterceptor.instance;
      
      assertSame(bean1hi, bean1bye);
      assertSame(pi1hi, pi1bye);
      assertNotSame(pj1hi, pj1bye);

      reset(true);
      result = proxy.sayHi("Me");
      assertEquals("Hi Me", result);
      ProxiedBean bean2hi = ProxiedBean.instance;
      PerInstanceInterceptor pi2hi = PerInstanceInterceptor.instance;
      PerJoinpointInterceptor pj2hi = PerJoinpointInterceptor.instance;
      assertNotSame(bean1hi, bean2hi);
      
      //FIXME - These must be enabled to start the test
//      assertNotSame(pi2hi, pi1hi);
//      assertNotSame(pj2hi, pj1hi);
   }
   
   private void reset(boolean createNewInstance)
   {
      ProxiedBean.instance = null;
      PerInstanceInterceptor.instance = null;
      PerJoinpointInterceptor.instance = null;
      SimplePoolInterceptor.createNewInstance = createNewInstance;      
   }
}
