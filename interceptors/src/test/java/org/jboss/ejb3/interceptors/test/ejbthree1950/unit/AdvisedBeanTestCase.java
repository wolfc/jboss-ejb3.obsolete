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
package org.jboss.ejb3.interceptors.test.ejbthree1950.unit;

import static org.junit.Assert.assertEquals;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.interceptors.proxy.ProxyContainer;
import org.jboss.ejb3.interceptors.test.ejbthree1950.Hobby;
import org.jboss.ejb3.interceptors.test.ejbthree1950.HobbyFasad;
import org.jboss.ejb3.interceptors.test.ejbthree1950.HobbyFasadBean;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * What if the bean class is already advised via aopc?
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AdvisedBeanTestCase
{
   private static final Logger log = Logger.getLogger(AdvisedBeanTestCase.class);
   
   private static AOPDeployer deployer1 = new AOPDeployer("proxy/jboss-aop.xml");
   private static AOPDeployer deployer2 = new AOPDeployer("ejbthree1950/jboss-aop.xml");
   
   @AfterClass
   public static void afterClass()
   {
      log.info(deployer2.undeploy());
      log.info(deployer1.undeploy());
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      log.info(deployer1.deploy());
      log.info(deployer2.deploy());
   }
   
   @Test
   public void testContainerInvocation() throws Throwable
   {
      ProxyContainer<HobbyFasadBean> container = new ProxyContainer<HobbyFasadBean>("AdvisedBeanTestCase", "InterceptorContainer", HobbyFasadBean.class);
      Class<?> interfaces[] = { HobbyFasad.class };
      HobbyFasad bean = container.constructProxy(interfaces);
      String name = "testContainerInvocation";
      String description = "Invocation via a proxy container";
      Hobby result = bean.skapaHobby1(name, description);
      assertEquals(description + " avlyssnas", result.getBeskrivning());
   }
   
   @Test
   public void testContainerInvocation2() throws Throwable
   {
      ProxyContainer<HobbyFasadBean> container = new ProxyContainer<HobbyFasadBean>("AdvisedBeanTestCase", "InterceptorContainer", HobbyFasadBean.class);
      Class<?> interfaces[] = { HobbyFasad.class };
      HobbyFasad bean = container.constructProxy(interfaces);
      String name = "testContainerInvocation";
      String description = "Invocation via a proxy container";
      Hobby result = bean.skapaHobby2(name, description);
      assertEquals(description + " avlyssnas", result.getBeskrivning());
   }
   
   @Test
   public void testNormalInvocation()
   {
      HobbyFasad bean = new HobbyFasadBean();
      String name = "testNormalInvocation";
      String description = "Normal invocation test";
      Hobby result = bean.skapaHobby1(name, description);
      assertEquals(description + " avlyssnas", result.getBeskrivning());
   }
}
