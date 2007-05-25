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
package org.jboss.ejb3;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Timeout;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jboss.annotation.ejb.PoolClass;
import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassContainer;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.AspectDefinition;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.Scope;
import org.jboss.aop.annotation.AnnotationElement;
import org.jboss.aop.joinpoint.ConstructorInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.ejb3.interceptor.InterceptorInfo;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.interceptor.InterceptorInjector;
import org.jboss.ejb3.interceptor.LifecycleInterceptorHandler;
import org.jboss.ejb3.metamodel.AssemblyDescriptor;
import org.jboss.ejb3.metamodel.EnterpriseBean;
import org.jboss.ejb3.security.JaccHelper;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.ejb3.tx.UserTransactionImpl;
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
import org.jboss.naming.Util;
import org.jboss.virtual.VirtualFile;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class EJBContainer extends ClassContainer implements Container, InjectionContainer
{

   private static final Logger log = Logger.getLogger(EJBContainer.class);

   protected EjbEncFactory encFactory = new DefaultEjbEncFactory();

   protected Pool pool;

   protected String ejbName;

   protected ObjectName objectName;

   protected int defaultConstructorIndex;

   protected String beanClassName;

   protected ClassLoader classloader;

   // for performance there is an array.
   protected List<Injector> injectors = new ArrayList<Injector>();

   protected Context enc;

   protected Class beanContextClass;

   //protected SessionCallbackHandler callbackHandler;
   protected LifecycleInterceptorHandler callbackHandler;

   protected Hashtable initialContextProperties;

   protected Map<String, EncInjector> encInjectors = new HashMap<String, EncInjector>();

   protected EnterpriseBean xml;
   protected AssemblyDescriptor assembly;

   protected Map<String, Map<AccessibleObject, Injector>> encInjections = new HashMap<String, Map<AccessibleObject, Injector>>();

   protected InterceptorInfoRepository interceptorRepository;

   protected List<InterceptorInfo> classInterceptors = new ArrayList<InterceptorInfo>();

   protected LinkedHashSet<InterceptorInfo> applicableInterceptors;

   private HashMap<Class, InterceptorInjector> interceptorInjectors = new HashMap<Class, InterceptorInjector>();

   private Ejb3Deployment deployment;

   private DependencyPolicy dependencyPolicy;

   private String jaccContextId;

   protected HashMap invokedMethod = new HashMap();

   protected InvocationStatistics invokeStats = new InvocationStatistics();
   
   /**
    * @param name                  Advisor name
    * @param manager               Domain to get interceptor bindings from
    * @param cl                    the EJB's classloader
    * @param beanClassName
    * @param ejbName
    * @param ctxProperties
    * @param interceptorRepository
    * @param deployment
    */

   public EJBContainer(String name, AspectManager manager, ClassLoader cl,
                       String beanClassName, String ejbName, Hashtable ctxProperties,
                       InterceptorInfoRepository interceptorRepository, Ejb3Deployment deployment)
   {
      super(name, manager);
      this.deployment = deployment;
      this.beanClassName = beanClassName;
      this.classloader = cl;
         
      super.setChainOverridingForInheritedMethods( true );
      
      try
      {
         clazz = classloader.loadClass(beanClassName);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
      this.ejbName = ejbName;
      String on = Ejb3Module.BASE_EJB3_JMX_NAME + deployment.getScopeKernelName() + ",name=" + ejbName;
      try
      {
         objectName = new ObjectName(on);
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException("failed to create object name for: " + on, e);
      }
      initialContextProperties = ctxProperties;
      try
      {
         Util.createSubcontext(getEnc(), "env");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      this.interceptorRepository = interceptorRepository;
      this.interceptorRepository.addBeanClass(clazz.getName());
   }


   public EjbEncFactory getEncFactory()
   {
      return encFactory;
   }

   public void setEncFactory(EjbEncFactory encFactory)
   {
      this.encFactory = encFactory;
   }

   /**
    * Makes sure that EJB's ENC is available
    * Delegates to whatever implementation is used to push the ENC of the EJB
    * onto the stack
    *
    */
   public void pushEnc()
   {
      encFactory.pushEnc(this);
   }


   /**
    * Pops EJB's ENC from the stack.  Delegates to whatever implementation
    * is used to pop the EJB's ENC from the stock
    *
    */
   public void popEnc()
   {
      encFactory.popEnc(this);
   }
   
   public EnvironmentRefGroup getEnvironmentRefGroup()
   {
      return xml;
   }

   public List<Injector> getInjectors()
   {
      return injectors;
   }


   public String getJaccContextId()
   {
      return jaccContextId;
   }

   public void setJaccContextId(String jaccContextId)
   {
      this.jaccContextId = jaccContextId;
   }
   
   public VirtualFile getRootFile()
   {
      return getDeploymentUnit().getRootFile();
   }
   
   public DeploymentUnit getDeploymentUnit()
   {
      return deployment.getDeploymentUnit();
   }

   public Ejb3Deployment getDeployment()
   {
      return deployment;
   }

   public DependencyPolicy getDependencyPolicy()
   {
      return dependencyPolicy;
   }

   /**
    * EJBContainer has finished with all metadata initialization from XML files and such.
    * this is really a hook to do some processing after XML has been set up and before
    * and processing of dependencies and such.
    */
   public void instantiated()
   {

   }

   /**
    * introspects EJB container to find all dependencies
    * and initialize any extra metadata.
    * <p/>
    * This must be called before container is registered with any microcontainer
    *
    * @param dependencyPolicy
    */
   public void processMetadata(DependencyPolicy dependencyPolicy)
   {
      this.dependencyPolicy = dependencyPolicy;
      // XML must be done first so that any annotation overrides are initialized

      // todo injection handlers should be pluggable from XML
      Collection<InjectionHandler> handlers = new ArrayList<InjectionHandler>();
      handlers.add(new EJBHandler());
      handlers.add(new DependsHandler());
      handlers.add(new JndiInjectHandler());
      handlers.add(new PersistenceContextHandler());
      handlers.add(new PersistenceUnitHandler());
      handlers.add(new ResourceHandler());
      handlers.add(new WebServiceRefHandler());

      ClassLoader old = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classloader);
      try
      {
         // EJB container's XML must be processed before interceptor's as it may override interceptor's references
         for (InjectionHandler handler : handlers) handler.loadXml(xml, this);

         Map<AccessibleObject, Injector> tmp = InjectionUtil.processAnnotations(this, handlers, getBeanClass());
         injectors.addAll(tmp.values());

         initialiseInterceptors();
         for (InterceptorInfo interceptorInfo : applicableInterceptors)
         {
            for (InjectionHandler handler : handlers)
            {
               handler.loadXml(interceptorInfo.getXml(), this);
            }
         }
         for (InterceptorInfo interceptorInfo : applicableInterceptors)
         {
            Map<AccessibleObject, Injector> tmpInterceptor = InjectionUtil.processAnnotations(this, handlers, interceptorInfo.getClazz());
            InterceptorInjector injector = new InterceptorInjector(this, interceptorInfo, tmpInterceptor);
            interceptorInjectors.put(interceptorInfo.getClazz(), injector);
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }

   public EnterpriseBean getXml()
   {
      return xml;
   }

   public void setXml(EnterpriseBean xml)
   {
      this.xml = xml;
   }

   public AssemblyDescriptor getAssemblyDescriptor()
   {
      return assembly;
   }

   public void setAssemblyDescriptor(AssemblyDescriptor assembly)
   {
      this.assembly = assembly;
   }

   public InterceptorInfoRepository getInterceptorRepository()
   {
      return interceptorRepository;
   }

   public List<InterceptorInfo> getClassInterceptors()
   {
      initialiseInterceptors();
      return classInterceptors;
   }

   public HashSet<InterceptorInfo> getApplicableInterceptors()
   {
      initialiseInterceptors();
      return applicableInterceptors;
   }

   public HashMap<Class, InterceptorInjector> getInterceptorInjectors()
   {
      initialiseInterceptors();
      return interceptorInjectors;
   }


   public Map<String, EncInjector> getEncInjectors()
   {
      return encInjectors;
   }

   public ClassLoader getClassloader()
   {
      return classloader;
   }

   public InitialContext getInitialContext()
   {
      try
      {
         if (initialContextProperties == null)
            return new InitialContext();
         else
            return new InitialContext(initialContextProperties);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Map<String, Map<AccessibleObject, Injector>> getEncInjections()
   {
      return encInjections;
   }

   public Context getEnc()
   {
      if (enc == null)
      {
         enc = encFactory.getEnc(this);
      }
      return enc;
   }

   public Hashtable getInitialContextProperties()
   {
      return initialContextProperties;
   }

   public ObjectName getObjectName()
   {
      return objectName;
   }

   public String getEjbName()
   {
      return ejbName;
   }

   public String getBeanClassName()
   {
      return beanClassName;
   }

   public Class getBeanClass()
   {
      return clazz;
   }

   public Pool getPool()
   {
      return pool;
   }

   public Object construct()
   {
      Interceptor[] cInterceptors = constructorInterceptors[defaultConstructorIndex];
      if (cInterceptors == null)
      {
         try
         {
            return constructors[defaultConstructorIndex].newInstance();
         }
         catch (InstantiationException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException(e);
         }
         catch (InvocationTargetException e)
         {
            throw new RuntimeException(e);
         }
      }
      ConstructorInvocation invocation = new ConstructorInvocation(
              cInterceptors);

      invocation.setAdvisor(this);
      invocation.setConstructor(constructors[defaultConstructorIndex]);
      try
      {
         return invocation.invokeNext();
      }
      catch (Throwable throwable)
      {
         throw new RuntimeException(throwable);
      }

   }

   public void create() throws Exception
   {
      // EJBTHREE-655: we need an instance after create
      initializeClassContainer();
      for (int i = 0; i < constructors.length; i++)
      {
         if (constructors[i].getParameterTypes().length == 0)
         {
            defaultConstructorIndex = i;
            break;
         }
      }
   }

   // Everything must be done in start to make sure all dependencies have been satisfied
   public void start() throws Exception
   {
      initializePool();

      for (EncInjector injector : encInjectors.values())
      {
         injector.inject(this);   
      }

      // creating of injector array should come after injection into ENC as an ENC injector
      // may add additional injectors into the injector list.  An example is an extended persistence
      // context which mush be created and added to the SFSB bean context.

      Injector[] injectors2 = injectors.toArray(new Injector[injectors.size()]);
      if (pool != null) pool.setInjectors(injectors2);

      createCallbackHandler();

      JaccHelper.configureContainer(jaccContextId, this);
      log.info("STARTED EJB: " + clazz.getName() + " ejbName: " + ejbName);
   }

   public void stop() throws Exception
   {
      encFactory.cleanupEnc(this);
      
      if (pool != null)
      {
         pool.destroy();
         pool = null;
      }
      
      log.info("STOPPED EJB: " + clazz.getName() + " ejbName: " + ejbName);
   }

   public void destroy() throws Exception
   {
      super.cleanup();
   }

   protected void initializePool() throws Exception
   {
      PoolClass poolClass = (PoolClass) resolveAnnotation(PoolClass.class);
      Class<? extends Pool> poolClazz = poolClass.value();
      int maxSize = poolClass.maxSize();
      long timeout = poolClass.timeout();
      pool = poolClazz.newInstance();
      pool.initialize(this, beanContextClass, clazz, maxSize, timeout);

      resolveInjectors();
      pool.setInjectors(injectors.toArray(new Injector[injectors.size()]));
   }

   public void invokePostConstruct(BeanContext beanContext, Object[] params)
   {
      callbackHandler.postConstruct(beanContext, params);
   }

   public void invokePreDestroy(BeanContext beanContext)
   {
      callbackHandler.preDestroy(beanContext);
   }

   public void invokePostActivate(BeanContext beanContext)
   {
      throw new RuntimeException("PostActivate not implemented for container");
   }

   public void invokePrePassivate(BeanContext beanContext)
   {
      throw new RuntimeException("PrePassivate not implemented for container");
   }

   public void invokeInit(Object bean)
   {

   }

   public void invokeInit(Object bean, Class[] initParameterTypes,
                          Object[] initParameterValues)
   {

   }

   public static final String MANAGED_ENTITY_MANAGER_FACTORY = "ManagedEntityManagerFactory";

   public static final String ENTITY_MANAGER_FACTORY = "EntityManagerFactory";

   protected void resolveInjectors() throws Exception
   {
      pushEnc();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         // UserTransaction
         try
         {
            Util.rebind(getEnc(), "UserTransaction", new UserTransactionImpl());
         }
         catch (NamingException e)
         {
            NamingException namingException = new NamingException("Could not bind user transaction for ejb name " + ejbName + " into JNDI under jndiName: " + getEnc().getNameInNamespace() + "/" + "UserTransaction");
            namingException.setRootCause(e);
            throw namingException;
         }
         // TransactionSynchronizationRegistry
         try
         {
            Util.createLinkRef(getEnc(), "TransactionSynchronizationRegistry", "TransactionSynchronizationRegistry");
            log.debug("Linked java:comp/TransactionSynchronizationRegistry to JNDI name: TransactionSynchronizationRegistry");
         }
         catch (NamingException e)
         {
            NamingException namingException = new NamingException("Could not bind user transaction for ejb name " + ejbName + " into JNDI under jndiName: " + getEnc().getNameInNamespace() + "/" + "UserTransaction");
            namingException.setRootCause(e);
            throw namingException;
         }
      }
      finally
      {
         popEnc();
      }
   }

   protected void createCallbackHandler()
   {
      try
      {
         callbackHandler = new LifecycleInterceptorHandler(this,
                 getHandledCallbacks());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error creating callback handler for bean "
                 + beanClassName, e);
      }
   }

   protected Class[] getHandledCallbacks()
   {
      return new Class[]
              {PostConstruct.class, PreDestroy.class, Timeout.class};
   }

   private void initialiseInterceptors()
   {
      if (applicableInterceptors == null)
      {
         log.debug("Initialising interceptors for " + getEjbName() + "...");
         HashSet<InterceptorInfo> defaultInterceptors = interceptorRepository.getDefaultInterceptors();
         log.debug("Default interceptors: " + defaultInterceptors);

         classInterceptors = interceptorRepository.getClassInterceptors(this);
         log.debug("Class interceptors: " + classInterceptors);

         applicableInterceptors = new LinkedHashSet<InterceptorInfo>();
         if (defaultInterceptors != null) applicableInterceptors.addAll(defaultInterceptors);
         if (classInterceptors != null) applicableInterceptors.addAll(classInterceptors);

         Method[] methods = clazz.getMethods();
         for (int i = 0; i < methods.length; i++)
         {
            List methodIcptrs = interceptorRepository.getMethodInterceptors(this, methods[i]);
            if (methodIcptrs != null && methodIcptrs.size() > 0)
            {
               log.debug("Method interceptors for  " + methods[i] + ": " + methodIcptrs);
               applicableInterceptors.addAll(methodIcptrs);
            }
         }
         log.debug("All applicable interceptor classes: " + applicableInterceptors);
      }
   }

   public Object getBusinessObject(BeanContext beanContext, Class businessObject) throws IllegalStateException
   {
      throw new IllegalStateException("Not implemented");
   }

   public Object getInvokedBusinessInterface(BeanContext beanContext) throws IllegalStateException
   {
      throw new IllegalStateException("Not implemented");
   }

   protected Object getInvokedInterface(Method method)
   {
      Remote remoteAnnotation = (Remote) resolveAnnotation(Remote.class);
      if (remoteAnnotation != null)
      {
         Class[] remotes = remoteAnnotation.value();
         for (int i = 0; i < remotes.length; ++i)
         {
            try
            {
               remotes[i].getMethod(method.getName(), method.getParameterTypes());
               return remotes[i];
            }
            catch (NoSuchMethodException e)
            {
            }
         }
      }

      Local localAnnotation = (Local) resolveAnnotation(Local.class);
      if (localAnnotation != null)
      {
         Class[] locals = localAnnotation.value();
         for (int i = 0; i < locals.length; ++i)
         {
            Method[] interfaceMethods = locals[i].getMethods();
            for (int j = 0; j < interfaceMethods.length; ++j)
            {
               if (interfaceMethods[j].equals(method))
                  return locals[i];
            }
         }
      }

      return null;
   }

   // todo these method overrides for aop are for performance reasons
   private Class loadPublicAnnotation(String annotation)
   {
      try
      {
         Class ann = classloader.loadClass(annotation);
         if (!ann.isAnnotation()) return null;
         Retention retention = (Retention) ann.getAnnotation(Retention.class);
         if (retention != null && retention.value() == RetentionPolicy.RUNTIME) return ann;

      }
      catch (ClassNotFoundException ignored)
      {
      }
      return null;
   }

   @Override
   public boolean hasAnnotation(Class tgt, String annotation)
   {
      if (annotations.hasClassAnnotation(annotation)) return true;
      if (tgt == null) return false;
      try
      {
         Class ann = loadPublicAnnotation(annotation);
         // it is metadata or CLASS annotation
         if (ann == null) return AnnotationElement.isAnyAnnotationPresent(tgt, annotation);
         return tgt.isAnnotationPresent(ann);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }


   @Override
   public boolean hasAnnotation(Method m, String annotation)
   {
      if (annotations.hasAnnotation(m, annotation)) return true;
      try
      {
         Class ann = loadPublicAnnotation(annotation);
         // it is metadata or CLASS annotation
         if (ann == null) return AnnotationElement.isAnyAnnotationPresent(m, annotation);
         return m.isAnnotationPresent(ann);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }

   @Override
   public boolean hasAnnotation(Field m, String annotation)
   {
      if (annotations.hasAnnotation(m, annotation)) return true;
      try
      {
         Class ann = loadPublicAnnotation(annotation);
         // it is metadata or CLASS annotation
         if (ann == null) return AnnotationElement.isAnyAnnotationPresent(m, annotation);
         return m.isAnnotationPresent(ann);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }

   @Override
   public boolean hasAnnotation(Constructor m, String annotation)
   {
      if (annotations.hasAnnotation(m, annotation)) return true;
      try
      {
         Class ann = loadPublicAnnotation(annotation);
         // it is metadata or CLASS annotation
         if (ann == null) return AnnotationElement.isAnyAnnotationPresent(m, annotation);
         return m.isAnnotationPresent(ann);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
   }

   public Container resolveEjbContainer(String link, Class businessIntf)
   {
      return deployment.getEjbContainer(link, businessIntf);
   }

   public Container resolveEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      return deployment.getEjbContainer(businessIntf);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz)
   {
      if (clazz == this.getBeanClass())
      {
         return (T) resolveAnnotation(annotationType);
      }
      return clazz.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Method method)
   {
      if (clazz == this.getBeanClass())
      {
         return (T) resolveAnnotation(method, annotationType);
      }
      return method.getAnnotation(annotationType);
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Method method)
   {
      return (T) resolveAnnotation(method, annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Field field)
   {
      if (clazz == this.getBeanClass())
      {
         return (T) resolveAnnotation(field, annotationType);
      }
      return field.getAnnotation(annotationType);
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Field field)
   {
      return (T) resolveAnnotation(field, annotationType);
   }
   
   @Override
   public Object resolveAnnotation(Method m, Class annotation)
   {
      Object value = super.resolveAnnotation(m, annotation);
      if (value == null && m.isBridge()) value = getBridgedAnnotation(m, annotation);
      return value;
   }
   
   protected Object getBridgedAnnotation(Method bridgeMethod, Class annotation)
   {
      Method[] methods = bridgeMethod.getDeclaringClass().getMethods();
      int i = 0;
      boolean found = false;
      Class[] bridgeParams = bridgeMethod.getParameterTypes();
      while (i < methods.length && !found)
      {
         if (!methods[i].isBridge() && methods[i].getName().equals(bridgeMethod.getName()))
         {
            Class[] params = methods[i].getParameterTypes();
            if (params.length == bridgeParams.length)
            {
               int j = 0;
               boolean matches = true;
               while (j < params.length && matches)
               {
                  if (!bridgeParams[j].isAssignableFrom(params[j]))
                     matches = false;
                  ++j;
               }
               
               if (matches)
                  return resolveAnnotation(methods[i], annotation);
            }
         }
         ++i;
      }
 
      return null;
   }
   
   public Object resolveAnnotation(Method m, Class[] annotationChoices)
   {
      Object value = null;
      int i = 0;
      while (value == null && i < annotationChoices.length){
         value = resolveAnnotation(m, annotationChoices[i++]);
      }
      
      return value;
   }

   public String getIdentifier()
   {
      return getEjbName();
   }

   public String getDeploymentDescriptorType()
   {
      return "ejb-jar.xml";
   }

   public PersistenceUnitDeployment getPersistenceUnitDeployment(String unitName) throws NameNotFoundException
   {
      return deployment.getPersistenceUnitDeployment(unitName);
   }

   public String getEjbJndiName(Class businessInterface) throws NameNotFoundException
   {
      return deployment.getEjbJndiName(businessInterface);
   }

   public String getEjbJndiName(String link, Class businessInterface)
   {
      return deployment.getEjbJndiName(link, businessInterface);
   }
   
   public InvocationStatistics getInvokeStats()
   {
      return invokeStats;
   }


   public MethodInfo getMethodInfo(Method method)
   {
      long hash = MethodHashing.calculateHash(method);
      MethodInfo info = (MethodInfo) methodInterceptors.get(hash);
      if (info == null)
      {
         throw new RuntimeException("Could not resolve beanClass method from proxy call: " + method.toString());
      }
      return info;
   }
}
