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
package org.jboss.injection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.ejb.EJB;

import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * Only does the injection side of an @EJB, not the enc setup.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 68286 $
 */
public class EJBInjectionHandler<X extends RemoteEnvironment> extends AbstractHandler<X>
{
   protected String getEncName(EJB ref, Field field)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(field);
      }
      else
      {
         encName = "env/" + encName;
      }
      return encName;
   }
   
   protected String getEncName(EJB ref, Method method)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(method);
      }
      else
      {
         encName = "env/" + encName;
      }
      return encName;
   }
   
   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      // do nothing, all class level @EJB are for seting up the enc
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      EJB ref = container.getAnnotation(EJB.class, field);
      if(ref == null)
         return;
      
      String encName = getEncName(ref, field);
      injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      EJB ref = container.getAnnotation(EJB.class, method);
      if(ref == null)
         return;
      
      String encName = getEncName(ref, method);
      injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
   }

   public void loadXml(X xml, InjectionContainer container)
   {
      // TODO: inventorize
      // Process injection targets?
   }
}
