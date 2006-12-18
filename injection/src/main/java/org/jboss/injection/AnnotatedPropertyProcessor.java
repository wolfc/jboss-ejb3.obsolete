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

import java.lang.annotation.Annotation;

import org.jboss.injection.lang.reflect.BeanProperty;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AnnotatedPropertyProcessor<FactoryType extends InjectorFactory<AnnotationType>, AnnotationType extends Annotation> extends AbstractProcessor<BeanProperty>
{
   private FactoryType factory;
   private Class<AnnotationType> annotationClass;
   
   protected AnnotatedPropertyProcessor(FactoryType factory, Class<AnnotationType> annotationClass)
   {
      assert factory != null;
      assert annotationClass != null;
      
      this.factory = factory;
      this.annotationClass = annotationClass;
   }
   
   public Injector processOne(BeanProperty property)
   {
      AnnotationType resource = property.getAnnotation(annotationClass);
      if(resource == null) return null;
      
      return factory.create(property, resource);
   }
}
