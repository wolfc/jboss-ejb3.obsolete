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
package org.jboss.ejb3.interceptors.container;

import java.lang.reflect.Constructor;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbackBeanMethodInterceptor;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbackInterceptorMethodInterceptor;

/**
 * LifecycleMethodInterceptorsInvocation
 * 
 * An {@link Invocation} which is aware of the bean context.
 * The {@link LifecycleMethodInterceptorsInvocation} is used for
 * creating an AOP invocation for lifecycle callback methods defined
 * either on the bean class or on the interceptor classes associated
 * with the bean. The end target of the {@link LifecycleMethodInterceptorsInvocation},
 * is always a bean instance. But the target (bean instance) is *never* invoked at the 
 * end of the interceptor chain. This is because the lifecycle method(s) 
 * may or may not be present on the bean class. If there are any lifecycle methods
 * present on the bean class, then its the responsibility of the 
 * code using this {@link LifecycleMethodInterceptorsInvocation} to create
 * an appropriate AOP interceptor (ex: {@link LifecycleCallbackBeanMethodInterceptor}
 * and making it available in the interceptor chain that is passed to the constructor 
 * of this class 
 * 
 * @see LifecycleCallbackBeanMethodInterceptor
 * @see LifecycleCallbackInterceptorMethodInterceptor
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class LifecycleMethodInterceptorsInvocation extends ConstructionInvocation
{

   /**
    * Bean context
    */
   protected BeanContext<?> beanContext;

   /**
    * Constructor 
    * 
    * @param beanContext The bean context. Cannot be null
    * @param interceptors The interceptors
    * @throws IllegalArgumentException if <code>beanContext</code> is null
    * 
    */
   public LifecycleMethodInterceptorsInvocation(BeanContext<?> beanContext, Interceptor[] interceptors)
   {
      super(interceptors, null);

      try
      {
         Object beanInstance = beanContext.getInstance();
         Constructor<?> constructor = beanInstance.getClass().getConstructor();
         // set the constructor and the target objects
         this.setConstructor(constructor);
         this.targetObject = beanInstance;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      this.beanContext = beanContext;
   }

   /**
    * Returns the bean context associated with this invocation
    * @return
    */
   public BeanContext<?> getBeanContext()
   {
      return this.beanContext;
   }

   @Override
   public Object invokeTarget() throws Throwable
   {
      // we don't invoke on the bean target. See javadoc of this class
      return null;
   }

   /**
    * Throws exception since we do not allow setting the target object
    * explicitly
    * 
    * Don't allow set/overriding the target object. The target is inferred
    * from the bean context passed to the constructor of this class
    */
   @Override
   public void setTargetObject(Object targetObject)
   {
      throw new RuntimeException("Target should not be explicitly set on " + this.getClass()
            + " - target will be infered from bean context");
   }
}
