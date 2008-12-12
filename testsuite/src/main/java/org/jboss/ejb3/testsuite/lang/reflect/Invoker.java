/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.testsuite.lang.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Kick a class in the privates. :-)
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class Invoker
{
   public static Field getField(Class<?> cls, String fieldName)
   {
      try
      {
         Field field = cls.getDeclaredField(fieldName);
         field.setAccessible(true);
         return field;
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchFieldException e)
      {
         throw new RuntimeException("No such field, choices are " + Arrays.toString(cls.getDeclaredFields()), e);
      }
   }
   
   /**
    * Get the contents of a (private) field.
    * 
    * Slow!
    * 
    * @param <R>
    * @param obj
    * @param cls
    * @param name
    * @return
    */
   @SuppressWarnings("unchecked")
   public static <R> R getFieldValue(Object obj, Class<?> cls, String name)
   {
      assert cls.isAssignableFrom(obj.getClass()) : "obj " + obj + " is not of a sub-class of " + cls;
      Field field = getField(cls, name);
      return (R) getFieldValue(field, obj);
   }
   
   /**
    * @param field
    * @param obj
    * @return
    */
   @SuppressWarnings("unchecked")
   public static <R> R getFieldValue(Field field, Object obj)
   {
      try
      {
         return (R) field.get(obj);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static Method getMethod(Class<?> cls, String methodName, Class<?>... parameterTypes)
   {
      try
      {
         Method method = cls.getDeclaredMethod(methodName, parameterTypes);
         method.setAccessible(true);
         return method;
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException("No such method, choices are " + Arrays.toString(cls.getDeclaredMethods()), e);
      }
   }
   
   public static Object invoke(Method method, Object obj, Object... args)
   {
      try
      {
         return method.invoke(obj, args);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         try
         {
            throw e.getTargetException();
         }
         catch(Error error)
         {
            throw error;
         }
         catch(RuntimeException re)
         {
            throw re;
         }
         catch(Throwable th)
         {
            throw new RuntimeException(th);
         }
      }
   }
}
