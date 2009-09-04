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
package org.jboss.ejb3.interceptors.aop;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.container.LifecycleMethodInterceptorsInvocation;

/**
 * LifecycleCallbackInterceptorMethodLazyInterceptor
 * 
 * An AOP interceptor wrapper for lifecycle callback methods on (javax.interceptor.Interceptors) 
 * interceptor classes. Unlike the {@link LifecycleCallbackInterceptorMethodInterceptor} AOP interceptor,
 * this class does *not* require a interceptor class instance while creating this interceptor. Instead
 * the interceptor instance is obtained through the {@link Invocation} when the {@link Interceptor#invoke(Invocation)}
 * method is invoked
 * 
 * @see #invoke(Invocation)
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class LifecycleCallbackInterceptorMethodLazyInterceptor implements Interceptor
{

   /**
    * The lifecycle method
    */
   private Method lifecycleMethod;

   /**
    * Lifecycle interceptor class
    */
   private Class<?> lifecycleInterceptorClass;

   /**
    * Creates a {@link LifecycleCallbackInterceptorMethodLazyInterceptor} based on the 
    * interceptor class and the interceptor method.
    *  
    * The interceptor instance, corresponding to the <code>interceptorClass</code>
    * will be obtained through the {@link Invocation} (if it's available), when this interceptor is invoked.
    * 
    * @param interceptorClass The interceptor class
    * @param lifecycleCallbackMethod The interceptor method
    * 
    * @see LifecycleCallbackInterceptorMethodLazyInterceptor#getInterceptorInstance(Invocation)
    */
   public LifecycleCallbackInterceptorMethodLazyInterceptor(Class<?> interceptorClass, Method lifecycleCallbackMethod)
   {
      if (interceptorClass == null || lifecycleCallbackMethod == null)
      {
         throw new IllegalArgumentException("Either interceptor class " + interceptorClass + " or interceptor method "
               + lifecycleCallbackMethod + " is null. Both are required to be non-null");
      }

      this.lifecycleInterceptorClass = interceptorClass;
      this.lifecycleMethod = lifecycleCallbackMethod;
   }

   /**
    * @see Interceptor#getName()
    */
   public String getName()
   {
      return this.getClass().getSimpleName();
   }

   /**
    * Invokes the lifecycle callback method on the interceptor.
    * The interceptor instance is obtained from the <code>invocation</code>, if available
    * 
    * @see #getInterceptorInstance(Invocation)
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      InvocationContext ctx = InvocationContextInterceptor.getInvocationContext(invocation);

      Object args[] =
      {ctx};
      boolean accessible = this.lifecycleMethod.isAccessible();
      this.lifecycleMethod.setAccessible(true);
      try
      {
         Object interceptorInstance = this.getInterceptorInstance(invocation);
         return this.lifecycleMethod.invoke(interceptorInstance, args);
      }
      finally
      {
         this.lifecycleMethod.setAccessible(accessible);
      }

   }

   /**
    * Returns the lifecycle callback interceptor instance,  from the <code>invocation</code>, if available.
    * 
    *  
    * 
    * @param invocation AOP invocation
    * @return Returns the lifecycle callback interceptor instance
    * 
    * @throws IllegalStateException if the interceptor instance cannot be
    * obtained through <code>invocation</code>
    */
   protected Object getInterceptorInstance(final Invocation invocation)
   {

      // check if the invocation is capable of return the bean context
      // and ultimately the interceptor instance
      if (invocation instanceof LifecycleMethodInterceptorsInvocation)
      {
         LifecycleMethodInterceptorsInvocation lifecycleMethodInvocation = (LifecycleMethodInterceptorsInvocation) invocation;
         BeanContext<?> beanContext = lifecycleMethodInvocation.getBeanContext();
         return beanContext.getInterceptor(this.lifecycleInterceptorClass);
      }
      throw new IllegalStateException("Interceptor instance unavailable for invocation " + invocation);
   }

}
