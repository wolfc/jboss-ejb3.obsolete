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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Methods which should have been in Class.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class ClassHelper
{
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
      for(Method method : cls.getDeclaredMethods())
         methods.add(method);
   }
}
