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
package org.jboss.ejb3.interceptors.test.indirectcontainer.unit;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Proxy;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.test.indirectcontainer.Dummy;
import org.jboss.ejb3.interceptors.test.indirectcontainer.DummyContainerContainer;
import org.jboss.ejb3.interceptors.test.indirectcontainer.DummyIndirectContainer;
import org.jboss.ejb3.interceptors.test.indirectcontainer.DummyInterceptor;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class IndirectContainerTestCase
{
   private static final Logger log = Logger.getLogger(IndirectContainerTestCase.class);
   
   private static AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
   
   @AfterClass
   public static void afterClass()
   {
      log.info(deployer.undeploy());
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      log.info(deployer.deploy());
   }
   
   @Test
   public void test1() throws Exception
   {
      int previous = DummyInterceptor.getInvocations();
      
      DummyContainerContainer containerContainer = new DummyContainerContainer("Test", "InterceptorContainer", DummyIndirectContainer.class);
      BeanContext<DummyIndirectContainer> interceptorContainer = containerContainer.construct();
      // TODO: why do we need this explicitly, can't the direct container handle this?
      interceptorContainer.getInstance().setBeanContext(interceptorContainer);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { Dummy.class };
      Dummy dummy = (Dummy) Proxy.newProxyInstance(loader, interfaces, interceptorContainer.getInstance());
      dummy.hit();
      
      int current = DummyInterceptor.getInvocations();
      
      assertEquals("DummyInterceptor was not hit", 1, current - previous);
   }
}
