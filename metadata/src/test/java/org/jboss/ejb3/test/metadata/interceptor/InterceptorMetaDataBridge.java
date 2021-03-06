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
package org.jboss.ejb3.test.metadata.interceptor;

import java.lang.annotation.Annotation;

import javax.interceptor.AroundInvoke;

import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.metadata.spi.signature.DeclaredMethodSignature;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class InterceptorMetaDataBridge extends EnvironmentInterceptorMetaDataBridge<InterceptorMetaData> implements MetaDataBridge<InterceptorMetaData>
{
   private static final Logger log = Logger.getLogger(InterceptorMetaDataBridge.class);

   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, InterceptorMetaData metaData, ClassLoader classLoader)
   {
      return super.retrieveAnnotation(annotationClass, metaData, classLoader);
   }

   @Override
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, InterceptorMetaData interceptorMetaData, ClassLoader classLoader, DeclaredMethodSignature method)
   {
      if(annotationClass == AroundInvoke.class)
      {
         Annotation annotation = getAroundInvokeAnnotation(interceptorMetaData.getAroundInvokes(), method);
         if(annotation != null)
            return annotationClass.cast(annotation);
      }
      return super.retrieveAnnotation(annotationClass, interceptorMetaData, classLoader, method);
   }
}
