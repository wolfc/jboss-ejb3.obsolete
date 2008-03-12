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
package org.jboss.ejb3.test.interceptors.supermethod.unit;

import java.net.URL;
import java.util.LinkedHashMap;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.interceptors.proxy.ProxyContainer;
import org.jboss.ejb3.test.interceptors.supermethod.AroundInvokeBean;
import org.jboss.ejb3.test.interceptors.supermethod.AroundInvokeIF;
import org.jboss.logging.Logger;

/**
 * EJBTHREE-1214: Pointcut expression which applies to sub-classes is not applied to super-methods
 * 
 * AroundInvokeBean implements SessionSynchronization, thus all methods including
 * the afterBegintTest defined in AroundInvokeBase must contain SessionSynchronizationInterceptor.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SuperMethodTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(SuperMethodTestCase.class);

   public void test1() throws Throwable
   {
      //AspectManager.verbose = true;
      
      // TODO: During inventory surefire boots up BasicTestSuite
      LinkedHashMap pointcuts = AspectManager.instance().getPointcuts();
      if(!pointcuts.isEmpty())
      {
         //System.err.println("AspectManager still contains: " + pointcuts);
         URL url = Thread.currentThread().getContextClassLoader().getResource("basic/jboss-aop.xml");
         AspectXmlLoader.undeployXML(url);
      }
      
      // Bootstrap AOP
      URL url = Thread.currentThread().getContextClassLoader().getResource("supermethod/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);

      Thread.currentThread().setContextClassLoader(AroundInvokeIF.class.getClassLoader());
      
      ProxyContainer<AroundInvokeBean> container = new ProxyContainer<AroundInvokeBean>("SuperMethodTestCase", "InterceptorContainer", AroundInvokeBean.class);
      
      Class<?> interfaces[] = { AroundInvokeIF.class };
      AroundInvokeIF proxy = container.constructProxy(interfaces);
      
      proxy.afterBeginTest();
   }
}
