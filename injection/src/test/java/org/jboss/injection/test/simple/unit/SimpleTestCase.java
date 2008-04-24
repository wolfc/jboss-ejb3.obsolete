/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.injection.test.simple.unit;

import org.jboss.injection.aop.InjectionEnvironment;
import org.jboss.injection.test.common.AOPTestDelegate;
import org.jboss.injection.test.common.Counter;
import org.jboss.injection.test.simple.InjectedBean;
import org.jboss.test.AbstractTestCaseWithSetup;
import org.jboss.test.AbstractTestDelegate;

/**
 * Run with: -Djava.system.class.loader=org.jboss.aop.standalone.SystemClassLoader
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleTestCase extends AbstractTestCaseWithSetup
{
   public SimpleTestCase(String name)
   {
      super(name);
   }

   public static AbstractTestDelegate getDelegate(Class<?> cls)
   {
      return new AOPTestDelegate(cls);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      Counter.reset();
      
      InjectionEnvironment.getCurrent().clear();
   }
   
   public void test1() throws Exception
   {
      InjectionEnvironment.getCurrent().put(InjectedBean.class.getName() + "/value", "Hello world");
      
      InjectedBean bean = new InjectedBean();
      
      bean.check();
      
      assertEquals(1, Counter.postConstructs);
      
      bean = null;
      
      Runtime.getRuntime().gc();
      Runtime.getRuntime().runFinalization();
      
      //assertEquals(1, Counter.preDestroys);
   }
}
