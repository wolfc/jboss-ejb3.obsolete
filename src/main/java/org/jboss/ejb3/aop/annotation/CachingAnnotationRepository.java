/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.aop.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CtMember;

import org.jboss.annotation.factory.AnnotationCreator;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.ejb3.metadata.annotation.AnnotationRepositoryToMetaData;
import org.jboss.ejb3.metadata.annotation.ExtendedAnnotationRepository;
import org.jboss.logging.Logger;

/**
 * Cache the results from an AnnotationRepository to get some speed out of the lookup.
 * 
 * Note that this class is not thread safe. If one thread adds annotations while another reads
 * there is no guarantee which result is returned.
 * 
 * The following operation are not supported:
 * - anything using javassist
 * - querying for complete annotation maps
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class CachingAnnotationRepository extends AnnotationRepository implements ExtendedAnnotationRepository
{
   private static final Logger log = Logger.getLogger(CachingAnnotationRepository.class);
   
   // a magic NULL marker for in the cache
   private static final Object NULL = new Object();
   
   private AnnotationRepositoryToMetaData delegate;
   // Because AnnotationRepositoryToMetaData also does class loading, we should do this as well to
   // cache the String variants of annotation lookup.
   private ClassLoader classLoader;
   
   private Map<Class<?>, Object> classAnnotationsCache = new ConcurrentHashMap<Class<?>, Object>();
   private ConcurrentHashMap<Member, Map<Class<?>, Object>> memberCache = new ConcurrentHashMap<Member, Map<Class<?>, Object>>();
   
   public CachingAnnotationRepository(AnnotationRepositoryToMetaData delegate, ClassLoader classLoader)
   {
      assert delegate != null;
      assert classLoader != null;
      
      this.delegate = delegate;
      this.classLoader = classLoader;
   }
   
   @Override
   public void addAnnotation(CtMember m, String annotation)
   {
      log.error("EJBTHREE-1914: Unsupported");
      throw new UnsupportedOperationException("EJBTHREE-1914: Unsupported");
   }
   
   @Override
   public void addAnnotation(Member m, String annotation, Object value)
   {
      addAnnotation(m, loadClass(annotation), initAnnotation(value));
   }
   
   @Override
   public void addAnnotation(Member m, Class annotation, Object value)
   {
      Map<Class<?>, Object> annotationCache = getAnnotationCache(m);
      annotationCache.put(annotation, value);
      delegate.addAnnotation(m, annotation, value);
   }
   
   @Override
   public void addClassAnnotation(Class annotationType, Object value)
   {
      classAnnotationsCache.put(annotationType, value);
      delegate.addClassAnnotation(annotationType, value);
   }
   
   @Override
   public void addClassAnnotation(String annotation, String value)
   {
      addClassAnnotation(loadClass(annotation), initAnnotation(value));
   }
   
   @Override
   public void disableAnnotation(Member m, String annotation)
   {
      delegate.disableAnnotation(m, annotation);
   }
   
   @Override
   public void disableAnnotation(String annotation)
   {
      delegate.disableAnnotation(annotation);
   }
   
   @Override
   public void enableAnnotation(String annotation)
   {
      delegate.enableAnnotation(annotation);
   }
   
   protected Map<Class<?>, Object> getAnnotationCache(Member m)
   {
      Map<Class<?>, Object> annotationCache = memberCache.get(m);
      if(annotationCache == null)
      {
         annotationCache = new ConcurrentHashMap<Class<?>, Object>();
         memberCache.put(m, annotationCache);
      }
      return annotationCache;
   }
   
   @Override
   public Map getAnnotations()
   {
      log.error("EJBTHREE-1914: Unsupported");
      throw new UnsupportedOperationException("EJBTHREE-1914: Unsupported");
   }
   
   @Override
   public Map getClassAnnotations()
   {
      log.error("EJBTHREE-1914: Unsupported");
      throw new UnsupportedOperationException("EJBTHREE-1914: Unsupported");
   }
   
   public boolean hasAnnotation(Class<?> cls, Class<? extends Annotation> annotationType)
   {
      return delegate.hasAnnotation(cls, annotationType);
   }
   
   public boolean hasAnnotation(Class<?> cls, Member member, Class<? extends Annotation> annotationType)
   {
      return delegate.hasAnnotation(cls, member, annotationType);
   }
   
   @Override
   public boolean hasAnnotation(CtMember m, String annotation)
   {
      log.error("EJBTHREE-1914: Unsupported");
      throw new UnsupportedOperationException("EJBTHREE-1914: Unsupported");
   }
   
   @Override
   public boolean hasAnnotation(Member m, Class annotation)
   {
      return resolveAnnotation(m, annotation) != null;
   }
   
   @Override
   public boolean hasAnnotation(Member m, String annotation)
   {
      return hasAnnotation(m, loadClass(annotation));
   }
   
   @Override
   public boolean hasClassAnnotation(Class annotationType)
   {
      return resolveClassAnnotation(annotationType) != null;
   }
   
   @Override
   public boolean hasClassAnnotation(String annotation)
   {
      return hasClassAnnotation(loadClass(annotation));
   }

   // See AnnotationRepositoryToMetaData.initAnnotation
   protected Annotation initAnnotation(Object annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");

      if (annotation instanceof Annotation)
         return (Annotation) annotation;
      
      if (annotation instanceof String == false)
         throw new IllegalArgumentException("Not an annotation: " + annotation);
      
      try
      {
         return (Annotation) AnnotationCreator.createAnnotation((String) annotation, classLoader);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error creating annotation: " + annotation, e);
      }
   }
   
   @Override
   public boolean isDisabled(Class annotation)
   {
      return delegate.isDisabled(annotation);
   }
   
   @Override
   public boolean isDisabled(Member m, Class annotation)
   {
      return delegate.isDisabled(m, annotation);
   }
   
   @Override
   public boolean isDisabled(Member m, String annotation)
   {
      log.error("EJBTHREE-1914: Unsupported");
      throw new UnsupportedOperationException("EJBTHREE-1914: Unsupported");
   }
   
   @Override
   public boolean isDisabled(String annotation)
   {
      log.error("EJBTHREE-1914: Unsupported");
      throw new UnsupportedOperationException("EJBTHREE-1914: Unsupported");
   }
   
   protected Class<?> loadClass(String className)
   {
      try
      {
         return classLoader.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         // AOP discards exceptions
         if(log.isTraceEnabled())
            log.trace(e.getMessage(), e);
         throw new RuntimeException(e);
      }
   }
   
   public <A extends Annotation> A resolveAnnotation(Class<?> cls, Class<A> annotationType)
   {
      return delegate.resolveAnnotation(cls, annotationType);
   }
   
   public <A extends Annotation> A resolveAnnotation(Class<?> cls, Member member, Class<A> annotationType)
   {
      return delegate.resolveAnnotation(cls, member, annotationType);
   }
   
   @Override
   public Object resolveAnnotation(Member m, Class annotationType)
   {
      Map<Class<?>, Object> annotationCache = getAnnotationCache(m);
      Object annotation = annotationCache.get(annotationType);
      if(annotation == null)
      {
         annotation = delegate.resolveAnnotation(m, annotationType);
         if(annotation != null)
            annotationCache.put(annotationType, annotation);
         else
            annotationCache.put(annotationType, NULL);
      }
      if(annotation == NULL)
         annotation = null;
      return annotation;
   }
   
   @Override
   protected Object resolveAnnotation(Member m, String annotation)
   {
      log.error("EJBTHREE-1914: Unsupported");
      throw new UnsupportedOperationException("EJBTHREE-1914: Unsupported");
   }
   
   @Override
   public Object resolveClassAnnotation(Class annotationType)
   {
      Object annotation = classAnnotationsCache.get(annotationType);
      if(annotation == null)
      {
         annotation = delegate.resolveClassAnnotation(annotationType);
         if(annotation != null)
            classAnnotationsCache.put(annotationType, annotation);
         else
            classAnnotationsCache.put(annotationType, NULL);
      }
      if(annotation == NULL)
         annotation = null;
      return annotation;
   }
}
