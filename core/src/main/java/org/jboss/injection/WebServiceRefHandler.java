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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import javax.naming.Context;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.jboss.JBossServiceReferenceMetaData;
import org.jboss.metadata.javaee.spec.*;

/**
 * Handle @WebServiceRef annotations
 * 
 * @author Thomas.Diesler@jboss.com
 */
public class WebServiceRefHandler<X extends RemoteEnvironment> implements InjectionHandler<X>
{
   private static final Logger log = Logger.getLogger(WebServiceRefHandler.class);
   private Map<String, ServiceReferenceMetaData> srefMap = new HashMap<String, ServiceReferenceMetaData>();

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml == null) return;
      ServiceReferencesMetaData serviceRefs = xml.getServiceReferences();
      if (serviceRefs == null) return;
      for (ServiceReferenceMetaData sref : serviceRefs)
      {
         log.debug("service-ref: " + sref);
         if (srefMap.get(sref.getServiceRefName()) != null)
               throw new IllegalStateException ("Duplicate <service-ref-name> in " + sref);
         
         srefMap.put(sref.getServiceRefName(), sref);

         String encName = "env/" + sref.getServiceRefName();
         AnnotatedElement annotatedElement = sref.getAnnotatedElement();
         if(annotatedElement == null)
         {
            if(sref.getInjectionTargets() != null && sref.getInjectionTargets().size() > 0)
            {
               for(ResourceInjectionTargetMetaData trg : sref.getInjectionTargets())
               {
                  annotatedElement = InjectionUtil.findInjectionTarget(container.getClassloader(), trg);
                  addInjector(container, encName, annotatedElement);   
               }
            }
            else
               log.warn("No injection target for service-ref: " + sref.getServiceRefName());
         }
         // annotated classes do not specify injection target
         else if(!(annotatedElement instanceof java.lang.reflect.Type))
         {
            addInjector(container, encName, annotatedElement);   
         }         
      }
   }

   public void handleClassAnnotations(Class<?> type, InjectionContainer container)
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

   private void addInjector(InjectionContainer container, String encName, AnnotatedElement annotatedElement)
   {
      Injector jndiInjector;
      if(annotatedElement instanceof Method)
         jndiInjector = new JndiMethodInjector((Method)annotatedElement, encName, container.getEnc());
      else if(annotatedElement instanceof Field)
         jndiInjector = new JndiFieldInjector((Field)annotatedElement, encName, container.getEnc());
      else
         throw new IllegalStateException("Annotated element for '" + encName + "' is niether Method nor Field: " + annotatedElement);      
      container.getInjectors().add(jndiInjector);
   }

   private void bindRefOnType(Class<?> type, InjectionContainer container, WebServiceRef wsref)
   {
      String name = wsref.name();
      if (name.equals(""))
         name = InjectionUtil.getEncName(type).substring(4);
      
      if (!container.getEncInjectors().containsKey(name))
      {
         String encName = "env/" + name;
         ServiceReferenceMetaData sref = getServiceRef(name);
         container.getEncInjectors().put(name, new ServiceRefInjector(encName, type, sref));
      }
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      String serviceRefName = null;

      // injector first
      ServiceReferenceMetaData tmp = getServiceRefForInjectionTarget(method);
      if(tmp!=null)
      {
         serviceRefName = tmp.getServiceRefName();
      }
      else
      {
         // annotation second
         WebServiceRef wsref = method.getAnnotation(WebServiceRef.class);
         if(wsref!=null)
         {
            serviceRefName = wsref.name();

            if (serviceRefName.equals(""))
               serviceRefName = InjectionUtil.getEncName(method).substring(4);
         }
      }

      if(null==serviceRefName)
         return;

      if (!method.getName().startsWith("set"))
         throw new RuntimeException("@WebServiceRef can only be used with a set method: " + method);
      
      String encName = "env/" + serviceRefName;
      Context encCtx = container.getEnc();
      if (!container.getEncInjectors().containsKey(serviceRefName))
      {
         ServiceReferenceMetaData sref = getServiceRef(serviceRefName);
         container.getEncInjectors().put(serviceRefName, new ServiceRefInjector(encName, method, sref));
      }

      injectors.put(method, new JndiMethodInjector(method, encName, encCtx));
   }

   private ServiceReferenceMetaData getServiceRefForInjectionTarget(Method method)
   {
      ServiceReferenceMetaData match = null;

      Iterator<String> iterator = srefMap.keySet().iterator();
      while(iterator.hasNext())
      {
         ServiceReferenceMetaData sref = srefMap.get(iterator.next());
         if(sref.getInjectionTargets()!=null)
         {
            for(ResourceInjectionTargetMetaData injectionTuple : sref.getInjectionTargets())
            {
               if(method.getDeclaringClass().getName().equals(injectionTuple.getInjectionTargetClass())
                 && method.getName().equals(injectionTuple.getInjectionTargetName()))
               {
                  match = sref;
                  break;
               }
            }
         }
      }
      return match;
   }

   private ServiceReferenceMetaData getServiceRefForInjectionTarget(Field field)
   {
      ServiceReferenceMetaData match = null;

      Iterator<String> iterator = srefMap.keySet().iterator();
      while(iterator.hasNext())
      {
         ServiceReferenceMetaData sref = srefMap.get(iterator.next());
         if(sref.getInjectionTargets()!=null)
         {
            for(ResourceInjectionTargetMetaData injectionTuple : sref.getInjectionTargets())
            {
               if(field.getDeclaringClass().getName().equals(injectionTuple.getInjectionTargetClass())
                 && field.getName().equals(injectionTuple.getInjectionTargetName()))
               {
                  match = sref;
                  break;
               }
            }
         }
      }
      return match;
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      String serviceRefName = null;

      // injector first
      ServiceReferenceMetaData tmp = getServiceRefForInjectionTarget(field);
      if(tmp!=null)
      {
         serviceRefName = tmp.getServiceRefName();
      }
      else
      {
         // annotation second
         WebServiceRef wsref = field.getAnnotation(WebServiceRef.class);
         if(wsref!=null)
         {
            serviceRefName = wsref.name();

            if (serviceRefName.equals(""))
               serviceRefName = InjectionUtil.getEncName(field).substring(4);
         }
      }

      if(null==serviceRefName)
         return;

      String encName = "env/" + serviceRefName;
      Context encCtx = container.getEnc();
      if (!container.getEncInjectors().containsKey(serviceRefName))
      {
         ServiceReferenceMetaData sref = getServiceRef(serviceRefName);
         container.getEncInjectors().put(serviceRefName, new ServiceRefInjector(encName, field, sref));
      }

      injectors.put(field, new JndiFieldInjector(field, encName, encCtx));
   }

   private ServiceReferenceMetaData getServiceRef(String name)
   {
      ServiceReferenceMetaData sref = srefMap.get(name);
      if (sref == null)
      {
         log.debug("No override for @WebServiceRef.name: " + name);
         sref = new JBossServiceReferenceMetaData();
         sref.setServiceRefName(name);
      }
      return sref;
   }
}
