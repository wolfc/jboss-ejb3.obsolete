/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.interceptors.test.container.unit;

import java.util.List;

import junit.framework.TestCase;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.LifecycleMethodInterceptorsInvocation;
import org.jboss.ejb3.interceptors.test.container.DummyBeanContext;
import org.jboss.ejb3.interceptors.test.container.FirstInterceptor;
import org.jboss.ejb3.interceptors.test.container.InterceptorInvocationOrderTracker;
import org.jboss.ejb3.interceptors.test.container.SecondInterceptor;

/**
 * LifecycleMethodInterceptorsInvocationTestCase
 * 
 * Tests {@link LifecycleMethodInterceptorsInvocation}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class LifecycleMethodInterceptorsInvocationTestCase extends TestCase
{

   /**
    * Test that the {@link LifecycleMethodInterceptorsInvocation} invokes 
    * the interceptors in the correct order
    * 
    * @throws Throwable
    */
   public void testInterceptorInvocationOrder() throws Throwable
   {
      Interceptor[] interceptors = new Interceptor[2];
      Interceptor firstInterceptor = new FirstInterceptor();
      Interceptor secondInterceptor = new SecondInterceptor();

      interceptors[0] = firstInterceptor;
      interceptors[1] = secondInterceptor;

      DummyBeanContext<Object> dummyBeanContext = new DummyBeanContext<Object>();
      dummyBeanContext.setBeanInstance(new Object());
      // Create an invocation with the beancontext and the interceptors
      LifecycleMethodInterceptorsInvocation invocation = new LifecycleMethodInterceptorsInvocation(dummyBeanContext,
            interceptors);
      // invoke
      Object result = invocation.invokeNext();

      // now check the number of interceptors invoked and their order
      assertNull(LifecycleMethodInterceptorsInvocation.class.getName() + " invocation returned a non-null result",
            result);
      List<Interceptor> invokedInterceptors = InterceptorInvocationOrderTracker.getInstance().getInvokedInterceptors();

      assertNotNull("No interceptors were invoked", invokedInterceptors);
      assertEquals("Unexpected number of interceptors invoked during invocation through "
            + LifecycleMethodInterceptorsInvocation.class.getSimpleName(), 2, invokedInterceptors.size());

      // check the order
      assertEquals("First interceptor not invoked in the right order during invocation through "
            + LifecycleMethodInterceptorsInvocation.class.getSimpleName(), firstInterceptor, invokedInterceptors.get(0));

      assertEquals("Second interceptor not invoked in the right order during invocation through "
            + LifecycleMethodInterceptorsInvocation.class.getSimpleName(), secondInterceptor, invokedInterceptors
            .get(1));

   }

   /**
    * Test that the "target" of a {@link LifecycleMethodInterceptorsInvocation} is inferred
    * from the {@link BeanContext#getInstance()}
    *  
    * @throws Throwable
    */
   public void testTargetObject() throws Throwable
   {
      DummyBeanContext<Object> dummyBeanContext = new DummyBeanContext<Object>();
      Object beanInstance = new Object();
      dummyBeanContext.setBeanInstance(beanInstance);
      // Create a invocation out of the beancontext
      LifecycleMethodInterceptorsInvocation invocation = new LifecycleMethodInterceptorsInvocation(dummyBeanContext,
            new Interceptor[0]);
      // now ensure that the target is the same as the bean instance (obtained through beancontext)
      assertEquals("Unexpected target object in LifecycleMethodInterceptorsInvocation", beanInstance, invocation
            .getTargetObject());

   }
}
