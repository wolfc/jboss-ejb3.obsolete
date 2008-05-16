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
package org.jboss.ejb3.proxy.lang;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * InvokedMethod
 * 
 * A Serializable view of an Invoked Method, providing
 * overridden implementations of hashCode, equals, and toString
 * 
 * JIRA: EJBTHREE-1269
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class InvokedMethod implements Serializable
{
   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The name of the method
    */
   private String name;

   /**
    * Fully-qualified class name of the method
    */
   private String className;

   /**
    * Fully-qualfied class name of the return type of the method
    */
   private String returnType;

   /**
    * Array of fully-qualified class names of arguments, in order
    */
   private String[] argumentTypes;

   // ------------------------------------------------------------------------------||
   // Constructors -----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param Method The method this view represents
    */
   public InvokedMethod(Method method)
   {
      // Set properties
      this.setName(method.getName());
      this.setClassName(method.getDeclaringClass().getName());
      this.setReturnType(method.getReturnType().getName());
      Class<?>[] paramTypes = method.getParameterTypes();
      List<String> paramTypesString = new ArrayList<String>();
      if (paramTypes != null)
      {
         for (Class<?> paramType : paramTypes)
         {
            paramTypesString.add(paramType.getName());
         }
      }
      this.setArgumentTypes(paramTypesString.toArray(new String[]
      {}));
   }

   // ------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------||
   // ------------------------------------------------------------------------------||

   @Override
   public boolean equals(Object obj)
   {
      // If not an instance of InvokedMethod
      if (!(obj instanceof InvokedMethod))
      {
         // Different types, we can't be equal
         return false;
      }

      // Cast
      InvokedMethod other = InvokedMethod.class.cast(obj);

      // We're equal if all properties are equal
      return this.getClassName().equals(other.getClassName()) && this.getName().equals(other.getName())
            && Arrays.equals(this.getArgumentTypes(), other.getArgumentTypes());
   }

   @Override
   public int hashCode()
   {
      // toString is unique, use it
      return this.toString().hashCode();
   }

   @Override
   public String toString()
   {
      // Initialize
      StringBuffer sb = new StringBuffer();

      // Construct
      sb.append(this.getClassName());
      sb.append('.');
      sb.append(this.getName());
      sb.append('(');
      int count = 0;
      for (String argType : this.getArgumentTypes())
      {
         count++;
         sb.append(argType);
         if (count < this.getArgumentTypes().length)
         {
            sb.append(',');
         }
      }
      sb.append(')');

      // Return
      return sb.toString();
   }

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public String getClassName()
   {
      return className;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }

   public String getReturnType()
   {
      return returnType;
   }

   public void setReturnType(String returnType)
   {
      this.returnType = returnType;
   }

   public String[] getArgumentTypes()
   {
      return argumentTypes;
   }

   public void setArgumentTypes(String[] argumentTypes)
   {
      this.argumentTypes = argumentTypes;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

}
