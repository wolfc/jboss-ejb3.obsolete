/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.injection.lang.reflect;

import java.lang.reflect.Field;

import org.jboss.logging.Logger;

/**
 * Morphes a field into a bean property.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class FieldBeanProperty extends AbstractAccessibleObjectBeanProperty<Field>
{
   private static final Logger log = Logger.getLogger(FieldBeanProperty.class);
   
   /**
    * @param field
    */
   public FieldBeanProperty(Field field)
   {
      super(field);
   }
   
   public Class<?> getDeclaringClass()
   {
      return getField().getDeclaringClass();
   }
   
   protected Field getField()
   {
      return getAccessibleObject();
   }
   
   public String getName()
   {
      return getField().getName();
   }
   
   public Class<?> getType()
   {
      return getField().getType();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.lang.reflect.BeanProperty#set(java.lang.Object)
    */
   public void set(Object instance, Object value)
   {
      Field field = getField();
      try
      {
         field.set(instance, value);
      }
      catch(IllegalAccessException e)
      {
         log.fatal("illegal access on field " + field, e);
         throw new RuntimeException(e);
      }
      catch(IllegalArgumentException e)
      {
         String msg = "failed to set value " + value + " on field " + field; 
         
         // Help out with the error message; let the developer know if the 
         // value and target field CLs are not equal
         ClassLoader fieldLoader = field.getType().getClassLoader();
         ClassLoader valueLoader = value.getClass().getClassLoader();
         boolean equalLoaders = fieldLoader.equals(valueLoader);
         if (!equalLoaders)
         {
            log.error("Field Classloader: " + fieldLoader + "\nValue ClassLoader: " + valueLoader + "\nEqual Loaders: "
                  + equalLoaders);
            msg = msg + "; Reason: ClassLoaders of value and target are not equal";

         }
         
         log.error(msg, e);
         throw new IllegalArgumentException(msg);
      }
   }
}
