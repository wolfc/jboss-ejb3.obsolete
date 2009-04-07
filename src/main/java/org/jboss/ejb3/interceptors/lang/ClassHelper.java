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
package org.jboss.ejb3.interceptors.lang;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Methods which should have been in Class.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class ClassHelper
{
   private static Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
   static
   {
      primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
      primitiveWrapperMap.put(Byte.TYPE, Byte.class);
      primitiveWrapperMap.put(Character.TYPE, Character.class);
      primitiveWrapperMap.put(Short.TYPE, Short.class);
      primitiveWrapperMap.put(Integer.TYPE, Integer.class);
      primitiveWrapperMap.put(Long.TYPE, Long.class);
      primitiveWrapperMap.put(Double.TYPE, Double.class);
      primitiveWrapperMap.put(Float.TYPE, Float.class);
      primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
   }

   private static boolean checkParameters(Class<?> parameterTypes[], Class<?> methodParameterTypes[])
   {
      if(parameterTypes.length != methodParameterTypes.length)
         return false;
      for(int i = 0; i < parameterTypes.length; i++)
      {
         Class<?> methodParameterType = methodParameterTypes[i];
         if(methodParameterType.isPrimitive())
            methodParameterType = primitiveWrapperMap.get(methodParameterType);
         Class<?> parameterType = parameterTypes[i];
         if(parameterType.isPrimitive())
            parameterType = primitiveWrapperMap.get(parameterType);
         if(!methodParameterType.isAssignableFrom(parameterType))
         {
            return false;
         }
      }
      return true;
   }

   /**
    * Returns all public, private and package protected methods including
    * inherited ones.
    *
    * (Slow method)
    *
    * @param cls
    * @return
    */
   public static Method[] getAllMethods(Class<?> cls)
   {
      ArrayList<Method> list = new ArrayList<Method>();
      populateAllMethods(cls, list);
      return list.toArray(new Method[0]);
   }

   /**
    * Returns the method with the specified method name.
    *
    * (Slow method)
    *
    * @param methodName
    * @return
    * @throws NoSuchMethodException
    */
   public static Method getMethod(Class<?> cls, String methodName) throws NoSuchMethodException
   {
      if(cls == null)
         throw new NoSuchMethodException(methodName);
      Method methods[] = cls.getDeclaredMethods();
      for(Method method : methods)
      {
         if(method.getName().equals(methodName))
            return method;
         // TODO: shall we continue search for ambiguous match?
      }
      try
      {
         return getMethod(cls.getSuperclass(), methodName);
      }
      catch(NoSuchMethodException e)
      {
         throw new NoSuchMethodException("No method named " + methodName + " in " + cls + " (or super classes)");
      }
   }

   /**
    * Returns the method with the specified method name and parameters.
    *
    * @param cls
    * @param methodName
    * @param params
    * @return
    * @throws NoSuchMethodException
    */
   public static Method getMethod(Class<?> cls, String methodName, Class<?> ... params) throws NoSuchMethodException
   {
      if(cls == null)
      {
         throw new NoSuchMethodException("Class is null " + methodName);
      }

      Method m = getDeclaredMethod(cls, methodName, params);
      if (m == null)
      {
         throw new NoSuchMethodException("No method named " + methodName + "(" + (params != null ? Arrays.toString(params) : "") + ") in " + cls + " (or super classes)");
      }
      return m;
   }

   private static Method getDeclaredMethod(Class<?> cls, String methodName, Class<?> ... params)
   {
      Method methods[] = SecurityActions.getDeclaredMethods(cls);
      for(Method method : methods)
      {
         if(method.getName().equals(methodName))
         {
            if(params == null)
               return method;
            Class<?> methodParameterTypes[] = method.getParameterTypes();
            if(params.length != methodParameterTypes.length)
               continue;
            if(checkParameters(params, methodParameterTypes))
               return method;
         }
      }

      try
      {
         return SecurityActions.getDeclaredMethod(cls, methodName, params);
      }
      catch (NoSuchMethodException e1)
      {
      }

      if (cls == Object.class)
      {
         return null;
      }

      return getDeclaredMethod(cls.getSuperclass(), methodName, params);
   }
   /**
    * Returns all public, private and package protected methods including
    * inherited ones in a map indexed by name.
    *
    * (Slow method)
    *
    * @param cls
    * @return
    */
   public static Map<String, List<Method>> getAllMethodsMap(Class<?> cls)
   {
      Map<String, List<Method>> methodMap = new HashMap<String, List<Method>>();
      ArrayList<Method> list = new ArrayList<Method>();
      populateAllMethods(cls, list);

      for (Method method : list)
      {
         List<Method> methods = methodMap.get(method.getName());
         if (methods == null)
         {
            methods = new ArrayList<Method>();
            methodMap.put(method.getName(), methods);
         }
         methods.add(method);
      }
      return methodMap;
   }

   /**
    * Find all methods starting with the most general super class.
    * (See 12.4.1 bullet 4)
    *
    * This makes the class unusable for other scenarios.
    *
    * @param cls
    * @param methods
    */
   private static void populateAllMethods(Class<?> cls, List<Method> methods)
   {
      if(cls == null) return;
      populateAllMethods(cls.getSuperclass(), methods);
      for(Method method : SecurityActions.getDeclaredMethods(cls))
         methods.add(method);
   }

   public static boolean isOverridden(Class<?> icptr, Method method)
   {
      if(Modifier.isPrivate(method.getModifiers()))
         return false;
      try
      {
         Method bottomMethod = getMethod(icptr, method.getName(), method.getParameterTypes());
         if (bottomMethod.getDeclaringClass() == method.getDeclaringClass())
         {
            return false;
         }
         return true;
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Returns all (public,private, package, protected) methods belonging to the
    * <code>klass</code> and its superclasses whose params match the <code>paramTypes</code>
    * and whose return type is <code>returnType</code>
    *
    * @param klass
    * @param paramTypes
    * @return
    */
   public static Method[] getMethods(Class<?> klass, Class<?> returnType, Class<?>...paramTypes)
   {
      // the methods which will be returned, after match
      Set<Method> methodsAcceptingParamTypes = new LinkedHashSet<Method>();

      // All (public,private,protected,package) methods on the klass and its superclasses.
      // Does NOT take into account the param types yet.
      Method[] allMethodsOfClass = getAllMethods(klass);

      // Filter out relevant methods based on param types
      for (Method method : allMethodsOfClass)
      {
         // compare return type
         if (!method.getReturnType().equals(returnType))
         {
            // doesn't match, skip
            continue;
         }

         if (method.getParameterTypes().length != paramTypes.length)
         {
            // irrelevant method, skip and move to next
            continue;
         }
         // if params match, then the method is relevant, else move to next
         if (checkParameters(method.getParameterTypes(), paramTypes))
         {
            // matching method (accepting the param types). Add to collection to
            // be returned
            methodsAcceptingParamTypes.add(method);
         }

      }
      return methodsAcceptingParamTypes.toArray(new Method[methodsAcceptingParamTypes.size()]);
   }

   /**
    * Checks whether the <code>method</code> is overriden by any of the methods
    * *in the passed collection of <code>methods</code>*. Returns true if the method
    * is overriden by any of the passed methods, else returns false.
    *
    * @param method
    * @param methods
    * @return
    */
   public static boolean isOverridden(Method method, Method... methods)
   {

      for (Method someMethod : methods)
      {
         // first compare the names, if not equal then no
         // point in do other checks.
         if (!method.getName().equals(someMethod.getName()))
         {
            // skip and move to next
            continue;
         }

         // Now check the params
         if (method.getParameterTypes().length != someMethod.getParameterTypes().length)
         {
            // skip
            continue;
         }
         // param types
         if (!checkParameters(method.getParameterTypes(), someMethod.getParameterTypes()))
         {
            // skip
            continue;
         }
         // check the declaring class, if it's the same, then
         // we are checking the method on the same class, so NOT overridden
         if (method.getDeclaringClass().equals(someMethod.getDeclaringClass()))
         {
            // skip
            continue;
         }
         // the final check to see whether the declaring class
         // of the method being compared is a superclass of the other method.
         // If yes, then the method is overridden
         if (method.getDeclaringClass().isAssignableFrom(someMethod.getDeclaringClass()))
         {
            // yes, this method is overridden
            return true;
         }
      }
      // the method is not overridden
      return false;
   }

}
