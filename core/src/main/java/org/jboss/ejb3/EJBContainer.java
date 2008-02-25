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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Timeout;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassContainer;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.annotation.AnnotationElement;
import org.jboss.aop.joinpoint.ConstructorInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.defaults.PoolDefaults;
import org.jboss.ejb3.deployers.Ejb3Deployer;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.ejb3.interceptor.InterceptorInfo;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.interceptor.InterceptorInjector;
import org.jboss.ejb3.interceptor.LifecycleInterceptorHandler;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.javaee.JavaEEComponentHelper;
import org.jboss.ejb3.javaee.JavaEEModule;
import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.pool.PoolFactory;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.ejb3.security.JaccHelper;
import org.jboss.ejb3.security.SecurityDomainManager;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.ejb3.tx.UserTransactionImpl;
import org.jboss.iiop.CorbaORBService;
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
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.naming.Util;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.virtual.VirtualFile;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class EJBContainer extends ClassContainer implements Container, InjectionContainer, JavaEEComponent
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

   //protected SessionCallbackHandler callbackHandler;
   protected LifecycleInterceptorHandler callbackHandler;

   protected Hashtable initialContextProperties;

   protected Map<String, EncInjector> encInjectors = new HashMap<String, EncInjector>();

   protected JBossEnterpriseBeanMetaData xml;
   protected JBossAssemblyDescriptorMetaData assembly;

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
   
   private String partitionName;
   
   private List<Class<?>> businessInterfaces;
   
   private ThreadLocalStack<BeanContext<?>> currentBean = new ThreadLocalStack<BeanContext<?>>();
   
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
      
      assert interceptorRepository != null : "interceptorRepository is null";
      assert deployment != null : "deployment is null";
      
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
      String on = createObjectName(ejbName);
      try
      {
         objectName = new ObjectName(on);
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException("failed to create object name for: " + on, e);
      }
      
      annotations = new AnnotationRepositoryToMetaData(this);
      
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
      bindORB();
      bindEJBContext();
      
      this.dependencyPolicy = deployment.createDependencyPolicy(this);
   }

   private void bindEJBContext()
   {
      try 
      {
         Reference ref = new Reference(EJBContext.class.getName(), EJBContextFactory.class.getName(), null);
         ref.add(new StringRefAddr("containerGuid", Ejb3Registry.guid(this)));
         ref.add(new StringRefAddr("containerClusterUid", Ejb3Registry.clusterUid(this)));
         ref.add(new StringRefAddr("isClustered", Boolean.toString(isClustered())));
         Util.rebind(getEnc(), "EJBContext", ref);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private void bindORB()
   {
      try
      {
         Util.rebind(getEnc(), "ORB", new LinkRef("java:/" + CorbaORBService.ORB_NAME));
      }
      catch(NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public abstract BeanContext<?> createBeanContext();
   
   public String createObjectName(String ejbName)
   {
      return JavaEEComponentHelper.createObjectName(deployment, ejbName);
   }
   
   public String createObjectName(String unitName, String ejbName)
   {
      return JavaEEComponentHelper.createObjectName(deployment, unitName, ejbName);
   }
   
   public void pushContext(BeanContext<?> beanContext)
   {
      currentBean.push(beanContext);
   }
   
   /**
    * Makes sure that EJB's ENC is available
    * Delegates to whatever implementation is used to push the ENC of the EJB
    * onto the stack
    *
    */
   protected void pushEnc()
   {
      encFactory.pushEnc(this);
   }

   public BeanContext<?> peekContext()
   {
      BeanContext<?> ctx = currentBean.get();
      assert ctx != null : "ctx is null";
      return ctx;
   }
   
   public BeanContext<?> popContext()
   {
      return currentBean.pop();
   }
   
   /**
    * Pops EJB's ENC from the stack.  Delegates to whatever implementation
    * is used to pop the EJB's ENC from the stock
    *
    */
   protected void popEnc()
   {
      encFactory.popEnc(this);
   }
   
   public Environment getEnvironmentRefGroup()
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
   
   /**
    * Return all the business interfaces implemented by this bean.
    * 
    * Available after the meta data has been processed.
    * 
    * @return   an array of business interfaces or empty if no interface is provided
    */
   public List<Class<?>> getBusinessInterfaces()
   {
      if(businessInterfaces == null) throw new IllegalStateException("businessInterfaces not yet initialized");
      return businessInterfaces;
   }
      
   public String getDeploymentQualifiedName()
   {
      return objectName.getCanonicalName();
   }
   
   /**
    * Returns a String identifier for this bean that is qualified by the
    * deployment, and hence should be unique across deployments. Name is of the 
    * form "ear=foo.ear,jar=foo.jar,name=Bar", where "Bar" is the value 
    * returned by {@link #getEjbName()}. The "ear=foo.ear" portion is ommitted 
    * if the bean is not packaged in an ear.
    */
   public String getDeploymentPropertyListString()
   {
      return objectName.getCanonicalKeyPropertyListString();
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
    * Is the method a business method of this container.
    * 
    * @param businessMethod     the method in question
    * @return   true if so, otherwise false
    */
   public boolean isBusinessMethod(Method businessMethod)
   {
      for(Class<?> businessInterface : getBusinessInterfaces())
      {
         for(Method method : businessInterface.getMethods())
         {
            if(isCallable(method, businessMethod))
               return true;
         }
      }
      return false;
   }
   
   /**
    * Can method definition method be used to call method other.
    * For example if the method is defined in an interface it can be used to call a method
    * in a class.
    * 
    * @param method
    * @param other
    * @return
    */
   private static boolean isCallable(Method method, Method other)
   {
      if ((method.getDeclaringClass().isAssignableFrom(other.getDeclaringClass())) && (method.getName() == other.getName()))
      {
         if (!method.getReturnType().equals(other.getReturnType()))
            return false;
         Class[] params1 = method.getParameterTypes();
         Class[] params2 = other.getParameterTypes();
         if (params1.length == params2.length)
         {
            for (int i = 0; i < params1.length; i++)
            {
               if (params1[i] != params2[i])
                  return false;
            }
            return true;
         }
      }
      return false;
   }   
   
   /**
    * introspects EJB container to find all dependencies
    * and initialize any extra metadata.
    * <p/>
    * This must be called before container is registered with any microcontainer
    *
    * @param dependencyPolicy
    */
   public void processMetadata()
   {
      // XML must be done first so that any annotation overrides are initialized
      
      // todo injection handlers should be pluggable from XML
      Collection<InjectionHandler<Environment>> handlers = new ArrayList<InjectionHandler<Environment>>();
      handlers.add(new EJBHandler<Environment>());
      handlers.add(new DependsHandler<Environment>());
      handlers.add(new JndiInjectHandler<Environment>());
      handlers.add(new PersistenceContextHandler<Environment>());
      handlers.add(new PersistenceUnitHandler<Environment>());
      handlers.add(new ResourceHandler<Environment>());
      handlers.add(new WebServiceRefHandler<Environment>());

      ClassLoader old = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classloader);
      try
      {
         // EJB container's XML must be processed before interceptor's as it may override interceptor's references
         for (InjectionHandler<Environment> handler : handlers) handler.loadXml(xml, this);

         Map<AccessibleObject, Injector> tmp = InjectionUtil.processAnnotations(this, handlers, getBeanClass());
         injectors.addAll(tmp.values());

         initialiseInterceptors();
         for (InterceptorInfo interceptorInfo : applicableInterceptors)
         {
            for (InjectionHandler<Environment> handler : handlers)
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

         // When @WebServiceRef is not used service-ref won't be processed
         // In this case we process them late
         if(xml != null && xml.getServiceReferences() != null)
         {
            for(ServiceReferenceMetaData sref : xml.getServiceReferences())
            {
               // FIXME: fix WS metadata
               /*
               if(!sref.isProcessed())
               {
                  try
                  {
                     String name = sref.getServiceRefName();
                     String encName = "env/" + name;
                     Context encCtx = getEnc();

                     UnifiedVirtualFile vfsRoot = new VirtualFileAdaptor(getRootFile());
                     new ServiceRefDelegate().bindServiceRef(encCtx, encName, vfsRoot, getClassloader(), sref);

                  }
                  catch (Exception e)
                  {
                     log.error("Failed to bind service-ref", e);
                  }
               }
               */
            }
         }
         
         // EJBTHREE-1025
         this.checkForDuplicateLocalAndRemoteInterfaces();
         
         for(Class<?> businessInterface : getBusinessInterfaces())
            ((JBoss5DependencyPolicy) getDependencyPolicy()).addSupply(businessInterface);
         
         Class localHomeInterface = ProxyFactoryHelper.getLocalHomeInterface(this);
         if(localHomeInterface != null)
            ((JBoss5DependencyPolicy) getDependencyPolicy()).addSupply(localHomeInterface);
         
         Class remoteHomeInterface = ProxyFactoryHelper.getRemoteHomeInterface(this);
         if(remoteHomeInterface != null)
            ((JBoss5DependencyPolicy) getDependencyPolicy()).addSupply(remoteHomeInterface);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }
   
   /**
    * Ensures that the bean does not implement any one interface as both @Local and @Remote
    *
    * @throws EJBException If the bean does implements any one interface as both @Local and @Remote
    */
   protected void checkForDuplicateLocalAndRemoteInterfaces() throws EJBException
   {
      // Initialize issue used in Error Message
      String issue = "(EJBTHREE-1025)";

      // Obtain annotations, if found
      Local local = (Local) resolveAnnotation(Local.class);
      Remote remote = (Remote) resolveAnnotation(Remote.class);

      // If either local or remote is unspecified, return safely - there can be no overlap
      if (local == null || remote == null)
      {
         return;
      }

      // Ensure "value" attribute of both local and remote are not blank
      if (local.value().length < 1 && local.value().length < 1)
      {
         throw new EJBException("Cannot designate both " + Local.class.getName() + " and " + Remote.class.getName()
               + " annotations without 'value' attribute on " + this.getEjbName() + ". " + issue);
      }

      // Iterate through local and remote interfaces, ensuring any one interface is not being used for both local and remote exposure
      for (Class<?> localClass : local.value())
      {
         for (Class<?> remoteClass : remote.value())
         {
            if (localClass.equals(remoteClass))
            {
               throw new EJBException("Cannot designate " + localClass.getName() + " as both " + Local.class.getName()
                     + " and " + Remote.class.getName() + " on " + this.getEjbName() + ". " + issue);
            }
         }
      }
   }

   public JBossEnterpriseBeanMetaData getXml()
   {
      return xml;
   }

   public void setXml(JBossEnterpriseBeanMetaData xml)
   {
      this.xml = xml;
   }

   public JBossAssemblyDescriptorMetaData getAssemblyDescriptor()
   {
      return assembly;
   }

   public void setAssemblyDescriptor(JBossAssemblyDescriptorMetaData assembly)
   {
      this.assembly = assembly;
   }

   protected abstract List<Class<?>> resolveBusinessInterfaces();
   
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
         return InitialContextFactory.getInitialContext(initialContextProperties);
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

   public Class<?> getBeanClass()
   {
      return clazz;
   }

   public Pool getPool()
   {
      return pool;
   }
   
   /**
    * Gets the name of the cluster partition with which this container is
    * associated. Not available until <code>EJBContainer.start()</code>
    * is completed.
    * 
    * @return the name of the cluster partition with which this container is
    *         associated, or <code>null</code> if the container is not clustered
    */
   public String getPartitionName()
   {
      if (partitionName == null)
         this.findPartitionName();
      return partitionName;
   }

   protected Object construct()
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
      
      // If we're clustered, find our partition name
      findPartitionName();
      
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

   @SuppressWarnings("unchecked")
   public <T> T getSecurityManager(Class<T> type)
   {
      try
      {
         InitialContext ctx = getInitialContext();
         SecurityDomain securityAnnotation = (SecurityDomain) resolveAnnotation(SecurityDomain.class);
         if (securityAnnotation != null && securityAnnotation.value().length() > 0)
         {
            return (T) SecurityDomainManager.getSecurityManager(securityAnnotation.value(),ctx);
         }
         return null;
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected void initializePool() throws Exception
   {
      org.jboss.ejb3.annotation.Pool poolAnnotation = getAnnotation(org.jboss.ejb3.annotation.Pool.class);
      if (poolAnnotation == null)
         throw new IllegalStateException("No pool annotation");
      String registeredPoolName = poolAnnotation.value();
      // EJBTHREE-1119
      if(registeredPoolName==null||registeredPoolName.trim().equals(""))
      {
         // Default the Pool Implementation
         registeredPoolName = PoolDefaults.POOL_IMPLEMENTATION_THREADLOCAL;
      }
      int maxSize = poolAnnotation.maxSize();
      long timeout = poolAnnotation.timeout();
      Ejb3Deployer deployer = deployment.getDeployer();
      PoolFactoryRegistry registry = deployer.getPoolFactoryRegistry();
      PoolFactory factory = registry.getPoolFactory(registeredPoolName);
      pool = factory.createPool();
      pool.initialize(this, maxSize, timeout);

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

   public void invokeInit(Object bean, Class[] initParameterTypes,
                          Object[] initParameterValues)
   {
      // do nothing, only useful on a stateful session bean
   }

   public static final String MANAGED_ENTITY_MANAGER_FACTORY = "ManagedEntityManagerFactory";

   public static final String ENTITY_MANAGER_FACTORY = "EntityManagerFactory";

   protected void resolveInjectors() throws Exception
   {
      pushEnc();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
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
         try
         {
            Util.rebind(getEnc(), "TransactionSynchronizationRegistry", new LinkRef("java:TransactionSynchronizationRegistry"));
            log.debug("Linked java:comp/TransactionSynchronizationRegistry to JNDI name: java:TransactionSynchronizationRegistry");
         }
         catch (NamingException e)
         {
            NamingException namingException = new NamingException("Could not bind TransactionSynchronizationRegistry for ejb name " + ejbName + " into JNDI under jndiName: " + getEnc().getNameInNamespace() + "/" + "TransactionSynchronizationRegistry");
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
   
   protected void findPartitionName()
   {
      Clustered clustered = (Clustered) resolveAnnotation(Clustered.class);
      if (clustered == null)
      {
         partitionName = null;
         return;
      }
      
      String value = clustered.partition();
      try
      {
         String replacedValue = StringPropertyReplacer.replaceProperties(value);
         if (value != replacedValue)
         {            
            log.debug("Replacing @Clustered partition attribute " + value + " with " + replacedValue);
            value = replacedValue;
         }
      }
      catch (Exception e)
      {
         log.warn("Unable to replace @Clustered partition attribute " + value + 
                  ". Caused by " + e.getClass() + " " + e.getMessage());         
      }
      
      partitionName = value;
   }

   public <T> T getBusinessObject(BeanContext<?> beanContext, Class<T> businessInterface) throws IllegalStateException
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

   public String resolveMessageDestination(String link)
   {
      return deployment.resolveMessageDestination(link);
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      if (this.getAnnotations().isDisabled(annotationType))
         return null;
      
      return (T) resolveAnnotation(annotationType);
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
      if (this.getAnnotations().isDisabled(method, annotationType))
         return null;
      
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
      MethodInfo info = super.getMethodInfo(hash);
      if (info == null)
      {
         throw new RuntimeException("Could not resolve beanClass method from proxy call: " + method.toString());
      }
      return info;
   }
   
   public boolean isClustered()
   {
      return false;
   }
   
   public JavaEEModule getModule()
   {
      return deployment;
   }
   
   public abstract boolean hasJNDIBinding(String jndiName);
   
   /**
    * After XML processing has been done this allows the container
    * to further initialize the meta data.
    */
   public void instantiated()
   {
      this.businessInterfaces = resolveBusinessInterfaces();
      
      // Before we start to process annotations, make sure we also have the ones from interceptors-aop.
      initializeClassContainer();
   }
   
   public String toString()
   {
      return getObjectName().getCanonicalName();
   }
}
