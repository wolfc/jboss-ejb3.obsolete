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

import java.util.List;

import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.Domain;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.InstanceAdvisorDelegate;
import org.jboss.aop.advice.AspectDefinition;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.introduction.AnnotationIntroduction;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.ejb3.interceptors.ManagedObject;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ManagedObjectAdvisor<T, C extends AbstractContainer<T, C>> extends ClassAdvisor implements InstanceAdvisor
{
   private static final Logger log = Logger.getLogger(ManagedObjectAdvisor.class);
   
   private C container;
   private InstanceAdvisorDelegate instanceAdvisorDelegate;
   
   protected ManagedObjectAdvisor(C container, String name, AspectManager manager, Class<?> beanClass)
   {
      super(name, manager);
      assert beanClass != null : "beanClass is null";
      
      // For convenience we add the ManagedObject annotation
      annotations.addClassAnnotation(ManagedObject.class, new Object());
      
      // Poking starts here
      attachClass(beanClass);
      
      this.instanceAdvisorDelegate = new InstanceAdvisorDelegate(this, this);
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
   
   @Override
   protected void rebindClassMetaData()
   {
      super.rebindClassMetaData();
      
      // Why does AOP not process the annotation introductions!?
      deployAnnotationIntroductions();
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
      return instanceAdvisorDelegate.getMetaData();
   }

   public Object getPerInstanceAspect(String aspectName)
   {
      // TODO: is this correct?
      return instanceAdvisorDelegate.getPerInstanceAspect(aspectName);
   }

   public Object getPerInstanceAspect(AspectDefinition def)
   {
      return instanceAdvisorDelegate.getPerInstanceAspect(def);
   }

   public Object getPerInstanceJoinpointAspect(Joinpoint joinpoint, AspectDefinition def)
   {
      return instanceAdvisorDelegate.getPerInstanceJoinpointAspect(joinpoint, def);
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
}
