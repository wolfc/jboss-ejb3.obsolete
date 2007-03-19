/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.clientmodule;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Module;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.deployers.JBoss5DeploymentScope;
import org.jboss.ejb3.deployers.JBoss5DeploymentUnit;
import org.jboss.ejb3.enc.DeploymentEjbResolver;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.ejb3.metamodel.ApplicationClientDD;
import org.jboss.injection.DependsHandler;
import org.jboss.injection.EJBHandler;
import org.jboss.injection.EncInjector;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.JndiInjectHandler;
import org.jboss.injection.PersistenceContextHandler;
import org.jboss.injection.PersistenceUnitHandler;
import org.jboss.injection.ResourceHandler;
import org.jboss.injection.WebServiceRefHandler;
import org.jboss.logging.Logger;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;
import org.jboss.virtual.VirtualFile;

/**
 * This class builds up the java:comp namespace for JavaEE 5 application clients.
 * It uses the existing injection framework to get this done.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class ClientENCInjectionContainer implements InjectionContainer
{
   private static final Logger log = Logger.getLogger(ClientENCInjectionContainer.class);
   
   private DeploymentUnit ejb3Unit;
   private ApplicationClientDD xml;
   private Class<?> mainClass;
   private String applicationClientName;
   private ClassLoader classLoader;
   
   // TODO: remove injectors, these are not supported
   private List<Injector> injectors = new ArrayList<Injector>();
   private Map<String, Map<AccessibleObject, Injector>> encInjections = new HashMap<String, Map<AccessibleObject, Injector>>();
   private Map<String, EncInjector> encInjectors = new HashMap<String, EncInjector>();
   
   private Context enc;
   
   private DeploymentEjbResolver ejbResolver;
   private ObjectName objectName;
   private DependencyPolicy dependencyPolicy = new JBoss5DependencyPolicy();

   public ClientENCInjectionContainer(org.jboss.deployers.spi.deployer.DeploymentUnit unit, ApplicationClientDD xml, Class<?> mainClass, String applicationClientName, ClassLoader classLoader, Context encCtx) throws NamingException
   {
      if(mainClass == null)
         throw new NullPointerException("mainClass is mandatory");
      if(applicationClientName == null)
         throw new NullPointerException("applicationClientName is mandatory");
      if(classLoader == null)
         throw new NullPointerException("classLoader is mandatory");
      
      this.ejb3Unit = new JBoss5DeploymentUnit(unit);
      this.xml = xml;
      this.mainClass = mainClass;
      this.applicationClientName = applicationClientName;
      this.classLoader = classLoader;
      
      this.enc = encCtx;
      
      /*
      EAR ear = null;

      if (di.parent != null)
      {
         if (di.parent.shortName.endsWith(".ear") || di.parent.shortName.endsWith(".ear/"))
         {
            synchronized (di.parent.context)
            {
               ear = (EAR) di.parent.context.get("EJB3_EAR_METADATA");
               if (ear == null)
               {
                  ear = new JmxEARImpl(di.parent.shortName);
                  di.parent.context.put("EJB3_EAR_METADATA", ear);
               }
            }
         }
      }
      */
      
      DeploymentScope scope = null;
      if (unit.getDeploymentContext().getParent() != null)
      {
         scope = new JBoss5DeploymentScope(unit.getDeploymentContext().getParent());
      }
      
      ejbResolver = new ClientEjbResolver(scope, unit.getDeploymentContext().getRoot().getName());
      
      String on = Ejb3Module.BASE_EJB3_JMX_NAME + createScopeKernelName(unit, scope) + ",name=" + applicationClientName;
      try
      {
         this.objectName = new ObjectName(on);
      }
      catch(MalformedObjectNameException e)
      {
         // should not happen
         throw new RuntimeException(e);
      }
      
      processMetaData();
   }
   
   private String createScopeKernelName(org.jboss.deployers.spi.deployer.DeploymentUnit unit, DeploymentScope ear)
   {
      String scopedKernelName = "";
      if (ear != null) scopedKernelName += ",ear=" + ear.getShortName();
      scopedKernelName += ",jar=" + unit.getDeploymentContext().getRoot().getName();
      return scopedKernelName;
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz)
   {
      return clazz.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Method method)
   {
      return method.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Method method)
   {
      return method.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Field field)
   {
      return field.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Field field)
   {
      return field.getAnnotation(annotationType);
   }

   public VirtualFile getRootFile()
   {
      return ejb3Unit.getRootFile();
   }

   public ClassLoader getClassloader()
   {
      return classLoader;
   }

   public DependencyPolicy getDependencyPolicy()
   {
      return dependencyPolicy;
   }

   public String getDeploymentDescriptorType()
   {
      return "application-client.xml";
   }

   public String getEjbJndiName(Class businessInterface) throws NameNotFoundException
   {
      return ejbResolver.getEjbJndiName(businessInterface);
   }

   public String getEjbJndiName(String link, Class businessInterface)
   {
      return ejbResolver.getEjbJndiName(link, businessInterface);
   }

   public Context getEnc()
   {
      return enc;
   }

   public Map<String, Map<AccessibleObject, Injector>> getEncInjections()
   {
      return encInjections;
   }

   public Map<String, EncInjector> getEncInjectors()
   {
      return encInjectors;
   }

   public EnvironmentRefGroup getEnvironmentRefGroup()
   {
      return xml;
   }

   public String getIdentifier()
   {
      return applicationClientName;
   }

   /**
    * A client enc injection container doesn't support injectors, because
    * these must be run client side.
    */
   public List<Injector> getInjectors()
   {
      //throw new RuntimeException("not supported");
      return injectors;
   }

   public Class<?> getMainClass()
   {
      return mainClass;
   }
   
   public ObjectName getObjectName()
   {
      return objectName;
   }
   
   public PersistenceUnitDeployment getPersistenceUnitDeployment(String unitName) throws NameNotFoundException
   {
      throw new RuntimeException("NYI");
   }

   private void populateEnc()
   {
      for (EncInjector injector : encInjectors.values())
      {
         log.trace("encInjector: " + injector);
         injector.inject(this);
      }
   }
   
   private void processMetaData()
   {
      for(String dependency : xml.getDependencies())
      {
         getDependencyPolicy().addDependency(dependency);
      }
      
      // TODO: check which handlers an application client should support
      Collection<InjectionHandler> handlers = new ArrayList<InjectionHandler>();
      handlers.add(new EJBHandler());
      handlers.add(new DependsHandler());
      handlers.add(new JndiInjectHandler());
      handlers.add(new PersistenceContextHandler());
      handlers.add(new PersistenceUnitHandler());
      handlers.add(new ResourceHandler());
      handlers.add(new WebServiceRefHandler());
      
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try
      {
         // EJB container's XML must be processed before interceptor's as it may override interceptor's references
         for (InjectionHandler handler : handlers) handler.loadXml(xml, this);

         Map<AccessibleObject, Injector> tmp = InjectionUtil.processAnnotations(this, handlers, getMainClass());
         injectors.addAll(tmp.values());

//         initialiseInterceptors();
//         for (InterceptorInfo interceptorInfo : applicableInterceptors)
//         {
//            for (InjectionHandler handler : handlers)
//            {
//               handler.loadXml(interceptorInfo.getXml(), this);
//            }
//         }
//         for (InterceptorInfo interceptorInfo : applicableInterceptors)
//         {
//            Map<AccessibleObject, Injector> tmpInterceptor = InjectionUtil.processAnnotations(this, handlers, interceptorInfo.getClazz());
//            InterceptorInjector injector = new InterceptorInjector(this, interceptorInfo, tmpInterceptor);
//            interceptorInjectors.put(interceptorInfo.getClazz(), injector);
//         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }
   
   public Container resolveEjbContainer(String link, Class businessIntf)
   {
      return ejbResolver.getEjbContainer(link, businessIntf);
   }

   public Container resolveEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      return ejbResolver.getEjbContainer(businessIntf);
   }

   public void start()
   {
      log.trace("start");
      
      populateEnc();
      
      // Don't run any injectors, they must be run client side
   }
   
   public void stop()
   {
      log.trace("stop");
   }
}
