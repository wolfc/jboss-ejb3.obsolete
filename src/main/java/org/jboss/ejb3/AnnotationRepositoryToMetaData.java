/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.jboss.annotation.factory.AnnotationCreator;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.logging.Logger;
import org.jboss.metadata.plugins.context.AbstractMetaDataContext;
import org.jboss.metadata.plugins.loader.memory.MemoryMetaDataLoader;
import org.jboss.metadata.plugins.loader.reflection.ClassMetaDataRetrievalFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.context.MetaDataContext;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.retrieval.MetaDataRetrievalToMetaDataBridge;
import org.jboss.metadata.spi.scope.CommonLevels;
import org.jboss.metadata.spi.scope.Scope;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.ConstructorSignature;
import org.jboss.metadata.spi.signature.FieldSignature;
import org.jboss.metadata.spi.signature.MethodSignature;
import org.jboss.metadata.spi.signature.Signature;

/**
 * AnnotationRepositoryToMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class AnnotationRepositoryToMetaData extends AnnotationRepository
{
   /** The log */
   private static final Logger log = Logger.getLogger(AnnotationRepositoryToMetaData.class);
   
   private static final String CLASS_ANNOTATION = "CLASS";
   
   /** The metadata */
   private MetaData metaData;
   
   protected ConcurrentHashMap<Object, Object> disabledAnnotations = new ConcurrentHashMap<Object, Object>();;
   
   /** The mutable metadata */
   private MemoryMetaDataLoader mutableMetaData;
   
   /** The classloader */
   private ClassLoader classLoader;

   public AnnotationRepositoryToMetaData(EJBContainer container)
   {
      if (container == null)
         throw new IllegalArgumentException("Null container");

      Class<?> beanClass = container.getClazz();
      if (beanClass == null)
         throw new IllegalArgumentException("Null bean class");
      String name = container.getObjectName().getCanonicalName();
      if (name == null)
         throw new IllegalArgumentException("Null name");
      this.classLoader = container.getClassloader();
      if (classLoader == null)
         throw new IllegalArgumentException("Null class");
      
      MetaDataRetrieval classMetaData = ClassMetaDataRetrievalFactory.INSTANCE.getMetaDataRetrieval(new Scope(CommonLevels.CLASS, beanClass));
      ScopeKey instanceScope = new ScopeKey(CommonLevels.INSTANCE, name);
      mutableMetaData = new MemoryMetaDataLoader(instanceScope);
      MetaDataRetrieval dynamicXml = new EJBMetaDataLoader(instanceScope, container);
      
      MetaDataContext classContext = new AbstractMetaDataContext(classMetaData);
      MetaDataRetrieval[] instance = { dynamicXml, mutableMetaData }; 
      MetaDataContext instanceContext = new AbstractMetaDataContext(classContext, Arrays.asList(instance));
      metaData = new MetaDataRetrievalToMetaDataBridge(instanceContext);
   }
   
   /**
    * Create a signature from javassist member
    * 
    * @param member the member
    * @return the signature
    */
   protected static Signature getSignature(CtMember member)
   {
      if (member == null)
         throw new IllegalArgumentException("Null member");

      try
      {
         if (member instanceof CtMethod)
         {
            CtMethod method = (CtMethod) member;
            CtClass[] parameterTypes = method.getParameterTypes();
            String[] params = Signature.NO_PARAMETERS;
            if (parameterTypes.length > 0)
            {
               params = new String[parameterTypes.length];
               for (int i = 0; i < params.length; ++i)
                  params[i] = parameterTypes[i].getName();
            }
            return new MethodSignature(method.getName(), params);
         }
         if (member instanceof CtConstructor)
         {
            CtConstructor constructor = (CtConstructor) member;
            CtClass[] parameterTypes = constructor.getParameterTypes();
            String[] params = Signature.NO_PARAMETERS;
            if (parameterTypes.length > 0)
            {
               params = new String[parameterTypes.length];
               for (int i = 0; i < params.length; ++i)
                  params[i] = parameterTypes[i].getName();
            }
            return new ConstructorSignature(params);
         }
         if (member instanceof CtField)
         {
            return new FieldSignature(member.getName());
         }
         throw new IllegalArgumentException("Unknown member type: " + member);
      }
      catch (NotFoundException e)
      {
         throw new RuntimeException("Error determing signature: " + member, e);
      }
   }

   /**
    * Initialise an annotation
    * 
    * @param annotation the annotation or a string
    * @return the annotation
    */
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
   
   protected Class loadClass(String className)
   {
      try
      {
         return classLoader.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Unable to load class for annotation " + className + " using class loader " + classLoader);
      }
   }
   
   public void addAnnotation(CtMember m, String annotation)
   {
      mutableMetaData.addAnnotation(getSignature(m), initAnnotation(annotation));
   }

   public void addAnnotation(Member m, Class annotation, Object value)
   {
      mutableMetaData.addAnnotation(m, initAnnotation(value));
   }

   public void addAnnotation(Member m, String annotation, Object value)
   {
      mutableMetaData.addAnnotation(m, initAnnotation(value));
   }

   public void addClassAnnotation(Class annotation, Object value)
   {
      mutableMetaData.addAnnotation(initAnnotation(value));
   }

   public void addClassAnnotation(String annotation, String value)
   {
      mutableMetaData.addAnnotation(initAnnotation(value));
   }

   public void disableAnnotation(Member m, String annotation)
   {
      ConcurrentHashMap<String, String> disabledClassAnnotations = (ConcurrentHashMap<String, String>)disabledAnnotations.get(m);
      if (disabledClassAnnotations == null)
      {
         disabledClassAnnotations = new ConcurrentHashMap<String, String>();
         disabledAnnotations.put(m, disabledClassAnnotations);
      }
      
      disabledClassAnnotations.put(annotation, annotation);
   }

   public void disableAnnotation(String annotation)
   {
      ConcurrentHashMap<String, String> disabledClassAnnotations = (ConcurrentHashMap<String, String>)disabledAnnotations.get(CLASS_ANNOTATION);
      if (disabledClassAnnotations == null)
      {
         disabledClassAnnotations = new ConcurrentHashMap<String, String>();
         disabledAnnotations.put(CLASS_ANNOTATION, disabledClassAnnotations);
      }
      
      disabledClassAnnotations.put(annotation, annotation);
   }

   public void enableAnnotation(String annotation)
   {
      log.warn("Not implemented: enableAnnotation(" + annotation + ")");
   }

   public Map getAnnotations()
   {
      log.warn("Not implemented: getAnnotations()");
      return Collections.emptyMap();
   }

   public Map getClassAnnotations()
   {
      log.warn("Not implemented: getClassAnnotations()");
      return Collections.emptyMap();
   }

   public boolean hasAnnotation(CtMember m, String annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");
      MetaData component = metaData.getComponentMetaData(getSignature(m));
      if (component == null)
         return false;
      return component.isMetaDataPresent(annotation);
   }

   public boolean hasAnnotation(Member m, Class annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");
      MetaData component = metaData.getComponentMetaData(Signature.getSignature(m));
      if (component == null)
         return false;
      return component.isAnnotationPresent(annotation);
   }

   public boolean hasAnnotation(Member m, String annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");
      MetaData component = metaData.getComponentMetaData(Signature.getSignature(m));
      if (component == null)
         return false;
      return component.isAnnotationPresent(loadClass(annotation));
   }

   public boolean hasClassAnnotation(Class annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");
      return metaData.isAnnotationPresent(annotation);
   }

   public boolean hasClassAnnotation(String annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");
      return metaData.isAnnotationPresent(loadClass(annotation));
   }

   public boolean isDisabled(Class annotation)
   {
      ConcurrentHashMap<String, String> disabledClassAnnotations = (ConcurrentHashMap<String, String>)disabledAnnotations.get(CLASS_ANNOTATION);
      if (disabledClassAnnotations != null)
      {
         return disabledClassAnnotations.get(annotation.getName()) != null;
      }
      
      return false;
   }

   public boolean isDisabled(Member m, Class annotation)
   {
      ConcurrentHashMap<String, String> disabledClassAnnotations = (ConcurrentHashMap<String, String>)disabledAnnotations.get(CLASS_ANNOTATION);
      if (disabledClassAnnotations != null)
      {
         return disabledClassAnnotations.get(annotation.getName()) != null;
      }
      
      return false;
   }

   public boolean isDisabled(Member m, String annotation)
   {
      ConcurrentHashMap<String, String> disabledClassAnnotations = (ConcurrentHashMap<String, String>)disabledAnnotations.get(CLASS_ANNOTATION);
      if (disabledClassAnnotations != null)
      {
         return disabledClassAnnotations.get(annotation) != null;
      }
      
      return false;
   }

   public boolean isDisabled(String annotation)
   {
      return false;
   }

   public Object resolveClassAnnotation(Class annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");
      return metaData.getAnnotation(annotation);
   }

   public Object resolveAnnotation(Member m, Class annotation)
   {
      if (annotation == null)
         throw new IllegalArgumentException("Null annotation");
      MetaData component = metaData.getComponentMetaData(Signature.getSignature(m));
      if (component == null)
         return null;
      return component.getAnnotation(annotation);
   }

   protected Object resolveAnnotation(Member m, String annotation)
   {
      return resolveAnnotation(m, loadClass(annotation));
   }
}
