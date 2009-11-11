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
package org.jboss.ejb3.test.sandbox.performance.unit;


import java.net.URL;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.BeanContextFactory;
import org.jboss.ejb3.sandbox.interceptorcontainer.InjectingBeanContextFactory;
import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainerContainer;
import org.jboss.ejb3.sandbox.stateless.StatelessInterceptor;
import org.jboss.ejb3.test.sandbox.performance.Calculator;
import org.jboss.ejb3.test.sandbox.performance.CalculatorBean;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jnp.server.SingletonNamingServer;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class EJBUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(EJBUnitTestCase.class);
   
   public void test1() throws Throwable
   {
      SingletonNamingServer namingServer = new SingletonNamingServer();
      
      // Bootstrap AOP
      URL url = Thread.currentThread().getContextClassLoader().getResource("stateless/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);

      InitialContext ctx = new InitialContext();
      
      Util.bind(ctx, "java:comp/env/beanClass", CalculatorBean.class);
      
//      DirectContainer<InterceptorContainer> interceptorContainerContainer = new DirectContainer<InterceptorContainer>("FIXME", "InterceptorContainer", InterceptorContainer.class);
      final InterceptorContainerContainer interceptorContainerContainer = new InterceptorContainerContainer("FIXME", "InterceptorContainer", InterceptorContainer.class);
      // With EJBTHREE-1246 construction with arguments no longer works
      Class<? extends BeanContextFactory<InterceptorContainer, InterceptorContainerContainer>> beanContextFactoryClass = (Class<? extends BeanContextFactory<InterceptorContainer, InterceptorContainerContainer>>) InjectingBeanContextFactory.class;
      interceptorContainerContainer.setBeanContextFactoryClass(beanContextFactoryClass);
      final BeanContext<InterceptorContainer> interceptorContainer = interceptorContainerContainer.construct();
      // FIXME: bug in AbstractDirectContainer.construct
      interceptorContainer.getInstance().setDirectContainer(interceptorContainerContainer);
      interceptorContainer.getInstance().setBeanContext(interceptorContainer);
      
      Calculator bean = (Calculator) ctx.lookup("CalculatorBean/local");
      
      assertNotNull(bean);
      
      System.out.println(bean.calculatePi(10));
      
      System.out.println("BLACKBOX MEASURED STATS:");
      StressCreator.createStress(bean);
      //System.err.println(bean.calculatePi(10));
      
      namingServer.destroy();
      
      System.out.println("WHITEBOX MEASURED STATS:");
      System.out.println("Average wait queue " + ((StatelessInterceptor.accumelatedWaitingTime / 100.0) / 1000.0));
      System.out.println("Average execution " + ((StatelessInterceptor.accumelatedExecutionTime / 100.0) / 1000.0));
   }
}
