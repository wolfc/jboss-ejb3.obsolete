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
import org.jboss.annotation.IgnoreDependency;
import org.jboss.ejb3.entity.InjectedEntityManagerFactory;
import org.jboss.ejb3.entity.InjectedSessionFactory;
import org.jboss.ejb3.entity.ManagedEntityManagerFactory;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.logging.Logger;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;
import org.jboss.metamodel.descriptor.PersistenceUnitRef;

/**
 * Searches bean class for all @Inject and create Injectors
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class PersistenceUnitHandler implements InjectionHandler
{
   private static final Logger log = Logger.getLogger(PersistenceUnitHandler.class);

   public void loadXml(EnvironmentRefGroup xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getPersistenceUnitRefs() == null) return;

      for (PersistenceUnitRef ref : xml.getPersistenceUnitRefs())
      {
         String encName = "env/" + ref.getRefName();
         // we add injection target no matter what.  enc injection might be overridden but
         // XML injection cannot be overriden
         Class injectionType = InjectionUtil.injectionTarget(encName, ref, container, container.getEncInjections());
         if (container.getEncInjectors().containsKey(encName))
            return;
         container.getEncInjectors().put(encName, new PuEncInjector(encName, injectionType, ref.getUnitName(), "<persistence-unit-ref>"));
         try
         {
            addPUDependency(ref.getUnitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal <persistence-unit-ref> of " + ref.getRefName() + " :" + e.getMessage());
         }
      }
   }


   public void handleClassAnnotations(Class clazz, InjectionContainer container)
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

   private static void handleClassAnnotation(PersistenceUnit ref, InjectionContainer container, Class clazz)
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
      PersistenceUnitDeployment deployment = null;
      // look in EAR first
      deployment = container.getPersistenceUnitDeployment(unitName);
      if (deployment != null)
      {
         container.getDependencyPolicy().addDependency(deployment.getKernelName());
         log.debug("***** adding PU dependency from located persistence unit: " + deployment.getKernelName());
         return;
      }
      // probably not deployed yet.
      // todo not sure if we should do this in JBoss 5
      log.debug("******* could not find PU dependency so adding a default: " + PersistenceUnitDeployment.getDefaultKernelName(unitName));
      container.getDependencyPolicy().addDependency(PersistenceUnitDeployment.getDefaultKernelName(unitName));
   }

   public static ManagedEntityManagerFactory getManagedEntityManagerFactory(InjectionContainer container, String unitName)
           throws NameNotFoundException
   {
      ManagedEntityManagerFactory factory;
      PersistenceUnitDeployment deployment = container.getPersistenceUnitDeployment(unitName);
      if (deployment != null)
      {
         factory = deployment.getManagedFactory();
      }
      else
      {
         throw new NameNotFoundException("Unable to find persistence unit: " + unitName + " for deployment: " + container.getIdentifier());
      }
      return factory;
   }


   public static EntityManagerFactory getEntityManagerFactory(PersistenceUnit ref, InjectionContainer container) throws NameNotFoundException
   {
      return getEntityManagerFactory(ref.unitName(), container);
   }

   public static Object getFactory(Class type, String unitName, InjectionContainer container) throws NameNotFoundException
   {
      if (type != null && type.getName().equals(SessionFactory.class.getName()))
         return getSessionFactory(unitName, container);
      return getEntityManagerFactory(unitName, container);
   }

   public static EntityManagerFactory getEntityManagerFactory(String unitName, InjectionContainer container) throws NameNotFoundException
   {
      ManagedEntityManagerFactory managedFactory;
      PersistenceUnitDeployment deployment = container.getPersistenceUnitDeployment(unitName);
      if (deployment != null)
      {
         managedFactory = deployment.getManagedFactory();
      }
      else
      {
         return null;
      }
      return new InjectedEntityManagerFactory(managedFactory);
   }


   private static SessionFactory getSessionFactory(String ref, InjectionContainer container) throws NameNotFoundException
   {
      ManagedEntityManagerFactory managedFactory;
      PersistenceUnitDeployment deployment = container.getPersistenceUnitDeployment(ref);
      if (deployment != null)
      {
         managedFactory = deployment.getManagedFactory();
      }
      else
      {
         return null;
      }
      return new InjectedSessionFactory(managedFactory);
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
