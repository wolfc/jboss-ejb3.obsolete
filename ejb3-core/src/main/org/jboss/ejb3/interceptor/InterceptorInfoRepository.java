/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptor;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.metamodel.EjbJarDD;
import org.jboss.ejb3.metamodel.Interceptor;
import org.jboss.ejb3.metamodel.InterceptorBinding;
import org.jboss.ejb3.metamodel.Interceptors;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A repository of interceptor details shared amongst all containers in this deployment.
 * Interceptors differ from other ejb 3 artifacts in that we can have annotations on the
 * interceptor classes which are not the ejb container, so we cannot use annotation overrides
 * on the interceptors themselves.<BR/>
 * <BR/>
 * The xml structures get added on deployment.<BR/>
 * Interceptors only declared by using @Interceptors on the bean class get added on demand.<BR/>
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class InterceptorInfoRepository
{
   private static Logger log = Logger.getLogger(InterceptorInfoRepository.class);

   private Set<String> beanClasses = new HashSet<String>();

   private Interceptors interceptorsXml;

   private List<InterceptorBinding> bindingsXml;

   private ConcurrentMap<Class, InterceptorInfo> infos = new ConcurrentHashMap<Class, InterceptorInfo>();

   private ConcurrentMap<String, InterceptorInfo> ejbInfos = new ConcurrentHashMap<String, InterceptorInfo>();

   private LinkedHashSet<InterceptorInfo> defaultInterceptors = null;
   
   private InterceptorSorter sorter = new InterceptorSorter();

   public InterceptorInfoRepository()
   {
   }

   public void initialise(EjbJarDD dd)
   {
      this.interceptorsXml = dd.getInterceptors();

      if (dd.getAssemblyDescriptor() != null)
      {
         this.bindingsXml = dd.getAssemblyDescriptor().getInterceptorBindings();
      }
      
      initialiseInfosFromXml();
      initialiseDefaultInterceptors();
   }

   public void addBeanClass(String classname)
   {
      beanClasses.add(classname);
   }

   public InterceptorInfo getInterceptorInfo(Class clazz)
   {
      initialiseInfosFromXml();
      return infos.get(clazz);
   }

   public HashSet<InterceptorInfo> getDefaultInterceptors()
   {
      return defaultInterceptors;
   }

   public boolean hasDefaultInterceptors()
   {
      return defaultInterceptors.size() > 0;
   }

   public ArrayList<InterceptorInfo> getClassInterceptors(EJBContainer container)
   {
      javax.interceptor.Interceptors interceptors = (javax.interceptor.Interceptors) container
            .resolveAnnotation(javax.interceptor.Interceptors.class);
      ArrayList<InterceptorInfo> infos = getInterceptorsFromAnnotation(container, interceptors);

      return infos;
   }

   public ArrayList<InterceptorInfo> getMethodInterceptors(EJBContainer container, Method m)
   {
      javax.interceptor.Interceptors interceptors = (javax.interceptor.Interceptors) container.resolveAnnotation(m,
            javax.interceptor.Interceptors.class);
      ArrayList<InterceptorInfo> infos = getInterceptorsFromAnnotation(container, interceptors);

      return infos;
   }

   public Method[] getBeanClassAroundInvokes(EJBContainer container)
   {
      return getBeanClassInterceptors(container, AroundInvoke.class);
   }

   public Method[] getBeanClassPostConstructs(EJBContainer container)
   {
      return getBeanClassInterceptors(container, PostConstruct.class);
   }

   public Method[] getBeanClassPostActivates(EJBContainer container)
   {
      return getBeanClassInterceptors(container, PostActivate.class);
   }

   public Method[] getBeanClassPrePassivates(EJBContainer container)
   {
      return getBeanClassInterceptors(container, PrePassivate.class);
   }

   public Method[] getBeanClassPreDestroys(EJBContainer container)
   {
      return getBeanClassInterceptors(container, PreDestroy.class);
   }

   private Method[] getBeanClassInterceptors(EJBContainer container, Class type)
   {
      InterceptorInfo info = getOrInitialiseFromAnnotations(container);
      return getMethodsForEvent(info, type);
   }

   public InterceptorInfo[] getBusinessInterceptors(EJBContainer container, Method method)
   {
      return getInterceptors(container, AroundInvoke.class, method);
   }

   public InterceptorInfo[] getPostConstructInterceptors(EJBContainer container)
   {
      return getInterceptors(container, PostConstruct.class, null);
   }

   public InterceptorInfo[] getPostActivateInterceptors(EJBContainer container)
   {
      return getInterceptors(container, PostActivate.class, null);
   }

   public InterceptorInfo[] getPrePassivateInterceptors(EJBContainer container)
   {
      return getInterceptors(container, PrePassivate.class, null);
   }

   public InterceptorInfo[] getPreDestroyInterceptors(EJBContainer container)
   {
      return getInterceptors(container, PreDestroy.class, null);
   }

   private InterceptorInfo[] getInterceptors(EJBContainer container, Class type, Method method)
   {
      ArrayList<InterceptorInfo> interceptors = new ArrayList<InterceptorInfo>();

      if (!hasAnnotation(container, ExcludeDefaultInterceptors.class, method))
      {
         HashSet<InterceptorInfo> infos = getDefaultInterceptors();
         if (infos != null)
         {
            interceptors.addAll(trimUnwanted(infos, type));
         }
         sorter.sortDefaultInterceptors(container, interceptors);
      }

      
      if (!hasAnnotation(container, ExcludeClassInterceptors.class, method))
      {
         List<InterceptorInfo> infos = container.getClassInterceptors();
         if (infos != null)
         {
            interceptors.addAll(trimUnwanted(infos, type));
         }
         
         if (type != AroundInvoke.class)
         {
            List<InterceptorInfo> methodOnlyInterceptors = getMethodOnlyInterceptorsForLifecycle(container, type, interceptors);
            if (infos != null)
               interceptors.addAll(methodOnlyInterceptors);
         }
         sorter.sortClassInterceptors(container, interceptors);
      }

      if (type == AroundInvoke.class)
      {
         List<InterceptorInfo> infos = getMethodInterceptors(container, method);
         if (infos != null)
            interceptors.addAll(trimUnwanted(infos, type));
         sorter.sortMethodInterceptors(container, method, interceptors);
      }

      InterceptorInfo[] ints = interceptors.toArray(new InterceptorInfo[interceptors.size()]);
      return ints;
   }

   private List<InterceptorInfo> getMethodOnlyInterceptorsForLifecycle(EJBContainer container, Class type, List<InterceptorInfo> infos)
   {
      HashSet<InterceptorInfo> methodLevelInterceptors = (HashSet<InterceptorInfo>)container.getApplicableInterceptors().clone();
      
      for (InterceptorInfo info : infos)
      {
         if (methodLevelInterceptors.contains(info))
         {
            methodLevelInterceptors.remove(info);
         }
      }
      
      if (defaultInterceptors != null)
      {
         for (InterceptorInfo info : defaultInterceptors)
         {
            if (methodLevelInterceptors.contains(info))
            {
               methodLevelInterceptors.remove(info);
            }
         }
      }
      
      List<InterceptorInfo> trimmedInfos = trimUnwanted(methodLevelInterceptors, type); 
      return trimmedInfos;
   }
   
   private boolean hasAnnotation(EJBContainer container, Class annotation, Method method)
   {
      if (container.resolveAnnotation(annotation) != null)
      {
         return true;
      }

      if (method != null)
      {
         return container.resolveAnnotation(method, annotation) != null;
      }

      return false;
   }

   private List<InterceptorInfo> trimUnwanted(Collection<InterceptorInfo> interceptors, Class type)
   {
      ArrayList<InterceptorInfo> ints = new ArrayList<InterceptorInfo>(interceptors.size());
      ints.addAll(interceptors);

      for (Iterator<InterceptorInfo> it = ints.iterator(); it.hasNext();)
      {
         InterceptorInfo info = it.next();
         if (!hasMethodsForEvent(info, type))
         {
            it.remove();
         }
      }

      return ints;
   }

   private boolean hasMethodsForEvent(InterceptorInfo info, Class type)
   {
      return getMethodsForEvent(info, type) != null;
   }
   
   private Method[] getMethodsForEvent(InterceptorInfo info, Class type)
   {
      if (type == AroundInvoke.class)
         return info.getAroundInvokes();
      else if (type == PostConstruct.class)
         return info.getPostConstructs();
      else if (type == PostActivate.class)
         return info.getPostActivates();
      else if (type == PrePassivate.class)
         return info.getPrePassivates();
      else if (type == PreDestroy.class)
         return info.getPreDestroys();
      return null;
   }

   private ArrayList<InterceptorInfo> getInterceptorsFromAnnotation(EJBContainer container,
         javax.interceptor.Interceptors interceptors)
   {
      ArrayList<InterceptorInfo> inters = new ArrayList<InterceptorInfo>();
      if (interceptors == null)
         return inters;

      for (Class clazz : interceptors.value())
      {
         InterceptorInfo info = getOrInitialiseFromAnnotations(clazz);
         validateInterceptorForContainer(container, info.getClazz());
         inters.add(info);
      }

      return inters;
   }

   private void validateInterceptorForContainer(EJBContainer container, Class interceptor)
   {
      if (beanClasses.contains(interceptor.getName()))
      {
         if (!interceptor.equals(container.getClazz()))
         {
            throw new RuntimeException("Bean class " + interceptor.getName() + " cannot be used as an interceptor for "
                  + container.getEjbName());
         }
      }
   }

   private void initialiseInfosFromXml()
   {
      if (interceptorsXml != null)
      {
         //Initialise all interceptor entries so we know which classes we have xml for
         HashMap<String, AnnotationInitialiser> initialisers = new HashMap<String, AnnotationInitialiser>();
         for (Interceptor xml : interceptorsXml.getInterceptors())
         {
            XmlInitialiser init = new XmlInitialiser(xml);
            initialisers.put(xml.getInterceptorClass(), init);
         }

         //Create entries recursively, top classes first so we get the method hierarchies         
         for (Interceptor xml : interceptorsXml.getInterceptors())
         {
            String clazz = xml.getInterceptorClass();
            initialiseSuperClassesFirstFromXmlOrAnnotations(initialisers, clazz);
         }

      }
   }

   private InterceptorInfo initialiseSuperClassesFirstFromXmlOrAnnotations(
         HashMap<String, AnnotationInitialiser> initialisers, String superClassName)
   {
      if ("java.lang.Object".equals(superClassName))
      {
         return null;
      }

      AnnotationInitialiser initialiser = initialisers.get(superClassName);
      if (initialiser == null)
      {
         initialiser = new AnnotationInitialiser(superClassName, InterceptorSignatureValidator.instance);
         initialisers.put(initialiser.getClazz().getName(), initialiser);
      }
      InterceptorInfo superInfo = initialiseSuperClassesFirstFromXmlOrAnnotations(
            initialisers, initialiser.getClazz().getSuperclass().getName());

      InterceptorInfo info = initialiser.getInfo();
      info.calculateHierarchy(superInfo);
      infos.put(info.getClazz(), info);
      return info;
   }

   /*
    * Default interceptors are defined using xml only
    */
   private void initialiseDefaultInterceptors()
   {
      defaultInterceptors = new LinkedHashSet<InterceptorInfo>();

      if (bindingsXml != null)
      {
         for (InterceptorBinding bindingXml : bindingsXml)
         {
            if (bindingXml.getEjbName().equals("*")
                  && (bindingXml.getMethodName() == null || bindingXml.getMethodName().length() == 0))
            {
               for (String classname : bindingXml.getInterceptorClasses())
               {
                  if (beanClasses.contains(classname))
                  {
                     throw new RuntimeException("Bean class defined in default binding " + classname);
                  }
                  InterceptorInfo info = getOrInitialiseFromAnnotations(classname);
                  defaultInterceptors.add(info);
               }
            }
         }
      }
   }

   private InterceptorInfo getOrInitialiseFromAnnotations(String classname)
   {
      Class clazz = loadClass(classname);
      return getOrInitialiseFromAnnotations(clazz);
   }

   private InterceptorInfo getOrInitialiseFromAnnotations(Class clazz)
   {
      InterceptorInfo info = infos.get(clazz);

      if (info == null)
      {
         synchronized (this)
         {
            info = infos.get(clazz);
            if (info == null)
            {
               info = initialiseFromAnnotations(clazz);
               infos.put(clazz, info);
            }
         }
      }

      return info;
   }

   private InterceptorInfo initialiseFromAnnotations(Class clazz)
   {
      InterceptorInfo superInfo = null;
      if (clazz.getSuperclass() != Object.class)
      {
         superInfo = getOrInitialiseFromAnnotations(clazz.getSuperclass());
      }

      AnnotationInitialiser init = new AnnotationInitialiser(clazz, InterceptorSignatureValidator.instance);
      InterceptorInfo info = init.getInfo();
      info.calculateHierarchy(superInfo);
      return info;
   }

   private InterceptorInfo getOrInitialiseFromAnnotations(EJBContainer container)
   {
      InterceptorInfo info = ejbInfos.get(container.getEjbName());

      if (info == null)
      {
         synchronized (this)
         {
            info = ejbInfos.get(container.getEjbName());
            if (info == null)
            {
               info = initialiseFromAnnotations(container);
               ejbInfos.put(container.getEjbName(), info);
            }
         }
      }

      return info;
   }

   private InterceptorInfo initialiseFromAnnotations(EJBContainer container)
   {
      //Currently I see no way in spec for specifying interceptors of an ejb super class using xml,
      //use annotations only to initialise super classes, and don't store these
      InterceptorInfo superInfo = initialiseContainerSuperClassFromAnnotationsOnly(container.getClazz().getSuperclass());
      AnnotationInitialiser init = new ContainerInitialiser(container);
      InterceptorInfo info = init.getInfo();
      info.calculateHierarchy(superInfo);
      return info;
   }

   private InterceptorInfo initialiseContainerSuperClassFromAnnotationsOnly(Class clazz)
   {
      InterceptorInfo superInfo = null;
      if (clazz != Object.class)
      {
         superInfo = initialiseContainerSuperClassFromAnnotationsOnly(clazz.getSuperclass());
      }

      AnnotationInitialiser init = new AnnotationInitialiser(clazz, BeanSignatureValidator.instance);
      InterceptorInfo info = init.getInfo();
      info.calculateHierarchy(superInfo);
      return info;
   }
   
   
   private Class loadClass(String name)
   {
      try
      {
         return Thread.currentThread().getContextClassLoader().loadClass(name);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Interceptor class not found: " + name);
      }
   }

   public static boolean checkValidBusinessSignature(Method method)
   {
      int modifiers = method.getModifiers();

      if (!Modifier.isStatic(modifiers))
      {
         if (method.getReturnType().equals(Object.class))
         {
            Class[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].equals(InvocationContext.class))
            {
               Class[] exceptions = method.getExceptionTypes();
               if (exceptions.length == 1 && exceptions[0].equals(Exception.class))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   public static boolean checkValidLifecycleSignature(Method method)
   {
      int modifiers = method.getModifiers();

      if (!Modifier.isStatic(modifiers))
      {
         if (method.getReturnType().equals(Void.TYPE))
         {
            Class[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].equals(InvocationContext.class))
            {
               return true;
            }
         }
      }
      return false;
   }

   public static boolean checkValidBeanLifecycleSignature(Method method)
   {
      int modifiers = method.getModifiers();
      if (method.getName().equals("ejbCreate"))
      {
         // for public void ejbCreate() throws javax.ejb.CreateException
         if (!Modifier.isStatic(modifiers) && method.getReturnType().equals(Void.TYPE)
               && method.getParameterTypes().length == 0 && method.getExceptionTypes().length <= 1)
            return true;
      }
      else if (!Modifier.isStatic(modifiers) && method.getReturnType().equals(Void.TYPE)
            && method.getParameterTypes().length == 0 && method.getExceptionTypes().length == 0)
      {
         return true;
      }
      return false;
   }

   public static String simpleType(Class type)
   {
      Class ret = type;
      if (ret.isArray())
      {
         Class arr = ret;
         String array = "";
         while (arr.isArray())
         {
            array += "[]";
            arr = arr.getComponentType();
         }
         return arr.getName() + array;
      }
      return ret.getName();
   }
   

   private interface SignatureValidator
   {
      boolean checkValidLifecycle(Method m);
      boolean checkValidAround(Method m);
   }

   private static class InterceptorSignatureValidator implements SignatureValidator
   {
      static SignatureValidator instance = new InterceptorSignatureValidator();
      public boolean checkValidAround(Method m)
      {
         return checkValidBusinessSignature(m);
      }

      public boolean checkValidLifecycle(Method m)
      {
         return checkValidLifecycleSignature(m);
      }
   }

   private static class BeanSignatureValidator implements SignatureValidator
   {
      static SignatureValidator instance = new BeanSignatureValidator();
      public boolean checkValidAround(Method m)
      {
         return checkValidBusinessSignature(m);
      }

      public boolean checkValidLifecycle(Method m)
      {
         return checkValidBeanLifecycleSignature(m);
      }
   }
   
   
   private class AnnotationInitialiser
   {
      SignatureValidator signatureValidator;
      Class clazz;

      InterceptorInfo info;

      AnnotationInitialiser(String classname, SignatureValidator signatureValidator)
      {
         clazz = loadClass(classname);
         this.signatureValidator = signatureValidator;
         info = new InterceptorInfo(clazz);
      }

      AnnotationInitialiser(Class clazz, SignatureValidator signatureValidator)
      {
         this.clazz = clazz;
         this.signatureValidator = signatureValidator;
         info = new InterceptorInfo(clazz);
      }

      public Class getClazz()
      {
         return clazz;
      }

      InterceptorInfo getInfo()
      {
         for (Method method : clazz.getDeclaredMethods())
         {
            info.setAroundInvoke(resolveAroundInvoke(method));
            info.setPostConstruct(resolvePostConstruct(method));
            info.setPostActivate(resolvePostActivate(method));
            info.setPreDestroy(resolvePreDestroy(method));
            info.setPrePassivate(resolvePrePassivate(method));
         }
         return info;
      }

      Method resolveAroundInvoke(Method method)
      {
         AroundInvoke ann = (AroundInvoke) getAnnotation(method, AroundInvoke.class);
         if (ann != null)
         {
            if (!signatureValidator.checkValidAround(method))
            {
               throw new RuntimeException("@" + ((Annotation) ann).annotationType().getName()
                     + " annotated method in has the wrong signature - " + method);
            }
            return method;
         }
         return null;
      }

      Method resolvePostConstruct(Method method)
      {
         PostConstruct ann = (PostConstruct) getAnnotation(method, PostConstruct.class);
         return resolveLifecycleMethod(method, ann);
      }

      Method resolvePostActivate(Method method)
      {
         PostActivate ann = (PostActivate) getAnnotation(method, PostActivate.class);
         return resolveLifecycleMethod(method, ann);
      }

      Method resolvePreDestroy(Method method)
      {
         PreDestroy ann = (PreDestroy) getAnnotation(method, PreDestroy.class);
         return resolveLifecycleMethod(method, ann);
      }

      Method resolvePrePassivate(Method method)
      {
         PrePassivate ann = (PrePassivate) getAnnotation(method, PrePassivate.class);
         return resolveLifecycleMethod(method, ann);
      }

      Method resolveLifecycleMethod(Method method, Annotation ann)
      {
         if (ann != null)
         {
            if (!signatureValidator.checkValidLifecycle(method))
            {
               throw new RuntimeException("@" + ((Annotation) ann).annotationType().getName()
                     + " annotated method  has the wrong signature - " + method);
            }
            return method;
         }
         return null;
      }

      Object getAnnotation(Method method, Class annotation)
      {
         return method.getAnnotation(annotation);
      }

   }

   private class ContainerInitialiser extends AnnotationInitialiser
   {
      EJBContainer container;

      public ContainerInitialiser(EJBContainer container)
      {
         // FIXME ContainerInitialiser constructor
         super(container.getBeanClass(), BeanSignatureValidator.instance);
         this.container = container;
      }

      Object getAnnotation(Method method, Class annotation)
      {
         return container.resolveAnnotation(method, annotation);
      }

      /*
       * Lifecycle methods on bean class have a different signature from those defined on
       * 
       */
      Method resolveLifecycleMethod(Method method, Annotation ann)
      {
         if (ann != null)
         {
            if (!signatureValidator.checkValidLifecycle(method))
            {
               // FIXME: JBCTS-322
               // This is just a hack that prevents the RTE below. Please fix appropriately.
               if (ann.annotationType() == PostConstruct.class && method.getName().equals("ejbCreate"))
               {
            	   return method;
               }
               
               throw new RuntimeException("@" + ann.annotationType().getName()
                       + " annotated method has the wrong signature - " + method);
            }
            return method;
         }
         return null;
      }
   }

   private class XmlInitialiser extends AnnotationInitialiser
   {
      Interceptor xml;

      XmlInitialiser(Interceptor xml)
      {
         super(xml.getInterceptorClass(), InterceptorSignatureValidator.instance);
         this.xml = xml;
      }

      InterceptorInfo getInfo()
      {
         info.setAroundInvoke(findInterceptorMethodFromXml(clazz, "around-invoke-method", xml.getAroundInvoke()));
         info.setPostConstruct(findInterceptorMethodFromXml(clazz, "post-construct-method", xml.getPostConstruct()));
         info.setPostActivate(findInterceptorMethodFromXml(clazz, "post-activate-method", xml.getPostActivate()));
         info.setPreDestroy(findInterceptorMethodFromXml(clazz, "pre-destroy-method", xml.getPreDestroy()));
         info.setPrePassivate(findInterceptorMethodFromXml(clazz, "pre-passivate-method", xml.getPrePassivate()));
         super.getInfo();
         info.setXml(xml);
         return info;
      }

      java.lang.reflect.Method findInterceptorMethodFromXml(Class clazz, String lookingFor, org.jboss.ejb3.metamodel.Method xml)
      {
         if (xml == null)
            return null;
         if (xml.getMethodName() == null || xml.getMethodName().trim().equals(""))
         {
            throw new RuntimeException(lookingFor + " must contain a valid method name for interceptor "
                  + clazz.getName());
         }
         if (xml.getMethodParams() != null)
         {
            log.debug("Ignoring method parameters for " + lookingFor + " in interceptor " + clazz.getName());
         }

         ArrayList<Method> possible = new ArrayList<Method>();
         for (java.lang.reflect.Method method : clazz.getDeclaredMethods())
         {
            if (xml.getMethodName().equals(method.getName()))
            {
               possible.add(method);
            }
         }

         if (possible.size() == 0)
         {
            throw new RuntimeException(lookingFor + " must contain a valid method name for interceptor "
                  + clazz.getName());
         }

         Method found = null;

         for (Method method : possible)
         {
            if (lookingFor.equals("around-invoke-method"))
            {
               if (signatureValidator.checkValidAround(method))
               {
                  found = method;
               }
            }
            else
            {
               if (signatureValidator.checkValidLifecycle(method))
               {
                  found = method;
               }
            }
         }

         if (found == null)
         {
            throw new RuntimeException(lookingFor + " has the wrong method signature for interceptor "
                  + clazz.getName());
         }

         return found;
      }
   }

   private class InterceptorSorter
   {
      boolean initialised;
      List<InterceptorBinding> orderedBindings;
      
      private void initialise()
      {
         if (!initialised)
         {
            synchronized(this)
            {
               if (bindingsXml != null)
               {
                  for (InterceptorBinding binding : bindingsXml)
                  {
                     if (binding.isOrdered())
                     {
                        //Validate each interceptor only occurs once
                        HashSet<String> names = new HashSet<String>();
                        for (Iterator it = binding.getInterceptorClasses().iterator() ; it.hasNext() ; )
                        {
                           String className = (String)it.next();
                           if (names.contains(className))
                           {
                              throw new RuntimeException(className + " occurs more than once in ordered binding " + 
                                    getInterceptorBindingString(binding));
                           }
                           names.add(className);
                        }
                        
                        if (orderedBindings == null)
                        {
                           orderedBindings = new ArrayList<InterceptorBinding>();
                        }
                        orderedBindings.add(binding);
                     }
                  }
               }
            }
            log.trace("orderedBindings = " + orderedBindings);
            initialised = true;
         }
      }
      
      void sortDefaultInterceptors(EJBContainer container, ArrayList<InterceptorInfo> infos)
      {
         initialise();
         if (orderedBindings == null)  return;
         ArrayList<String> bindingOrder = null;
         for (InterceptorBinding binding : orderedBindings)
         {
            if (binding.getEjbName().equals("*"))
            {
               if (bindingOrder != null)
               {
                  throw new RuntimeException("There should only be one interceptor-binding specifying " +
                        "the order of default interceptors " + getInterceptorBindingString(binding));
               }
               bindingOrder = binding.getInterceptorClasses();
            }
         }
         sortInterceptors(infos, bindingOrder);
      }
      
      void sortClassInterceptors(EJBContainer container, ArrayList<InterceptorInfo> infos)
      {
         initialise();
         if (orderedBindings == null)  return;
         ArrayList<String> bindingOrder = null;
         for (InterceptorBinding binding : orderedBindings)
         {
            if (binding.getMethodName() != null && binding.getMethodName().trim().length() > 0)
            {
               continue;
            }
            if (binding.getEjbName().equals(container.getEjbName()))
            {
               if (bindingOrder != null)
               {
                  throw new RuntimeException("There should only be one interceptor-binding specifying " +
                        "the order of class interceptors: " + getInterceptorBindingString(binding));
               }
               bindingOrder = binding.getInterceptorClasses();
            }
         }
         sortInterceptors(infos, bindingOrder);
      }
      
      void sortMethodInterceptors(EJBContainer container, Method method, ArrayList<InterceptorInfo> infos)
      {
         initialise();
         if (orderedBindings == null)  return;
         ArrayList<String> methodNoParamsOrder = null;
         ArrayList<String> methodParamsOrder = null;
         for (InterceptorBinding binding : orderedBindings)
         {
            if (binding.getEjbName().equals(container.getEjbName()))
            {
               if (binding.getMethodName() != null && binding.getMethodName().equals(method.getName()))
               {
                  if (binding.getMethodParams() == null)
                  {
                     if (methodNoParamsOrder != null)
                     {
                        throw new RuntimeException("There should only be one interceptor-binding specifying " +
                              "the order of method interceptors: "  + getInterceptorBindingString(binding));
                     }
                     methodNoParamsOrder = binding.getInterceptorClasses();
                  }
                  else
                  {
                     Class[] params = method.getParameterTypes();
                     List<String> methodParams = binding.getMethodParams();
                     if (methodParams.size() == params.length)
                     {
                        boolean matches = true;
                        for (int i = 0 ; i < params.length ; i++)
                        {
                           if (!simpleType(params[i]).equals(methodParams.get(i)))
                           {
                              matches = false;
                              break;
                           }
                        }
                        
                        if (matches)
                        {
                           if (methodParamsOrder != null)
                           {
                              boolean first = false;
                              StringBuffer paramBuf = new StringBuffer();
                              paramBuf.append("(");
                              for (String par : methodParams)
                              {
                                 if (!first) paramBuf.append(",");
                                 paramBuf.append(par);
                              }
                              paramBuf.append(")");
                              throw new RuntimeException("There should only be one interceptor-binding specifying " +
                                    "the order of method interceptors: " + getInterceptorBindingString(binding));
                           }
                           
                           methodParamsOrder = binding.getInterceptorClasses();
                        }
                     }
                  }
               }
            }
         }
         
         if (methodParamsOrder != null)
         {
            sortInterceptors(infos, methodParamsOrder);
            
         }
         else
         {
            sortInterceptors(infos, methodNoParamsOrder);
         }
      }
      
      void sortInterceptors(ArrayList<InterceptorInfo> infos, ArrayList<String> interceptorOrder)
      {
         if (interceptorOrder == null) return;
         Collections.sort(infos, new InterceptorComparator(interceptorOrder));
      }

      String getInterceptorBindingString(InterceptorBinding binding)
      {
         StringBuffer buf = new StringBuffer();
         List methodParams = binding.getMethodParams();
         
         buf.append(binding.getEjbName());
         if (binding.getMethodName() != null)
         {
            buf.append("." + binding.getMethodName());
            if (methodParams != null)
            {
               buf.append("(");
               for (int i = 0 ; i < methodParams.size() ; )
               {
                  if (i == 0) buf.append(",");
                  buf.append((String)methodParams.get(i));
               }
               buf.append(")");
            }
         }
         
         return buf.toString();
      }
   }
   
   private class InterceptorComparator implements Comparator<InterceptorInfo>
   {
      ArrayList<String> ordered;

      InterceptorComparator(ArrayList<String> ordered)
      {
         this.ordered = ordered;
      }
      
      public int compare(InterceptorInfo o1, InterceptorInfo o2)
      {
         int pos1 = ordered.indexOf(o1.getClazz().getName());
         int pos2 = ordered.indexOf(o2.getClazz().getName());
         
         //Make anything not specified in the order come last
         if (pos1 < 0) pos1 = Integer.MAX_VALUE;
         if (pos2 < 0) pos2 = Integer.MAX_VALUE;
         
         if (pos1 < pos2)
         {
            return -1;
         }
         else if (pos1 > pos2)
         {
            return 1;
         }
         else
         {
            return 0;
         }
      }
      
      
   }
   
}
