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

import org.jboss.annotation.JndiInject;
import org.jboss.logging.Logger;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;
import org.jboss.metamodel.descriptor.JndiRef;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Searches bean class for all @Inject and create Injectors
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class JndiInjectHandler implements InjectionHandler
{
   private static final Logger log = Logger.getLogger(JndiInjectHandler.class);
   
   public void loadXml(EnvironmentRefGroup xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getJndiRefs() == null) return;
      for (JndiRef ref : xml.getJndiRefs())
      {
         if (ref.getMappedName() == null || ref.getMappedName().equals(""))
            throw new RuntimeException("mapped-name is required for " + ref.getJndiRefName() + " of container " + container.getIdentifier());

         String encName = "env/" + ref.getJndiRefName();
         if (!container.getEncInjectors().containsKey(encName))
         {
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.getMappedName(), "jndi ref"));
         }
         InjectionUtil.injectionTarget(encName, ref, container, container.getEncInjections());
      }
   }

   public void handleClassAnnotations(Class clazz, InjectionContainer container)
   {
      // complete
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      JndiInject ref = method.getAnnotation(JndiInject.class);
      if (ref != null)
      {
         if (!method.getName().startsWith("set"))
            throw new RuntimeException("@EJB can only be used with a set method: " + method);
         String encName = InjectionUtil.getEncName(method);
         if (!container.getEncInjectors().containsKey(encName))
         {
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.jndiName(), "@JndiInject"));
         }
         injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
      }
   }
   
   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      JndiInject ref = field.getAnnotation(JndiInject.class);
      if (ref != null)
      {
         String encName = InjectionUtil.getEncName(field);
         if (!container.getEncInjectors().containsKey(encName))
         {
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.jndiName(), "@JndiInject"));
         }
         injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
      }
   }
}
