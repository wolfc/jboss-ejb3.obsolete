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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.TimedObject;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.transaction.TransactionManager;

import org.jboss.aop.Advisor;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.aop.joinpoint.ConstructionInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aspects.currentinvocation.CurrentInvocationInterceptor;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.defaults.PoolDefaults;
import org.jboss.ejb3.aop.BeanContainer;
import org.jboss.ejb3.common.spi.ErrorCodes;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.injection.InjectionInvocation;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.interceptor.InterceptorInjector;
import org.jboss.ejb3.interceptors.aop.LifecycleCallbacks;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;
import org.jboss.ejb3.interceptors.direct.DirectContainer;
import org.jboss.ejb3.interceptors.direct.IndirectContainer;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.javaee.JavaEEComponentHelper;
import org.jboss.ejb3.javaee.JavaEEModule;
import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.pool.PoolFactory;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.security.SecurityDomainManager;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.ejb3.tx.UserTransactionImpl;
import org.jboss.injection.DependsHandler;
import org.jboss.injection.EJBHandler;
import org.jboss.injection.EJBInjectionContainer;
import org.jboss.injection.EncInjector;
import org.jboss.injection.ExtendedInjectionContainer;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.JndiInjectHandler;
import org.jboss.injection.PersistenceContextHandler;
import org.jboss.injection.PersistenceUnitHandler;
import org.jboss.injection.ResourceHandler;
import org.jboss.injection.WebServiceRefHandler;
import org.jboss.jca.spi.ComponentStack;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.metadata.ejb.spec.InterceptorsMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.util.naming.Util;
import org.jboss.virtual.VirtualFile;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class EJBContainer 
   implements Container, IndirectContainer<EJBContainer, DirectContainer<EJBContainer>>, 
      EJBInjectionContainer, ExtendedInjectionContainer, JavaEEComponent
{
   private static final Logger log = Logger.getLogger(EJBContainer.class);

   private String name;
   
   private BeanContainer beanContainer;
   
   private DirectContainer<EJBContainer> directContainer;
   
   protected EjbEncFactory encFactory = new DefaultEjbEncFactory();

   protected Pool pool;

   protected String ejbName;
   
   protected ObjectName objectName;

   protected int defaultConstructorIndex;

   protected String beanClassName;
   
   private Class<?> beanClass;

   protected ClassLoader classloader;

   // for performance there is an array.
   protected List<Injector> injectors = new ArrayList<Injector>();

   protected Context enc;

//   protected LifecycleInterceptorHandler callbackHandler;

   protected Hashtable initialContextProperties;

   protected Map<String, EncInjector> encInjectors = new HashMap<String, EncInjector>();

   protected JBossEnterpriseBeanMetaData xml;
   protected JBossAssemblyDescriptorMetaData assembly;

   protected Map<String, Map<AccessibleObject, Injector>> encInjections = new HashMap<String, Map<AccessibleObject, Injector>>();

//   protected List<InterceptorInfo> classInterceptors = new ArrayList<InterceptorInfo>();
//
//   protected LinkedHashSet<InterceptorInfo> applicableInterceptors;

   private HashMap<Class<?>, InterceptorInjector> interceptorInjectors = new HashMap<Class<?>, InterceptorInjector>();

   private Ejb3Deployment deployment;

   private DependencyPolicy dependencyPolicy;

   private String jaccContextId;

   protected InvocationStatistics invokeStats = new InvocationStatistics();
   
   private String partitionName;
   
   private List<Class<?>> businessInterfaces;
   
   private ThreadLocalStack<BeanContext<?>> currentBean = new ThreadLocalStack<BeanContext<?>>();
   
   protected boolean reinitialize = false;
   
   private static final int TOTAL_PERMITS = Integer.MAX_VALUE;
   
   // To support clean startup/shutdown
   private final Semaphore semaphore = new Semaphore(TOTAL_PERMITS, true);
   private final Lock invocationLock = new SemaphoreLock(this.semaphore);
   
   private static final Interceptor[] currentInvocationStack = new Interceptor[] { new CurrentInvocationInterceptor() };
   
   private ComponentStack cachedConnectionManager;
   
   /**
    * @param name                  Advisor name
    * @param manager               Domain to get interceptor bindings from
    * @param cl                    the EJB's classloader
    * @param beanClassName
    * @param ejbName
    * @param ctxProperties
    * @param interceptorRepository
    * @param deployment
    * @param beanMetaData           the meta data for this bean or null
    */

   public EJBContainer(String name, Domain domain, ClassLoader cl,
                       String beanClassName, String ejbName, Hashtable ctxProperties,
                       Ejb3Deployment deployment, JBossEnterpriseBeanMetaData beanMetaData) throws ClassNotFoundException
   {
      assert name != null : "name is null";
      assert deployment != null : "deployment is null";
      
      this.name = name;
      this.deployment = deployment;
      this.beanClassName = beanClassName;
      this.classloader = cl;
      this.xml = beanMetaData;
         
      this.beanClass = classloader.loadClass(beanClassName);
      
      // We can't type cast the direct container, because we just loaded the beanClass
      // so assuming we have an object is a safe bet.
      this.beanContainer = new BeanContainer(this);
      
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
      
      // Because interceptors will query back the EJBContainer for annotations
      // we must have set beanContainer first and then do the advisor. 
      try
      {
         beanContainer.initialize(ejbName, domain, beanClass, beanMetaData, cl);
      }
      catch(Exception e)
      {
         throw new RuntimeException("failed to initialize bean container ",e);
      }
      
      //annotations = new AnnotationRepositoryToMetaData(this);
      
      initialContextProperties = ctxProperties;
      try
      {
         Util.createSubcontext(getEnc(), "env");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
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
         Util.rebind(getEnc(), "ORB", new LinkRef("java:/JBossCorbaORB"));
      }
      catch(NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Deprecated
   public boolean canResolveEJB()
   {
      return deployment.canResolveEJB();
   }
   
   public abstract BeanContext<?> createBeanContext();
   
   public String createObjectName(String ejbName)
   {
      return JavaEEComponentHelper.createObjectName(deployment, ejbName);
   }
   
   /**
    * Do not call, for BeanContainer.
    * @throws IllegalAccessException 
    * @throws InstantiationException 
    */
   public Object createInterceptor(Class<?> interceptorClass) throws InstantiationException, IllegalAccessException
   {
      Object instance = interceptorClass.newInstance();
      InterceptorInjector interceptorInjector = interceptorInjectors.get(interceptorClass);
      assert interceptorInjector != null : "interceptorInjector not found for " + interceptorClass;
      interceptorInjector.inject(null, instance);
      return instance;
   }
   
   public String createObjectName(String unitName, String ejbName)
   {
      return JavaEEComponentHelper.createObjectName(deployment, unitName, ejbName);
   }
   
   /**
    * Do nothing.
    * @param ctx
    */
   public void destroyBeanContext(org.jboss.ejb3.interceptors.container.BeanContext<?> ctx)
   {
      
   }
   
   // TODO: re-evaluate this exposure
   @Deprecated
   public Advisor getAdvisor()
   {
      return beanContainer._getAdvisor();
   }

   /*
    * TODO: re-evalute this exposure
    */
   @Deprecated
   public AnnotationRepository getAnnotations()
   {
      return beanContainer.getAnnotationRepository();
   }

   protected BeanContainer getBeanContainer()
   {
      return beanContainer;
   }
   
   /**
    * 
    * @return   the bean class of this container
    * @deprecated   use getBeanClass
    */
   public Class<?> getClazz()
   {
      return getBeanClass();
   }
   
   @SuppressWarnings("unchecked")
   public static <C extends EJBContainer> C getEJBContainer(Advisor advisor)
   {
      try
      {
         return (C) ((ManagedObjectAdvisor<Object, BeanContainer>) advisor).getContainer().getEJBContainer();
      }
      catch(ClassCastException e)
      {
         throw new ClassCastException(e.getMessage() + " using " + advisor);
      }
   }
   
   public String getName()
   {
      return name;
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

   /**
    * Do not call, used by BeanContainer.
    * @return
    */
   public List<Method> getVirtualMethods()
   {
      return null;
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

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return beanContainer.isAnnotationPresent(annotationType);
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
      if ((method.getDeclaringClass().isAssignableFrom(other.getDeclaringClass())) && (method.getName().equals(other.getName())))
      {
         if (!method.getReturnType().equals(other.getReturnType()))
            return false;
         Class<?>[] params1 = method.getParameterTypes();
         Class<?>[] params2 = other.getParameterTypes();
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
      Collection<InjectionHandler<Environment>> handlers = this.deployment.getHandlers();
      if(handlers == null)
      {
         handlers = new ArrayList<InjectionHandler<Environment>>();
         handlers.add(new EJBHandler<Environment>());
         handlers.add(new DependsHandler<Environment>());
         handlers.add(new JndiInjectHandler<Environment>());
         handlers.add(new PersistenceContextHandler<Environment>());
         handlers.add(new PersistenceUnitHandler<Environment>());
         handlers.add(new ResourceHandler<Environment>());
         handlers.add(new WebServiceRefHandler<Environment>());
      }

      ClassLoader old = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classloader);
      try
      {
         // EJB container's XML must be processed before interceptor's as it may override interceptor's references
         for (InjectionHandler<Environment> handler : handlers) handler.loadXml(xml, this);

         Map<AccessibleObject, Injector> tmp = InjectionUtil.processAnnotations(this, handlers, getBeanClass());
         injectors.addAll(tmp.values());

         /*
         initialiseInterceptors();
         */
         for (Class<?> interceptorClass : beanContainer.getInterceptorClasses())
         {
            InterceptorMetaData interceptorMetaData = findInterceptor(interceptorClass);
            if(interceptorMetaData == null)
               continue;
            
            for (InjectionHandler<Environment> handler : handlers)
            {
               handler.loadXml(interceptorMetaData, this);
            }
         }
         for (Class<?> interceptorClass : beanContainer.getInterceptorClasses())
         {
            Map<AccessibleObject, Injector> injections = InjectionUtil.processAnnotations(this, handlers, interceptorClass);
            InterceptorInjector injector = new InterceptorInjector(injections);
            interceptorInjectors.put(interceptorClass, injector);
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
    * EJBTHREE-1025
    *
    * @throws EJBException If the bean does implements any one interface as both @Local and @Remote
    */
   protected void checkForDuplicateLocalAndRemoteInterfaces() throws EJBException
   {
      // Initialize issue used in Error Message
      String issue = "[" + ErrorCodes.ERROR_CODE_EJBTHREE1025 + "]";

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

   public JBossAssemblyDescriptorMetaData getAssemblyDescriptor()
   {
      return assembly;
   }

   // FIXME: remove
   @Deprecated
   public void setAssemblyDescriptor(JBossAssemblyDescriptorMetaData assembly)
   {
      this.assembly = assembly;
   }

   protected abstract List<Class<?>> resolveBusinessInterfaces();
   
   public InterceptorInfoRepository getInterceptorRepository()
   {
      throw new RuntimeException("invalid");
   }
   
   public Map<String, EncInjector> getEncInjectors()
   {
      return encInjectors;
   }

   public ComponentStack getCachedConnectionManager()
   {
      return cachedConnectionManager;
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
      return beanClass;
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
      /*
      try
      {
         return beanContainer.construct();
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
      */
      try
      {
         return beanClass.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected void reinitialize()
   {
      // FIXME: is this correct?
      beanContainer.reinitializeAdvisor();
      
      /*
      initClassMetaDataBindingsList();
      adviceBindings.clear();
      doesHaveAspects = false;
      constructorInfos = null;
      rebuildInterceptors();
      */
      
      bindEJBContext();
      
      reinitialize = false;
   }

   public void create() throws Exception
   {
      // Blocking invocations until start()
      this.blockInvocations();
      
      /*
      initializeClassContainer();
      for (int i = 0; i < constructors.length; i++)
      {
         if (constructors[i].getParameterTypes().length == 0)
         {
            defaultConstructorIndex = i;
            break;
         }
      }
      */
   }

   public final void start() throws Exception
   {
      this.lockedStart();
      
      // Allow invocations until stop()
      this.allowInvocations();
   }
   
   // Everything must be done in start to make sure all dependencies have been satisfied
   protected void lockedStart() throws Exception
   {
      if (reinitialize)
         reinitialize();
       
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

//      createCallbackHandler();
      
      // If we're clustered, find our partition name
      findPartitionName();
      
      log.info("STARTED EJB: " + beanClass.getName() + " ejbName: " + ejbName);
   }

   public final void stop() throws Exception
   {
      // Wait for active invocations to complete - and block new invocations
      this.blockInvocations();
      
      this.lockedStop();
   }
   
   protected void lockedStop() throws Exception
   {
      reinitialize = true;
      
      //encFactory.cleanupEnc(this);
      
      if (pool != null)
      {
         pool.destroy();
         pool = null;
      }
      
      injectors = new ArrayList<Injector>();
      encInjectors = new HashMap<String, EncInjector>();
      
      InitialContextFactory.close(enc, this.initialContextProperties);
      enc = null; 
      
      log.info("STOPPED EJB: " + beanClass.getName() + " ejbName: " + ejbName);
   }

   public void destroy() throws Exception
   {
      encFactory.cleanupEnc(this);
      
      // TODO: clean up BeanContainer?
      //super.cleanup();
      
      // Restore to pre- create() state
      this.allowInvocations();
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
   
   protected Method getTimeoutCallback(NamedMethodMetaData timeoutMethodMetaData, Class<?> beanClass)
   {
      JBossEnterpriseBeanMetaData metaData = xml;
      if(metaData != null)
      {
         if(timeoutMethodMetaData != null)
         {
            String methodName = timeoutMethodMetaData.getMethodName();
            try
            {
               return beanClass.getMethod(methodName, Timer.class);
            }
            catch (SecurityException e)
            {
               throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e)
            {
               throw new RuntimeException("No method " + methodName + "(javax.ejb.Timer timer) found on bean " + ejbName, e);
            }
         }
      }
      
      if(TimedObject.class.isAssignableFrom(beanClass))
      {
         try
         {
            return TimedObject.class.getMethod("ejbTimeout", Timer.class);
         }
         catch (SecurityException e)
         {
            throw new RuntimeException(e);
         }
         catch (NoSuchMethodException e)
         {
            throw new RuntimeException(e);
         }
      }
      
      if(metaData != null)
      {  
         // TODO: cross cutting concern
         if(metaData.getEjbJarMetaData().isMetadataComplete())
            return null;
      }
      
      for (Method method : beanClass.getMethods())
      {
         if (getAnnotation(Timeout.class, method) != null)
         {
            if (Modifier.isPublic(method.getModifiers()) &&
                  method.getReturnType().equals(Void.TYPE) &&
                  method.getParameterTypes().length == 1 &&
                  method.getParameterTypes()[0].equals(Timer.class))
            {
               // TODO: check for multiples
               return method;
            }
            else
            {
               throw new RuntimeException("@Timeout method " + method + " must have signature: void <METHOD>(javax.ejb.Timer timer) (EJB3 18.2.2)");
            }
         }
      }
      
      return null;
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
      PoolFactoryRegistry registry = deployment.getPoolFactoryRegistry();
      PoolFactory factory = registry.getPoolFactory(registeredPoolName);
      pool = factory.createPool();
      pool.initialize(this, maxSize, timeout);

      resolveInjectors();
      pool.setInjectors(injectors.toArray(new Injector[injectors.size()]));
   }

   /**
    * Note this method is a WIP.
    * 
    * In actuality ejb3-interceptors should perform the injection itself,
    * but this requires a rewrite of all injectors.
    */
   public void injectBeanContext(BeanContext<?> beanContext)
   {
      try
      {
         if(injectors == null)
            return;
         Advisor advisor = getAdvisor();
         for (Injector injector : injectors)
         {
            InjectionInvocation invocation = new InjectionInvocation(beanContext, injector, currentInvocationStack);
            invocation.setAdvisor(advisor);
            invocation.setTargetObject(beanContext.getInstance());
            invocation.invokeNext();
         }
      }
      catch(Throwable t)
      {
         if(t instanceof Error)
            throw (Error) t;
         if(t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw new RuntimeException(t);
      }
   }
   
   /**
    * Note that this method is a WIP.
    * 
    * @param beanContext
    * @param callbackAnnotationClass    on of PostConstruct, PreDestroy, PostActivate or PrePassivate
    */
   protected void invokeCallback(BeanContext<?> beanContext, Class<? extends Annotation> callbackAnnotationClass)
   {
      try
      {
         // Do lifecycle callbacks
         List<Class<?>> lifecycleInterceptorClasses = beanContainer.getInterceptorRegistry().getLifecycleInterceptorClasses();
         Advisor advisor = getAdvisor();
         Interceptor interceptors[] = LifecycleCallbacks.createLifecycleCallbackInterceptors(advisor, lifecycleInterceptorClasses, beanContext, callbackAnnotationClass);
         
         Constructor<?> constructor = beanClass.getConstructor();
         Object initargs[] = null;
         ConstructionInvocation invocation = new ConstructionInvocation(interceptors, constructor, initargs);
         invocation.setAdvisor(advisor);
         invocation.setTargetObject(beanContext.getInstance());
         invocation.invokeNext();
      }
      catch(Throwable t)
      {
         throw new RuntimeException(t);
      }
   }
   
   public void invokePostConstruct(BeanContext<?> beanContext)
   {
      // FIXME: This is a dirty hack to notify AS EJBTimerService about what's going on
      AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsAssociation.IN_EJB_CREATE);
      try
      {
         invokeCallback(beanContext, PostConstruct.class);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   @Deprecated
   public void invokePostConstruct(BeanContext beanContext, Object[] params)
   {
      invokePostConstruct(beanContext);
   }

   public void invokePreDestroy(BeanContext beanContext)
   {
      invokeCallback(beanContext, PreDestroy.class);
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

   /*
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
   */

   protected Class[] getHandledCallbacks()
   {
      return new Class[]
              {PostConstruct.class, PreDestroy.class, Timeout.class};
   }

   // TODO: once injection is finalized this method will disappear
   private InterceptorMetaData findInterceptor(Class<?> interceptorClass)
   {
      if(xml == null)
         return null;
      JBossMetaData ejbJarMetaData = xml.getEjbJarMetaData();
      if(ejbJarMetaData == null)
         return null;
      InterceptorsMetaData interceptors = ejbJarMetaData.getInterceptors();
      if(interceptors == null)
         return null;
      for(InterceptorMetaData interceptorMetaData : interceptors)
      {
         if(interceptorMetaData.getInterceptorClass().equals(interceptorClass.getName()))
            return interceptorMetaData;
      }
      return null;
   }
   
   protected void findPartitionName()
   {
      Clustered clustered = (Clustered) getAnnotation(Clustered.class);
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

   /*
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
   */
   
   public String resolveEJB(String link, Class<?> beanInterface, String mappedName)
   {
      return deployment.resolveEJB(link, beanInterface, mappedName);
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
   
   public String resolvePersistenceUnitSupplier(String unitName)
   {
      return getDeployment().resolvePersistenceUnitSupplier(unitName);
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      if (this.getAnnotations().isDisabled(annotationType))
         return null;
      
      return beanContainer.getAnnotation(annotationType);
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz)
   {
      return beanContainer.getAnnotation(clazz, annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Method method)
   {
      return beanContainer.getAnnotation(annotationType, clazz, method);
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Method method)
   {
      if (this.getAnnotations().isDisabled(method, annotationType))
         return null;
      
      return beanContainer.getAnnotation(annotationType, method);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Field field)
   {
      return beanContainer.getAnnotation(annotationType, clazz, field);
   }
   
   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Field field)
   {
      return beanContainer.getAnnotation(annotationType, field);
   }
   
   /**
    * @deprecated use getAnnotation
    */
   @SuppressWarnings("unchecked")
   public Object resolveAnnotation(Class annotationType)
   {
      return getAnnotation(annotationType);
   }
   
   /**
    * @deprecated use getAnnotation
    */
   @SuppressWarnings("unchecked")
   public Object resolveAnnotation(Field field, Class annotationType)
   {
      return getAnnotation(annotationType, field);
   }
   
   /**
    * @deprecated use getAnnotation
    */
   @SuppressWarnings("unchecked")
   public Object resolveAnnotation(Method method, Class annotationType)
   {
      return getAnnotation(annotationType, method);
   }
   
   /**
    * @deprecated this is going to be gone soon
    */
   @SuppressWarnings("unchecked")
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

   @Deprecated
   protected MethodInfo getMethodInfo(Method method)
   {
      long hash = MethodHashing.calculateHash(method);
      MethodInfo info = getAdvisor().getMethodInfo(hash);
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
      // FIXME: because of the flaked life cycle of an EJBContainer (we add annotations after it's been
      // constructed), we must reinitialize the whole thing. 
      beanContainer.reinitializeAdvisor();
   }
   
   @Inject
   public void setCachedConnectionManager(ComponentStack ccm)
   {
      this.cachedConnectionManager = ccm;
   }
   
   public void setDirectContainer(DirectContainer<EJBContainer> container)
   {
      this.directContainer = container;
   }
   
   protected Method getNonBridgeMethod(Method bridgeMethod)
   {
      Class clazz = bridgeMethod.getDeclaringClass();
      Method[] methods = clazz.getMethods();
      for (Method method : methods)
      {
         if (!method.isBridge() && method.getParameterTypes().length == bridgeMethod.getParameterTypes().length)
         {
            return method;
         }
      }
      
      return bridgeMethod;
   }
   
   public Lock getInvocationLock()
   {
      return this.invocationLock;
   }

   // to make sure we have a dependency on the TransactionManager
   // note that the actual tx interceptors don't make use of the injected tm
   @Inject
   public void setTransactionManager(TransactionManager tm)
   {
      
   }
   
   public String toString()
   {
      return getObjectName().getCanonicalName();
   }
   
   private void blockInvocations() throws InterruptedException
   {
      // Allow re-entrance
      if (this.semaphore.tryAcquire())
      {
         try
         {
            // Acquire all remaining permits, blocking invocation lock
            this.semaphore.acquire(TOTAL_PERMITS - 1);
         }
         catch (InterruptedException e)
         {
            this.semaphore.release();
            
            throw e;
         }
      }
   }
   
   private void allowInvocations()
   {
      // Allow re-entrance
      if (!this.semaphore.tryAcquire())
      {
         // Make all permits available to invocation lock
         this.semaphore.release(TOTAL_PERMITS);
      }
      else
      {
         // Release the one we just acquired
         this.semaphore.release();
      }
   }
   
   /**
    * {@link java.util.concurrent.locks.Lock} facade for this container's semaphore
    * @author Paul Ferraro
    */
   private static class SemaphoreLock implements Lock
   {
      private final Semaphore semaphore;
      
      SemaphoreLock(Semaphore semaphore)
      {
         this.semaphore = semaphore;
      }
      
      /**
       * @see java.util.concurrent.locks.Lock#lock()
       */
      public void lock()
      {
         this.semaphore.acquireUninterruptibly();
      }

      /**
       * @see java.util.concurrent.locks.Lock#lockInterruptibly()
       */
      public void lockInterruptibly() throws InterruptedException
      {
         this.semaphore.acquire();
      }

      /**
       * @see java.util.concurrent.locks.Lock#newCondition()
       */
      public Condition newCondition()
      {
         throw new UnsupportedOperationException();
      }

      /**
       * @see java.util.concurrent.locks.Lock#tryLock()
       */
      public boolean tryLock()
      {
         return this.semaphore.tryAcquire();
      }

      /**
       * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
       */
      public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException
      {
         return this.semaphore.tryAcquire(timeout, unit);
      }

      /**
       * @see java.util.concurrent.locks.Lock#unlock()
       */
      public void unlock()
      {
         this.semaphore.release();
      }
   }
}
