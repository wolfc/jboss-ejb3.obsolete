/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.aop;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.aop.AspectManager;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;

/**
 * The extended manager object advisor allows for virtual methods.
 * 
 * TODO: move this functionality to ejb3-interceptors
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ExtendedManagedObjectAdvisor extends ManagedObjectAdvisor<Object, BeanContainer>
{
   protected ExtendedManagedObjectAdvisor(BeanContainer container, String name, AspectManager manager, AnnotationRepository annotations)
   {
      super(container, name, manager, annotations);
   }
   
   @Override
   protected void createMethodTables() throws Exception
   {
      super.createMethodTables();
      List<Method> virtualMethods = getVirtualMethods();
      if(virtualMethods != null)
      {
         for(Method virtualMethod : virtualMethods)
         {
            long hash = MethodHashing.methodHash(virtualMethod);
            advisedMethods.put(hash, virtualMethod);
         }
      }
   }
   
   private List<Method> getVirtualMethods()
   {
      return getContainer().getVirtualMethods();
   }
   
   @Override
   protected void initialize(Class<?> beanClass)
   {
      super.initialize(beanClass);
   }
   
   protected void reinitialize() throws Exception
   {
      // recreate the advised methods, because virtual methods are now filled
      createMethodTables();
      // for some reason methodInfos are not rebuild during rebuildInterceptors
      initializeMethodChain();
      rebuildInterceptors();
   }
}
