/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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

// $Id: WebServiceRefHandler.java 61084 2007-03-05 14:50:45Z thomas.diesler@jboss.com $

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;
import org.jboss.metadata.serviceref.ServiceRefDelegate;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;
import org.jboss.ws.integration.ServiceRefMetaData;

/**
 * Handle @WebServiceRef annotations
 * 
 * @author Thomas.Diesler@jboss.com
 */
public class WebServiceRefHandler implements InjectionHandler
{
   private static final Logger log = Logger.getLogger(WebServiceRefHandler.class);
   private Map<String, ServiceRefMetaData> srefMap = new HashMap<String, ServiceRefMetaData>();

   public void loadXml(EnvironmentRefGroup xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getServiceRefs() == null) return;
      for (ServiceRefMetaData sref : xml.getServiceRefs())
      {
         log.debug("service-ref: " + sref);
         if (srefMap.get(sref.getServiceRefName()) != null)
               throw new IllegalStateException ("Duplicate <service-ref-name> in " + sref);
         
         srefMap.put(sref.getServiceRefName(), sref);
      }
   }

   public void handleClassAnnotations(Class type, InjectionContainer container)
   {
      WebServiceRef wsref = container.getAnnotation(WebServiceRef.class, type);
      if (wsref != null)
      {
         bindRefOnType(type, container, wsref);
      }

      WebServiceRefs refs = container.getAnnotation(WebServiceRefs.class, type);
      if (refs != null)
      {
         for (WebServiceRef refItem : refs.value())
         {
            bindRefOnType(type, container, refItem);
         }
      }
   }

   private void bindRefOnType(Class type, InjectionContainer container, WebServiceRef wsref)
   {
      String name = wsref.name();
      if (name.equals(""))
         name = InjectionUtil.getEncName(type).substring(4);
      
      if (!container.getEncInjectors().containsKey(name))
      {
         String encName = "env/" + name;
         ServiceRefMetaData sref = getServiceRef(name);
         container.getEncInjectors().put(name, new ServiceRefInjector(encName, type, sref));
      }
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      WebServiceRef wsref = method.getAnnotation(WebServiceRef.class);
      if (wsref == null) return;

      if (!method.getName().startsWith("set"))
         throw new RuntimeException("@WebServiceRef can only be used with a set method: " + method);

      String name = wsref.name();
      if (name.equals(""))
         name = InjectionUtil.getEncName(method).substring(4);
      
      String encName = "env/" + name;
      Context encCtx = container.getEnc();
      if (!container.getEncInjectors().containsKey(name))
      {
         ServiceRefMetaData sref = getServiceRef(name);
         container.getEncInjectors().put(name, new ServiceRefInjector(encName, method, sref));
      }

      injectors.put(method, new JndiMethodInjector(method, encName, encCtx));
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      WebServiceRef wsref = field.getAnnotation(WebServiceRef.class);
      if (wsref == null) return;

      String name = wsref.name();
      if (name.equals(""))
         name = InjectionUtil.getEncName(field).substring(4);

      String encName = "env/" + name;
      Context encCtx = container.getEnc();
      if (!container.getEncInjectors().containsKey(name))
      {
         ServiceRefMetaData sref = getServiceRef(name);
         container.getEncInjectors().put(name, new ServiceRefInjector(encName, field, sref));
      }

      injectors.put(field, new JndiFieldInjector(field, encName, encCtx));
   }

   private ServiceRefMetaData getServiceRef(String name)
   {
      ServiceRefMetaData sref = srefMap.get(name);
      if (sref == null)
      {
         log.debug("No override for @WebServiceRef.name: " + name);
         sref = new ServiceRefDelegate().newServiceRefMetaData();
         sref.setServiceRefName(name);
      }
      return sref;
   }
}
