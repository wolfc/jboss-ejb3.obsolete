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
package org.jboss.ejb3.metadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.interceptor.Interceptors;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.impl.InterceptorsImpl;
import org.jboss.ejb3.annotation.impl.SecurityDomainImpl;
import org.jboss.ejb3.metadata.plugins.loader.ClassMetaDataLoader;
import org.jboss.ejb3.metadata.plugins.loader.InterceptorClassMetaDataLoader;
import org.jboss.ejb3.metadata.spi.signature.ClassSignature;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingsMetaData;
import org.jboss.metadata.ejb.spec.InterceptorClassesMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.metadata.ejb.spec.InterceptorsMetaData;
import org.jboss.metadata.plugins.loader.BasicMetaDataLoader;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.AnnotationsItem;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationItem;
import org.jboss.metadata.spi.retrieval.simple.SimpleAnnotationsItem;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.MethodSignature;
import org.jboss.metadata.spi.signature.Signature;

/**
 * EJBMetaDataLoader.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class EJBMetaDataLoader extends ClassMetaDataLoader
{
   /** The container */
   private JBossEnterpriseBeanMetaData beanMetaData;
   
   private ClassLoader classLoader;
   
   /**
    * Create a new EJBMetaDataLoader.
    * 
    * @param key            the scope
    * @param beanMetaData   the meta data associated with this EJB or null
    * @param classLoader    the class loader that must be used to load new classes
    */
   public EJBMetaDataLoader(ScopeKey key, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader)
   {
      super(key);
      assert classLoader != null : "classLoader is null";
      
      this.beanMetaData = beanMetaData;
      this.classLoader = classLoader;
   }
   
   protected MetaDataRetrieval createComponentMetaDataRetrieval(Signature signature)
   {
      JBossEnterpriseBeanMetaData beanMetaData = getBeanMetaData();
      if (beanMetaData == null)
         return null;

      MetaDataRetrieval retrieval = null;
      if(signature instanceof ClassSignature)
      {
         // FIXME: it's not always an interceptor, could be a super class
         retrieval = new InterceptorClassMetaDataLoader(getScope(), findInterceptor(signature.getName()));
      }
      else if(signature instanceof MethodSignature)
         retrieval = new MethodMetaDataRetrieval((MethodSignature) signature);
      
      return retrieval;
   }

   private InterceptorMetaData findInterceptor(String name)
   {
      InterceptorsMetaData interceptors = beanMetaData.getEjbJarMetaData().getInterceptors();
      for(InterceptorMetaData interceptorMetaData : interceptors)
      {
         if(interceptorMetaData.getInterceptorClass().equals(name))
            return interceptorMetaData;
      }
      return null;
   }
   
   /**
    * Get the bean metadata
    * 
    * @return the bean metadata
    */
   protected JBossEnterpriseBeanMetaData getBeanMetaData()
   {
      return beanMetaData;
   }
   
   public boolean isEmpty()
   {
      return getBeanMetaData() != null;
   }

   private Class<?> loadClass(String name)
   {
      try
      {
         return classLoader.loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
   {
      JBossEnterpriseBeanMetaData beanMetaData = getBeanMetaData();
      if (beanMetaData == null)
         return null;
      
      String ejbName = beanMetaData.getEjbName();
      
      if(annotationType == Interceptors.class)
      {
         InterceptorBindingsMetaData bindings = beanMetaData.getEjbJarMetaData().getAssemblyDescriptor().getInterceptorBindings();
         if(bindings != null)
         {
            for(InterceptorBindingMetaData binding : bindings)
            {
               // For the method component
               if(binding.getMethod() != null)
                  continue;
               
               String bindingEjbName = binding.getEjbName();
               if(bindingEjbName.equals("*") || bindingEjbName.equals(ejbName))
               {
                  //List<Class<?>> interceptorClasses = new ArrayList<Class<?>>();
                  InterceptorsImpl interceptors = new InterceptorsImpl();
                  InterceptorClassesMetaData interceptorClassesMetaData;
                  if(binding.isTotalOrdering())
                  {
                     interceptorClassesMetaData = binding.getInterceptorOrder();
                  }
                  else
                  {
                     interceptorClassesMetaData = binding.getInterceptorClasses();
                  }
                  for(String interceptorClassName : interceptorClassesMetaData)
                  {
                     interceptors.addValue(loadClass(interceptorClassName));
                  }
                  return new SimpleAnnotationItem<T>(annotationType.cast(interceptors));
               }
            }
         }
      }
      
      if (annotationType == SecurityDomain.class)
      {
         String securityDomain = beanMetaData.getSecurityDomain();
         if (securityDomain != null)
            return new SimpleAnnotationItem<T>(annotationType.cast(new SecurityDomainImpl(securityDomain)));
      }
      return null;
   }
   
   public AnnotationsItem retrieveAnnotations()
   {
      List<AnnotationItem<?>> annotations = new ArrayList<AnnotationItem<?>>();
      AnnotationItem<SecurityDomain> annotation = retrieveAnnotation(SecurityDomain.class);
      if (annotation != null)
         annotations.add(annotation);
      if (annotations.isEmpty())
         return SimpleAnnotationsItem.NO_ANNOTATIONS;
      else
         return new SimpleAnnotationsItem(annotations.toArray(new AnnotationItem[annotations.size()]));
   }

   /**
    * MethodMetaDataRetrieval.
    */
   private class MethodMetaDataRetrieval extends BasicMetaDataLoader
   {
      /** The signature */
      private MethodSignature signature;
      
      /**
       * Create a new MethodMetaDataRetrieval.
       * 
       * @param methodSignature the signature
       */
      public MethodMetaDataRetrieval(MethodSignature methodSignature)
      {
         this.signature = methodSignature;
      }

      public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
      {
         return null;
      }

      public boolean isEmpty()
      {
         return false;
      }

      public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
      {
         /* Example 
         JBossEnterpriseBeanMetaData beanMetaData = getBeanMetaData();
         if (beanMetaData == null)
            return null;
         
         if (annotationType == TransactionTimeout.class)
         {
            MethodAttributesMetaData methodAttributes = beanMetaData.getMethodAttributes();
            int timeout = methodAttributes.getMethodTransactionTimeout(signature.getName());
            return new SimpleAnnotationItem(new TransactionTimeoutImpl(timeout));
         }
         */
         return null;
      }

      public AnnotationsItem retrieveAnnotations()
      {
         // TODO
         return SimpleAnnotationsItem.NO_ANNOTATIONS;
      }
   }
}
