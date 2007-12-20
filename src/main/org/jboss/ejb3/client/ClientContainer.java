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
package org.jboss.ejb3.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.injection.DependsHandler;
import org.jboss.injection.EJBInjectionHandler;
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
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.client.spec.ApplicationClientMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.util.NotImplementedException;
import org.jboss.virtual.VirtualFile;

/**
 * Injection of the application client main class is handled from here.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 67021 $
 */
public class ClientContainer implements InjectionContainer
{
   private static final Logger log = Logger.getLogger(ClientContainer.class);
   
   private Class<?> mainClass;
   private JBossClientMetaData xml;
   private String applicationClientName;
   
   // for performance there is an array.
   private List<Injector> injectors = new ArrayList<Injector>();
   private Map<String, Map<AccessibleObject, Injector>> encInjections = new HashMap<String, Map<AccessibleObject, Injector>>();
   private Map<String, EncInjector> encInjectors = new HashMap<String, EncInjector>();
   
   private Context enc;
   private Context encEnv;
   
   private List<Method> postConstructs = new ArrayList<Method>();

   public ClientContainer(JBossClientMetaData xml, Class<?> mainClass, String applicationClientName) throws Exception
   {
      this.xml = xml;
      this.mainClass = mainClass;
      this.applicationClientName = applicationClientName;
      
      //Context ctx = getInitialContext();
      Context ctx = InitialContextFactory.getInitialContext();
      enc = (Context) ctx.lookup(applicationClientName);
      log.debug("Client ENC("+applicationClientName+"):");
      NamingEnumeration<NameClassPair> e = enc.list("");
      while(e.hasMore())
      {
         NameClassPair ncp = e.next();
         log.debug("  " + ncp);
      }
      //encEnv = (Context) enc.lookup("env");
//      enc = ThreadLocalENCFactory.create(ctx);
//      encEnv = Util.createSubcontext(enc, "env");
      
      processMetadata(null);
      
      for (EncInjector injector : encInjectors.values())
      {
         log.trace("encInjector: " + injector);
//         injector.inject(this);
      }
      
      for(Injector injector : injectors)
      {
         log.trace("injector: " + injector);
         injector.inject((Object) null);
      }
      
      postConstruct();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.Class)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Class<?> clazz)
   {
      return clazz.getAnnotation(annotationClass);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.Class, java.lang.reflect.Method)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Class<?> clazz, Method method)
   {
      return method.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.reflect.Method)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Method method)
   {
      return method.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.Class, java.lang.reflect.Field)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Class<?> clazz, Field field)
   {
      return field.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.reflect.Field)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Field field)
   {
      return field.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getClassloader()
    */
   public ClassLoader getClassloader()
   {
      //throw new RuntimeException("NYI");
      return Thread.currentThread().getContextClassLoader();
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getDependencyPolicy()
    */
   public DependencyPolicy getDependencyPolicy()
   {
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getDeploymentDescriptorType()
    */
   public String getDeploymentDescriptorType()
   {
      return "application-client.xml";
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEjbJndiName(java.lang.Class)
    */
   public String getEjbJndiName(Class businessInterface) throws NameNotFoundException
   {
      throw new RuntimeException("NYI");
      //return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEjbJndiName(java.lang.String, java.lang.Class)
    */
   public String getEjbJndiName(String link, Class<?> businessInterface)
   {
      throw new NotImplementedException();
      //return "java:comp/env/" + link + "/remote";
      //return applicationClientName + "/" + link + "/remote";
      //return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEnc()
    */
   public Context getEnc()
   {
      return enc;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEncInjections()
    */
   public Map<String, Map<AccessibleObject, Injector>> getEncInjections()
   {
      return encInjections;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEncInjectors()
    */
   public Map<String, EncInjector> getEncInjectors()
   {
      return encInjectors;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEnvironmentRefGroup()
    */
   public RemoteEnvironment getEnvironmentRefGroup()
   {
      return xml;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getIdentifier()
    */
   public String getIdentifier()
   {
//      throw new NotImplementedException;
      // FIXME: return the real identifier
      //return "client-identifier";
      return applicationClientName;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getInjectors()
    */
   public List<Injector> getInjectors()
   {
      throw new NotImplementedException();
   }

   public Class<?> getMainClass()
   {
      return mainClass;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getPersistenceUnitDeployment(java.lang.String)
    */
   public PersistenceUnitDeployment getPersistenceUnitDeployment(String unitName) throws NameNotFoundException
   {
      throw new NotImplementedException();
   }

   public boolean hasJNDIBinding(String jndiName)
   {
      return false;
   }
   
   public void invokeMain(String args[]) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> parameterTypes[] = { args.getClass() };
      Method method = mainClass.getDeclaredMethod("main", parameterTypes);
      method.invoke(null, (Object) args);
   }
   
   /**
    * Call post construct methods.
    * @throws IllegalAccessException  
    * @throws InstantiationException 
    * @throws InvocationTargetException 
    * @throws IllegalArgumentException 
    *
    */
   private void postConstruct() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
      log.info("postConstructs = " + postConstructs);
      for(Method method : postConstructs)
      {
         method.setAccessible(true);
         Object instance;
         if(Modifier.isStatic(method.getModifiers()))
            instance = null;
         else
            instance = method.getDeclaringClass().newInstance();
         Object args[] = null;
         method.invoke(instance, args);
      }
   }
   
   private void processMetadata(DependencyPolicy dependencyPolicy) throws Exception
   {
      log.debug("processMetadata");
      processPostConstructs();
      
      // TODO: check which handlers a client container should support
      Collection<InjectionHandler<RemoteEnvironment>> handlers = new ArrayList<InjectionHandler<RemoteEnvironment>>();
      handlers.add(new EJBInjectionHandler<RemoteEnvironment>());
      //handlers.add(new ClientEJBHandler());
      handlers.add(new DependsHandler<RemoteEnvironment>());
      handlers.add(new JndiInjectHandler<RemoteEnvironment>());
      handlers.add(new PersistenceUnitHandler<RemoteEnvironment>());
      // since enc injectors live on the server side, we don't want to check those
      handlers.add(new ResourceHandler<RemoteEnvironment>(false));
      handlers.add(new WebServiceRefHandler<RemoteEnvironment>());
      
      // TODO: we're going to use a jar class loader
//      ClassLoader old = Thread.currentThread().getContextClassLoader();
//      Thread.currentThread().setContextClassLoader(classloader);
      try
      {
         // EJB container's XML must be processed before interceptor's as it may override interceptor's references
         for (InjectionHandler<RemoteEnvironment> handler : handlers) handler.loadXml(xml, this);

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
//         Thread.currentThread().setContextClassLoader(old);
      }
   }
   
   /**
    * Create dummy dd for PostConstruct annotations.
    * @throws ClassNotFoundException 
    * @throws NoSuchMethodException 
    * @throws SecurityException 
    *
    */
   private void processPostConstructs() throws ClassNotFoundException, SecurityException, NoSuchMethodException
   {
      processPostConstructs(mainClass);
      
      LifecycleCallbacksMetaData callbacks = xml.getPostConstructs();
      if(callbacks != null)
      {
         for(LifecycleCallbackMetaData callback : callbacks)
         {
            String className = callback.getClassName();
            String methodName = callback.getMethodName();
            Class<?> lifecycleClass;
            if(className == null)
               lifecycleClass = mainClass;
            else
               lifecycleClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            Class<?> parameterTypes[] = new Class[0];
            Method method = lifecycleClass.getDeclaredMethod(methodName, parameterTypes);
            postConstructs.add(method);
         }
      }
   }
   
   private void processPostConstructs(Class<?> cls)
   {
      if(cls == null)
         return;
      
      for(Method method : cls.getDeclaredMethods())
      {
         PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
         if(postConstruct != null)
         {
            // TODO: sure?
            // http://java.sun.com/javase/6/docs/api/javax/annotation/PostConstruct.html
            if(postConstructs.size() > 0)
               throw new IllegalStateException("only one @PostConstruct allowed");
            postConstructs.add(method);
         }
      }
      
      processPostConstructs(cls.getSuperclass());
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#resolveEjbContainer(java.lang.String, java.lang.Class)
    */
   public Container resolveEjbContainer(String link, Class businessIntf)
   {
      log.warn("resolveEjbContainer(" + link + ", " + businessIntf + ") not implemented");
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#resolveEjbContainer(java.lang.Class)
    */
   public Container resolveEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      return null;
   }

   public String resolveMessageDestination(String link)
   {
      // Resolving something here is a nop
      return null;
   }
   
   public VirtualFile getRootFile()
   {
      throw new NotImplementedException();
   }
}
