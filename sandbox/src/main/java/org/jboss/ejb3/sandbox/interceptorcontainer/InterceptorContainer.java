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
package org.jboss.ejb3.sandbox.interceptorcontainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.interceptor.Interceptors;

import org.jboss.aop.Advised;
import org.jboss.ejb3.interceptors.ManagedObject;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.IndirectContainer;
import org.jboss.ejb3.sandbox.interceptorcontainer.impl.ContainersInterceptorsInterceptor;

/**
 * An interceptor container keeps track of container interceptors
 * on a class.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@Interceptors(ContainersInterceptorsInterceptor.class)
@ManagedObject
public class InterceptorContainer implements AnnotatedElement, IndirectContainer<InterceptorContainer, InterceptorContainerContainer>
{
   private Class<?> beanClass;
   private Object[] interceptors;
   private InterceptorContainerContainer directContainer;
   
   // the bean context of this interceptor container instance
   private BeanContext<InterceptorContainer> beanContext;
   
   private static final Method INVOKE_METHOD;
   
   static
   {
      try
      {
         INVOKE_METHOD = InterceptorContainer.class.getDeclaredMethod("invoke", new Class<?>[] { Method.class, new Object[0].getClass() });
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public InterceptorContainer()
   {
      super();
   }
   
   // this is not in EJB style
   public InterceptorContainer(Class<?> beanClass) throws Exception
   {
      assert beanClass != null;
      
      this.beanClass = beanClass;
   }

   private static <A extends Annotation> Method findMethod(Class<?> cls, Class<A> annotationClass)
   {
      if(cls == null)
         return null;
      
      for(Method method : cls.getDeclaredMethods())
      {
         if(method.getAnnotation(annotationClass) != null)
            return method;
      }
      
      return findMethod(cls.getSuperclass(), annotationClass);
   }

   public Class<?> getBeanClass()
   {
      return beanClass;
   }
   
   @Resource(name="beanClass")
   public void setBeanClass(Class<?> beanClass)
   {
      this.beanClass = beanClass;
   }
   
   public Object invoke(Method method, Object[] args) throws Throwable
   {
      //return new BeanClassInvocationContext(AroundInvoke.class, method, args).proceed();
      //throw new RuntimeException("Interceptor chain does not contain an instance interceptor");
      // TODO: I actually want to invoke the direct container if we're not already running a chain
      // and then continue invocation if we are.
      // I can run in two modes: direct advisement or indirect advisement
      if(directContainer != null)
      {
         // I'm indirectly advised, let's delegate to the direct container
         Object arguments[] = { method, args };
         return directContainer.invokeIndirect(beanContext, INVOKE_METHOD, arguments);
      }
      else
      {
         // I'm directly advised
         assert this instanceof Advised;
         return null;
      }
   }

   public void setBeanContext(BeanContext<InterceptorContainer> beanContext)
   {
      assert beanContext.getInstance() == this;
      this.beanContext = beanContext;
   }
   
   public void setDirectContainer(InterceptorContainerContainer container)
   {
      assert container != null : "directContainer is null";
      this.directContainer = container;
   }
   
   /*
    * Helpers
    */
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
   {
      return beanClass.getAnnotation(annotationClass);
   }

   public Annotation[] getAnnotations()
   {
      return beanClass.getAnnotations();
   }

   public Annotation[] getDeclaredAnnotations()
   {
      return beanClass.getAnnotations();
   }

   public boolean isAnnotationPresent(Class<?> cls, Class<? extends Annotation> annotationClass)
   {
      return cls.isAnnotationPresent(annotationClass);
   }
   
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
   {
      return beanClass.isAnnotationPresent(annotationClass);
   }
   
   public boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationClass)
   {
      return method.isAnnotationPresent(annotationClass);
   }
}
