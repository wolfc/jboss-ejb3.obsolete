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
package org.jboss.ejb3.service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.AllowedOperationsFlags;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.Service;
import org.jboss.ejb3.asynchronous.AsynchronousInterceptor;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.clustered.objectstore.ClusteredObjectStoreBindings;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactory;
import org.jboss.ejb3.proxy.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.factory.session.service.ServiceRemoteProxyFactory;
import org.jboss.ejb3.proxy.objectstore.ObjectStoreBindings;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateful.StatefulContainerInvocation;
import org.jboss.ejb3.timerservice.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.TimerServiceFactory;
import org.jboss.injection.Injector;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;
import org.jboss.util.NotImplementedException;


/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class ServiceContainer extends SessionContainer implements TimedObjectInvoker, InvokableContext
{
   ServiceMBeanDelegate delegate;

   Object singleton;

   BeanContext beanContext;

   MBeanServer mbeanServer;

   ObjectName delegateObjectName;

   private TimerService timerService;

   private Object mbean = new ServiceDelegateWrapper(this);

   private Method timeoutMethod;

   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(ServiceContainer.class);
   
   /*
    * Define lifecycle callback method names
    */
   
   private static final String METHOD_NAME_LIFECYCLE_CALLBACK_CREATE = "create";
   private static final String METHOD_NAME_LIFECYCLE_CALLBACK_START = "start";
   private static final String METHOD_NAME_LIFECYCLE_CALLBACK_STOP = "stop";
   private static final String METHOD_NAME_LIFECYCLE_CALLBACK_DESTROY = "destroy";

   public ServiceContainer(MBeanServer server, ClassLoader cl, String beanClassName, String ejbName, Domain domain,
         Hashtable ctxProperties, Ejb3Deployment deployment, JBossServiceBeanMetaData beanMetaData)
         throws ClassNotFoundException
   {
      super(cl, beanClassName, ejbName, domain, ctxProperties, deployment, beanMetaData);
      this.mbeanServer = server;

      initializeTimeoutMethod();
   }

   // TODO: integrate with StatelessContainer.callTimeout
   public void callTimeout(Timer timer) throws Exception
   {
      if (timeoutMethod == null)
         throw new EJBException("No method has been annotated with @Timeout");
      Object[] args =
      {timer};
      AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsFlags.IN_EJB_TIMEOUT);
      try
      {
         localInvoke(timeoutMethod, args);
      }
      catch (Throwable throwable)
      {
         if (throwable instanceof Exception)
            throw (Exception) throwable;
         throw new RuntimeException(throwable);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   @Override
   public BeanContext<?> createBeanContext()
   {
      return new ServiceBeanContext(this, singleton);
   }

   @Override
   @Deprecated
   protected org.jboss.ejb3.proxy.factory.SessionProxyFactory getProxyFactory(LocalBinding binding)
   {
     throw new NotImplementedException("@Service container is using old Proxy mechanism");
   }

   @Override
   protected SessionProxyFactory getProxyFactory(RemoteBinding binding)
   {
      //TODO Should be obtained from JNDI Registrar, needs to be looked up by a @RemoteBinding key

      /*
       * In this implementation we just make a new Proxy Factory, for now
       */

      // Create
      SessionProxyFactory factory = new ServiceRemoteProxyFactory(this.getName(), this.getName(), Ejb3Registry
            .guid(this), (JBossServiceBeanMetaData) this.getMetaData(), this.getClassloader(), binding.clientBindUrl(),
            this.getAdvisor(), binding.interceptorStack());

      // Start the factory
      try
      {
         factory.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error in starting " + factory, e);
      }

      // Return
      return factory;
   }

   /**
    * @param binding
    * @return
    * @deprecated Until @Service uses EJB3 Proxy
    */
   @Deprecated
   public RemoteProxyFactory getProxyFactoryForService(RemoteBinding binding)
   {
      throw new NotImplementedException(this + " is no longer using unsupported (legacy) proxy impl from ejb3-core");
   }

   // TODO: integrate with StatelessContainer.initializeTimeout
   private void initializeTimeoutMethod()
   {
      JBossSessionBeanMetaData metaData = getMetaData();
      NamedMethodMetaData timeoutMethodMetaData = null;
      if (metaData != null)
         timeoutMethodMetaData = metaData.getTimeoutMethod();
      this.timeoutMethod = getTimeoutCallback(timeoutMethodMetaData, getBeanClass());
   }

   public Serializable createSession(Class<?> initTypes[], Object initArgs[])
   {
      //      if((initTypes != null && initTypes.length > 0) || (initArgs != null && initArgs.length > 0))
      //         throw new IllegalArgumentException("service bean create method must take no arguments");
      throw new UnsupportedOperationException("Service Containers have no Sessions");
   }
   
   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   protected String getJndiRegistrarBindName()
   {
      return isClustered() ? ClusteredObjectStoreBindings.CLUSTERED_OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SERVICE
                           : ObjectStoreBindings.OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SERVICE;
   }

   public Object getMBean()
   {
      return mbean;
   }

   public Object getSingleton()
   {
      return singleton;
   }

   public void create() throws Exception
   {
      super.create();

      // EJBTHREE-655: fire up an instance for use as MBean delegate
      singleton = super.construct();
      
      registerManagementInterface();
      
      invokeOptionalMethod(METHOD_NAME_LIFECYCLE_CALLBACK_CREATE);
   }

   @Override
   public void instantiated()
   {
      super.instantiated();
   }

   @Override
   protected List<Class<?>> resolveBusinessInterfaces()
   {
      List<Class<?>> interfaces = super.resolveBusinessInterfaces();
      Management man = (Management) resolveAnnotation(Management.class);
      if (man != null)
      {
         Class<?> iface = man.value();
         if (iface != null)
         {
            interfaces.add(iface);
         }
      }

      Class<?>[] implIfaces = getBeanClass().getInterfaces();
      for (Class<?> iface : implIfaces)
      {
         if (iface.getAnnotation(Management.class) != null)
         {
            interfaces.add(iface);
         }
      }
      return interfaces;
   }

   protected void reinitialize()
   {
      super.reinitialize();

      singleton = super.construct();

      invokeOptionalMethod(METHOD_NAME_LIFECYCLE_CALLBACK_CREATE);
   }

   @Override
   protected void lockedStart() throws Exception
   {
      super.lockedStart();

      try
      {
         initBeanContext();

         // make sure the timer service is there before injection takes place
         timerService = TimerServiceFactory.getInstance().createTimerService(this, this);

         injectDependencies(beanContext);

         TimerServiceFactory.getInstance().restoreTimerService(timerService);
         invokeOptionalMethod(METHOD_NAME_LIFECYCLE_CALLBACK_START);
      }
      catch (Exception e)
      {
         log.error("Encountered an error in start of " + this.getMetaData().getEjbName(), e);
         try
         {
            lockedStop();
         }
         catch(Exception stopEx)
         {
            log.error("Error during forced container stop", stopEx);
         }
         throw e;
      }
   }

   @Override
   protected void lockedStop() throws Exception
   {
      // Make the lifecycle callback
      invokeOptionalMethod(METHOD_NAME_LIFECYCLE_CALLBACK_STOP);

      if (timerService != null)
      {
         TimerServiceFactory.getInstance().removeTimerService(timerService);
         timerService = null;
      }

      super.lockedStop();

   }

   public void destroy() throws Exception
   {
      // Make the lifecycle callback
      invokeOptionalMethod(METHOD_NAME_LIFECYCLE_CALLBACK_DESTROY);

      // Unregister w/ MBean Server
      unregisterManagementInterface();
      
      // Null out
      singleton = null;
      beanContext = null;

      // Call super impl
      super.destroy();
   }

   public void initializePool() throws Exception
   {
      resolveInjectors();
   }

   public TimerService getTimerService()
   {
      return timerService;
   }

   public TimerService getTimerService(Object pKey)
   {
      assert timerService != null : "Timer Service not yet initialized";
      return timerService;
   }
   
   private void setTcl(final ClassLoader cl)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            Thread.currentThread().setContextClassLoader(cl);
            return null;
         }
      });
   }

   /**
    * Invoke a method on the singleton without a specific security or transaction context.
    * 
    * @param methodName
    */
   private void invokeOptionalMethod(String methodName)
   {
      ClassLoader oldCl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });
      try
      {
         this.setTcl(this.getClassloader());
         Class<?> parameterTypes[] =
         {};
         Method method = this.singleton.getClass().getMethod(methodName, parameterTypes);
         Object[] args = new Object[]{};

         // Invoke
         if (log.isTraceEnabled())
         {
            log.trace("Attempting to invoke \"" + methodName + "()\" upon " + this.getBeanClassName() + "...");
         }
         method.invoke(this.singleton, args);
      }
      catch (IllegalArgumentException e)
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
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         // Ignore
         if (log.isTraceEnabled())
         {
            log.trace("Could not execute optional method \"" + methodName + "\" upon " + this.getBeanClassName()
                  + ", so ignoring");
         }
      }
      finally
      {
         this.setTcl(oldCl);
      }

   }

   public void invokePostConstruct(BeanContext beanContext, Object[] params)
   {
      //Ignore
   }

   public void invokePreDestroy(BeanContext beanContext)
   {
      //Ignore
   }

   public Object localInvoke(Object id, Method method, Object[] args, FutureHolder provider) throws Throwable
   {
      return localInvoke(method, args, provider);
   }

   public Object localHomeInvoke(Method method, Object[] args) throws Throwable
   {
      // no home interface for Service beans
      return null;
   }

   /**
    * Performs a synchronous local invocation
    */
   public Object localInvoke(Method method, Object[] args) throws Throwable
   {
      return localInvoke(method, args, null);
   }

   /**
    * Performs a synchronous or asynchronous local invocation
    *
    * @param provider If null a synchronous invocation, otherwise an asynchronous
    */
   public Object localInvoke(Method method, Object[] args, FutureHolder provider) throws Throwable
   {
      long start = System.currentTimeMillis();

      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         invokeStats.callIn();

         Thread.currentThread().setContextClassLoader(classloader);
         long hash = MethodHashing.calculateHash(method);
         MethodInfo info = getAdvisor().getMethodInfo(hash);
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call: " + method.toString());
         }
         StatefulContainerInvocation nextInvocation = new StatefulContainerInvocation(info, null);
         nextInvocation.setAdvisor(getAdvisor());
         nextInvocation.setArguments(args);

         nextInvocation = populateInvocation(nextInvocation);

         if (provider != null)
         {
            nextInvocation.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH,
                  AsynchronousInterceptor.INVOKE_ASYNCH, "YES", PayloadKey.AS_IS);
            nextInvocation.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH,
                  AsynchronousInterceptor.FUTURE_HOLDER, provider, PayloadKey.AS_IS);
         }
         return nextInvocation.invokeNext();
      }
      finally
      {
         if (method != null)
         {
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            invokeStats.updateStats(method, elapsed);
         }

         invokeStats.callOut();

         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }
   
   public InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable
   {
      long start = System.currentTimeMillis();

      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      StatefulContainerInvocation newSi = null;

      MethodInvocation si = (MethodInvocation) invocation;
      MethodInfo info = getAdvisor().getMethodInfo(si.getMethodHash());
      Method method = info.getUnadvisedMethod();
      try
      {
         invokeStats.callIn();

         Thread.currentThread().setContextClassLoader(classloader);

         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call");
         }
         newSi = new StatefulContainerInvocation(info, null);
         newSi.setArguments(si.getArguments());
         newSi.setMetaData(si.getMetaData());
         newSi.setAdvisor(getAdvisor());

         newSi = populateInvocation(newSi);

         Object rtn = null;
         try
         {
            rtn = newSi.invokeNext();
         }
         catch (Throwable throwable)
         {
            return marshallException(invocation, throwable, newSi.getResponseContextInfo());
         }
         InvocationResponse response = SessionContainer.marshallResponse(invocation, rtn, newSi
               .getResponseContextInfo());

         return response;
      }
      finally
      {
         if (method != null)
         {
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            invokeStats.updateStats(method, elapsed);
         }

         invokeStats.callOut();

         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }

   protected void initBeanContext() throws RuntimeException
   {
      if (beanContext == null)
      {
         synchronized (singleton)
         {
            if (beanContext == null)
            {
               beanContext = createBeanContext();
               pushEnc();
               try
               {
                  beanContext.initialiseInterceptorInstances();
               }
               finally
               {
                  popEnc();
               }
            }
         }
      }
   }

   public BeanContext<?> peekContext()
   {
      return beanContext;
   }

   @Override
   protected StatefulContainerInvocation populateInvocation(StatefulContainerInvocation invocation)
   {
      invocation.setBeanContext(beanContext);
      return invocation;
   }

   protected synchronized void injectDependencies(BeanContext ctx)
   {
      if (injectors != null)
      {
         try
         {
            pushEnc();
            for (Injector injector : injectors)
            {
               injector.inject(ctx);
            }
         }
         finally
         {
            popEnc();
         }
      }
   }

   // Dynamic MBean implementation --------------------------------------------------

   public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      return delegate.getAttribute(attribute);
   }

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
         MBeanException, ReflectionException
   {
      delegate.setAttribute(attribute);
   }

   public AttributeList getAttributes(String[] attributes)
   {
      return delegate.getAttributes(attributes);
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      return delegate.setAttributes(attributes);
   }

   /**
    * @see InvokableContext
    */
   public Object invoke(Object proxy, SerializableMethod method, Object[] args) throws Throwable
   {
      return this.localInvoke(method.toMethod(), args);
   }
   
   public Object invoke(String actionName, Object params[], String signature[]) throws MBeanException,
         ReflectionException
   {
      assert delegate != null : "MBean delegate was null, cannot perform invocation";
      return delegate.invoke(actionName, params, signature);
   }

   public MBeanInfo getMBeanInfo()
   {
      return delegate.getMBeanInfo();
   }

   public Object createLocalProxy(Object id, LocalBinding binding) throws Exception
   {
      throw new NotImplementedException(this + " is using unsupported legacy Proxy implementation");
   }

   @Deprecated
   public Object createRemoteProxy(Object id, RemoteBinding binding) throws Exception
   {
      throw new NotImplementedException(this + " is no longer using unsupported (legacy) proxy impl from ejb3-core");
   }

   private void registerManagementInterface()
   {
      try
      {
         Management annotation = this.getAnnotation(Management.class);

         Class<?> intf = null;
         if (annotation != null)
            intf = annotation.value();

         if (intf == null)
         {
            Class<?>[] interfaces = this.getBeanClass().getInterfaces();
            int interfaceIndex = 0;
            while (intf == null && interfaceIndex < interfaces.length)
            {
               if (interfaces[interfaceIndex].getAnnotation(Management.class) != null)
                  intf = interfaces[interfaceIndex];
               else
                  ++interfaceIndex;
            }
         }
         
         /*
          * Construct a DependencyPolicy for the MBean which will also 
          * define a demand upon this container
          */
         DependencyPolicy containerPolicy = this.getDependencyPolicy();
         DependencyPolicy newPolicy = containerPolicy.clone();
         String cName = this.getObjectName().getCanonicalName();
         newPolicy.addDependency(cName);
         
         // Find MBean Server if not specified
         if (mbeanServer == null)
         {
            try
            {
               mbeanServer = org.jboss.mx.util.MBeanServerLocator.locateJBoss();
            }
            catch (IllegalStateException ise)
            {
               // Not found; ignore for now and we'll catch this later when we absolutely need MBean server
               log.warn(ise);
            }
         }
         
         /*
          * Construct the ObjectName
          */
         Service service = this.getAnnotation(Service.class);
         String objname = service.objectName();
         delegateObjectName = (objname == null || objname.equals("")) ? new ObjectName(getObjectName()
               .getCanonicalName()
               + ",type=ManagementInterface") : new ObjectName(objname);

         // For @Management
         if (intf != null)
         {
            if (mbeanServer == null)
               throw new RuntimeException("There is a @Management interface on " + ejbName
                     + " but the MBeanServer has not been initialized for it");

            delegate = new ServiceMBeanDelegate(mbeanServer, this, intf, delegateObjectName);
            
            getDeployment().getKernelAbstraction().installMBean(delegateObjectName, newPolicy, delegate);
         }
         // XMBeans
         else
         {
            if (service.xmbean().length() > 0)
            {

               if (mbeanServer == null)
                  throw new RuntimeException(ejbName
                        + "is defined as an XMBean, but the MBeanServer has not been initialized for it");


               delegate = new ServiceMBeanDelegate(mbeanServer, this, service.xmbean(), delegateObjectName);

               getDeployment().getKernelAbstraction().installMBean(delegateObjectName, newPolicy, delegate);
            }
         }

      }
      catch (Exception e)
      {
         throw new RuntimeException("Problem registering @Management interface for @Service " + getBeanClass(), e);
      }
   }

   private void unregisterManagementInterface() throws InstanceNotFoundException, MBeanRegistrationException
   {
      if (delegate != null)
      {
         getDeployment().getKernelAbstraction().uninstallMBean(delegateObjectName);
      }
   }

   protected void removeHandle(Handle handle)
   {
      throw new RuntimeException("Don't do this");
   }
}
