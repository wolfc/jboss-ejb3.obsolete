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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;

import org.hibernate.SessionFactory;
import org.jboss.ejb3.annotation.IgnoreDependency;
import org.jboss.ejb3.entity.InjectedSessionFactory;
import org.jboss.jpa.deployment.ManagedEntityManagerFactory;
import org.jboss.jpa.injection.InjectedEntityManagerFactory;
import org.jboss.jpa.spi.PersistenceUnitRegistry;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * Searches bean class for all @Inject and create Injectors
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class PersistenceUnitHandler<X extends RemoteEnvironment> implements InjectionHandler<X>
{
   private static final Logger log = Logger.getLogger(PersistenceUnitHandler.class);
   
   /**
    * Contracted error message value, form may be checked from Integration Tests
    */
   public static final String ERROR_MESSAGE_FAILED_TO_RESOVLE_PU = " failed to resolve persistence unit ";

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getPersistenceUnitRefs() == null) return;

      for (PersistenceUnitReferenceMetaData ref : xml.getPersistenceUnitRefs())
      {
         String encName = "env/" + ref.getPersistenceUnitRefName();
         // we add injection target no matter what.  enc injection might be overridden but
         // XML injection cannot be overriden
         Class<?> injectionType = InjectionUtil.injectionTarget(encName, ref, container, container.getEncInjections());
         if (container.getEncInjectors().containsKey(encName))
            return;
         container.getEncInjectors().put(encName, new PuEncInjector(encName, injectionType, ref.getPersistenceUnitName(), "<persistence-unit-ref>"));
         try
         {
            addPUDependency(ref.getPersistenceUnitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal <persistence-unit-ref> of " + ref.getPersistenceUnitRefName() + " :" + e.getMessage());
         }
      }
   }


   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      PersistenceUnits resources = container.getAnnotation(
              PersistenceUnits.class, clazz);
      if (resources != null)
      {
         for (PersistenceUnit ref : resources.value())
         {
            handleClassAnnotation(ref, container, clazz);
         }
      }
      PersistenceUnit pu = container.getAnnotation(PersistenceUnit.class, clazz);
      if (pu != null)
      {
         handleClassAnnotation(pu, container, clazz);
      }
   }

   private static void handleClassAnnotation(PersistenceUnit ref, InjectionContainer container, Class<?> clazz)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         throw new RuntimeException("JBoss requires name() for class level @PersistenceUnit");
      }
      encName = "env/" + encName;
      if (container.getEncInjectors().containsKey(encName)) return;
      container.getEncInjectors().put(encName, new PuEncInjector(encName, null, ref.unitName(), "@PersistenceUnit"));
      try
      {
         addPUDependency(ref.unitName(), container);
      }
      catch (NameNotFoundException e)
      {
         throw new RuntimeException("Illegal @PersistenceUnit on " + clazz.getName() + " of unitname " + ref.unitName() + " :" + e.getMessage());
      }
   }

   public static void addPUDependency(String unitName, InjectionContainer container) throws NameNotFoundException
   {
      if(container instanceof ExtendedInjectionContainer)
      {
         ExtendedInjectionContainer eic = (ExtendedInjectionContainer) container;
         try
         {
            String dependency = eic.resolvePersistenceUnitSupplier(unitName);
            container.getDependencyPolicy().addDependency(dependency);
         }
         catch(IllegalArgumentException e)
         {
            throw new IllegalArgumentException("Container " + container + ERROR_MESSAGE_FAILED_TO_RESOVLE_PU + unitName, e);
         }
         return;
      }
      throw new UnsupportedOperationException("Container " + container + " does not implement ExtendedInjectionContainer, can't resolve persistence unit " + unitName);
   }

   public static ManagedEntityManagerFactory getManagedEntityManagerFactory(InjectionContainer container, String unitName)
           throws NameNotFoundException
   {
      if(container instanceof ExtendedInjectionContainer)
      {
         ExtendedInjectionContainer eic = (ExtendedInjectionContainer) container;
         String beanName = eic.resolvePersistenceUnitSupplier(unitName);
         return ((org.jboss.jpa.deployment.PersistenceUnitDeployment) PersistenceUnitRegistry.getPersistenceUnit(beanName)).getManagedFactory();
      }
      throw new UnsupportedOperationException("Container " + container + " does not implement ExtendedInjectionContainer, can't resolve persistence unit " + unitName);
   }

   public static EntityManagerFactory getEntityManagerFactory(PersistenceUnit ref, InjectionContainer container) throws NameNotFoundException
   {
      return getEntityManagerFactory(ref.unitName(), container);
   }

   public static Object getFactory(Class<?> type, String unitName, InjectionContainer container) throws NameNotFoundException
   {
      if (type != null && type.getName().equals(SessionFactory.class.getName()))
         return getSessionFactory(unitName, container);
      return getEntityManagerFactory(unitName, container);
   }

   public static EntityManagerFactory getEntityManagerFactory(String unitName, InjectionContainer container) throws NameNotFoundException
   {
      if(container instanceof ExtendedInjectionContainer)
      {
         ExtendedInjectionContainer eic = (ExtendedInjectionContainer) container;
         String beanName = eic.resolvePersistenceUnitSupplier(unitName);
         ManagedEntityManagerFactory managedFactory = ((org.jboss.jpa.deployment.PersistenceUnitDeployment) PersistenceUnitRegistry.getPersistenceUnit(beanName)).getManagedFactory();
         return new InjectedEntityManagerFactory(managedFactory);
      }
      throw new UnsupportedOperationException("Container " + container + " does not implement ExtendedInjectionContainer, can't resolve persistence unit " + unitName);
   }


   private static SessionFactory getSessionFactory(String ref, InjectionContainer container) throws NameNotFoundException
   {
      if(container instanceof ExtendedInjectionContainer)
      {
         ExtendedInjectionContainer eic = (ExtendedInjectionContainer) container;
         String beanName = eic.resolvePersistenceUnitSupplier(ref);
         ManagedEntityManagerFactory managedFactory = ((org.jboss.jpa.deployment.PersistenceUnitDeployment) PersistenceUnitRegistry.getPersistenceUnit(beanName)).getManagedFactory();
         return new InjectedSessionFactory(managedFactory);
      }
      throw new UnsupportedOperationException("Container " + container + " does not implement ExtendedInjectionContainer, can't resolve persistence unit " + ref);
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      PersistenceUnit ref = method.getAnnotation(PersistenceUnit.class);
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
         encName = "env/" + encName;
      }
      if (!container.getEncInjectors().containsKey(encName))
      {
         container.getEncInjectors().put(encName, new PuEncInjector(encName, method.getParameterTypes()[0], ref.unitName(), "@PersistenceUnit"));
         try
         {
            if (!method.isAnnotationPresent(IgnoreDependency.class)) addPUDependency(ref.unitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal @PersistenceUnit on " + method + " :" + e.getMessage());
         }
      }

      injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      PersistenceUnit ref = field.getAnnotation(PersistenceUnit.class);
      if (ref == null) return;
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(field);
      }
      else
      {
         encName = "env/" + encName;
      }
      if (!container.getEncInjectors().containsKey(encName))
      {
         container.getEncInjectors().put(encName, new PuEncInjector(encName, field.getType(), ref.unitName(), "@PersistenceUnit"));
         try
         {
            if (!field.isAnnotationPresent(IgnoreDependency.class)) addPUDependency(ref.unitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal @PersistenceUnit on " + field + " :" + e.getMessage());
         }
      }

      injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
   }
}
