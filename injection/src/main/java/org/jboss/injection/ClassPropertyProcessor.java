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
package org.jboss.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.FieldBeanProperty;
import org.jboss.injection.lang.reflect.MethodBeanProperty;

/**
 * Processes the properties of a class for injection.
 * 
 * Usually used to find annotations on properties.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ClassPropertyProcessor implements InjectionProcessor<Class<?>>
{
   private InjectionProcessor<BeanProperty> propertyProcessor;
   
   public ClassPropertyProcessor(InjectionProcessor<BeanProperty> propertyProcessor)
   {
      assert propertyProcessor != null;
      
      this.propertyProcessor = propertyProcessor;
   }
   
   /**
    * Find all properties defined in the class and pass
    * the property to the propertyProcessor.
    * 
    * @param cls
    */
   public Collection<Injector> process(Class<?> cls)
   {
      Collection<Injector> list = new ArrayList<Injector>();
      
      Field fields[] = cls.getDeclaredFields();
      for(Field field : fields)
      {
         BeanProperty property = new FieldBeanProperty(field);
         list.addAll(propertyProcessor.process(property));
      }
      
      Method methods[] = cls.getDeclaredMethods();
      for(Method method : methods)
      {
         if(MethodBeanProperty.isValid(method))
         {
            BeanProperty property = new MethodBeanProperty(method);
            list.addAll(propertyProcessor.process(property));
         }
      }
      
      return list;
   }
}
