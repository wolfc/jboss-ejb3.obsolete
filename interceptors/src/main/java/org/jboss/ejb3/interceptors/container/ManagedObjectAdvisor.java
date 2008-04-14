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
package org.jboss.ejb3.interceptors.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.Domain;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.InstanceAdvisorDelegate;
import org.jboss.aop.advice.AspectDefinition;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.InterceptorFactory;
import org.jboss.aop.advice.Scope;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.aop.introduction.AnnotationIntroduction;
import org.jboss.aop.joinpoint.ConstructorJoinpoint;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.ejb3.interceptors.ManagedObject;
import org.jboss.ejb3.interceptors.aop.ExtendedAdvisor;
import org.jboss.ejb3.metadata.annotation.ExtendedAnnotationRepository;
import org.jboss.logging.Logger;

/**
 * 
 * Invariant:
 * - there is always an instance advisor delegate
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class ManagedObjectAdvisor<T, C extends AbstractContainer<T, C>> extends ClassAdvisor implements ExtendedAdvisor, InstanceAdvisor
{
   private static final Logger log = Logger.getLogger(ManagedObjectAdvisor.class);
   
   private C container;
   private InstanceAdvisorDelegate instanceAdvisorDelegate;
   
   protected ManagedObjectAdvisor(C container, String name, AspectManager manager)
   {
      this(container, name, manager, null);
   }

   /**
    * 
    * @param container
    * @param name
    * @param manager
    * @param pAnnotations   an alternate annotation repository or null for the default
    */
   protected ManagedObjectAdvisor(C container, String name, AspectManager manager, AnnotationRepository pAnnotations)
   {
      super(name, manager);
      
      assert container != null : "container is null";
      
      this.container = container;
      
      if(pAnnotations != null)
         this.annotations = pAnnotations;
      
      // For convenience we add the ManagedObject annotation
      ManagedObject annotation = new ManagedObject()
      {
         public Class<? extends Annotation> annotationType()
         {
            return ManagedObject.class;
         }
      };
      annotations.addClassAnnotation(ManagedObject.class, annotation);
   }
   
   /*
    * To match point cut expressions on super methods which apply only 
    * to the sub class, this value must be set to true.
    * 
    * @see org.jboss.aop.Advisor#chainOverridingForInheritedMethods()
    */
   @Override
   public boolean chainOverridingForInheritedMethods()
   {
      return true;
   }
   
   @Override
   protected void createInterceptorChain(InterceptorFactory[] factories, ArrayList newinterceptors, Joinpoint joinpoint)
   {
      // This is an ugly hack to make sure we have got an PER_JOINPOINT_INSTANCE interceptor.
      // Normally AOP won't create a per joinpoint instance interceptor if the join point is
      // a constructor and we're not running instrument (I've no clue why).
      
      if(joinpoint instanceof ConstructorJoinpoint)
      {
         for(InterceptorFactory factory : factories)
         {
            AspectDefinition def = factory.getAspect();
            if(def.getScope() == Scope.PER_JOINPOINT)
            {
               //addPerInstanceJoinpointAspect(joinpoint, def);
               newinterceptors.add(def.getFactory().createPerJoinpoint(this, this, joinpoint));
               return;
            }
         }
      }
      
      super.createInterceptorChain(factories, newinterceptors, joinpoint);
   }
   
   private void deployAnnotationIntroduction(AnnotationIntroduction introduction)
   {
      log.debug("deploy annotation introduction " + introduction);
      // Poke introductions into the overrides
      deployAnnotationOverride(introduction);
   }
   
   @SuppressWarnings("unchecked")
   private void deployAnnotationIntroductions()
   {
      List<AnnotationIntroduction> annotationIntroductions = getManager().getAnnotationIntroductions();
      if (annotationIntroductions != null)
      {
         for(AnnotationIntroduction ai : annotationIntroductions)
         {
            deployAnnotationIntroduction(ai);
         }
      }
   }

   public C getContainer()
   {
      return container;
   }
   
   protected void initialize(Class<?> beanClass)
   {
      assert beanClass != null : "beanClass is null";
      
      // Poking starts here
      attachClass(beanClass);
   }
   
   @Override
   protected void rebindClassMetaData()
   {
      super.rebindClassMetaData();
      
      // Why does AOP not process the annotation introductions!?
      deployAnnotationIntroductions();
   }
   
   /**
    * attachClass will want simpleMetaData at some point, which needs
    * an instanceAdvisorDelegate. So we have detached construction.
    * 
    * @return the instance advisor delegate, never null
    */
   private final InstanceAdvisorDelegate getInstanceAdvisorDelegate()
   {
      if(instanceAdvisorDelegate != null)
         return instanceAdvisorDelegate;

      synchronized (this)
      {
         if(instanceAdvisorDelegate == null)
         {
            instanceAdvisorDelegate = new InstanceAdvisorDelegate(this, this);
            instanceAdvisorDelegate.initialize();
         }
      }
      return instanceAdvisorDelegate;
   }
   
   public final void setInstanceAdvisorDelegate(InstanceAdvisorDelegate delegate)
   {
      instanceAdvisorDelegate = delegate;
   }
   
   public void appendInterceptor(Interceptor interceptor)
   {
      throw new RuntimeException("NYI");
   }

   public void appendInterceptor(int index, Interceptor interceptor)
   {
      throw new RuntimeException("NYI");
   }

   public void appendInterceptorStack(String stackName)
   {
      throw new RuntimeException("NYI");
   }

   public Domain getDomain()
   {
      throw new RuntimeException("NYI");
   }

   public Object getInstance()
   {
      throw new RuntimeException("NYI");
   }

   public Interceptor[] getInterceptors()
   {
      throw new RuntimeException("NYI");
   }

   public Interceptor[] getInterceptors(Interceptor[] baseChain)
   {
      throw new RuntimeException("NYI");
   }

   public SimpleMetaData getMetaData()
   {
      return getInstanceAdvisorDelegate().getMetaData();
   }

   public Object getPerInstanceAspect(String aspectName)
   {
      // TODO: is this correct?
      return getInstanceAdvisorDelegate().getPerInstanceAspect(aspectName);
   }

   public Object getPerInstanceAspect(AspectDefinition def)
   {
      return getInstanceAdvisorDelegate().getPerInstanceAspect(def);
   }

   public Object getPerInstanceJoinpointAspect(Joinpoint joinpoint, AspectDefinition def)
   {
      return getInstanceAdvisorDelegate().getPerInstanceJoinpointAspect(joinpoint, def);
   }

   public boolean hasInterceptors()
   {
      throw new RuntimeException("NYI");
   }

   public void insertInterceptor(Interceptor interceptor)
   {
      throw new RuntimeException("NYI");
   }

   public void insertInterceptor(int index, Interceptor interceptor)
   {
      throw new RuntimeException("NYI");
   }

   public void insertInterceptorStack(String stackName)
   {
      throw new RuntimeException("NYI");
   }

   public void removeInterceptor(String name)
   {
      throw new RuntimeException("NYI");
   }

   public void removeInterceptorStack(String name)
   {
      throw new RuntimeException("NYI");
   }
   
   /* ExtendedAdvisor */

   public boolean isAnnotationPresent(Class<?> cls, Class<? extends Annotation> annotationType)
   {
      if(annotations instanceof ExtendedAnnotationRepository)
      {
         // TODO: disabled?
         if(((ExtendedAnnotationRepository) annotations).hasAnnotation(cls, annotationType))
            return true;
      }
      return cls.isAnnotationPresent(annotationType);
   }

   public boolean isAnnotationPresent(Class<?> cls, Member member, Class<? extends Annotation> annotationType)
   {
      if(annotations instanceof ExtendedAnnotationRepository)
      {
         // TODO: disabled?
         if(((ExtendedAnnotationRepository) annotations).hasAnnotation(cls, member, annotationType))
            return true;
      }
      if(member instanceof AnnotatedElement)
      {
         if(((AnnotatedElement) member).isAnnotationPresent(annotationType))
            return true;
      }
      return false;
   }

   public <A extends Annotation> A resolveAnnotation(Class<?> cls, Class<A> annotationType)
   {
      A annotation = null;
      if(annotations instanceof ExtendedAnnotationRepository)
      {
         // TODO: disabled?
         annotation = ((ExtendedAnnotationRepository) annotations).resolveAnnotation(cls, annotationType);
      }
      if(annotation == null)
         annotation = cls.getAnnotation(annotationType);
      return annotation;
   }

   public <A extends Annotation> A resolveAnnotation(Class<?> cls, Member member, Class<A> annotationType)
   {
      A annotation = null;
      if(annotations instanceof ExtendedAnnotationRepository)
      {
         // TODO: disabled?
         annotation = ((ExtendedAnnotationRepository) annotations).resolveAnnotation(cls, member, annotationType);
      }
      if(annotation == null && member instanceof AnnotatedElement)
         annotation = ((AnnotatedElement) member).getAnnotation(annotationType);
      return annotation;
   }
}
