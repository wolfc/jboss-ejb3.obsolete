/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.injection.test.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameNotFoundException;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.injection.EncInjector;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.Injector;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DummyInjectionContainer implements InjectionContainer
{
   private Context enc;
   private Map<String, Map<AccessibleObject, Injector>> encInjections = new HashMap<String, Map<AccessibleObject, Injector>>();
   private Map<String, EncInjector> encInjectors = new HashMap<String, EncInjector>();
   
   public DummyInjectionContainer(Context enc)
   {
      this.enc = enc;
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz)
   {
      return null;
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Method method)
   {
      return null;
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Method method)
   {
      return null;
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Field field)
   {
      return null;
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Field field)
   {
      return null;
   }

   public ClassLoader getClassloader()
   {
      return Thread.currentThread().getContextClassLoader();
   }

   public DependencyPolicy getDependencyPolicy()
   {
      return null;
   }

   public String getDeploymentDescriptorType()
   {
      return null;
   }

   public String getEjbJndiName(Class<?> businessInterface) throws NameNotFoundException
   {
      return null;
   }

   public String getEjbJndiName(String link, Class<?> businessInterface)
   {
      return null;
   }

   public Context getEnc()
   {
      return enc;
   }

   public Map<String, Map<AccessibleObject, Injector>> getEncInjections()
   {
      return encInjections;
   }

   public Map<String, EncInjector> getEncInjectors()
   {
      return encInjectors;
   }

   public RemoteEnvironment getEnvironmentRefGroup()
   {
      return null;
   }

   public String getIdentifier()
   {
      return null;
   }

   public List<Injector> getInjectors()
   {
      return null;
   }

   public PersistenceUnitDeployment getPersistenceUnitDeployment(String unitName) throws NameNotFoundException
   {
      return null;
   }

   public VirtualFile getRootFile()
   {
      return null;
   }

   public boolean hasJNDIBinding(String jndiName)
   {
      return false;
   }

   public Container resolveEjbContainer(String link, Class<?> businessIntf)
   {
      return null;
   }

   public Container resolveEjbContainer(Class<?> businessIntf) throws NameNotFoundException
   {
      return null;
   }

   public String resolveMessageDestination(String link)
   {
      return null;
   }

}
