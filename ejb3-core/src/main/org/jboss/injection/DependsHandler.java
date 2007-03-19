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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.util.Map;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.jboss.annotation.ejb.Depends;
import org.jboss.logging.Logger;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class DependsHandler implements InjectionHandler
{
   private static final Logger log = Logger.getLogger(DependsHandler.class);

   public void loadXml(EnvironmentRefGroup xml, InjectionContainer container)
   {
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      Depends dep = container.getAnnotation(Depends.class, method.getDeclaringClass(), method);
      if (dep != null)
      {
         if (!method.getName().startsWith("set"))
            throw new RuntimeException("@EJB can only be used with a set method: " + method);
         String[] names = dep.value();
         if (names.length != 1)
            throw new RuntimeException("@Depends on a field can only take one object name: " + method);
         ObjectName on = null;
         try
         {
            on = new ObjectName(names[0]);
         }
         catch (MalformedObjectNameException e)
         {
            throw new RuntimeException(e);
         }

         // don't replace other injections
         if (injectors.get(method) == null)
            injectors.put(method, new DependsMethodInjector(method, on));

         container.getDependencyPolicy().addDependency(names[0]);
      }
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      Depends dep = container.getAnnotation(Depends.class, field.getDeclaringClass(), field);
      if (dep != null)
      {
         String[] names = dep.value();
         if (names.length != 1)
            throw new RuntimeException("@Depends on a field can only take one object name: " + field);
         ObjectName on = null;
         try
         {
            on = new ObjectName(names[0]);
         }
         catch (MalformedObjectNameException e)
         {
            throw new RuntimeException(e);
         }

         // don't replace other injections
         if (injectors.get(field) == null)
            injectors.put(field, new DependsFieldInjector(field, on));

         container.getDependencyPolicy().addDependency(names[0]);
      }
   }

   public void handleClassAnnotations(Class clazz, InjectionContainer container)
   {
      Depends dep = (Depends)container.getAnnotation(Depends.class, clazz);
      if (dep == null) return;
      for (String dependency : dep.value())
      {
         container.getDependencyPolicy().addDependency(dependency);
      }
   }

}
