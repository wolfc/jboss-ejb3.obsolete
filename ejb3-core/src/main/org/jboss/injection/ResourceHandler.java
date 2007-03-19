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

import org.jboss.ejb3.Container;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.BeanPropertyFactory;
import org.jboss.injection.lang.reflect.FieldBeanProperty;
import org.jboss.injection.lang.reflect.MethodBeanProperty;
import org.jboss.logging.Logger;
import org.jboss.metamodel.descriptor.EnvEntry;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;
import org.jboss.metamodel.descriptor.MessageDestinationRef;
import org.jboss.metamodel.descriptor.ResourceEnvRef;
import org.jboss.metamodel.descriptor.ResourceRef;
import org.jboss.reflect.plugins.ValueConvertor;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;
import javax.xml.ws.WebServiceContext;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ResourceHandler implements InjectionHandler
{
   private static final Logger log = Logger.getLogger(ResourceHandler.class);

   private static void loadEnvEntry(InjectionContainer container, Collection<EnvEntry> envEntries)
   {
      for (EnvEntry envEntry : envEntries)
      {
         String encName = "env/" + envEntry.getEnvEntryName();
         InjectionUtil.injectionTarget(encName, envEntry, container, container.getEncInjections());
         if (container.getEncInjectors().containsKey(encName)) continue;
         log.trace("adding env-entry injector " + encName);
         container.getEncInjectors().put(encName, new EnvEntryEncInjector(encName, envEntry.getEnvEntryType(), envEntry.getEnvEntryValue()));
      }
   }

   private static void loadXmlResourceRefs(InjectionContainer container, Collection<ResourceRef> refs)
   {
      for (ResourceRef envRef : refs)
      {
         String encName = "env/" + envRef.getResRefName();
         if (container.getEncInjectors().containsKey(encName)) continue;
         if (envRef.getMappedName() == null || envRef.getMappedName().equals(""))
         {
            if (envRef.getResUrl() != null)
            {
               try
               {
                  container.getEncInjectors().put(encName, new ValueEncInjector(encName, new URL(envRef.getResUrl().trim()), "<resource-ref>"));
               }
               catch (MalformedURLException e)
               {
                  throw new RuntimeException(e);
               }
            }
            else
            {
               throw new RuntimeException("mapped-name is required for " + envRef.getResRefName() + " of deployment " + container.getIdentifier());
            }
         }
         else
         {
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, envRef.getMappedName(), "<resource-ref>"));
         }
         InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
      }
   }

   private static void loadXmlResourceEnvRefs(InjectionContainer container, Collection<ResourceEnvRef> refs)
   {
      for (ResourceEnvRef envRef : refs)
      {
         // EJBTHREE-712
         // TODO: refactor with handlePropertyAnnotation
         String resTypeName = envRef.getResType();
         try
         {
            if(resTypeName != null)
            {
               Class<?> resType = Class.forName(resTypeName);
               if(EJBContext.class.isAssignableFrom(resType))
               {
                  AccessibleObject ao = InjectionUtil.findInjectionTarget(container.getClassloader(), envRef.getInjectionTarget());
                  container.getInjectors().add(new EJBContextPropertyInjector(BeanPropertyFactory.create(ao)));
                  continue;
               }
            }
         }
         catch(ClassNotFoundException e)
         {
            throw new EJBException(e);
         }
         
         String encName = "env/" + envRef.getResRefName();
         if (container.getEncInjectors().containsKey(encName)) continue;
         if (envRef.getMappedName() == null || envRef.getMappedName().equals(""))
         {
            throw new RuntimeException("mapped-name is required for " + envRef.getResRefName() + " of deployment " + container.getIdentifier());
         }
         container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, envRef.getMappedName(), "<resource-ref>"));
         InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
      }
   }

   private static void loadXmlMessageDestinationRefs(InjectionContainer container, Collection<MessageDestinationRef> refs)
   {
      for (MessageDestinationRef envRef : refs)
      {
         String encName = "env/" + envRef.getMessageDestinationRefName();
         if (container.getEncInjectors().containsKey(encName)) continue;
         if (envRef.getMappedName() == null || envRef.getMappedName().equals(""))
         {
            // Look for a message-destination-link
            String link = envRef.getMessageDestinationLink();
            if( link != null )
            {
               // TODO: Resolve the link...
            }
            throw new RuntimeException("mapped-name is required for " + envRef.getMessageDestinationRefName() + " of deployment " + container.getIdentifier());
         }
         container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, envRef.getMappedName(), "<message-destination-ref>"));
         InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
      }
   }

   public void loadXml(EnvironmentRefGroup xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getMessageDestinationRefs() != null) loadXmlMessageDestinationRefs(container, xml.getMessageDestinationRefs());
      if (xml.getResourceEnvRefs() != null) loadXmlResourceEnvRefs(container, xml.getResourceEnvRefs());
      if (xml.getResourceRefs() != null) loadXmlResourceRefs(container, xml.getResourceRefs());
      if (xml.getEnvEntries() != null) loadEnvEntry(container, xml.getEnvEntries());
   }

   public void handleClassAnnotations(Class clazz, InjectionContainer container)
   {
      Resources resources = container.getAnnotation(Resources.class, clazz);
      if (resources != null)
      {
      for (Resource ref : resources.value())
      {
         handleClassAnnotation(ref, container, clazz);
      }
      }
      Resource res = container.getAnnotation(Resource.class, clazz);
      if (res != null) handleClassAnnotation(res, container, clazz);
   }

   private void handleClassAnnotation(Resource ref, InjectionContainer container, Class clazz)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         throw new RuntimeException("JBoss requires name() for class level @Resource");
      }
      encName = "env/" + ref.name();
      if (container.getEncInjectors().containsKey(encName)) return;

      String mappedName = ref.mappedName();
      if (mappedName == null || mappedName.equals(""))
      {
         throw new RuntimeException("You did not specify a @Resource.mappedName() for name: "
               +ref.name()+", class: " + clazz.getName() + " and there is no binding for that enc name in XML");
      }

      if (ref.type() == URL.class)
      {
         // Create a URL from the mappedName
         try
         {
            URL url = new URL(ref.mappedName().trim());
            container.getEncInjectors().put(encName, new ValueEncInjector(encName, url, "@Resource"));
         }
         catch (MalformedURLException e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.mappedName(), "@Resource"));
      }
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      Resource ref = method.getAnnotation(Resource.class);
      if (ref == null) return;

      log.trace("method " + method + " has @Resource");
      
      handlePropertyAnnotation(ref, new MethodBeanProperty(method), container, injectors);
      /*
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(method);
      }
      else
      {
         encName = "env/" + encName;
      }

      method.setAccessible(true);

      if (!method.getName().startsWith("set"))
         throw new RuntimeException("@Resource can only be used with a set method: " + method);
      if (method.getParameterTypes().length != 1)
         throw new RuntimeException("@Resource can only be used with a set method of one parameter: " + method);

      Class type = method.getParameterTypes()[0];
      if (!ref.type().equals(Object.class))
      {
         type = ref.type();
      }
      if (type.equals(UserTransaction.class))
      {
         injectors.put(method, new UserTransactionMethodInjector(method, container));
      }
      else if (type.equals(TimerService.class))
      {
         injectors.put(method, new TimerServiceMethodInjector(method, (Container) container)); // only EJBs
      }
      else if (EJBContext.class.isAssignableFrom(type))
      {
         injectors.put(method, new EJBContextMethodInjector(method));
      }
      else if (type.equals(WebServiceContext.class))
      {
         // FIXME: For now we skip it, and rely on the WS stack to perform the injection
      }
      else if (type.equals(String.class)
              || type.equals(Character.class)
              || type.equals(Byte.class)
              || type.equals(Short.class)
              || type.equals(Integer.class)
              || type.equals(Long.class)
              || type.equals(Boolean.class)
              || type.equals(Double.class)
              || type.equals(Float.class)
              || type.isPrimitive()
              )
      {

         // don't add an injector if no XML <env-entry is present as there will be no value to inject
         if (container.getEncInjectors().containsKey(encName))
         {
            injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
         }
         else if (ref.mappedName() != null && ref.mappedName().length() > 0)
         {
            // Use the mappedName as the string value
            String s = ref.mappedName().trim();
            try
            {
               Object value = ValueConvertor.convertValue(type, s);
               container.getEncInjectors().put(encName, new ValueEncInjector(encName, value, "@Resource"));
               injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
            }
            catch(Throwable t)
            {
               throw new RuntimeException("Failed to convert: "+ref.mappedName()+" to type:"+type, t);
            }
         }
      }
      else
      {
         if (!container.getEncInjectors().containsKey(encName))
         {
            String mappedName = ref.mappedName();
            if (mappedName == null || mappedName.equals(""))
            {
              throw new RuntimeException("You did not specify a @Resource.mappedName() on " + method + " and there is no binding for that enc name in XML");
            }
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.mappedName(), "@Resource"));
         }
         injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
      }
      */
   }
   
   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      Resource ref = field.getAnnotation(Resource.class);
      if (ref == null) return;

      log.trace("field " + field + " has @Resource");
      
      handlePropertyAnnotation(ref, new FieldBeanProperty(field), container, injectors);
      /*
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(field);
      }
      else
      {
         encName = "env/" + encName;
      }

      field.setAccessible(true);

      Class type = field.getType();
      if (!ref.type().equals(Object.class))
      {
         type = ref.type();
      }
      if (type.equals(UserTransaction.class))
      {
         injectors.put(field, new UserTransactionFieldInjector(field, container));
      }
      else if (type.equals(TimerService.class))
      {
         injectors.put(field, new TimerServiceFieldInjector(field, (Container) container)); // only EJBs
      }
      else if (EJBContext.class.isAssignableFrom(type))
      {
         injectors.put(field, new EJBContextFieldInjector(field));
      }
      else if (type.equals(WebServiceContext.class))
      {
         // FIXME: For now we skip it, and rely on the WS stack to perform the injection
      }
      else if (type.equals(String.class)
              || type.equals(Character.class)
              || type.equals(Byte.class)
              || type.equals(Short.class)
              || type.equals(Integer.class)
              || type.equals(Long.class)
              || type.equals(Boolean.class)
              || type.equals(Double.class)
              || type.equals(Float.class)
              || type.isPrimitive()
              )
      {
         // don't add an injector if no XML <env-entry is present as there will be no value to inject
         if (container.getEncInjectors().containsKey(encName))
         {
            injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
         }
         else if (ref.mappedName() != null && ref.mappedName().length() > 0)
         {
            // Use the mappedName as the string value
            String s = ref.mappedName().trim();
            try
            {
               Object value = ValueConvertor.convertValue(type, s);
               container.getEncInjectors().put(encName, new ValueEncInjector(encName, value, "@Resource"));
               injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
            }
            catch(Throwable t)
            {
               throw new RuntimeException("Failed to convert: "+ref.mappedName()+" to type:"+type, t);
            }
         }
         else
         {
            log.warn("Not injecting " + field.getName() + ", no matching enc injector " + encName + " found");
         }
      }
      else
      {
         if (!container.getEncInjectors().containsKey(encName))
         {
            String mappedName = ref.mappedName();
            if (mappedName == null || mappedName.equals(""))
            {
              throw new RuntimeException("You did not specify a @Resource.mappedName() on " + field + " and there is no binding for enc name " + encName + " in XML");
            }
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.mappedName(), "@Resource"));
         }
         injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
      }
      */
   }

   private void handlePropertyAnnotation(Resource ref, BeanProperty property, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      assert ref != null;
      assert property != null;
      assert container != null;
      assert injectors != null;
      
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         //encName = InjectionUtil.getEncName(field);
         encName = property.getDeclaringClass().getName() + "/" + property.getName();
      }
      if (!encName.startsWith("env/"))
      {
         encName = "env/" + encName;
      }

      AccessibleObject accObj = property.getAccessibleObject();
      
      Class<?> type = property.getType();
      if (!ref.type().equals(Object.class))
      {
         type = ref.type();
      }
      if (type.equals(UserTransaction.class))
      {
         injectors.put(accObj, new UserTransactionPropertyInjector(property, container));
      }
      else if (type.equals(TimerService.class))
      {
         injectors.put(accObj, new TimerServicePropertyInjector(property, (Container) container)); // only EJBs
      }
      else if (EJBContext.class.isAssignableFrom(type))
      {
         injectors.put(accObj, new EJBContextPropertyInjector(property));
      }
      else if (type.equals(WebServiceContext.class))
      {
         injectors.put(accObj, new WebServiceContextPropertyInjector(property));
      }
      else if (type.equals(String.class)
              || type.equals(Character.class)
              || type.equals(Byte.class)
              || type.equals(Short.class)
              || type.equals(Integer.class)
              || type.equals(Long.class)
              || type.equals(Boolean.class)
              || type.equals(Double.class)
              || type.equals(Float.class)
              || type.isPrimitive()
              )
      {
         // don't add an injector if no XML <env-entry is present as there will be no value to inject
         if (container.getEncInjectors().containsKey(encName))
         {
            injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
         }
         else if (ref.mappedName() != null && ref.mappedName().length() > 0)
         {
            // Use the mappedName as the string value
            String s = ref.mappedName().trim();
            try
            {
               Object value = ValueConvertor.convertValue(type, s);
               container.getEncInjectors().put(encName, new ValueEncInjector(encName, value, "@Resource"));
               injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
            }
            catch(Throwable t)
            {
               throw new RuntimeException("Failed to convert: "+ref.mappedName()+" to type:"+type, t);
            }
         }
         else
         {
            log.warn("Not injecting " + property.getName() + ", no matching enc injector " + encName + " found");
         }
      }
      else
      {
         if (!container.getEncInjectors().containsKey(encName))
         {
            String mappedName = ref.mappedName();
            if (mappedName == null || mappedName.equals(""))
            {
               // TODO: is this a nice trick?
//               if(ConnectionFactory.class.isAssignableFrom(type))
//               {
//                  // neat little trick
//                  mappedName = "java:/ConnectionFactory";
//               }
//               else
                  throw new RuntimeException("You did not specify a @Resource.mappedName() on " + accObj + " and there is no binding for enc name " + encName + " in XML");
            }
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.mappedName(), "@Resource"));
         }
         injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
      }      
   }
}
