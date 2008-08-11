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
package org.jboss.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.ejb3.EJBContainer;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.BeanPropertyFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ResourceInjectionMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class InjectionUtil
{
   private static final Logger log = Logger
           .getLogger(InjectionUtil.class);


   /**
    * This method will take a set of XML loaded injectors and collapse them based on spec inheritance rules
    * It will remove injectors that should not be used in the injection of the base component class.
    *
    * @param visitedMethods
    * @param clazz
    * @param xmlDefinedInjectors
    * @param classInjectors
    */
   public static void collapseXmlMethodInjectors(Set<String> visitedMethods, Class clazz, Map<String, Map<AccessibleObject, Injector>> xmlDefinedInjectors, Map<AccessibleObject, Injector> classInjectors)
   {
      if (clazz == null || clazz.equals(Object.class))
      {
         return;
      }
      Map<AccessibleObject, Injector> xmlInjectors = xmlDefinedInjectors.get(clazz.getName());
      if (xmlInjectors != null)
      {
         Method[] methods = clazz.getDeclaredMethods();
         for (Method method : methods)
         {
            if (method.getParameterTypes().length != 1) continue;

            if (!Modifier.isPrivate(method.getModifiers()))
            {
               if (visitedMethods.contains(method.getName()))
               {
                  xmlInjectors.remove(method); // if not private then it has been overriden
                  continue;
               }
               visitedMethods.add(method.getName());
            }
         }
         classInjectors.putAll(xmlInjectors);
      }
      // recursion needs to come last as the method could be overriden and we don't want the overriding method to be ignored
      collapseXmlMethodInjectors(visitedMethods, clazz.getSuperclass(), xmlDefinedInjectors, classInjectors);
   }

   /**
    * Create and add multiple injectors for injection targets.
    * 
    * @param injectors          the list on which to add injectors
    * @param classLoader        the class loader to resolve an injection target
    * @param factory            the injector factory
    * @param injectionTargets   the injection targets
    */
   public static void createInjectors(List<Injector> injectors, ClassLoader classLoader, InjectorFactory<?> factory, Collection<ResourceInjectionTargetMetaData> injectionTargets)
   {
      for(ResourceInjectionTargetMetaData injectionTarget : injectionTargets)
      {
         AccessibleObject ao = findInjectionTarget(classLoader, injectionTarget);
         BeanProperty property = BeanPropertyFactory.create(ao);
         injectors.add(factory.create(property));
      }
   }
   
   public static <X extends RemoteEnvironment> void processMethodAnnotations(InjectionContainer container, Collection<InjectionHandler<X>> handlers, Set<String> visitedMethods, Class<?> clazz, Map<AccessibleObject, Injector> classInjectors)
   {
      if (clazz == null || clazz.equals(Object.class))
      {
         return;
      }
      Method[] methods = clazz.getDeclaredMethods();
      for (Method method : methods)
      {
         if (method.getParameterTypes().length != 1) continue;

         if (!Modifier.isPrivate(method.getModifiers()))
         {
            if (visitedMethods.contains(method.getName()))
            {
               continue;
            }
            visitedMethods.add(method.getName());
         }
        
         if (handlers != null)
         {
            for (InjectionHandler<?> handler : handlers)
            {
               handler.handleMethodAnnotations(method, container, classInjectors);
            }
         }
      }
      // recursion needs to come last as the method could be overriden and we don't want the overriding method to be ignored
      processMethodAnnotations(container, handlers, visitedMethods, clazz.getSuperclass(), classInjectors);
   }

   public static <X extends RemoteEnvironment> void processFieldAnnotations(InjectionContainer container, Collection<InjectionHandler<X>> handlers, Class<?> clazz, Map<AccessibleObject, Injector> classInjectors)
   {
      if (clazz == null || clazz.equals(Object.class))
      {
         return;
      }
 
      if (handlers != null)
      {
         Field[] fields = clazz.getDeclaredFields();
         for (Field field : fields)
         {
            log.trace("process field annotation for " + field.toGenericString());
            for (InjectionHandler<?> handler : handlers)
            {
               handler.handleFieldAnnotations(field, container, classInjectors);
            }
         }
      }
      
      // recursion needs to come last as the method could be overriden and we don't want the overriding method to be ignored
      processFieldAnnotations(container, handlers, clazz.getSuperclass(), classInjectors);
   }

   public static <X extends RemoteEnvironment> void processClassAnnotations(InjectionContainer container, Collection<InjectionHandler<X>> handlers, Class<?> clazz)
   {
      if (clazz == null || clazz.equals(Object.class))
      {
         return;
      }
    
      if (handlers != null)
      {
         for (InjectionHandler<?> handler : handlers)
         {
            handler.handleClassAnnotations(clazz, container);
         }
      }
      
      // recursion needs to come last as the method could be overriden and we don't want the overriding method to be ignored
      processClassAnnotations(container, handlers, clazz.getSuperclass());
   }

   public static <X extends RemoteEnvironment> Map<AccessibleObject, Injector> processAnnotations(InjectionContainer container, Collection<InjectionHandler<X>> handlers, Class<?> clazz)
   {
      Map<AccessibleObject, Injector> classInjectors = new HashMap<AccessibleObject, Injector>();
      HashSet<String> visitedMethods = new HashSet<String>();
      collapseXmlMethodInjectors(visitedMethods, clazz, container.getEncInjections(), classInjectors);

      processClassAnnotations(container, handlers, clazz);
      visitedMethods = new HashSet<String>();
      processMethodAnnotations(container, handlers, visitedMethods, clazz, classInjectors);
      processFieldAnnotations(container, handlers, clazz, classInjectors);
      return classInjectors;
   }

   public static AccessibleObject findInjectionTarget(ClassLoader loader, ResourceInjectionTargetMetaData target)
   {
      Class<?> clazz = null;
      try
      {
         clazz = loader.loadClass(target.getInjectionTargetClass());
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("<injection-target> class: " + target.getInjectionTargetClass() + " was not found in deployment");
      }

      for (Field field : clazz.getDeclaredFields())
      {
         if (target.getInjectionTargetName().equals(field.getName())) return field;
      }

      for (java.lang.reflect.Method method : clazz.getDeclaredMethods())
      {
         if (method.getName().equals(target.getInjectionTargetName())) return method;
      }

      throw new RuntimeException("<injection-target> could not be found: " + target.getInjectionTargetClass() + "." + target.getInjectionTargetName());

   }

   public static String getEncName(Class type)
   {
      return "env/" + type.getName();
   }

   public static String getEncName(Method method)
   {
      String encName = method.getName().substring(3);
      if (encName.length() > 1)
      {
         encName = encName.substring(0, 1).toLowerCase() + encName.substring(1);
      }
      else
      {
         encName = encName.toLowerCase();
      }

      encName = getEncName(method.getDeclaringClass()) + "/" + encName;
      return encName;
   }

   public static String getEncName(Field field)
   {
      return getEncName(field.getDeclaringClass()) + "/" + field.getName();
   }

   public static Object getAnnotation(Class<? extends Annotation> annotation, EJBContainer container, Class<?> annotatedClass, boolean isContainer)
   {
      if (isContainer)
      {
         return container.resolveAnnotation(annotation);
      }
      else
      {
         return annotatedClass.getAnnotation(annotation);
      }
   }

   public static Object getAnnotation(Class<? extends Annotation> annotation, EJBContainer container, Method method, boolean isContainer)
   {
      if (isContainer)
      {
         return container.resolveAnnotation(method, annotation);
      }
      else
      {
         return method.getAnnotation(annotation);
      }
   }

   public static Object getAnnotation(Class<? extends Annotation> annotation, EJBContainer container, Field field, boolean isContainer)
   {
      if (isContainer)
      {
         return container.resolveAnnotation(field, annotation);
      }
      else
      {
         return field.getAnnotation(annotation);
      }
   }

   public static Class<?> injectionTarget(String encName, ResourceInjectionMetaData ref, InjectionContainer container, Map<String, Map<AccessibleObject, Injector>> classInjectors)
   {
      Class<?> injectionType = null;
      
      if(ref.getInjectionTargets() == null)
         return injectionType;
      
      for(ResourceInjectionTargetMetaData injectionTarget : ref.getInjectionTargets())
      {
         // todo, get injection target class
         AccessibleObject ao = findInjectionTarget(container.getClassloader(), injectionTarget);
         Map<AccessibleObject, Injector> injectors = classInjectors.get(injectionTarget.getInjectionTargetClass());
         if (injectors == null)
         {
            injectors = new HashMap<AccessibleObject, Injector>();
            classInjectors.put(injectionTarget.getInjectionTargetClass(), injectors);
         }
         Class<?> type;
         if (ao instanceof Field)
         {
            type = ((Field) ao).getType();
            injectors.put(ao, new JndiFieldInjector((Field) ao, encName, container.getEnc()));
         }
         else
         {
            type = ((Method) ao).getParameterTypes()[0];
            injectors.put(ao, new JndiMethodInjector((Method) ao, encName, container.getEnc()));
         }
         if(injectionType == null)
            injectionType = type;
         else
         {
            if(!injectionType.equals(type))
               throw new IllegalStateException("Found multiple injection targets with different types");
         }
      }
      
      return injectionType;
   }
}
