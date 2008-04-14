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
package org.jboss.ejb3.test.interceptors.signature.unit;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.DirectContainer;
import org.jboss.ejb3.test.interceptors.common.AOPDeployer;
import org.jboss.ejb3.test.interceptors.signature.PackageProtectedInterceptor;
import org.jboss.ejb3.test.interceptors.signature.SignatureTestBean;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class SignatureTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(SignatureTestCase.class);
   
   private static List<String> lifeCycleVisits = new ArrayList<String>();
   
   public static boolean addLifeCycleVisit(Class<?> cls, String methodName)
   {
      return lifeCycleVisits.add(cls.getName() + "." + methodName);
   }
   
   AOPDeployer deployer = new AOPDeployer("proxy/jboss-aop.xml");
   
   @Override
   protected void setUp() throws Exception
   {
      log.info(deployer.deploy());
   }

   @Override
   protected void tearDown() throws Exception
   {
      log.info(deployer.undeploy());
   }

   public void test() throws Throwable
   {
      log.info("======= Signature.test()");
      //AspectManager.verbose = true;
      
      // To make surefire happy
      Thread.currentThread().setContextClassLoader(SignatureTestBean.class.getClassLoader());
      
      lifeCycleVisits.clear();
      
      DirectContainer<SignatureTestBean> container = new DirectContainer<SignatureTestBean>("SignatureTestBean", "Test", SignatureTestBean.class);
      
      BeanContext<SignatureTestBean> bean = container.construct();
      
      List<String> expectedLifeCycleVisits = Arrays.asList("org.jboss.ejb3.test.interceptors.signature.PackageProtectedInterceptor.postConstruct");
      assertEquals(expectedLifeCycleVisits, lifeCycleVisits);
      
      List<Class<?>> visits = new ArrayList<Class<?>>();
      Integer numVisits = container.invoke(bean, "test", visits);
      
      assertEquals(2, numVisits.intValue());
      List<Class<?>> expectedVisits = Arrays.asList(PackageProtectedInterceptor.class, SignatureTestBean.class);
      assertEquals(expectedVisits, visits);

      log.info("======= Done");
   }
}
