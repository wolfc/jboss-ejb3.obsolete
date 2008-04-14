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
package org.jboss.ejb3.test.interceptors.instances.unit;

import junit.framework.TestCase;

import org.jboss.ejb3.interceptors.proxy.ProxyContainer;
import org.jboss.ejb3.test.interceptors.common.AOPDeployer;
import org.jboss.ejb3.test.interceptors.instances.SimpleBean;
import org.jboss.ejb3.test.interceptors.instances.StatefulInterceptor;
import org.jboss.ejb3.test.interceptors.instances.StatefulInterceptorInterface;
import org.jboss.logging.Logger;

/**
 * EJB 3 12.2: Interceptor Life Cycle
 * The lifecycle of an interceptor instance is the same as that of the bean instance with
 * which it is associated.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorInstancesTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(InterceptorInstancesTestCase.class);
   
   public void test1() throws Throwable
   {
      log.info("======= InterceptorInstances.test1()");
      //AspectManager.verbose = true;
      
      AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
      try
      {
         // Bootstrap AOP
         // FIXME: use the right jboss-aop.xml
         log.info(deployer.deploy());
         
         Thread.currentThread().setContextClassLoader(StatefulInterceptorInterface.class.getClassLoader());
         
         ProxyContainer<SimpleBean> container = new ProxyContainer<SimpleBean>("InterceptorInstancesTestCase", "InterceptorContainer", SimpleBean.class);
         
         assertEquals(0, StatefulInterceptor.postConstructs);
         
         StatefulInterceptorInterface bean1 = container.constructProxy(new Class[] { StatefulInterceptorInterface.class });
         StatefulInterceptorInterface bean2 = container.constructProxy(new Class[] { StatefulInterceptorInterface.class });
         
         bean1.setState(1);
         bean2.setState(2);
         
         assertEquals(1, bean1.getState());
         assertEquals(2, bean2.getState());
         
         assertEquals(2, StatefulInterceptor.postConstructs);
      }
      finally
      {
         log.info(deployer.undeploy());
      }
      log.info("======= Done");
   }
}
