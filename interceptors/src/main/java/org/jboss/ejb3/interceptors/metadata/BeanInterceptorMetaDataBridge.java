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
package org.jboss.ejb3.interceptors.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.interceptors.annotation.impl.InterceptorsImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PostActivateImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PostConstructImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PreDestroyImpl;
import org.jboss.ejb3.interceptors.annotation.impl.PrePassivateImpl;
import org.jboss.ejb3.interceptors.aop.annotation.DefaultInterceptors;
import org.jboss.ejb3.interceptors.aop.annotation.DefaultInterceptorsImpl;
import org.jboss.ejb3.interceptors.aop.annotation.InterceptorOrder;
import org.jboss.ejb3.interceptors.aop.annotation.InterceptorOrderImpl;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.ejb3.interceptors.util.InterceptorCollection;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.AroundInvokesMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingsMetaData;
import org.jboss.metadata.ejb.spec.InterceptorClassesMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;
import org.jboss.metadata.spi.signature.DeclaredMethodSignature;
import org.jboss.metadata.spi.signature.Signature;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class BeanInterceptorMetaDataBridge extends EnvironmentInterceptorMetaDataBridge<JBossEnterpriseBeanMetaData> implements MetaDataBridge<JBossEnterpriseBeanMetaData>
{
   private static final Logger log = Logger.getLogger(BeanInterceptorMetaDataBridge.class);

   private volatile boolean initialisedBean;
   
   //Class level stuff
   private DefaultInterceptors defaultInterceptors;
   private Interceptors interceptors;
   private InterceptorOrder interceptorOrder;
   private ExcludeDefaultInterceptors excludeDefaultInterceptors;
   
   //Method-level things
   private Map<DeclaredMethodSignature, InterceptorsImpl> methodInterceptors = new HashMap<DeclaredMethodSignature, InterceptorsImpl>(); 
   private Map<DeclaredMethodSignature, InterceptorOrderImpl> methodInterceptorOrders = new HashMap<DeclaredMethodSignature, InterceptorOrderImpl>();
   private Map<DeclaredMethodSignature, ExcludeDefaultInterceptors> methodExcludeDefaultInterceptors = new HashMap<DeclaredMethodSignature, ExcludeDefaultInterceptors>();
   private Map<DeclaredMethodSignature, ExcludeClassInterceptors> methodExcludeClassInterceptors = new HashMap<DeclaredMethodSignature, ExcludeClassInterceptors>();
   
   
   //Bean class methods
   private Map<DeclaredMethodSignature, AroundInvoke> aroundInvokes;
   private Map<DeclaredMethodSignature, PostConstruct> postConstructs;
   private Map<DeclaredMethodSignature, PostActivate> postActivates;
   private Map<DeclaredMethodSignature, PrePassivate> prePassivates;
   private Map<DeclaredMethodSignature, PreDestroy> preDestroys;
   
   private Class<?> beanClass;
   private ClassLoader classLoader;
   private JBossEnterpriseBeanMetaData beanMetaData;

   public static long time;
   
   public BeanInterceptorMetaDataBridge(Class<?> beanClass, ClassLoader classLoader, JBossEnterpriseBeanMetaData beanMetaData)
   {
      assert beanClass != null : "beanClass is null";
      this.beanClass = beanClass;
      this.classLoader = classLoader;
      this.beanMetaData = beanMetaData;
      
      initialise();
   }

   protected Class<?> getBeanClass()
   {
      return beanClass;
   }
   
   private static boolean add(InterceptorCollection interceptors, ClassLoader classLoader, InterceptorBindingMetaData binding)
   {
      boolean result = false;
      InterceptorClassesMetaData interceptorClassesMetaData;
      if(binding.isTotalOrdering())
      {
         interceptorClassesMetaData = binding.getInterceptorOrder();
      }
      else
      {
         interceptorClassesMetaData = binding.getInterceptorClasses();
      }
      if(interceptorClassesMetaData != null)
      {
         for(String interceptorClassName : interceptorClassesMetaData)
         {
            result |= interceptors.addValue(loadClass(classLoader, interceptorClassName));
         }
      }
      return result;
   }
   
   protected static boolean add(List<Class<?>> interceptors, ClassLoader classLoader, InterceptorBindingMetaData binding)
   {
      InterceptorClassesMetaData interceptorClassesMetaData;
      if(binding.isTotalOrdering())
      {
         interceptorClassesMetaData = binding.getInterceptorOrder();
      }
      else
      {
         interceptorClassesMetaData = binding.getInterceptorClasses();
      }
      for(String interceptorClassName : interceptorClassesMetaData)
      {
         interceptors.add(loadClass(classLoader, interceptorClassName));
      }
      return true;
   }
   
   private static Class<?> loadClass(ClassLoader classLoader, String name)
   {
      try
      {
         return classLoader.loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private synchronized void initialise()
   {
      if (initialisedBean)
      {
         return;
      }
      
      List<InterceptorBindingMetaData> defaultInterceptorBindingMetaData = new ArrayList<InterceptorBindingMetaData>();
      List<InterceptorBindingMetaData> classInterceptorBindingMetaData = new ArrayList<InterceptorBindingMetaData>();
      List<InterceptorBindingMetaData> classInterceptorOrderMetaData = new ArrayList<InterceptorBindingMetaData>();
      List<InterceptorBindingMetaData> methodInterceptorBindingMetaData = new ArrayList<InterceptorBindingMetaData>();
      List<InterceptorBindingMetaData> methodInterceptorOrderMetaData = new ArrayList<InterceptorBindingMetaData>();
      
      
      setupMetaDataLists(beanMetaData, 
            defaultInterceptorBindingMetaData, 
            classInterceptorBindingMetaData, 
            classInterceptorOrderMetaData, 
            methodInterceptorBindingMetaData, 
            methodInterceptorOrderMetaData);

      initialiseDefaultInterceptors(defaultInterceptorBindingMetaData);
      initialiseInterceptors(classInterceptorBindingMetaData);
      initialiseInterceptorOrder(classInterceptorOrderMetaData);

      Method realMethods[] = ClassHelper.getAllMethods(beanClass);
      List<DeclaredMethodSignature> methods = new ArrayList<DeclaredMethodSignature>();
      for(Method realMethod : realMethods)
      {
         methods.add(new DeclaredMethodSignature(realMethod));
      }
      
      initialiseMethodInterceptors(methods, methodInterceptorBindingMetaData);
      initialiseMethodInterceptorOrders(methods, methodInterceptorOrderMetaData);

      initialiseAroundInvoke(methods);
   }
   
   private void setupMetaDataLists(JBossEnterpriseBeanMetaData beanMetaData,
                                 List<InterceptorBindingMetaData> defaultInterceptorBindingMetaData, 
                                 List<InterceptorBindingMetaData> classInterceptorBindingMetaData, 
                                 List<InterceptorBindingMetaData> classInterceptorOrderMetaData, 
                                 List<InterceptorBindingMetaData> methodInterceptorBindingMetaData,
                                 List<InterceptorBindingMetaData> methodInterceptorOrderMetaData)
   {
      try
      {
         InterceptorBindingsMetaData bindings = beanMetaData.getEjbJarMetaData().getAssemblyDescriptor().getInterceptorBindings();

         if (bindings != null)
         {
            String ejbName = beanMetaData.getEjbName();;
            for (InterceptorBindingMetaData binding : bindings)
            {
               String bindingEjbName = binding.getEjbName();
               checkBeanExistsInDeployment(beanMetaData, bindingEjbName);
               if (bindingEjbName.equals("*")) 
               {
                  assert binding.getMethod() == null : "method binding not allowed on default interceptor";
                  defaultInterceptorBindingMetaData.add(binding);
                  continue;
               }
               if (bindingEjbName.equals(ejbName))
               {
                  if (binding.getMethod() == null)
                  {
                     if (binding.isTotalOrdering())
                     {
                        classInterceptorOrderMetaData.add(binding);
                     }
                     else
                     {
                        classInterceptorBindingMetaData.add(binding);
                     }
                  }
                  else
                  {
                     if (binding.isTotalOrdering())
                     {
                        methodInterceptorOrderMetaData.add(binding);
                     }
                     else
                     {
                        methodInterceptorBindingMetaData.add(binding);
                     }
                  }
               }
            }
         }
      }
      catch (NullPointerException e)
      {
         if (beanMetaData == null)
         {
            throw new IllegalStateException("Null beannMetaData", e);
         }
         else if (beanMetaData.getEjbJarMetaData() == null)
         {
            throw new IllegalStateException("Null ejbJarMetaData", e);
         }
         else if (beanMetaData.getEjbJarMetaData().getAssemblyDescriptor() == null)
         {
            throw new IllegalStateException("Null AssemblyDescriptor", e);
         }
         throw e;
      }
   }

   
   private void initialiseDefaultInterceptors(List<InterceptorBindingMetaData> bindings)
   {
      if (bindings != null && bindings.size() > 0)
      {
         List<Class<?>> classes = new ArrayList<Class<?>>();
         for (InterceptorBindingMetaData binding : bindings)
         {
            add(classes, classLoader, binding);
         }
         if(!classes.isEmpty())
            defaultInterceptors = new DefaultInterceptorsImpl(classes);
      }
   }
   
   private void initialiseInterceptors(List<InterceptorBindingMetaData> bindings)
   {
      if (bindings != null && bindings.size() > 0)
      {
         InterceptorsImpl interceptors = new InterceptorsImpl();
         for (InterceptorBindingMetaData binding : bindings)
         {
            add(interceptors, classLoader, binding);
            checkClassLevelExcludeDefaultInterceptors(binding);
         }
         if(!interceptors.isEmpty())
            this.interceptors = interceptors;
      }
   }
   
   private void initialiseInterceptorOrder(List<InterceptorBindingMetaData> bindings)
   {
      if (bindings != null && bindings.size() > 0)
      {
         InterceptorOrderImpl interceptors = new InterceptorOrderImpl();
         for (InterceptorBindingMetaData binding : bindings)
         {
            add(interceptors, classLoader, binding);
            checkClassLevelExcludeDefaultInterceptors(binding);
         }
         if(!interceptors.isEmpty())
            this.interceptorOrder = interceptors;
      }
   }

   private void checkClassLevelExcludeDefaultInterceptors(InterceptorBindingMetaData binding)
   {
      ExcludeDefaultInterceptors exDefaultInterceptors = checkExcludeDefaultInterceptors(binding);
      if (exDefaultInterceptors != null)
      {
         excludeDefaultInterceptors = exDefaultInterceptors;
      }
   }
   
   private ExcludeDefaultInterceptors checkExcludeDefaultInterceptors(InterceptorBindingMetaData binding)
   {
      if (binding.isExcludeDefaultInterceptors())
      {
         return new ExcludeDefaultInterceptors() {

            public Class<? extends Annotation> annotationType()
            {
               return ExcludeDefaultInterceptors.class;
            }};
      }
      return null;
   }
   
   private void addMethodLevelExclusions(DeclaredMethodSignature sig, InterceptorBindingMetaData binding)
   {
      ExcludeDefaultInterceptors exDefaultInterceptors = checkExcludeDefaultInterceptors(binding);
      if (exDefaultInterceptors != null)
      {
         methodExcludeDefaultInterceptors.put(sig, exDefaultInterceptors);
      }
      if (binding.isExcludeClassInterceptors())
      {
         methodExcludeClassInterceptors.put(sig, new ExcludeClassInterceptors() {

            public Class<? extends Annotation> annotationType()
            {
               return ExcludeClassInterceptors.class;
            }});
      }
   }
   
   private void initialiseMethodInterceptors(List<DeclaredMethodSignature> methods, List<InterceptorBindingMetaData> bindings)
   {
      if (bindings != null && bindings.size() > 0)
      {
         this.methodInterceptors = new HashMap<DeclaredMethodSignature, InterceptorsImpl>();
         for (InterceptorBindingMetaData binding : bindings)
         {
            NamedMethodMetaData method = binding.getMethod();

            // TODO: this is weird, it should have been caught earlier (invalid xml)
            if(method.getMethodName() == null)
               continue;
            
            for (DeclaredMethodSignature refMethod : methods)
            {
               if(!matches(refMethod, method))
                  continue;
               InterceptorsImpl interceptors = methodInterceptors.get(refMethod);
               if (interceptors == null)
               {
                  interceptors = new InterceptorsImpl();
                  methodInterceptors.put(refMethod, interceptors);
               }
               add(interceptors, classLoader, binding);
               addMethodLevelExclusions(refMethod, binding);
            }
         }
      }
   }
   
   private void initialiseMethodInterceptorOrders(List<DeclaredMethodSignature> methods, List<InterceptorBindingMetaData> bindings)
   {
      if (bindings != null && bindings.size() > 0)
      {
         this.methodInterceptorOrders = new HashMap<DeclaredMethodSignature, InterceptorOrderImpl>();
         for (InterceptorBindingMetaData binding : bindings)
         {
            NamedMethodMetaData method = binding.getMethod();

            // TODO: this is weird, it should have been caught earlier (invalid xml)
            if(method.getMethodName() == null)
               continue;
            
            for (DeclaredMethodSignature refMethod : methods)
            {
               if(!matches(refMethod, method))
                  continue;
               InterceptorOrderImpl interceptors = methodInterceptorOrders.get(refMethod);
               if (interceptors == null)
               {
                  interceptors = new InterceptorOrderImpl();
                  methodInterceptorOrders.put(refMethod, interceptors);
               }
               add(interceptors, classLoader, binding);
            }
         }
      }
   }
   
   private void initialiseAroundInvoke(List<DeclaredMethodSignature> methods)
   {
      AroundInvokesMetaData aroundInvokes = null;
//    if(beanMetaData instanceof JBossGenericBeanMetaData)
//       aroundInvokes = ((JBossGenericBeanMetaData) beanMetaData).getAroundInvokes();
      if(beanMetaData instanceof JBossMessageDrivenBeanMetaData)
         aroundInvokes = ((JBossMessageDrivenBeanMetaData) beanMetaData).getAroundInvokes();
      else if(beanMetaData instanceof JBossSessionBeanMetaData)
         aroundInvokes = ((JBossSessionBeanMetaData) beanMetaData).getAroundInvokes();
      if(aroundInvokes != null)
      {
         for (DeclaredMethodSignature method : methods)
         {
            AroundInvoke aroundInvoke = getAroundInvokeAnnotation(aroundInvokes, method);
            if(aroundInvoke != null)
            {
               if (this.aroundInvokes == null)
               {
                  this.aroundInvokes = new HashMap<DeclaredMethodSignature, AroundInvoke>();
               }
               this.aroundInvokes.put(method, aroundInvoke);
            }
         }
      }
   }
   
   private void initialiseLifecycleAnnotations(List<DeclaredMethodSignature> methods)
   {
      if(beanMetaData instanceof JBossSessionBeanMetaData)
      {
         for (DeclaredMethodSignature method : methods)
         {
            
            PostConstruct postConstruct = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPostConstructs(), PostConstructImpl.class, method);
            if (postConstruct != null)
            {
               if (postConstructs == null)
               {
                  postConstructs = new HashMap<DeclaredMethodSignature, PostConstruct>();
               }
               postConstructs.put(method, postConstruct);
            }
            PostActivate postActivate = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPostActivates(), PostActivateImpl.class, method);
            if(postActivate != null)
            {
               if (postActivates == null)
               {
                  postActivates = new HashMap<DeclaredMethodSignature, PostActivate>();
               }
               postActivates.put(method, postActivate);
            }
            PrePassivate prePassivate = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPrePassivates(), PrePassivateImpl.class, method);
            if(prePassivate != null)
            {
               if (prePassivates == null)
               {
                  prePassivates = new HashMap<DeclaredMethodSignature, PrePassivate>();
               }
               prePassivates.put(method, prePassivate);
            }
            PreDestroy preDestroy = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPreDestroys(), PreDestroyImpl.class, method);
            if(preDestroy != null)
            {
               if (preDestroys == null)
               {
                  preDestroys = new HashMap<DeclaredMethodSignature, PreDestroy>();
               }
               preDestroys.put(method, preDestroy);
            }
         }
      }
   }

   private static boolean matches(DeclaredMethodSignature signature, NamedMethodMetaData method)
   {
      if(!signature.getName().equals(method.getMethodName()))
         return false;
      if(method.getMethodParams() == null)
      {
         if(signature.getParameters() == null || signature.getParameters().length == 0)
            return true;
         else
            return false;
               
      }
      return Arrays.equals(signature.getParameters(), method.getMethodParams().toArray());
   }
   
   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader)
   {
      if(annotationClass == DefaultInterceptors.class)
      {
         return annotationClass.cast(defaultInterceptors);
      }
      else if(annotationClass == InterceptorOrder.class)
      {
         return annotationClass.cast(interceptorOrder);
      }
      else if(annotationClass == Interceptors.class)
      {
         return annotationClass.cast(interceptors);
      }
      else if (annotationClass == ExcludeDefaultInterceptors.class)
      {
         return annotationClass.cast(excludeDefaultInterceptors);
      }
      return super.retrieveAnnotation(annotationClass, beanMetaData, classLoader);
   }

   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader, DeclaredMethodSignature method)
   {
      if(annotationClass == AroundInvoke.class)
      {
         String parameterTypes[] = method.getParameters();
         if (parameterTypes.length == 1 && parameterTypes[0].equals(InvocationContext.class.getName()) && aroundInvokes != null)
         {
            return annotationClass.cast(aroundInvokes.get(method));
         }
         return null;
      }
      else if(annotationClass == InterceptorOrder.class)
      {
         if (methodInterceptorOrders == null)
         {
            return null;
         }
         return annotationClass.cast(methodInterceptorOrders.get(method));
      }
      else if(annotationClass == Interceptors.class)
      {
         if (methodInterceptors == null)
         {
            return null;
         }
         return annotationClass.cast(methodInterceptors.get(method));
      }
      else if (annotationClass == ExcludeDefaultInterceptors.class)
      {
         return annotationClass.cast(methodExcludeDefaultInterceptors.get(method));
      }
      else if (annotationClass == ExcludeClassInterceptors.class)
      {
         return annotationClass.cast(methodExcludeClassInterceptors.get(method));
      }
      else if(annotationClass == PostActivate.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && method.getParameters().length == 0 && postActivates != null) 
         {
            return annotationClass.cast(postActivates.get(method));
         }
      }
      else if(annotationClass == PostConstruct.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && method.getParameters().length == 0 && postConstructs != null) 
         {
            return annotationClass.cast(postConstructs.get(method));
         }
      }
      else if(annotationClass == PreDestroy.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && method.getParameters().length == 0 && preDestroys != null) 
         {
            return annotationClass.cast(preDestroys.get(method));
         }
      }
      else if(annotationClass == PrePassivate.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && method.getParameters().length == 0 && prePassivates != null) 
         {
            return annotationClass.cast(prePassivates.get(method));
         }
      }
      return super.retrieveAnnotation(annotationClass, beanMetaData, classLoader, method);
   }
   
   private void checkBeanExistsInDeployment(JBossEnterpriseBeanMetaData beanMetaData, String ejbName)
   {
      if (ejbName.equals("*"))
      {
         return;
      }
      
      JBossEnterpriseBeansMetaData beansMetaData = beanMetaData.getEnterpriseBeansMetaData();
      if (beansMetaData.get(ejbName) == null)
      {
         throw new IllegalArgumentException("No bean with name specified in interceptor-binding: " + ejbName);
      }
   }
   
   private static class MethodSignatures
   {
      Map<Method, Signature> methodSignatures = new HashMap<Method, Signature>();
      
      Signature getSignature(Method m)
      {
         Signature s = methodSignatures.get(m);
         if (s == null)
         {
            s = new DeclaredMethodSignature(m);
            methodSignatures.put(m, s);
         }
         return s;
      }
   }
}
