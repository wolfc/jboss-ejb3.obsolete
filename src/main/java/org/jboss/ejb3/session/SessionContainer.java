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
package org.jboss.ejb3.session;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.LocalHome;
import javax.ejb.NoSuchEJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.RemoteHome;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Module;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.clustered.objectstore.ClusteredObjectStoreBindings;
import org.jboss.ejb3.proxy.clustered.registry.ProxyClusteringRegistry;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.impl.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.ejb3.remoting.IsLocalInterceptor;
import org.jboss.ejb3.stateful.StatefulContainerInvocation;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.serial.io.MarshalledObjectForLocalCalls;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class SessionContainer extends EJBContainer implements InvokableContext
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(SessionContainer.class);

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private JndiSessionRegistrarBase jndiRegistrar;

   protected ProxyDeployer proxyDeployer;

   private Map<String, HATarget> clusterFamilies;

   /**
    * Returns a remote binding for this container
    * 
    * @deprecated Non-deterministic, more than one binding may be specified 
    * for this container
    * @return
    */
   @Deprecated
   protected RemoteBinding getRemoteBinding()
   {
      RemoteBinding binding = null;
      RemoteBindings bindings = getAnnotation(RemoteBindings.class);
      if (bindings != null)
         binding = bindings.value()[0];
      else
         binding = getAnnotation(RemoteBinding.class);

      return binding;
   }

   public SessionContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
         Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData)
         throws ClassNotFoundException
   {
      super(Ejb3Module.BASE_EJB3_JMX_NAME + ",name=" + ejbName, domain, cl, beanClassName, ejbName, ctxProperties,
            deployment, beanMetaData);
      proxyDeployer = new ProxyDeployer(this);
   }

   protected SessionProxyFactory getProxyFactory(LocalBinding binding)
   {
      assert binding != null : LocalBinding.class.getSimpleName() + " must be specified";

      // Find the jndiName
      String jndiName = this.getMetaData().getLocalJndiName();
      if (binding != null)
      {
         jndiName = binding.jndiBinding();
      }

      // Get the Registry name
      String proxyFactoryRegistryBindName = this.getJndiRegistrar().getProxyFactoryRegistryKey(jndiName,
            this.getMetaData(), true);

      // Return
      return this.getProxyFactory(proxyFactoryRegistryBindName);
   }

   protected SessionProxyFactory getProxyFactory(RemoteBinding binding)
   {
      assert binding != null : RemoteBinding.class.getSimpleName() + " must be specified";

      // Get the Registry name
      String proxyFactoryRegistryBindName = this.getJndiRegistrar().getProxyFactoryRegistryKey(binding.jndiBinding(),
            this.getMetaData(), true);

      // Return
      return this.getProxyFactory(proxyFactoryRegistryBindName);
   }

   /**
    * Obtains the proxy factory bound at the specified registry name
    * 
    * @param proxyFactoryRegistryBindName
    * @return
    */
   protected SessionProxyFactory getProxyFactory(String proxyFactoryRegistryBindName)
   {
      // Lookup
      SessionProxyFactory factory = Ejb3RegistrarLocator.locateRegistrar().lookup(proxyFactoryRegistryBindName,
            SessionProxyFactory.class);

      // Return
      return factory;
   }

   /**
    * Entry point for remoting-based invocations via InvokableContextClassProxyHack
    */
   public abstract InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable;

   public JBossSessionBeanMetaData getMetaData()
   {
      // TODO: resolve this cast using generics on EJBContainer
      return (JBossSessionBeanMetaData) getXml();
   }

   @Override
   public void instantiated()
   {
      super.instantiated();
      proxyDeployer.initializeRemoteBindingMetadata();
      proxyDeployer.initializeLocalBindingMetadata();
   }

   @Override
   protected List<Class<?>> resolveBusinessInterfaces()
   {
      // Obtain all business interfaces
      List<Class<?>> list = new ArrayList<Class<?>>();
      list.addAll(Arrays.asList(ProxyFactoryHelper.getLocalBusinessInterfaces(this)));
      list.addAll(Arrays.asList(ProxyFactoryHelper.getRemoteBusinessInterfaces(this)));

      return list;
   }

   protected void lockedStart() throws Exception
   {
      super.lockedStart();
      this.registerWithAopDispatcher();

      // Obtain registrar
      JndiSessionRegistrarBase registrar = this.getJndiRegistrar();

      // Bind all appropriate references/factories to Global JNDI for Client access, if a JNDI Registrar is present
      if (registrar != null)
      {
         String guid = Ejb3Registry.guid(this);
         registrar.bindEjb(this.getInitialContext(), this.getMetaData(), this.getClassloader(), this.getObjectName()
               .getCanonicalName(), guid, this.getAdvisor());
      }
      else
      {
         log.warn("No " + JndiSessionRegistrarBase.class.getSimpleName()
               + " was found; byassing binding of Proxies to " + this.getName() + " in Global JNDI.");
      }
   }

   /**
    * Registers this Container with Remoting / AOP Dispatcher
    */
   protected void registerWithAopDispatcher()
   {
      String registrationName = this.getObjectName().getCanonicalName();
      ClassProxy classProxy = new InvokableContextClassProxyHack(this);

      // So that Remoting layer can reference this container easily.
      Dispatcher.singleton.registerTarget(registrationName, classProxy);

      // Log
      log.debug("Registered " + this + " with " + Dispatcher.class.getName() + " via "
            + InvokableContextClassProxyHack.class.getSimpleName() + " at key " + registrationName);
   }

   /**
    * This gets called by replicants manager interceptor factory
    * during the initialization of the bean container (during construction of EJBContainer).
    * So we have detached construction here.
    * 
    * @return the cluster families, never null
    */
   public Map<String, HATarget> getClusterFamilies()
   {
      if (clusterFamilies == null)
      {
         Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();
         ProxyClusteringRegistry registry = (ProxyClusteringRegistry) registrar
               .lookup(ClusteredObjectStoreBindings.CLUSTERED_OBJECTSTORE_BEAN_NAME_PROXY_CLUSTERING_REGISTRY);
         clusterFamilies = registry.getHATargets(this.getObjectName().getCanonicalName());
      }
      return clusterFamilies;
   }

   protected void lockedStop() throws Exception
   {

      try
      {
         Dispatcher.singleton.unregisterTarget(getObjectName().getCanonicalName());
      }
      catch (Exception ignore)
      {
         log.debug("Dispatcher unregister target failed", ignore);
      }

      // Deregister with Remoting
      Dispatcher.singleton.unregisterTarget(this.getName());

      // Unbind applicable JNDI Entries
      JndiSessionRegistrarBase jndiRegistrar = this.getJndiRegistrar();
      if (jndiRegistrar != null)
      {
         jndiRegistrar.unbindEjb(this.getInitialContext(), this.getMetaData());
      }

      super.lockedStop();
   }

   @Override
   public List<Method> getVirtualMethods()
   {
      List<Method> virtualMethods = new ArrayList<Method>();
      try
      {
         RemoteHome home = getAnnotation(RemoteHome.class);
         if (home != null)
         {
            Method[] declaredMethods = home.value().getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);

            declaredMethods = javax.ejb.EJBObject.class.getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);
         }

         LocalHome localHome = getAnnotation(LocalHome.class);
         if (localHome != null)
         {
            Method[] declaredMethods = localHome.value().getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);

            declaredMethods = javax.ejb.EJBLocalObject.class.getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);
         }
      }
      catch (SecurityException e)
      {
         // TODO: privileged?
         throw new RuntimeException(e);
      }
      return virtualMethods;
   }

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   protected abstract String getJndiRegistrarBindName();

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the JndiSessionRegistrarBase from MC, null if not found
    * 
    * @return
    */
   protected JndiSessionRegistrarBase getJndiRegistrar()
   {
      // If defined already, use it
      if (this.jndiRegistrar != null)
      {
         return this.jndiRegistrar;
      }

      // Initialize
      String jndiRegistrarBindName = this.getJndiRegistrarBindName();

      // Obtain Registrar
      Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();

      // Lookup
      Object obj = null;
      try
      {
         obj = registrar.lookup(jndiRegistrarBindName);
         this.setJndiRegistrar(jndiRegistrar);
      }
      // If not installed, warn and return null
      catch (NotBoundException e)
      {
         log.warn("No " + JndiSessionRegistrarBase.class.getName()
               + " was found installed in the ObjectStore (Registry) at " + jndiRegistrarBindName);
         return null;

      }

      // Cast
      JndiSessionRegistrarBase jndiRegistrar = (JndiSessionRegistrarBase) obj;

      // Return
      return jndiRegistrar;
   }

   public void setJndiRegistrar(JndiSessionRegistrarBase jndiRegistrar)
   {
      this.jndiRegistrar = jndiRegistrar;
   }

   //   /**
   //    * Obtains a List of all methods handled by the bean class
   //    * 
   //    * @return The methods handled by the bean class directly
   //    */
   //   @Override
   //   //FIXME: Should be adapted to use metadata view from metadata bridge
   //   // such that *-aop.xml annotations may be included
   //   public List<Method> getVirtualMethods()
   //   {
   //      // Initialize
   //      List<Method> virtualMethods = new ArrayList<Method>();
   //
   //      // Obtain Metadata
   //      JBossSessionBeanMetaData smd = this.getMetaData();
   //
   //      // Obtain CL
   //      ClassLoader cl = this.getClassloader();
   //
   //      /*
   //       * Business Remotes
   //       */
   //
   //      // Obtain all specified business remotes
   //      BusinessRemotesMetaData businessRemotes = smd.getBusinessRemotes();
   //      if (businessRemotes != null)
   //      {
   //         // For each business remote
   //         for (String businessRemote : businessRemotes)
   //         {
   //            // Load the Class
   //            Class<?> businessRemoteClass = null;
   //            try
   //            {
   //               businessRemoteClass = Class.forName(businessRemote, true, cl);
   //            }
   //            catch (ClassNotFoundException e)
   //            {
   //               throw new RuntimeException("Could not find specified business remote class: " + businessRemote, e);
   //            }
   //
   //            // Obtain all methods declared by the class
   //            Method[] declaredMethods = businessRemoteClass.getMethods();
   //
   //            // Add each method
   //            for (Method declaredMethod : declaredMethods)
   //            {
   //               virtualMethods.add(declaredMethod);
   //            }
   //         }
   //      }
   //
   //      /*
   //       * Business Locals
   //       */
   //
   //      // Obtain all specified business locals
   //      BusinessLocalsMetaData businessLocals = smd.getBusinessLocals();
   //      if (businessLocals != null)
   //      {
   //         // For each business local
   //         for (String businessLocal : businessLocals)
   //         {
   //            // Load the Class
   //            Class<?> businessLocalClass = null;
   //            try
   //            {
   //               businessLocalClass = Class.forName(businessLocal, true, cl);
   //            }
   //            catch (ClassNotFoundException e)
   //            {
   //               throw new RuntimeException("Could not find specified business local class: " + businessLocal, e);
   //            }
   //
   //            // Obtain all methods declared by the class
   //            Method[] declaredMethods = businessLocalClass.getMethods();
   //
   //            // Add each method
   //            for (Method declaredMethod : declaredMethods)
   //            {
   //               virtualMethods.add(declaredMethod);
   //            }
   //         }
   //      }
   //
   //      // Remote Home
   //      String remoteHomeClassName = smd.getHome();
   //      if (remoteHomeClassName != null)
   //      {
   //         Class<?> remoteHomeClass = null;
   //         try
   //         {
   //            remoteHomeClass = Class.forName(remoteHomeClassName, true, cl);
   //         }
   //         catch (ClassNotFoundException e)
   //         {
   //            throw new RuntimeException("Could not find specified Remote Home Class: " + remoteHomeClassName, e);
   //         }
   //         if (remoteHomeClass != null)
   //         {
   //            Method[] declaredMethods = remoteHomeClass.getMethods();
   //            for (Method declaredMethod : declaredMethods)
   //               virtualMethods.add(declaredMethod);
   //
   //            declaredMethods = javax.ejb.EJBObject.class.getMethods();
   //            for (Method declaredMethod : declaredMethods)
   //               virtualMethods.add(declaredMethod);
   //         }
   //      }
   //
   //      // Local Home
   //      String localHomeClassName = smd.getLocalHome();
   //      if (localHomeClassName != null)
   //      {
   //         Class<?> localHomeClass = null;
   //         try
   //         {
   //            localHomeClass = Class.forName(localHomeClassName, true, cl);
   //         }
   //         catch (ClassNotFoundException e)
   //         {
   //            throw new RuntimeException("Could not find specified Local Home Class: " + localHomeClass, e);
   //         }
   //         if (localHomeClass != null)
   //         {
   //            Method[] declaredMethods = localHomeClass.getMethods();
   //            for (Method declaredMethod : declaredMethods)
   //               virtualMethods.add(declaredMethod);
   //
   //            declaredMethods = javax.ejb.EJBLocalObject.class.getMethods();
   //            for (Method declaredMethod : declaredMethods)
   //               virtualMethods.add(declaredMethod);
   //         }
   //      }
   //      
   //      log.debug("Found virtual methods: ");
   //      for(Method m : virtualMethods)
   //      {
   //         log.debug("\t" + m + " - " + MethodHashing.calculateHash(m));
   //      }
   //      
   //
   //      return virtualMethods;
   //   }

   /*
   protected void createMethodMap()
   {
      super.createMethodMap();
      try
      {
         RemoteHome home = (RemoteHome) resolveAnnotation(RemoteHome.class);
         if (home != null)
         {
            Method[] declaredMethods = home.value().getMethods();
            for (int i = 0; i < declaredMethods.length; i++)
            {
               long hash = MethodHashing.methodHash(declaredMethods[i]);
               advisedMethods.put(hash, declaredMethods[i]);
            }

            declaredMethods = javax.ejb.EJBObject.class.getMethods();
            for (int i = 0; i < declaredMethods.length; i++)
            {
               long hash = MethodHashing.methodHash(declaredMethods[i]);
               advisedMethods.put(hash, declaredMethods[i]);
            }
         }

         LocalHome localHome = (LocalHome) resolveAnnotation(LocalHome.class);
         if (localHome != null)
         {
            Method[] declaredMethods = localHome.value().getMethods();
            for (int i = 0; i < declaredMethods.length; i++)
            {
               long hash = MethodHashing.methodHash(declaredMethods[i]);
               advisedMethods.put(hash, declaredMethods[i]);
            }

            declaredMethods = javax.ejb.EJBLocalObject.class.getMethods();
            for (int i = 0; i < declaredMethods.length; i++)
            {
               long hash = MethodHashing.methodHash(declaredMethods[i]);
               advisedMethods.put(hash, declaredMethods[i]);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   */

   public static InvocationResponse marshallException(Invocation invocation, Throwable exception, Map responseContext)
         throws Throwable
   {
      if (invocation.getMetaData(IsLocalInterceptor.IS_LOCAL, IsLocalInterceptor.IS_LOCAL) == null)
         throw exception;

      InvocationResponse response = new InvocationResponse();
      response.setContextInfo(responseContext);

      response.addAttachment(IsLocalInterceptor.IS_LOCAL_EXCEPTION, new MarshalledObjectForLocalCalls(exception));

      return response;
   }

   public static InvocationResponse marshallResponse(Invocation invocation, Object rtn, Map responseContext)
         throws java.io.IOException
   {
      InvocationResponse response;
      // marshall return value
      if (rtn != null && invocation.getMetaData(IsLocalInterceptor.IS_LOCAL, IsLocalInterceptor.IS_LOCAL) != null)
      {
         response = new InvocationResponse(new MarshalledObjectForLocalCalls(rtn));
      }
      else
      {
         response = new InvocationResponse(rtn);
      }
      response.setContextInfo(responseContext);
      return response;
   }

   /**
    * Invoke a method on the virtual EJB bean. The method must be one of the methods defined in one
    * of the business interfaces (or home interface) of the bean.
    * 
    * TODO: work in progress
    * 
    * @param factory    the originating end point
    * @param id         unique identifier (primary key), can be null for stateless
    * @param method     the business or home method to invoke
    * @param args       the arguments for the method
    * @param provider   for asynchronous usage
    * @deprecated Use "invoke" as defined by InvokableContext
    */
   @Deprecated
   public Object invoke(SessionProxyFactory factory, Object id, Method method, Object args[]) throws Throwable
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         long hash = MethodHashing.calculateHash(method);
         MethodInfo info = getAdvisor().getMethodInfo(hash);
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call: " + method.toString());
         }

         // Handled now by SessionSpecContainer
         //Method unadvisedMethod = info.getUnadvisedMethod();
         //         if (unadvisedMethod != null && isHomeMethod(unadvisedMethod))
         //         {
         //            return invokeHomeMethod(factory, info, args);
         //         }
         //         else if (unadvisedMethod != null && isEJBObjectMethod(unadvisedMethod))
         //         {
         //            return invokeEJBObjectMethod(factory, id, info, args);
         //         }

         // FIXME: Ahem, stateful container invocation works on all.... (violating contract though)
         StatefulContainerInvocation nextInvocation = new StatefulContainerInvocation(info, id);
         //StatefulSessionContainerMethodInvocation nextInvocation = new StatefulSessionContainerMethodInvocation(info,null);
         //EJBContainerInvocation nextInvocation = new StatefulContainerInvocation(info, id);
         nextInvocation.setAdvisor(getAdvisor());
         nextInvocation.setArguments(args);

         // allow a container to supplement information into an invocation
         nextInvocation = populateInvocation(nextInvocation);

         return nextInvocation.invokeNext();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   /**
    * Create session to an EJB bean.
    * 
    * @param initParameterTypes     the parameter types used by the home's create method
    * @param initParameterValues    the arguments for the home's create method
    * @return   the identifier of the session
    */
   abstract public Serializable createSession(Class<?> initParameterTypes[], Object initParameterValues[]);

   abstract public Object localInvoke(Object id, Method method, Object[] args) throws Throwable;

   abstract public Object localHomeInvoke(Method method, Object[] args) throws Throwable;

   public Serializable createSession()
   {
      return createSession(new Class<?>[]
      {}, new Object[]
      {});
   }

   /**
    * Destroy a created session.
    * 
    * @param id     the identifier of the session
    */
   protected void destroySession(Object id)
   {
      throw new RuntimeException("NYI");
   }

   /**
    * Checks if this session bean binds to the given JNDI name.
    */
   @Override
   public boolean hasJNDIBinding(String jndiName)
   {
      return proxyDeployer.hasJNDIBinding(jndiName);
   }

   protected Object invokeEJBObjectMethod(Object id, MethodInfo info, Object args[]) throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("getEJBHome"))
      {
         return this.getInitialContext().lookup(this.getMetaData().getHomeJndiName());
      }
      if (unadvisedMethod.getName().equals("getPrimaryKey"))
      {
         return id;
      }
      if (unadvisedMethod.getName().equals("isIdentical"))
      {
         // object has no identity
         if (id == null)
            return false;

         EJBObject bean = (EJBObject) args[0];

         Object primaryKey = bean.getPrimaryKey();
         if (primaryKey == null)
            return false;

         boolean isIdentical = id.equals(primaryKey);

         return isIdentical;
      }
      if (unadvisedMethod.getName().equals("remove"))
      {
         try
         {
            destroySession(id);
         }
         catch (NoSuchEJBException nsee)
         {
            String invokingClassName = unadvisedMethod.getDeclaringClass().getName();
            Exception newException = (Exception) this.constructProperNoSuchEjbException(nsee, invokingClassName);
            throw newException;
         }

         return null;
      }
      throw new RuntimeException("NYI");
   }

   /**
    * Obtains the proper Exception to return to the caller in 
    * the event a "remove" call is made on a bean that doesn't exist.
    * 
    * Implements EJB 3.0 Core Specification 14.3.9
    * 
    * @param original
    * @param invokingClassName
    * @return
    */
   protected Throwable constructProperNoSuchEjbException(NoSuchEJBException original, String invokingClassName)
   {
      /*
       * EJB 3.0 Core Specification 14.3.9
       * 
       * If a client makes a call to a stateful session or entity 
       * object that has been removed, the container should throw the 
       * javax.ejb.NoSuchEJBException. If the EJB 2.1 client view is used, 
       * the container should throw the java.rmi.NoSuchObjectException 
       * (which is a subclass of java.rmi.RemoteException) to a remote client, 
       * or the javax.ejb.NoSuchObjectLocalException to a local client.
       */

      // Initialize
      Throwable t = original;
      ClassLoader cl = this.getClassloader();

      // Obtain the actual invoked class
      Class<?> actualInvokingClass = null;
      try
      {
         actualInvokingClass = Class.forName(invokingClassName, true, cl);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Could not obtain invoking class", e);
      }

      // If local EJB2.x Client
      if (EJBLocalObject.class.isAssignableFrom(actualInvokingClass)
            || EJBLocalHome.class.isAssignableFrom(actualInvokingClass))
      {
         t = new NoSuchObjectLocalException(original.getMessage());
      }
      // If remote EJB2.x Client
      else if (Remote.class.isAssignableFrom(actualInvokingClass)
            || EJBObject.class.isAssignableFrom(actualInvokingClass)
            || EJBHome.class.isAssignableFrom(actualInvokingClass))
      {
         t = new NoSuchObjectException(original.getMessage());
      }
      // Business interface
      else
      {
         // Take no action, this is here just for readability
      }

      // Log
      if (log.isTraceEnabled())
      {
         log.trace("Throwing " + t.getClass().getName(), t);
      }

      // Return
      return t;
   }

   /**
    * Allow a container sub class to supplement an invocation. Per default nothing to supplement.
    * 
    * @param invocation
    * @return
    */
   protected StatefulContainerInvocation populateInvocation(StatefulContainerInvocation invocation)
   {
      return invocation;
   }

   abstract protected void removeHandle(Handle handle) throws Exception;

   /**
    * Requests of the container that the underlying target be removed.
    * Most frequently used in SFSB, but not necessarily supported 
    * by SLSB/Singleton/@Service Containers
    * 
    * @throws UnsupportedOperationException If the bean type 
    * does not honor client requests to remove the target
    * 
    * @param target
    * @throws UnsupportedOperationException
    */
   public void removeTarget(Object target) throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("EJB " + this.getName()
            + " does not support removal requests of underlying bean targets");
   }
}
