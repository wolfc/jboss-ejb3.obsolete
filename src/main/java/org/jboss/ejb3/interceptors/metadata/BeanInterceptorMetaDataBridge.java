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
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;
import org.jboss.metadata.spi.signature.MethodSignature;
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
   private Map<Signature, Interceptors> methodInterceptors = new HashMap<Signature, Interceptors>(); 
   private Map<Signature, InterceptorOrder> methodInterceptorOrders = new HashMap<Signature, InterceptorOrder>();
   private Map<Signature, ExcludeDefaultInterceptors> methodExcludeDefaultInterceptors = new HashMap<Signature, ExcludeDefaultInterceptors>();
   private Map<Signature, ExcludeClassInterceptors> methodExcludeClassInterceptors = new HashMap<Signature, ExcludeClassInterceptors>();
   
   
   //Bean class methods
   private Map<String, AroundInvoke> aroundInvokes;
   private Map<String, PostConstruct> postConstructs;
   private Map<String, PostActivate> postActivates;
   private Map<String, PrePassivate> prePassivates;
   private Map<String, PreDestroy> preDestroys;
   
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

      Map<String, List<Method>> methodMap = ClassHelper.getAllMethodsMap(beanClass);
      MethodSignatures methodSignatures = new MethodSignatures();
      initialiseMethodInterceptors(methodMap, methodSignatures, methodInterceptorBindingMetaData);
      initialiseMethodInterceptorOrders(methodMap, methodSignatures, methodInterceptorOrderMetaData);

      initialiseAroundInvoke(methodMap);
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
   
   private void addMethodLevelExclusions(Signature sig, InterceptorBindingMetaData binding)
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
   
   private void initialiseMethodInterceptors(Map<String, List<Method>> methodMap, MethodSignatures methodSignatures, List<InterceptorBindingMetaData> bindings)
   {
      if (bindings != null && bindings.size() > 0)
      {
         this.methodInterceptors = new HashMap<Signature, Interceptors>();
         for (InterceptorBindingMetaData binding : bindings)
         {
            NamedMethodMetaData method = binding.getMethod();

            // TODO: this is weird, it should have been caught earlier (invalid xml)
            if(method.getMethodName() == null)
               continue;
            
            List<Method> methods = methodMap.get(method.getMethodName());
            
            if (methods == null)
            {
               throw new IllegalStateException("Bean class " + beanClass.getName() + " does not have a method called '" + method.getMethodName() + "'. This method name was used in an interceptor-binding entry.");
            }
            
            for (Method refMethod : methods)
            {
               Signature signature = methodSignatures.getSignature(refMethod);
               if (matchesMethod(signature, refMethod, method))
               {
                  InterceptorsImpl interceptors = (InterceptorsImpl)methodInterceptors.get(signature);
                  if (interceptors == null)
                  {
                     interceptors = new InterceptorsImpl();
                     methodInterceptors.put(signature, interceptors);
                  }
                  add(interceptors, classLoader, binding);
                  addMethodLevelExclusions(signature, binding);
               }
            }
         }
      }
   }
   
   private void initialiseMethodInterceptorOrders(Map<String, List<Method>> methodMap, MethodSignatures methodSignatures, List<InterceptorBindingMetaData> bindings)
   {
      if (bindings != null && bindings.size() > 0)
      {
         this.methodInterceptorOrders = new HashMap<Signature, InterceptorOrder>();
         for (InterceptorBindingMetaData binding : bindings)
         {
            NamedMethodMetaData method = binding.getMethod();

            // TODO: this is weird, it should have been caught earlier (invalid xml)
            if(method.getMethodName() == null)
               continue;
            
            List<Method> methods = methodMap.get(method.getMethodName());
            for (Method refMethod : methods)
            {
               Signature signature = methodSignatures.getSignature(refMethod);
               if (matchesMethod(signature, refMethod, method))
               {
                  InterceptorOrderImpl interceptors = (InterceptorOrderImpl)methodInterceptors.get(signature);
                  if (interceptors == null)
                  {
                     interceptors = new InterceptorOrderImpl();
                     methodInterceptorOrders.put(signature, interceptors);
                  }
                  add(interceptors, classLoader, binding);
               }
            }
         }
      }
   }
   
   private boolean matchesMethod(Signature sig, Method refMethod, NamedMethodMetaData method)
   {
      assert refMethod.getName().equals(method.getMethodName());
      MethodParametersMetaData methodParams = method.getMethodParams();
      if(methodParams == null)
      {
         return true;
      }
      else
      {
         if(Arrays.equals(methodParams.toArray(), sig.getParameters()))
         {
            return true;
         }
      }
      
      return false;      
   }

   private void initialiseAroundInvoke(Map<String, List<Method>> methodMap)
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
         for (String methodName : methodMap.keySet())
         {
            AroundInvoke aroundInvoke = getAroundInvokeAnnotation(aroundInvokes, methodName);
            if(aroundInvoke != null)
            {
               if (this.aroundInvokes == null)
               {
                  this.aroundInvokes = new HashMap<String, AroundInvoke>();
               }
               this.aroundInvokes.put(methodName, aroundInvoke);
            }
         }
      }
   }
   
   private void initialiseLifecycleAnnotations(Map<String, List<Method>> methodMap)
   {
      if(beanMetaData instanceof JBossSessionBeanMetaData)
      {
         for (String methodName : methodMap.keySet())
         {
            
            PostConstruct postConstruct = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPostConstructs(), PostConstructImpl.class, methodName);
            if (postConstruct != null)
            {
               if (postConstructs == null)
               {
                  postConstructs = new HashMap<String, PostConstruct>();
               }
               postConstructs.put(methodName, postConstruct);
            }
            PostActivate postActivate = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPostActivates(), PostActivateImpl.class, methodName);
            if(postActivate != null)
            {
               if (postActivates == null)
               {
                  postActivates = new HashMap<String, PostActivate>();
               }
               postActivates.put(methodName, postActivate);
            }
            PrePassivate prePassivate = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPrePassivates(), PrePassivateImpl.class, methodName);
            if(prePassivate != null)
            {
               if (prePassivates == null)
               {
                  prePassivates = new HashMap<String, PrePassivate>();
               }
               prePassivates.put(methodName, prePassivate);
            }
            PreDestroy preDestroy = getLifeCycleAnnotation(((JBossSessionBeanMetaData) beanMetaData).getPreDestroys(), PreDestroyImpl.class, methodName);
            if(preDestroy != null)
            {
               if (preDestroys == null)
               {
                  preDestroys = new HashMap<String, PreDestroy>();
               }
               preDestroys.put(methodName, preDestroy);
            }
         }
      }
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
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader, String methodName, String... parameterNames)
   {
      if(annotationClass == AroundInvoke.class)
      {
         if (parameterNames.length == 1 && parameterNames[0].equals(InvocationContext.class.getName()) && aroundInvokes != null)
         {
            return annotationClass.cast(aroundInvokes.get(methodName));
         }
         return null;
      }
      else if(annotationClass == InterceptorOrder.class)
      {
         MethodSignature signature = new MethodSignature(methodName, parameterNames);
         if (methodInterceptorOrders == null)
         {
            return null;
         }
         return annotationClass.cast(methodInterceptorOrders.get(signature));
      }
      else if(annotationClass == Interceptors.class)
      {
         MethodSignature signature = new MethodSignature(methodName, parameterNames);
         if (methodInterceptors == null)
         {
            return null;
         }
         return annotationClass.cast(methodInterceptors.get(signature));
      }
      else if (annotationClass == ExcludeDefaultInterceptors.class)
      {
         MethodSignature signature = new MethodSignature(methodName, parameterNames);
         return annotationClass.cast(methodExcludeDefaultInterceptors.get(signature));
      }
      else if (annotationClass == ExcludeClassInterceptors.class)
      {
         MethodSignature signature = new MethodSignature(methodName, parameterNames);
         return annotationClass.cast(methodExcludeClassInterceptors.get(signature));
      }
      else if(annotationClass == PostActivate.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && parameterNames.length == 0 && postActivates != null) 
         {
            return annotationClass.cast(postActivates.get(methodName));
         }
      }
      else if(annotationClass == PostConstruct.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && parameterNames.length == 0 && postConstructs != null) 
         {
            return annotationClass.cast(postConstructs.get(methodName));
         }
      }
      else if(annotationClass == PreDestroy.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && parameterNames.length == 0 && preDestroys != null) 
         {
            return annotationClass.cast(preDestroys.get(methodName));
         }
      }
      else if(annotationClass == PrePassivate.class)
      {
         if(beanMetaData instanceof JBossSessionBeanMetaData && parameterNames.length == 0 && prePassivates != null) 
         {
            return annotationClass.cast(prePassivates.get(methodName));
         }
      }
      return super.retrieveAnnotation(annotationClass, beanMetaData, classLoader, methodName, parameterNames);
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
            s = new MethodSignature(m);
            methodSignatures.put(m, s);
         }
         return s;
      }
   }
}
