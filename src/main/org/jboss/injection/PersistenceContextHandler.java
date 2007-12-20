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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContexts;

import org.jboss.ejb3.annotation.IgnoreDependency;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.PersistenceContextReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * Searches bean class for all
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 67628 $
 * @Inject and create Injectors
 */
public class PersistenceContextHandler<X extends RemoteEnvironment> implements InjectionHandler<X>
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger
           .getLogger(PersistenceContextHandler.class);

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml instanceof Environment == false) return;
      Environment env = (Environment) xml;
      if (env.getPersistenceContextRefs() == null) return;
      for (PersistenceContextReferenceMetaData ref : env.getPersistenceContextRefs())
      {
         String encName = "env/" + ref.getPersistenceContextRefName();
         // we add injection target no matter what.  enc injection might be overridden but
         // XML injection cannot be overriden
         Class<?> injectionType = InjectionUtil.injectionTarget(encName, ref, container, container.getEncInjections());

         if (container.getEncInjectors().containsKey(encName))
            continue;
         // add it to list of
         String error = "unable to load <persistence-context-ref> for unitName: "
                 + ref.getPersistenceUnitName() + " <ref-name>: " + ref.getPersistenceContextRefName();
         PersistenceContextType type = ref.getPersistenceContextType();
         String unitName = ref.getPersistenceUnitName();
         container.getEncInjectors().put(encName, new PcEncInjector(encName, unitName, type, injectionType, error));
         try
         {
            PersistenceUnitHandler.addPUDependency(ref.getPersistenceUnitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal <persistence-context-ref> of " + ref.getPersistenceContextRefName() + " :" + e.getMessage());
         }
      }
   }

   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      PersistenceContexts resources = container.getAnnotation(PersistenceContexts.class, clazz);
      if (resources != null)
      {
         for (PersistenceContext ref : resources.value())
         {
            loadPersistenceContextClassAnnotation(ref, container, clazz);
         }
      }
      PersistenceContext pc = container.getAnnotation(PersistenceContext.class, clazz);

      if (pc != null)
      {
         loadPersistenceContextClassAnnotation(pc, container, clazz);
      }

   }

   private static void loadPersistenceContextClassAnnotation(
           PersistenceContext ref, InjectionContainer container, Class<?> clazz)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         throw new RuntimeException(
                 "JBoss requires name() for class level @PersistenceContext");
      }
      encName = "env/" + ref.name();
      if (container.getEncInjectors().containsKey(encName)) return;

      String error = "Unable to load class-level @PersistenceContext("
              + ref.unitName() + ") on " + container.getIdentifier();
      container.getEncInjectors().put(encName, new PcEncInjector(encName, ref.unitName(), ref.type(), null, error));
      try
      {
         PersistenceUnitHandler.addPUDependency(ref.unitName(), container);
      }
      catch (NameNotFoundException e)
      {
         throw new RuntimeException("Illegal @PersistenceUnit on " + clazz.getName() + " of unitname " + ref.unitName() + " :" + e.getMessage());
      }
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      PersistenceContext ref = method.getAnnotation(PersistenceContext.class);
      if (ref == null) return;
      if (!method.getName().startsWith("set"))
         throw new RuntimeException("@PersistenceUnit can only be used with a set method: " + method);

      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(method);
      }
      else
      {
         encName = "env/" + ref.name();
      }
      if (!container.getEncInjectors().containsKey(encName))
      {
         try
         {
            if (!method.isAnnotationPresent(IgnoreDependency.class)) PersistenceUnitHandler.addPUDependency(ref.unitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal @PersistenceUnit on " + method + " :" + e.getMessage());
         }
         String error = "@PersistenceContext(name='" + encName
                 + "',unitName='" + ref.unitName() + "') on EJB: "
                 + container.getIdentifier() + " failed to inject on method "
                 + method.toString();
         container.getEncInjectors().put(encName, new PcEncInjector(encName, ref.unitName(), ref.type(), method.getParameterTypes()[0], error));
      }
      injectors.put(method, new JndiMethodInjector(method,
              encName, container.getEnc()));
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      PersistenceContext ref = field.getAnnotation(PersistenceContext.class);
      if (ref == null) return;

      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(field);
      }
      else
      {
         encName = "env/" + ref.name();
      }
      if (!container.getEncInjectors().containsKey(encName))
      {
         try
         {
            if (!field.isAnnotationPresent(IgnoreDependency.class)) PersistenceUnitHandler.addPUDependency(ref.unitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal @PersistenceUnit on " + field + " :" + e.getMessage());
         }
         String error = "@PersistenceContext(name='" + encName
                 + "',unitName='" + ref.unitName() + "') on EJB: "
                 + container.getIdentifier() + " failed to inject on field "
                 + field.toString();
         container.getEncInjectors().put(encName, new PcEncInjector(encName, ref.unitName(), ref.type(), field.getType(), error));
      }
      injectors.put(field, new JndiFieldInjector(field,
              encName, container.getEnc()));
   }
}
