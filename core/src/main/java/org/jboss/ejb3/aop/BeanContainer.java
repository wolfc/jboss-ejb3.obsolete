/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.Advisor;
import org.jboss.aop.Domain;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.aop.annotation.CachingAnnotationRepository;
import org.jboss.ejb3.cluster.metadata.ClusteredMetaDataBridge;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;
import org.jboss.ejb3.interceptors.direct.AbstractDirectContainer;
import org.jboss.ejb3.interceptors.metadata.AdditiveBeanInterceptorMetaDataBridge;
import org.jboss.ejb3.interceptors.metadata.InterceptorComponentMetaDataLoaderFactory;
import org.jboss.ejb3.interceptors.metadata.InterceptorMetaDataBridge;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.security.bridge.RunAsMetaDataBridge;
import org.jboss.ejb3.security.bridge.SecurityDomainMetaDataBridge;
import org.jboss.ejb3.tx.metadata.ApplicationExceptionComponentMetaDataLoaderFactory;
import org.jboss.ejb3.tx.metadata.ApplicationExceptionMetaDataBridge;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.ApplicationExceptionMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BeanContainer extends AbstractDirectContainer<Object, BeanContainer>
{
   private EJBContainer container;
   
   public BeanContainer(EJBContainer container)
   {
      super();
      assert container != null : "container is null";
      this.container = container;
      setBeanContextFactoryClass(BeanContainerBeanContextFactory.class);
   }
   
   public List<Class<?>> getInterceptorClasses()
   {
      return getInterceptorRegistry().getInterceptorClasses();
   }
   
   @Override
   protected Object createInterceptor(Class<?> interceptorClass) throws InstantiationException, IllegalAccessException
   {
      return container.createInterceptor(interceptorClass);
   }
   
   // TODO: re-evaluate this exposure
   @Deprecated
   public Advisor _getAdvisor()
   {
      return super.getAdvisor();
   }

   // TODO: re-evaluate this exposure
   @Deprecated
   public AnnotationRepository getAnnotationRepository()
   {
      return getAdvisor().getAnnotations();
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
   {
      return (T) getAdvisor().resolveAnnotation(annotationClass);
   }
   
   @SuppressWarnings("unchecked")
//   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz)
   public <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType)
   {
      if(clazz == getBeanClass())
         return (T) getAdvisor().resolveAnnotation(annotationType);
      // TODO: this is not right
      return clazz.getAnnotation(annotationType);
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Field field)
   {
      if (clazz == this.getBeanClass())
      {
         return (T) getAdvisor().resolveAnnotation(field, annotationType);
      }
      return field.getAnnotation(annotationType);
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Method method)
   {
      if (clazz == this.getBeanClass())
      {
         return (T) getAdvisor().resolveAnnotation(method, annotationType);
      }
      // TODO: this is not right
      return method.getAnnotation(annotationType);
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Field field)
   {
      return (T) getAdvisor().resolveAnnotation(field, annotationType);
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Method method)
   {
      T annotation = (T) getAdvisor().resolveAnnotation(method, annotationType);
      if(annotation == null && method.isBridge())
         annotation = getBridgedAnnotation(annotationType, method);
      return annotation;
   }
   
   private <T extends Annotation> T getBridgedAnnotation(Class<T> annotationType, Method bridgeMethod)
   {
      assert bridgeMethod.isBridge();
      Method[] methods = bridgeMethod.getDeclaringClass().getMethods();
      int i = 0;
      boolean found = false;
      Class<?>[] bridgeParams = bridgeMethod.getParameterTypes();
      while (i < methods.length && !found)
      {
         if (!methods[i].isBridge() && methods[i].getName().equals(bridgeMethod.getName()))
         {
            Class<?>[] params = methods[i].getParameterTypes();
            if (params.length == bridgeParams.length)
            {
               int j = 0;
               boolean matches = true;
               while (j < params.length && matches)
               {
                  if (!bridgeParams[j].isAssignableFrom(params[j]))
                     matches = false;
                  ++j;
               }
               
               if (matches)
                  return getAnnotation(annotationType, methods[i]);
            }
         }
         ++i;
      }
 
      return null;
   }
   
   public final EJBContainer getEJBContainer()
   {
      return container;
   }

   protected List<Method> getVirtualMethods()
   {
      return container.getVirtualMethods();
   }
   
   public void initialize(String name, Domain domain, Class<? extends Object> beanClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader)
   {
      AnnotationRepositoryToMetaData annotations = new AnnotationRepositoryToMetaData(beanClass, beanMetaData, name, classLoader);
      List<MetaDataBridge<InterceptorMetaData>> interceptorBridges = new ArrayList<MetaDataBridge<InterceptorMetaData>>();
      interceptorBridges.add(new InterceptorMetaDataBridge());
      annotations.addComponentMetaDataLoaderFactory(new InterceptorComponentMetaDataLoaderFactory(interceptorBridges));
      annotations.addMetaDataBridge(new AdditiveBeanInterceptorMetaDataBridge(beanClass, classLoader, beanMetaData));
      
      List<MetaDataBridge<ApplicationExceptionMetaData>> appExceptionBridges = new ArrayList<MetaDataBridge<ApplicationExceptionMetaData>>();
      appExceptionBridges.add(new ApplicationExceptionMetaDataBridge());
      annotations.addComponentMetaDataLoaderFactory(new ApplicationExceptionComponentMetaDataLoaderFactory(appExceptionBridges));
      
      annotations.addMetaDataBridge(new RunAsMetaDataBridge());
      //Add a security domain bridge
      annotations.addMetaDataBridge(new SecurityDomainMetaDataBridge());
      // Ensure that an @Clustered annotation is visible to AOP if the XML says the bean is  clustered.
      annotations.addMetaDataBridge(new ClusteredMetaDataBridge());
      
      initializeAdvisor(name, domain, beanClass, new CachingAnnotationRepository(annotations, classLoader));
   }

   @Override
   protected ManagedObjectAdvisor<Object, BeanContainer> createAdvisor(String name, Domain domain, Class<? extends Object> beanClass, AnnotationRepository annotations)
   {
      return new ExtendedManagedObjectAdvisor(this, name, domain, annotations);
   }
   
   /*
   @Override
   public Object invoke(Object target, Method method, Object[] arguments) throws Throwable
   {
      // TODO: make the AbstractContainer extendable
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = getAdvisor().getMethodInfo(methodHash);
      if(info == null)
         throw new IllegalArgumentException("method " + method + " is not under advisement by " + this);
      MethodInvocation invocation = new MethodInvocation(info, info.getInterceptors());
      invocation.setArguments(arguments);
      invocation.setTargetObject(target);
      return invocation.invokeNext();
   }
   */
   
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return getAdvisor().resolveAnnotation(annotationType) != null;
   }
   
   /**
    * FIXME: This method should not be needed. Initialization should already have happened
    * earlier in the game. (Ejb3DescriptorHandler adds annotations after a container object is constructed.)
    */
   @Deprecated
   public void reinitializeAdvisor()
   {
      // FIXME: Q&D
      try
      {
         ((ExtendedManagedObjectAdvisor) getAdvisor()).reinitialize();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
