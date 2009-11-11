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
package org.jboss.ejb3.test.sandbox.interceptorinterceptor.unit;

import java.net.URL;

import junit.framework.TestCase;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.DirectContainer;
import org.jboss.ejb3.test.sandbox.interceptorinterceptor.InterceptorInterceptorBean;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class InterceptorInterceptorUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(InterceptorInterceptorUnitTestCase.class);
   
   public void test1() throws Throwable
   {
//      AspectManager.verbose = true;
      
      // Bootstrap AOP
      // TODO: choose the right jboss-aop.xml
      URL url = Thread.currentThread().getContextClassLoader().getResource("stateless/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);

      DirectContainer<InterceptorInterceptorBean> container = new DirectContainer<InterceptorInterceptorBean>("InterceptorInterceptorBean", "InterceptorContainer", InterceptorInterceptorBean.class);
      BeanContext<InterceptorInterceptorBean> instance = container.construct();
   }
}
