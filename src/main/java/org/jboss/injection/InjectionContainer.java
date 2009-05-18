/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameNotFoundException;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.virtual.VirtualFile;

/**
 * This is the container that manages all injections.  Could be an EJB Container
 * or a WAR.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public interface InjectionContainer
{
   /** Some identifier that can be used in error messages */
   String getIdentifier();

   /** 
    * For error messages
    * @return  ejb-jar.xml, web.xml, etc..
    */
   String getDeploymentDescriptorType();

   ClassLoader getClassloader();

   Map<String, EncInjector> getEncInjectors();
   Map<String, Map<AccessibleObject, Injector>> getEncInjections();

   // EncInjectors/Handlers may need to add extra instance injectors
   List<Injector> getInjectors();
   
   VirtualFile getRootFile();

   Context getEnc();

   /**
    * @param link
    * @param businessIntf
    * @return
    * @deprecated dependency resolving must not rely on runtime components
    */
   @Deprecated
   Container resolveEjbContainer(String link, Class<?> businessIntf);
   
   /**
    * @param businessIntf
    * @return
    * @throws NameNotFoundException
    * @deprecated dependency resolving must not rely on runtime components
    */
   @Deprecated
   Container resolveEjbContainer(Class<?> businessIntf) throws NameNotFoundException;
   
   String getEjbJndiName(Class<?> businessInterface) throws NameNotFoundException;
   String getEjbJndiName(String link, Class<?> businessInterface);
   
   /**
    * Find a message destination in a deployment.
    * 
    * @param link   the message destination name
    * @return       the jndi name of the message destination
    */
   String resolveMessageDestination(String link);

   /**
    * If class has container overridable annotations, this method will
    * discover those overriden annotations.
    */
   <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz);
   
   /**
    * If class has container overridable annotations, this method will
    * discover those overriden annotations.
    */
   <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Method method);
   
   <T extends Annotation> T getAnnotation(Class<T> annotationType, Method method);

   /**
    * If class has container overridable annotations, this method will
    * discover those overriden annotations.
    */
   <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Field field);
   
   <T extends Annotation> T getAnnotation(Class<T> annotationType, Field field);

   DependencyPolicy getDependencyPolicy();
   
   RemoteEnvironment getEnvironmentRefGroup();
   
   boolean hasJNDIBinding(String jndiName);
}
