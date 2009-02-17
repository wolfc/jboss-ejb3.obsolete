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
package org.jboss.ejb3.stateful;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.Init;
import javax.ejb.NoSuchEJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.RemoteHome;
import javax.ejb.RemoveException;
import javax.ejb.TimerService;

import org.jboss.aop.Advisor;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteHomeBinding;
import org.jboss.ejb3.cache.Cache;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulCacheFactory;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.interceptors.container.StatefulSessionContainerMethodInvocation;
import org.jboss.ejb3.proxy.ProxyUtils;
import org.jboss.ejb3.proxy.clustered.factory.session.stateful.StatefulSessionClusteredProxyFactory;
import org.jboss.ejb3.proxy.clustered.objectstore.ClusteredObjectStoreBindings;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringRegistry;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.session.stateful.StatefulSessionProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateful.StatefulSessionRemoteProxyFactory;
import org.jboss.ejb3.proxy.factory.stateful.StatefulLocalProxyFactory;
import org.jboss.ejb3.proxy.factory.stateful.StatefulRemoteProxyFactory;
import org.jboss.ejb3.proxy.impl.EJBMetaDataImpl;
import org.jboss.ejb3.proxy.impl.HomeHandleImpl;
import org.jboss.ejb3.proxy.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.jndiregistrar.JndiStatefulSessionRegistrar;
import org.jboss.ejb3.proxy.objectstore.ObjectStoreBindings;
import org.jboss.ejb3.proxy.remoting.SessionSpecRemotingMetadata;
import org.jboss.ejb3.proxy.remoting.StatefulSessionRemotingMetadata;
import org.jboss.ejb3.session.Ejb2xMethodNames;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.injection.Injector;
import org.jboss.injection.JndiPropertyInjector;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulContainer extends SessionSpecContainer
      implements
         StatefulObjectFactory<StatefulBeanContext>,
         PassivationManager<StatefulBeanContext>,
         StatefulSessionInvokableContext
{
   private static final Logger log = Logger.getLogger(StatefulContainer.class);

   protected Cache<StatefulBeanContext> cache;
   private StatefulDelegateWrapper mbean = new StatefulDelegateWrapper(this);

   public StatefulContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
                            Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData) throws ClassNotFoundException
   {
      super(cl, beanClassName, ejbName, domain, ctxProperties, deployment, beanMetaData);
   }

//   public StatefulBeanContext create(Class<?>[] initTypes, Object[] initValues)
//   {
//      StatefulBeanContext sfctx = (StatefulBeanContext) createBeanContext();
//      // Tell context how to handle replication
//      CacheConfig config = getAnnotation(CacheConfig.class);
//      if (config != null)
//      {
//         sfctx.setReplicationIsPassivation(config.replicationIsPassivation());
//      }
//
//      // this is for propagated extended PC's
//      sfctx = sfctx.pushContainedIn();
//      
//      pushContext(sfctx);
//      try
//      {
//         injectBeanContext(sfctx);
//
//         sfctx.initialiseInterceptorInstances();
//
//      }
//      finally
//      {
//         popContext();
//         // this is for propagated extended PC's
//         sfctx.popContainedIn();
//      }
//      
//      invokePostConstruct(sfctx, initValues);
//      
//      //TODO This needs to be reimplemented as replacement for create() on home interface
//      invokeInit(sfctx.getInstance(), initTypes, initValues);
//      
//      return sfctx;
//   }
   
   public StatefulBeanContext create(Class<?>[] initTypes, Object[] initValues, Map<Object, Object> sharedState)
   {
      StatefulBeanContext sfctx = (StatefulBeanContext) createBeanContext();
      // FIXME this needs to be passed via the constructor to establish invariant
      sfctx.setSharedState(sharedState);
      
      pushContext(sfctx);
      try
      {
         injectBeanContext(sfctx);

         sfctx.initialiseInterceptorInstances();

      }
      finally
      {
         popContext();
      }
      
      invokePostConstruct(sfctx, initValues);
      
      //TODO This needs to be reimplemented as replacement for create() on home interface
      invokeInit(sfctx.getInstance(), initTypes, initValues);
      
      log.trace("Created context " + sfctx.getId() + " for " + getName());
      
      return sfctx;
   }
   
   @Override
   public BeanContext<?> createBeanContext()
   {
      return new StatefulBeanContext(this, construct());
   }
   
   @Override
   protected StatefulLocalProxyFactory getProxyFactory(LocalBinding binding)
   {
      StatefulLocalProxyFactory factory = (StatefulLocalProxyFactory) this.proxyDeployer.getProxyFactory(binding);

      if (factory == null)
      {
         factory = new StatefulLocalProxyFactory(this, binding);

         try
         {
            factory.init();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      
      return factory;
   }
   
   public Object createProxyLocalEjb21(Object id, LocalBinding binding, String businessInterfaceType) throws Exception
   {
      StatefulLocalProxyFactory proxyFactory = this.getProxyFactory(binding);
      return proxyFactory.createProxyEjb21(id,businessInterfaceType);
   }
   
   public Object createProxyRemoteEjb21(Object id, String businessInterfaceType) throws Exception
   {
      RemoteBinding binding = this.getRemoteBinding();
      return this.createProxyRemoteEjb21(id,binding, businessInterfaceType);
   }

   public Object createProxyRemoteEjb21(Object id, RemoteBinding binding, String businessInterfaceType) throws Exception
   { 
      StatefulSessionProxyFactory proxyFactory = this.getProxyFactory(binding);
      return proxyFactory.createProxyEjb2x((Serializable)id);
   }
   
   public Object createProxyLocalEjb21(Object id, String businessInterfaceType) throws Exception
   {
      LocalBinding binding = this.getAnnotation(LocalBinding.class);
      return this.createProxyLocalEjb21(id,binding, businessInterfaceType);
   }
   
   @Override
   public Object createProxyLocalEjb21(LocalBinding binding, String businessInterfaceType) throws Exception
   {
      Object id = this.createSession();
      return this.createProxyLocalEjb21(id,binding, businessInterfaceType);
   }

   @Override
   public Object createProxyRemoteEjb21(RemoteBinding binding, String businessInterfaceType) throws Exception
   {
      Object id = this.createSession();
      return this.createProxyRemoteEjb21(id, binding, businessInterfaceType);
   }
   
   @Override
   protected StatefulSessionProxyFactory getProxyFactory(RemoteBinding binding)
   {
      //TODO Should be obtained from JNDI Registrar, needs to be looked up by a @RemoteBinding key

      /*
       * In this implementation we just make a new Proxy Factory, for now
       */

      // Initialize
      StatefulSessionProxyFactory factory = null;

      // If Clustered
      if (this.isAnnotationPresent(Clustered.class))
      {
         // Get the Proxy Clustering Registry
         Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();
         String mcName = ClusteredObjectStoreBindings.CLUSTERED_OBJECTSTORE_BEAN_NAME_PROXY_CLUSTERING_REGISTRY;
         ProxyClusteringRegistry registry = (ProxyClusteringRegistry) registrar.lookup(mcName);
         assert registry != null : "Could not find " + ProxyClusteringRegistry.class.getSimpleName() + " in the "
               + Ejb3Registrar.class.getSimpleName() + " under name " + mcName;
         factory = new StatefulSessionClusteredProxyFactory(this.getName(), this.getName(), Ejb3Registry.guid(this),
               this.getMetaData(), this.getClassloader(), binding.clientBindUrl(), this.getAdvisor(), registry, null);
      }
      else
      {
         factory = new StatefulSessionRemoteProxyFactory(this.getName(), this.getName(), Ejb3Registry.guid(this), this
               .getMetaData(), this.getClassloader(), binding.clientBindUrl(), this.getAdvisor(), null);
      }

      // Start the Factory
      try
      {
         factory.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not start " + factory, e);
      }

      // Return
      return factory;
   }
   
   public void destroy(StatefulBeanContext ctx)
   {
      try
      {
         invokePreDestroy(ctx);
      }
      finally
      {
         ctx.remove();
      }
   }
   
   public Object getMBean()
   {
      return mbean;
   }
   
   /**
    * Creates and starts the configured cache, if not
    * started already
    * 
    * @throws Exception
    */
   protected void createAndStartCache() throws Exception {
      
      // If Cache is initialized, exit
      if(this.cache!=null && this.cache.isStarted())
      {
         return;
      }
      
      org.jboss.ejb3.annotation.Cache cacheAnnotation = getAnnotation(org.jboss.ejb3.annotation.Cache.class);
      org.jboss.ejb3.annotation.CacheConfig cacheConfig = getAnnotation(org.jboss.ejb3.annotation.CacheConfig.class);
      CacheFactoryRegistry registry = getCacheFactoryRegistry();
      StatefulCacheFactory<StatefulBeanContext> factory = registry.getCacheFactory(cacheAnnotation.value());
      this.cache = factory.createCache(getName(), this, this, cacheConfig);
      this.cache.start();
   }
   
   @Override
   protected void lockedStart() throws Exception
   {
      try
      {
         super.lockedStart();
         this.createAndStartCache();
      }
      catch (Exception e)
      {
         try
         {
            this.lockedStop();
         }
         catch (Exception ignore)
         {
            log.debug("Failed to cleanup after start() failure", ignore);
         }
         throw e;
      }

   }

   @Override
   protected void lockedStop() throws Exception
   {
      if (cache != null) cache.stop();
      
      super.lockedStop();
   }

   public Cache<StatefulBeanContext> getCache()
   {
      // Ensure initialized
      try{
         this.createAndStartCache();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      
      // Return
      return cache;
   }
   
   public CacheFactoryRegistry getCacheFactoryRegistry()
   {
      return this.getDeployment().getCacheFactoryRegistry();
   }
   
   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   protected String getJndiRegistrarBindName()
   {
      return isClustered() ? ClusteredObjectStoreBindings.CLUSTERED_OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SFSB
                           : ObjectStoreBindings.OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SFSB;
   }

   /**
    * Performs a synchronous local invocation
    */
   public Object localInvoke(Object id, Method method, Object[] args)
           throws Throwable
   {
      return localInvoke(id, method, args, null);
   }

   /**
    * Performs a synchronous or asynchronous local invocation
    *
    */
   public Object localHomeInvoke(Method method, Object[] args) throws Throwable
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         long hash = MethodHashing.calculateHash(method);
         MethodInfo info = getAdvisor().getMethodInfo(hash);
         if (info == null)
         {
            throw new RuntimeException(
                    "Could not resolve beanClass method from proxy call: "
                            + method.toString());
         }
         return invokeLocalHomeMethod(info, args);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   /**
    * Performs a synchronous or asynchronous local invocation
    *
    * @param provider If null a synchronous invocation, otherwise an asynchronous
    */
   public Object localInvoke(Object id, Method method, Object[] args,
         FutureHolder provider) throws Throwable
   {
      long start = System.currentTimeMillis();

      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         long hash = MethodHashing.calculateHash(method);
         MethodInfo info = getAdvisor().getMethodInfo(hash);
         if (info == null)
         {
            throw new RuntimeException(
                  "Could not resolve beanClass method from proxy call: "
                  + method.toString());
         }
      
         Method unadvisedMethod = info.getUnadvisedMethod();
      
         try
         {
            invokeStats.callIn();
      
            if (unadvisedMethod != null && isHomeMethod(unadvisedMethod))
            {
               return invokeLocalHomeMethod(info, args);
            }
            else if (unadvisedMethod != null
                  && isEJBObjectMethod(unadvisedMethod))
            {
               return invokeEJBLocalObjectMethod(id, info, args);
            }
            
            SerializableMethod invoked = new SerializableMethod(method, method.getClass());
            
            StatefulContainerInvocation nextInvocation = new StatefulContainerInvocation(info, id);
            //StatefulSessionContainerMethodInvocation nextInvocation = new StatefulSessionContainerMethodInvocation(info);
            //nextInvocation.setSessionId(id);
            nextInvocation.setAdvisor(getAdvisor());
            nextInvocation.setArguments(args);
            
            ProxyUtils.addLocalAsynchronousInfo(nextInvocation, provider);
            
            //invokedMethod.push(invoked);
            return nextInvocation.invokeNext();
         }
         finally
         {
            if (unadvisedMethod != null)
            {
               long end = System.currentTimeMillis();
               long elapsed = end - start;
               invokeStats.updateStats(unadvisedMethod, elapsed);
            }
         
            invokeStats.callOut();
            
            //invokedMethod.pop();
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }
   
   /**
    * Create a stateful bean and return its oid.
    *
    * @return
    */
   public Serializable createSession(Class<?>[] initTypes, Object[] initValues)
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         Cache<StatefulBeanContext> cache = this.getCache();
         Object id = cache.create(initTypes, initValues);
         assert id instanceof Serializable : "SFSB Session IDs must be " + Serializable.class.getSimpleName();
         return (Serializable) id;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   protected void destroySession(Object id)
   {
      getCache().remove(id);
   }
   
   /**
    * Remote Invocation entry point, as delegated from
    * InvokableContextClassProxyHack (Remoting Dispatcher)
    */
   @Override
   public InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable
   {
      /*
       * Initialize
       */

      // Mark the start time
      long start = System.currentTimeMillis();

      // Create a pointer to a new Invocation
      StatefulContainerInvocation newSi = null;

      // Create a pointer to the response we'll return
      InvocationResponse response = null;

      // Create a pointer to the Session ID
      Serializable sessionId = null;

      /*
       * Setup Environment (Stack/Thread)
       */

      // Hold a reference to the existing TCL
      ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();

      // Set the Container's CL as TCL, required to unmarshall methods from the bean impl class
      Thread.currentThread().setContextClassLoader(this.getClassloader());

      try
      {

         /*
          * Obtain the target method (unmarshall from invocation)
          */

         // Cast
         assert invocation instanceof StatefulRemoteInvocation : SessionContainer.class.getName()
               + ".dynamicInoke supports only " + StatefulRemoteInvocation.class.getSimpleName()
               + ", but has been passed: " + invocation;
         StatefulRemoteInvocation si = (StatefulRemoteInvocation) invocation;

         // Get the method hash
         long methodHash = si.getMethodHash();
         log.debug("Received dynamic invocation for method with hash: " + methodHash);

         // Get the Method via MethodInfo from the Advisor
         Advisor advisor = this.getAdvisor();
         MethodInfo info = advisor.getMethodInfo(methodHash);
         Method unadvisedMethod = info.getMethod();
         SerializableMethod unadvisedMethodSerializable = new SerializableMethod(unadvisedMethod);
         
         // Get the invoked method from invocation metadata
         Object objInvokedMethod = si.getMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,
               SessionSpecRemotingMetadata.KEY_INVOKED_METHOD);
         assert objInvokedMethod != null : "Invoked Method must be set on invocation metadata";
         assert objInvokedMethod instanceof SerializableMethod : "Invoked Method set on invocation metadata is not of type "
               + SerializableMethod.class.getName() + ", instead: " + objInvokedMethod;
         SerializableMethod invokedMethod = (SerializableMethod) objInvokedMethod;
         
         /*
          * Set the invoked method
          */
         //TODO Remove when CurrentInvocation is ironed out
         
         // Set onto stack
         SessionSpecContainer.invokedMethod.push(invokedMethod);

         try
         {

            // Increment invocation statistics
            invokeStats.callIn();

            /*
             * Obtain Session ID
             */

            // Obtain the Session ID
            Object objSessionId = si.getMetaData(StatefulSessionRemotingMetadata.TAG_SFSB_INVOCATION,
                  StatefulSessionRemotingMetadata.KEY_SESSION_ID);
            if (objSessionId != null)
            {
               assert objSessionId instanceof Serializable : "Session IDs must be "
                     + Serializable.class.getSimpleName();
               sessionId = (Serializable) objSessionId;
            }

            if (info != null && unadvisedMethod != null && isHomeMethod(unadvisedMethodSerializable))
            {
               response = invokeHomeMethod(info, si);
            }
            else if (info != null && unadvisedMethod != null && isEjbObjectMethod(unadvisedMethodSerializable))
            {
               response = invokeEJBObjectMethod(invokedMethod, si);
            }
            else
            {
               if (unadvisedMethod.isBridge())
               {
                  unadvisedMethod = this.getNonBridgeMethod(unadvisedMethod);
                  info = super.getMethodInfo(unadvisedMethod);
               }

               if (sessionId == null)
               {
                  Object objNewId = getCache().create(null, null);
                  assert objNewId instanceof Serializable : "Obtained new Session ID from cache, " + objNewId
                        + ", which is not " + Serializable.class.getSimpleName();
                  sessionId = (Serializable) objNewId;
               }
               
               /*
                * Build a new Invocation
                */

               // Construct the invocation
               newSi = new StatefulContainerInvocation(info, sessionId);
               //newSi = new StatefulContainerInvocation(info.getInterceptors(), long methodHash, Method advisedMethod, Method unadvisedMethod, Advisor advisor, Object id);
               newSi.setArguments(si.getArguments());
               newSi.setMetaData(si.getMetaData());
               newSi.getMetaData().addMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,
                     SessionSpecRemotingMetadata.KEY_INVOKED_METHOD, invokedMethod, PayloadKey.AS_IS);
               
               //newSi.setAdvisor(getAdvisor());
               
               /*
                * Ensure ID exists (useful for catching problems while we have context as
                * to the caller, whereas in Interceptors we do not)
                */
               try
               {
                  this.getCache().get(sessionId);
               }
               catch(NoSuchEJBException nsee)
               {
                  throw this.constructProperNoSuchEjbException(nsee, invokedMethod.getActualClassName()); 
               }

               /*
                * Perform Invocation
                */
               
               // Create an object to hold the return value
               Object returnValue = null;

               // Invoke
               returnValue = newSi.invokeNext();

               // Marshall the response
               response = marshallResponse(invocation, returnValue, newSi.getResponseContextInfo());
               if (sessionId != null)
               {
                  response.addAttachment(StatefulConstants.NEW_ID, sessionId);
               }
               
//               response = marshallResponse(invocation, rtn, newSi.getResponseContextInfo());
//               if (newId != null) response.addAttachment(StatefulConstants.NEW_ID, newId);
               
               // Create a Response
//             response = new InvocationResponse(returnValue);
//             Map<Object, Object> responseContext = newSi.getResponseContextInfo();
//             response.setContextInfo(responseContext);
            }
         }
         catch (Throwable t)
         {
            Throwable exception = t;
//            if (sessionId != null)
//            {
//               exception = new ForwardId(t, sessionId);
//            }
            Map<Object, Object> responseContext = null;
            if (newSi != null)
            {
               responseContext = newSi.getResponseContextInfo();
            }
            response = marshallException(invocation, exception, responseContext);
            return response;
         }
         finally
         {
            /*
             * Update Invocation Statistics
             */
            if (unadvisedMethod != null)
            {
               // Mark end time
               long end = System.currentTimeMillis();

               // Calculate elapsed time
               long elapsed = end - start;

               // Update statistics with elapsed time
               invokeStats.updateStats(unadvisedMethod, elapsed);
            }

            // Complete call to increment statistics
            invokeStats.callOut();

         }

         // Return
         return response;
      }
      finally
      {
         
         // Pop invoked method off the stack
         //TODO Remove when CurrentInvocation handles this
         SessionSpecContainer.invokedMethod.pop();
         
         // Reset the TCL to original
         Thread.currentThread().setContextClassLoader(originalLoader);;
      }
   }
   
   public TimerService getTimerService()
   {
      throw new UnsupportedOperationException("stateful bean doesn't support TimerService (EJB3 18.2#2)");
   }

   public TimerService getTimerService(Object pKey)
   {
      return getTimerService();
   }
   
   @Override
   public void invokePostActivate(BeanContext beanContext)
   {
      for (Injector injector : injectors)
      {
         if (injector instanceof JndiPropertyInjector)
         {
            AccessibleObject field = ((JndiPropertyInjector) injector).getAccessibleObject();
            
            if (field.isAnnotationPresent(javax.ejb.EJB.class))
            {
               continue; // skip nested EJB injection since the local proxy will be (de)serialized correctly
            }
            
            if (field instanceof Field)
            {
               // reinject transient fields
               if ((((Field)field).getModifiers() & Modifier.TRANSIENT) > 0)
                  injector.inject(beanContext);
            }
         }
      }
      
      this.invokeCallback(beanContext, PostActivate.class);
   }

   @Override
   public void invokePrePassivate(BeanContext beanContext)
   {
      this.invokeCallback(beanContext, PrePassivate.class);
   }

   /*
   @Override
   protected Class[] getHandledCallbacks()
   {
      return new Class[]
              {PostConstruct.class, PreDestroy.class, PostActivate.class,
                      PrePassivate.class};
   }
   */

   public void invokeInit(Object bean, Class[] initParameterTypes,
         Object[] initParameterValues)
   {
      invokeInit(bean, bean.getClass(), initParameterTypes, initParameterValues);
   }
   
   private void invokeInit(Object bean, Class<?> cls, Class<?>[] initParameterTypes,
                          Object[] initParameterValues)
   {
      Class<?> superclass = cls.getSuperclass();
      if(superclass != null)
         invokeInit(bean, superclass, initParameterTypes, initParameterValues);
      int numParameters = 0;
      if(initParameterTypes != null)
         numParameters = initParameterTypes.length;
      try
      {
         for(Method method : cls.getDeclaredMethods())
         {
            if(numParameters != method.getParameterTypes().length)
               continue;
            
            if ((method.getAnnotation(Init.class) != null)
                    || (resolveAnnotation(method, Init.class) != null))
            {
               if(initParameterTypes != null)
               {
                  Object[] parameters = getInitParameters(method,
                          initParameterTypes, initParameterValues);
   
                  if (parameters != null)
                     method.invoke(bean, parameters);
               }
               else
               {
                  method.invoke(bean);
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected Object[] getInitParameters(Method method,
                                        Class[] initParameterTypes, Object[] initParameterValues)
   {
      if (method.getParameterTypes().length == initParameterTypes.length)
      {
         for (int i = 0; i < initParameterTypes.length; ++i)
         {
            Class formal = method.getParameterTypes()[i];
            Class actual = initParameterTypes[i];
            if (!isMethodInvocationConvertible(formal, actual == null ? null
                    : actual))
               return null;
         }
         return initParameterValues;
      }
      return null;
   }

   /**
    * Determines whether a type represented by a class object is convertible to
    * another type represented by a class object using a method invocation
    * conversion, treating object types of primitive types as if they were
    * primitive types (that is, a Boolean actual parameter type matches boolean
    * primitive formal type). This behavior is because this method is used to
    * determine applicable methods for an actual parameter list, and primitive
    * types are represented by their object duals in reflective method calls.
    *
    * @param formal the formal parameter type to which the actual parameter type
    *               should be convertible
    * @param actual the actual parameter type.
    * @return true if either formal type is assignable from actual type, or
    *         formal is a primitive type and actual is its corresponding object
    *         type or an object type of a primitive type that can be converted
    *         to the formal type.
    */
   private static boolean isMethodInvocationConvertible(Class formal,
                                                        Class actual)
   {
      /*
       * if it's a null, it means the arg was null
       */
      if (actual == null && !formal.isPrimitive())
      {
         return true;
      }
      /*
       * Check for identity or widening reference conversion
       */
      if (actual != null && formal.isAssignableFrom(actual))
      {
         return true;
      }
      /*
       * Check for boxing with widening primitive conversion. Note that actual
       * parameters are never primitives.
       */
      if (formal.isPrimitive())
      {
         if (formal == Boolean.TYPE && actual == Boolean.class)
            return true;
         if (formal == Character.TYPE && actual == Character.class)
            return true;
         if (formal == Byte.TYPE && actual == Byte.class)
            return true;
         if (formal == Short.TYPE
                 && (actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Integer.TYPE
                 && (actual == Integer.class || actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Long.TYPE
                 && (actual == Long.class || actual == Integer.class
                 || actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Float.TYPE
                 && (actual == Float.class || actual == Long.class
                 || actual == Integer.class || actual == Short.class || actual == Byte.class))
            return true;
         if (formal == Double.TYPE
                 && (actual == Double.class || actual == Float.class
                 || actual == Long.class || actual == Integer.class
                 || actual == Short.class || actual == Byte.class))
            return true;
      }
      return false;
   }

   private Object invokeEJBLocalObjectMethod(Object id, MethodInfo info,
                                             Object[] args) throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("remove"))
      {
         try
         {
            destroySession(id);
         }
         catch(NoSuchEJBException e)
         {
            throw new NoSuchObjectLocalException(e.getMessage(), e);
         }

         return null;
      }
      else if (unadvisedMethod.getName().equals("getEJBLocalHome"))
      {
         Object bean = getCache().get(id).getInstance();

         return bean;
      }
      else if (unadvisedMethod.getName().equals("getPrimaryKey"))
      {
         return id;
      }
      else if (unadvisedMethod.getName().equals("isIdentical"))
      {
         EJBObject bean = (EJBObject) args[0];

         Object primaryKey = bean.getPrimaryKey();

         boolean isIdentical = id.equals(primaryKey);

         return isIdentical;
      }
      else
      {
         return null;
      }
   }

   private Object invokeLocalHomeMethod(MethodInfo info, Object[] args)
           throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().startsWith("create"))
      {
         Class<?>[] initParameterTypes =
                 {};
         Object[] initParameterValues =
                 {};
         if (unadvisedMethod.getParameterTypes().length > 0)
         {
            initParameterTypes = unadvisedMethod.getParameterTypes();
            initParameterValues = args;
         }

         LocalBinding binding = this.getAnnotation(LocalBinding.class);

         StatefulLocalProxyFactory factory = new StatefulLocalProxyFactory(this, binding);
         factory.init();

         Object proxy = factory.createProxyEjb21(initParameterTypes,
                 initParameterValues, unadvisedMethod.getReturnType().getName());

         return proxy;
      }
      else if (unadvisedMethod.getName().equals("remove"))
      {
         remove(args[0]);

         return null;
      }
      else
      {
         return null;
      }
   }
   
   public Object createLocalProxy(Object id) throws Exception
   {
      return this.createLocalProxy(id, this.getAnnotation(LocalBinding.class));
   }
   
   public Object createLocalProxy(Object id, LocalBinding binding) throws Exception
   {
      StatefulLocalProxyFactory factory = new StatefulLocalProxyFactory(this, binding);
      factory.init();

      return factory.createProxyBusiness(id);
   }
   
   public Object createRemoteProxy(Object id, RemoteBinding binding) throws Exception
   {
      StatefulRemoteProxyFactory factory = new StatefulRemoteProxyFactory(this, binding);
      factory.init();

      if (id != null)
         return factory.createProxyBusiness(id,null);
      else
         return factory.createProxyBusiness();
   }
   
   public boolean isClustered()
   {
      return isAnnotationPresent(Clustered.class);
   }

   @Override
   protected Object invokeHomeCreate(Method method, Object[] args) throws Exception
   {
      // TODO: this is almost identical to SessionSpecContainer.invokeHomeCreate, so unify
      
      // Hold the JNDI Name
      String jndiName = null;

      // Flag for if we've found the interface
      boolean foundInterface = false;

      // Name of the EJB2.x Interface Class expected
      String ejb2xInterface = method.getReturnType().getName();

      // Get Metadata
      JBossSessionBeanMetaData smd = this.getMetaData();

      /*
       * Determine if the expected type is found in metadata as a EJB2.x Interface 
       */

      // Is this a Remote Interface ?
      boolean isLocal = false;
      String ejb2xRemoteInterface = smd.getRemote();
      if (ejb2xInterface.equals(ejb2xRemoteInterface))
      {
         // We've found it, it's false
         foundInterface = true;
         jndiName = smd.getJndiName();
      }

      // Is this a local interface?
      if (!foundInterface)
      {
         String ejb2xLocalInterface = smd.getLocal();
         if (ejb2xInterface.equals(ejb2xLocalInterface))
         {
            // Mark as found
            foundInterface = true;
            isLocal = true;
            jndiName = smd.getLocalJndiName();
         }
      }

      // If we haven't yet found the interface
      if (!foundInterface)
      {
         throw new RuntimeException("Specified return value for " + method + " notes an EJB 2.x interface: "
               + ejb2xInterface + "; this could not be found as either a valid remote or local interface for EJB "
               + this.getEjbName());
      }
      
      // Lookup
      String proxyFactoryKey = this.getJndiRegistrar().getProxyFactoryRegistryKey(jndiName, smd, isLocal);
      Object factory = Ejb3RegistrarLocator.locateRegistrar().lookup(proxyFactoryKey);
      
      // Cast
      assert factory instanceof StatefulSessionProxyFactory : "Specified factory " + factory.getClass().getName() + " is not of type "
         + StatefulSessionProxyFactory.class.getName() + " as required by " + StatefulContainer.class.getName() + ", but was instead " + factory;
      StatefulSessionProxyFactory statefulFactory = null;
      statefulFactory = StatefulSessionProxyFactory.class.cast(factory);
      
      Serializable sessionId = createSession(method.getParameterTypes(), args);
      Object proxy = statefulFactory.createProxyEjb2x(sessionId);
      
      return proxy;
   }
   
   protected InvocationResponse invokeHomeMethod(MethodInfo info,
         StatefulRemoteInvocation statefulInvocation) throws Throwable
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().startsWith("create"))
      {
         Class<?>[] initParameterTypes =
                 {};
         Object[] initParameterValues =
                 {};
         if (unadvisedMethod.getParameterTypes().length > 0)
         {
            initParameterTypes = unadvisedMethod.getParameterTypes();
            initParameterValues = statefulInvocation.getArguments();
         }

         StatefulSessionContainerMethodInvocation newStatefulInvocation = buildNewInvocation(
                 info, statefulInvocation, initParameterTypes,
                 initParameterValues);

         // Get JNDI Registrar
         JndiSessionRegistrarBase sfsbJndiRegistrar = this.getJndiRegistrar();
         
         // Determine if local/remote
         boolean isLocal = EJBLocalObject.class.isAssignableFrom(unadvisedMethod.getDeclaringClass());

         // Get the metadata
         JBossSessionBeanMetaData smd = this.getMetaData();

         // Get the appropriate JNDI Name
         String jndiName = isLocal ? smd.getLocalJndiName() : smd.getJndiName();

         // Find the Proxy Factory Key for this SFSB
         String proxyFactoryKey = sfsbJndiRegistrar.getProxyFactoryRegistryKey(jndiName, smd, isLocal);

         // Lookup the Proxy Factory in the Object Store
         StatefulSessionProxyFactory proxyFactory = Ejb3RegistrarLocator.locateRegistrar().lookup(proxyFactoryKey,
               StatefulSessionProxyFactory.class);
         
         // Create a new EJB2.x Proxy
         Object proxy = proxyFactory.createProxyEjb2x((Serializable)newStatefulInvocation.getSessionId());
         
         InvocationResponse response = marshallResponse(statefulInvocation, proxy, newStatefulInvocation.getResponseContextInfo());
         if (newStatefulInvocation.getSessionId() != null)
            response.addAttachment(StatefulConstants.NEW_ID,
                    newStatefulInvocation.getSessionId());
         return response;
      }
      else if (unadvisedMethod.getName().equals("remove"))
      {
         Object argument = statefulInvocation.getArguments()[0];
         remove(argument);

         InvocationResponse response = new InvocationResponse(null);
         response.setContextInfo(statefulInvocation.getResponseContextInfo());
         return response;
      }
      else if (unadvisedMethod.getName().equals("getEJBMetaData"))
      {
         Class<?> remote = null;
         Class<?> home = null;
         Class<?> pkClass = Object.class;
         HomeHandleImpl homeHandle = null;

         Class<?>[] remotes = ProxyFactoryHelper.getRemoteInterfaces(this);
         if (remotes != null && remotes.length > 0)
         {
            remote = remotes[0];
         }
         RemoteHome homeAnnotation = this.getAnnotation(RemoteHome.class);
         if (homeAnnotation != null)
            home = homeAnnotation.value();
         
         RemoteHomeBinding remoteHomeBinding = this.getAnnotation(RemoteHomeBinding.class);
         assert remoteHomeBinding != null : "remoteHomeBinding is null";
         homeHandle = new HomeHandleImpl(remoteHomeBinding.jndiBinding());
         
         EJBMetaDataImpl metadata = new EJBMetaDataImpl(remote, home, pkClass,
                 true, false, homeHandle);

         InvocationResponse response = marshallResponse(statefulInvocation, metadata, null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("getHomeHandle"))
      {
         HomeHandleImpl homeHandle = null;

         RemoteHomeBinding remoteHomeBinding = this.getAnnotation(RemoteHomeBinding.class);
         assert remoteHomeBinding != null : "remoteHomeBinding is null";
         homeHandle = new HomeHandleImpl(remoteHomeBinding.jndiBinding());
         
         InvocationResponse response = marshallResponse(statefulInvocation, homeHandle, null);
         return response;
      }
      else
      {
         return null;
      }
   }

   protected InvocationResponse invokeEJBObjectMethod(SerializableMethod method,
         StatefulRemoteInvocation statefulInvocation) throws Throwable
   {
      // Initialize
      ClassLoader cl = this.getClassloader();
      
      // Obtain actual method
      Method actualMethod = method.toMethod(cl);
      long hash = MethodHashing.calculateHash(actualMethod);
      MethodInfo info = this.getAdvisor().getMethodInfo(hash);
      Method unadvisedMethod = info.getUnadvisedMethod();
      
      if (unadvisedMethod.getName().equals("getHandle"))
      {
         StatefulContainerInvocation newStatefulInvocation = buildInvocation(
                 info, statefulInvocation);
         
         // Get JNDI Registrar
         JndiSessionRegistrarBase sfsbJndiRegistrar = this.getJndiRegistrar();

         // Determine if local/remote
         boolean isLocal = EJBLocalObject.class.isAssignableFrom(unadvisedMethod.getDeclaringClass());
         
         // Get the metadata
         JBossSessionBeanMetaData smd = this.getMetaData();

         // Get the appropriate JNDI Name
         String jndiName = isLocal ? smd.getLocalJndiName() : smd.getJndiName();
         
         // Find the Proxy Factory Key for this SFSB
         String proxyFactoryKey = sfsbJndiRegistrar.getProxyFactoryRegistryKey(jndiName, smd, isLocal);

         // Lookup the Proxy Factory in the Object Store
         StatefulSessionProxyFactory proxyFactory = Ejb3RegistrarLocator.locateRegistrar().lookup(proxyFactoryKey,
               StatefulSessionProxyFactory.class);
         
         // Create a new EJB2.x Proxy
         EJBObject proxy = (EJBObject)proxyFactory.createProxyEjb2x((Serializable)newStatefulInvocation.getId());
         
         StatefulHandleRemoteImpl handle = new StatefulHandleRemoteImpl(proxy);
         InvocationResponse response = marshallResponse(statefulInvocation, handle, null);
         return response;
      }
      
      // SFSB remove()
      else if (unadvisedMethod.getName().equals(Ejb2xMethodNames.METHOD_NAME_HOME_REMOVE))
      {
         try
         {
            // Attempt to remove the bean
            destroySession(statefulInvocation.getId());
         }
         catch (NoSuchEJBException e)
         {
            String invokingClassName = method.getActualClassName();
            Throwable newException = this.constructProperNoSuchEjbException(e, invokingClassName);
            throw newException;
         }

         InvocationResponse response = new InvocationResponse(null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("getEJBHome"))
      {
         HomeHandleImpl homeHandle = null;

         RemoteBinding remoteBindingAnnotation = this.getAnnotation(RemoteBinding.class);
         if (remoteBindingAnnotation != null)
            homeHandle = new HomeHandleImpl(ProxyFactoryHelper.getHomeJndiName(this));

         EJBHome ejbHome = homeHandle.getEJBHome();

         InvocationResponse response = marshallResponse(statefulInvocation, ejbHome, null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("getPrimaryKey"))
      {
         Object id = statefulInvocation.getId();

         InvocationResponse response = marshallResponse(statefulInvocation, id, null);
         return response;
      }
      else if (unadvisedMethod.getName().equals("isIdentical"))
      {
         Object id = statefulInvocation.getId();
         EJBObject bean = (EJBObject) statefulInvocation.getArguments()[0];

         Object primaryKey = bean.getPrimaryKey();

         boolean isIdentical = id.equals(primaryKey);

         InvocationResponse response = marshallResponse(statefulInvocation, isIdentical, null);
         return response;
      }
      else
      {
         return null;
      }
   }

   private StatefulSessionContainerMethodInvocation buildNewInvocation(MethodInfo info,
         StatefulRemoteInvocation statefulInvocation, Class<?>[] initParameterTypes,
         Object[] initParameterValues)
   {
      StatefulSessionContainerMethodInvocation newStatefulInvocation = null;

      Object id = null;
      StatefulBeanContext ctx = null;
      
      // ENC is required in scope to create a session
      this.pushEnc();
      
      try
      {
         if (initParameterTypes.length > 0)
            id = getCache().create(initParameterTypes, initParameterValues);
         else
            id = getCache().create(null, null);
         
         ctx = getCache().get(id);
      }
      finally
      {
         // Pop the ENC off the stack
         this.popEnc();
      }

      Object newId = ctx.getId();
      newStatefulInvocation = new StatefulSessionContainerMethodInvocation(info);
      newStatefulInvocation.setSessionId(newId);

      newStatefulInvocation.setArguments(statefulInvocation.getArguments());
      newStatefulInvocation.setMetaData(statefulInvocation.getMetaData());
      newStatefulInvocation.setAdvisor(getAdvisor());
      
      SerializableMethod invokedMethod = new SerializableMethod(info.getUnadvisedMethod());
      newStatefulInvocation.getMetaData().addMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,
            SessionSpecRemotingMetadata.KEY_INVOKED_METHOD, invokedMethod, PayloadKey.AS_IS);

      return newStatefulInvocation;
   }

   private StatefulContainerInvocation buildInvocation(MethodInfo info,
                                                       StatefulRemoteInvocation statefulInvocation)
   {
      StatefulContainerInvocation newStatefulInvocation = null;
      Object newId = null;
      if (statefulInvocation.getId() == null)
      {
         newId = getCache().create(null, null);
         // FIXME -- previous create(...) impl would do equiv of a get() as well;
         // do we need to add a cache.get(newId) to have the same behavior?
         newStatefulInvocation = new StatefulContainerInvocation(info, newId);
      }
      else
      {
         newStatefulInvocation = new StatefulContainerInvocation(info, statefulInvocation.getId());
      }

      newStatefulInvocation.setArguments(statefulInvocation.getArguments());
      newStatefulInvocation.setMetaData(statefulInvocation.getMetaData());
      newStatefulInvocation.setAdvisor(getAdvisor());
      
      SerializableMethod invokedMethod = new SerializableMethod(info.getUnadvisedMethod());
      newStatefulInvocation.getMetaData().addMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,
            SessionSpecRemotingMetadata.KEY_INVOKED_METHOD, invokedMethod, PayloadKey.AS_IS);

      return newStatefulInvocation;
   }

   @Override
   public Object getBusinessObject(BeanContext beanContext, Class businessInterface) throws IllegalStateException
   {
      assert beanContext != null : "beanContext is null";
      assert businessInterface != null : "businessInterface is null";
      
      StatefulBeanContext ctx = (StatefulBeanContext) beanContext;
      
      SessionContainer container = ctx.getContainer();
      assert container == this : "beanContext not of this container (" + container + " != " + this + ")";
      
      boolean isRemote = false;
      boolean found = false;
      Class<?>[] remoteInterfaces = ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(this);
      for (Class<?> intf : remoteInterfaces)
      {
         if (intf.getName().equals(businessInterface.getName()))
         {
            isRemote = true;
            found = true;
            break;
         }
      }
      if (found == false)
      {
         Class<?>[] localInterfaces = ProxyFactoryHelper.getLocalAndBusinessLocalInterfaces(this);
         for (Class<?> intf : localInterfaces)
         {
            if (intf.getName().equals(businessInterface.getName()))
            {
               found = true;
               break;
            }
         }
      }
      if (found == false) throw new IllegalStateException(businessInterface.getName() + " is not a business interface for container " + this);
      
      // Obtain SFSB JNDI Registrar
      String sfsbJndiRegistrarObjectStoreBindName = this.getJndiRegistrarBindName();
      JndiStatefulSessionRegistrar sfsbJndiRegistrar = Ejb3RegistrarLocator
            .locateRegistrar().lookup(sfsbJndiRegistrarObjectStoreBindName,JndiStatefulSessionRegistrar.class);
      
      // Get the metadata
      JBossSessionBeanMetaData smd = this.getMetaData();

      // Get the appropriate JNDI Name
      String jndiName = !isRemote ? smd.getLocalJndiName() : smd.getJndiName();
      
      // Find the Proxy Factory Key for this SFSB
      String proxyFactoryKey = sfsbJndiRegistrar.getProxyFactoryRegistryKey(jndiName, smd, !isRemote);
      
      // Lookup the Proxy Factory in the Object Store
      StatefulSessionProxyFactory proxyFactory = Ejb3RegistrarLocator.locateRegistrar().lookup(proxyFactoryKey,
            StatefulSessionProxyFactory.class);
      
      // Create a new business proxy
      Object proxy = proxyFactory.createProxyBusiness((Serializable) ctx.getId(), businessInterface.getName());
      
      // Return the Proxy
      return proxy;

//      Collection<ProxyFactory> proxyFactories = this.proxyDeployer.getProxyFactories().values();
//      for (ProxyFactory factory : proxyFactories)
//      {
//         if (isRemote && factory instanceof StatefulRemoteProxyFactory)
//         {
//            return ((StatefulRemoteProxyFactory) factory).createProxyBusiness(ctx.getId(),null);
//         }
//         else if (!isRemote && factory instanceof StatefulLocalProxyFactory)
//         {
//            return ((StatefulLocalProxyFactory) factory).createProxyBusiness(ctx.getId(),null);
//         }
//      }
//      throw new IllegalStateException("Unable to create proxy for getBusinessObject as a proxy factory was not found");
   }

   /**
    * Remove the given object. Called when remove on Home is invoked.
    * 
    * @param target             either a Handle or a primaryKey
    * @throws RemoveException   if it's not allowed to be removed
    */
   private void remove(Object target) throws RemoveException
   {
      // EJBTHREE-1217: EJBHome.remove(Object primaryKey) must throw RemoveException
      if(!(target instanceof Handle))
         throw new RemoveException("EJB 3 3.6.2.2: Session beans do not have a primary key");
      
      StatefulHandleRemoteImpl handle = (StatefulHandleRemoteImpl) target;

      try
      {
         handle.getEJBObject().remove();
      }
      catch(RemoteException re)
      {
         throw new RemoveException(re.getMessage());
      }
   }
   
   protected void removeHandle(Handle arg) throws Exception
   {
      /*
      StatefulHandleImpl handle = (StatefulHandleImpl) arg;

      destroySession(handle.id);
      */
      arg.getEJBObject().remove();
   }
   

   
   // --------------------------------------------------  PassivationManager

   public void postActivate(StatefulBeanContext ctx)
   {
      ctx.postActivate();
   }

   public void postReplicate(StatefulBeanContext ctx)
   {
      ctx.postReplicate(); 
   }

   public void prePassivate(StatefulBeanContext ctx)
   {
      ctx.prePassivate();
   }

   public void preReplicate(StatefulBeanContext ctx)
   {
      ctx.preReplicate();
   }
}
