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

import javax.interceptor.Interceptors;

import org.jboss.ejb3.interceptors.annotation.impl.InterceptorsImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;

/**
 * Extend bean interceptor meta data bridge for
 * additive operation.
 * 
 * TODO: this is ugly, because metadata complete should be a cross component function
 * TODO: additivity is probably also a cross component function
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class AdditiveBeanInterceptorMetaDataBridge extends BeanInterceptorMetaDataBridge
{
   private static final Logger log = Logger.getLogger(AdditiveBeanInterceptorMetaDataBridge.class);
   
   public AdditiveBeanInterceptorMetaDataBridge(Class<?> beanClass, ClassLoader classLoader, JBossEnterpriseBeanMetaData beanMetaData)
   {
      super(beanClass, classLoader, beanMetaData);
   }
   
   private static boolean isMetadataComplete(JBossEnterpriseBeanMetaData beanMetaData)
   {
      return beanMetaData.getEjbJarMetaData().isMetadataComplete();
   }
   
   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader)
   {
      if(annotationClass == Interceptors.class)
      {
         InterceptorsImpl interceptors = new InterceptorsImpl();
         if(!isMetadataComplete(beanMetaData))
            interceptors.add(getBeanClass().getAnnotation(Interceptors.class));
         
         interceptors.add(super.retrieveAnnotation(Interceptors.class, beanMetaData, classLoader));
         
         if(!interceptors.isEmpty())
            return annotationClass.cast(interceptors);
      }
      return super.retrieveAnnotation(annotationClass, beanMetaData, classLoader);
   }
   
   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader, String methodName, String... parameterNames)
   {
      return super.retrieveAnnotation(annotationClass, beanMetaData, classLoader, methodName, parameterNames);
   }
}
