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
package org.jboss.ejb3.interceptors.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;

import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.InterceptorFactoryRef;
import org.jboss.ejb3.interceptors.annotation.AnnotationAdvisor;
import org.jboss.ejb3.interceptors.annotation.AnnotationAdvisorSupport;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbacks;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.ejb3.interceptors.registry.InterceptorRegistry;
import org.jboss.logging.Logger;

/**
 * The base of all containers. Provides functions to allow for object
 * construction and invocation with interception.
 * 
 * Note that it's up to the actual implementation to expose any methods.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public abstract class AbstractContainer<T, C extends AbstractContainer<T, C>> extends AnnotationAdvisorSupport implements AnnotationAdvisor
{
   private static final Logger log = Logger.getLogger(AbstractContainer.class);
   
   private ManagedObjectAdvisor<T, C> advisor;
   
   private InterceptorRegistry interceptorRegistry;
   
   //private Class<BeanContextFactory<T, C>> beanContextFactoryClass = SimpleBeanContextFactory.class;
   private Class<? extends BeanContextFactory> beanContextFactoryClass = SimpleBeanContextFactory.class;
   private BeanContextFactory<T, C> beanContextFactory;
   
   /**
    * Maintains a array of interceptors applicable to this bean context
    * for each of the lifecycle callbacks
    */
   protected Map<Class<? extends Annotation>, Interceptor[]> lifecycleCallbackInterceptors = new HashMap<Class<? extends Annotation>, Interceptor[]>();

   
   /**
    * For a completely customized startup.
    * 
    * Note that before construction ends advisor must be set!
    */
   protected AbstractContainer()
   {
      
   }
   
   protected AbstractContainer(String name, Domain domain, Class<? extends T> beanClass)
   {
      initializeAdvisor(name, domain, beanClass);
   }
   
   protected AbstractContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      this(name, getDomain(domainName), beanClass);
   }
   
   protected BeanContext<T> construct(Constructor<? extends T> constructor, Object ... initargs)
   {
      int idx = advisor.getConstructorIndex(constructor);
      assert idx != -1 : "can't find constructor in the advisor";
      try
      {
         return getBeanContextFactory().createBean();
      }
      catch(Throwable t)
      {
         // TODO: disect
         if(t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw new RuntimeException(t);
      }
   }
   
   protected Object createInterceptor(Class<?> interceptorClass) throws InstantiationException, IllegalAccessException
   {
      return interceptorClass.newInstance();
   }
   
   protected void destroy(BeanContext<T> bean)
   {
      getBeanContextFactory().destroyBean(bean);
   }
   
   private BeanContextFactory<T, C> getBeanContextFactory()
   {
      if(beanContextFactory == null)
      {
         synchronized (this)
         {
            if(beanContextFactory == null)
            {
               if(beanContextFactoryClass == null)
                  throw new IllegalStateException("beanContextFactoryClass has not been set");
               try
               {
                  beanContextFactory = beanContextFactoryClass.newInstance();
                  beanContextFactory.setContainer((C) this);
               }
               catch (InstantiationException e)
               {
                  throw new RuntimeException(e.getCause());
               }
               catch (IllegalAccessException e)
               {
                  throw new RuntimeException(e);
               }
            }
         }
      }
      return beanContextFactory;
   }
   
   /**
    * Finalize construction of the abstract container by setting the advisor.
    * 
    * @param name       the name of the advisor
    * @param domain     the domain for the advisor
    * @param beanClass  the class being advised
    */
   protected void initializeAdvisor(String name, Domain domain, Class<? extends T> beanClass)
   {
      initializeAdvisor(name, domain, beanClass, null);
   }
   
   protected final void initializeAdvisor(String name, Domain domain, Class<? extends T> beanClass, AnnotationRepository annotations)
   {
      if(this.advisor != null) throw new IllegalStateException("advisor already set to " + advisor);
      
      assert name != null : "name is null";
      assert domain != null : "domain is null";
      assert beanClass != null : "beanClass is null";
      
      // Decouple setting the advisor and initializing it, so interceptors
      // can get it.
      createAndSetAdvisor(name, domain, beanClass, annotations);
      advisor.getAnnotations().addClassAnnotation(InterceptorFactoryRef.class, new InterceptorFactoryRefImpl(ContainerInterceptorFactory.class));
      advisor.initialize(beanClass);
   }
   
   protected final void createAndSetAdvisor(String name, Domain domain, Class<? extends T> beanClass, AnnotationRepository annotations)
   {
      this.advisor = createAdvisor(name, domain, beanClass, annotations);
   }
   
   protected ManagedObjectAdvisor<T, C> createAdvisor(String name, Domain domain, Class<? extends T> beanClass, AnnotationRepository annotations)
   {
      return new ManagedObjectAdvisor<T, C>((C) this, name, domain, annotations); 
   }
   
   protected final ManagedObjectAdvisor<T, C> getAdvisor()
   {
      if(advisor == null) throw new IllegalStateException("advisor has not been initialized");
      return advisor;
   }
   
   @SuppressWarnings("unchecked")
   protected Class<? extends T> getBeanClass()
   {
      return getAdvisor().getClazz();
   }
   
   @SuppressWarnings("unchecked")
   public static <C extends AbstractContainer<?, ?>> C getContainer(Advisor advisor)
   {
      return (C) ((ManagedObjectAdvisor) advisor).getContainer();
   }
   
   /*
    * TODO: this should not be here, it's an AspectManager helper function.
    */
   protected static final Domain getDomain(String domainName)
   {
      DomainDefinition domainDefinition = AspectManager.instance().getContainer(domainName);
      if(domainDefinition == null)
         throw new IllegalArgumentException("Domain definition '" + domainName + "' can not be found");
      
      final Domain domain = (Domain) domainDefinition.getManager();
      return domain;
   }
   
   public InterceptorRegistry getInterceptorRegistry()
   {
      if(interceptorRegistry == null)
      {
         synchronized (this)
         {
            if(interceptorRegistry == null)
               interceptorRegistry = new InterceptorRegistry(getAdvisor());
         }
      }
      return interceptorRegistry;
   }
   
   /**
    * Invokes the lifecycle callback(s) represented by the <code>callbackAnnotationClass</code>
    * through (cached) AOP interceptor chain.
    * 
    * Internally, the AOP interceptor chain consists of the LifecycleCallback AOP stack
    * interceptors, the javax.interceptor.Interceptor(s) and the lifecycle methods on the bean 
    * implementation class
    * 
    * @see AbstractContainer#getLifecycleInterceptors(Class) for more details.  
    * 
    * @param beanContext The bean context
    * @param callbackAnnotationClass The lifecycle callback (ex: @PostConstruct, @PrePassivate etc...)
    */
   public void invokeCallback(BeanContext<?> beanContext, Class<? extends Annotation> callbackAnnotationClass)
   {
      try
      {
         Interceptor interceptors[] = this.getLifecycleInterceptors(callbackAnnotationClass);
         LifecycleMethodInterceptorsInvocation invocation = new LifecycleMethodInterceptorsInvocation(beanContext,
               interceptors);
         invocation.setAdvisor(this.getAdvisor());
         invocation.invokeNext();
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }

   /**
    * Returns the interceptor instances (which is a combination of our internal
    * AOP interceptors and bean developer defined {@link javax.interceptor.Interceptors}),
    * corresponding to the <code>lifecycleCallbackAnnotation</code>.
    * 
    * Internally caches the interceptor instances corresponding to each of the lifecycle 
    * callbacks, for this bean context 
    *  
    * @param lifecycleCallbackAnnotation Lifecycle callback annotations like {@link PrePassivate},
    *       {@link PostActivate}, {@link PreDestroy}, {@link PostConstruct} 
    *       
    * @return Returns an empty array if there are no interceptor instances associated with this
    *       bean context, for the <code>lifecycleCallbackAnnotation</code>. Else, returns the
    *       array of interceptors applicable to this bean context for the 
    *       <code>lifecycleCallbackAnnotation</code>
    */
   protected Interceptor[] getLifecycleInterceptors(Class<? extends Annotation> lifecycleCallbackAnnotation)
   {
      Interceptor[] interceptors = this.lifecycleCallbackInterceptors.get(lifecycleCallbackAnnotation);
      // If null then we haven't yet initialized the lifecycle callback interceptors, since
      // we intentionally do a lazy initialization per lifecycle callback. The initialization
      // happens only once per bean for each lifecycle callback annotation type
      if (interceptors == null)
      {
         List<Class<?>> lifecycleInterceptorClasses = this.getInterceptorRegistry().getLifecycleInterceptorClasses();
         // create the interceptor chain
         interceptors = LifecycleCallbacks.createLifecycleCallbackInterceptors(this.getAdvisor(),
               lifecycleInterceptorClasses, lifecycleCallbackAnnotation);
         if (interceptors == null)
         {
            // No interceptors available, so create an empty chain and maintain in the map,
            // to avoid trying to init again the next time this method
            // is called for this specific lifecycle callback
            interceptors = new Interceptor[0];
         }
         this.lifecycleCallbackInterceptors.put(lifecycleCallbackAnnotation, interceptors);
      }
      return this.lifecycleCallbackInterceptors.get(lifecycleCallbackAnnotation);
   }
   
   /**
    * Call a method upon a target object with all interceptors in place.
    * 
    * @param target     the target to invoke upon
    * @param method     the method to invoke
    * @param arguments  arguments to the method
    * @return           return value of the method
    * @throws Throwable if anything goes wrong
    */
   protected Object invoke(BeanContext<T> target, Method method, Object arguments[]) throws Throwable
   {
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = getAdvisor().getMethodInfo(methodHash);
      if(info == null)
         throw new IllegalArgumentException("method " + method + " is not under advisement by " + this);
      ContainerMethodInvocation invocation = new ContainerMethodInvocation(info, target, arguments);
      return invocation.invokeNext();
   }
   
   /**
    * A convenient, but unchecked and slow method to call a method upon a target.
    * 
    * (Slow method)
    * 
    * @param <R>        the return type
    * @param target     the target to invoke upon
    * @param methodName the method name to invoke
    * @param args       the arguments to the method
    * @return           the return value
    * @throws Throwable if anything goes wrong
    */
   @SuppressWarnings("unchecked")
   protected <R> R invoke(BeanContext<T> target, String methodName, Object ... args) throws Throwable
   {
      Method method = ClassHelper.getMethod(target.getInstance().getClass(), methodName);
      return (R) invoke(target, method, args);
   }
   
   public void setBeanContextFactoryClass(Class<? extends BeanContextFactory<T, C>> beanContextFactoryClass)
   {
      this.beanContextFactoryClass = beanContextFactoryClass;
   }
}
