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
package org.jboss.ejb3.interceptors.proxy.aop;

import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.Domain;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.InstanceAdvisorDelegate;
import org.jboss.aop.advice.AspectDefinition;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ManagedObjectContainer extends ClassAdvisor implements InstanceAdvisor
{
   private static final Logger log = Logger.getLogger(ManagedObjectContainer.class);
   
   private InstanceAdvisorDelegate instanceAdvisorDelegate;
   
   public ManagedObjectContainer(String name, AspectManager manager, Class<?> beanClass)
   {
      super(name, manager);
      assert beanClass != null : "beanClass is null";
      // Poking starts here
      attachClass(beanClass);
      
      this.instanceAdvisorDelegate = new InstanceAdvisorDelegate(this, this);
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
