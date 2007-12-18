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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * An interceptor container keeps track of container interceptors
 * on a class.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorContainer implements AnnotatedElement
{
   private Class<?> beanClass;
   private Object[] interceptors;
   
   public class BeanClassInvocationContext implements InvocationContext
   {
      private Class<? extends Annotation> annotationClass;
      private Method method;
      private Object args[];
      
      private Map<String, Object> contextData = new HashMap<String, Object>();
      
      private Iterator<Object> it = Arrays.asList(interceptors).iterator();
      
      protected BeanClassInvocationContext(Class<? extends Annotation> annotationClass)
      {
         assert annotationClass != null : "annotationClass is null";
         
         this.annotationClass = annotationClass;
      }
      
      protected BeanClassInvocationContext(Class<? extends Annotation> annotationClass, Method method, Object args[])
      {
         this(annotationClass);
         
         assert method != null : "method is null";
         assert args != null : "args is null";
         
         this.method = method;
         this.args = args;
      }
      
      public Map<String, Object> getContextData()
      {
         // Must not return null
         return contextData;
      }

      public Method getMethod()
      {
         // For lifecycle callback methods, returns null
         
         return method;
      }

      public Object[] getParameters()
      {
         if(method == null && args == null)
         {
            // If invoked in a lifecycle callback
            throw new IllegalStateException();
         }
         return args;
      }

      public InterceptorContainer getTarget()
      {
         return InterceptorContainer.this;
      }

      public Object proceed() throws Exception
      {
         if(!it.hasNext())
            return null;
         Object interceptor = it.next();
         Method callback = findMethod(interceptor.getClass(), annotationClass);
         if(callback == null)
            return proceed();
         Object args[] = { BeanClassInvocationContext.this };
         return callback.invoke(interceptor, args);
      }

      public void setParameters(Object[] params)
      {
         // If invoked in a lifecycle callback
         throw new IllegalStateException();
      }
      
   }
   
   public InterceptorContainer(Class<?> beanClass) throws Exception
   {
      assert beanClass != null;
      
      this.beanClass = beanClass;
      
      // TODO: use a delegate to get annotations (so we can hook into meta data)
      ContainerInterceptors interceptors = beanClass.getAnnotation(ContainerInterceptors.class);
      if(interceptors == null)
         throw new IllegalArgumentException("Class " + beanClass + " doesn't have any container interceptors");
      
      Class<?> interceptorClasses[] = interceptors.value();
      this.interceptors = new Object[interceptorClasses.length];
      for(int i = 0; i < interceptorClasses.length; i++)
      {
         this.interceptors[i] = interceptorClasses[i].newInstance();
      }
      
      // TODO: inject interceptors
      
      // Post construct
      new BeanClassInvocationContext(PostConstruct.class).proceed();
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
   
   public Object invoke(Method method, Object[] args) throws Throwable
   {
      return new BeanClassInvocationContext(AroundInvoke.class, method, args).proceed();
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

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
   {
      return beanClass.isAnnotationPresent(annotationClass);
   }
}
