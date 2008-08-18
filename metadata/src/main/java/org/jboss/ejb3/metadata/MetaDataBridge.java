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
package org.jboss.ejb3.metadata;

import java.lang.annotation.Annotation;

import org.jboss.metadata.spi.signature.DeclaredMethodSignature;

/**
 * Retrieve an annotation based on a piece of meta data.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public interface MetaDataBridge<M>
{
   /**
    * Retrieve a class annotation.
    * 
    * @param <A>
    * @param annotationClass
    * @param metaData
    * @param classLoader
    * @return                   the annotation or null if not found
    */
   <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, M metaData, ClassLoader classLoader);
   
   /**
    * Retrieve a method annotation.
    * 
    * @param <A>
    * @param annotationClass
    * @param metaData
    * @param classLoader
    * @param method
    * @return                   the annotation of null if not found
    */
   <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, M metaData, ClassLoader classLoader, DeclaredMethodSignature method);
}
