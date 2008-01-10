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

import java.lang.reflect.Method;
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

import org.jboss.aop.AspectManager;
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
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.Service;
import org.jboss.ejb3.asynchronous.AsynchronousInterceptor;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.timerservice.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.TimerServiceFactory;
import org.jboss.injection.Injector;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class ServiceContainer extends SessionContainer implements TimedObjectInvoker
{
   ServiceMBeanDelegate delegate;
   Object singleton;
   boolean injected;
   BeanContext beanContext;
   MBeanServer mbeanServer;
   ObjectName delegateObjectName;
   private TimerService timerService;
   private Object mbean = new ServiceDelegateWrapper(this);

   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(ServiceContainer.class);

   public ServiceContainer(MBeanServer server, ClassLoader cl, String beanClassName, String ejbName,
                           AspectManager manager, Hashtable ctxProperties, InterceptorInfoRepository interceptorRepository,
                           Ejb3Deployment deployment)
   {
      super(cl, beanClassName, ejbName, manager, ctxProperties, interceptorRepository, deployment);
      this.mbeanServer = server;
   }

   public void callTimeout(Timer timer) throws Exception
   {
      Method timeout = callbackHandler.getTimeoutCallback();
      if (timeout == null) throw new EJBException("No method has been annotated with @Timeout");
      Object[] args = {timer};
      AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsFlags.IN_EJB_TIMEOUT);
      try
      {
         localInvoke(timeout, args);
      }
      catch(Throwable throwable)
      {
         if (throwable instanceof Exception) throw (Exception) throwable;
         if(throwable instanceof Error) throw (Error) throwable;
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
   protected ProxyFactory createProxyFactory(LocalBinding binding)
   {
      return new ServiceLocalProxyFactory(this, binding);
   }
   
   @Override
   protected RemoteProxyFactory createRemoteProxyFactory(RemoteBinding binding)
   {
      // TODO Implement clustering
      return new ServiceRemoteProxyFactory(this, binding);
   }
   
   public Object createSession(Class initTypes[], Object initArgs[])
   {
//      if((initTypes != null && initTypes.length > 0) || (initArgs != null && initArgs.length > 0))
//         throw new IllegalArgumentException("service bean create method must take no arguments");
      throw new RuntimeException("NYI");
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

      // won't work, before starting the management interface MBean injection must have been done.
      //registerManagementInterface();
      
      invokeOptionalMethod("create");
   }

   @Override
   protected List<Class<?>> resolveBusinessInterfaces()
   {
      List<Class<?>> interfaces = super.resolveBusinessInterfaces();
      Management man = (Management) resolveAnnotation(Management.class);
      if (man != null)
      {
         Class iface = man.value();
         if (iface != null)
         {
            interfaces.add(iface);
         }
      }

      Class[] implIfaces = getBeanClass().getInterfaces();
      for (Class<?> iface : implIfaces)
      {
         if (iface.getAnnotation(Management.class) != null)
         {
            interfaces.add(iface);
         }
      }
      return interfaces;
   }
   
   public void start() throws Exception
   {
      super.start();

      try
      {
         initBeanContext();

         // make sure the timer service is there before injection takes place
         timerService = TimerServiceFactory.getInstance().createTimerService(this, this);

         injectDependencies(beanContext);

         // TODO: EJBTHREE-655: shouldn't happen here, but in create
         registerManagementInterface();
         
         TimerServiceFactory.getInstance().restoreTimerService(timerService);
         
         invokeOptionalMethod("start");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         stop();
      }
   }

   public void stop() throws Exception
   {
      invokeOptionalMethod("stop");
      
      if (timerService != null)
      {
         TimerServiceFactory.getInstance().removeTimerService(timerService);
         timerService = null;
      }

      // TODO: EJBTHREE-655: shouldn't happen here, but in destroy
      unregisterManagementInterface();

      injected = false;
      
      super.stop();
   }

   public void destroy() throws Exception
   {
      invokeOptionalMethod("destroy");
      
      //unregisterManagementInterface();
      
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
   
   /**
    * Invoke a method on the singleton without a specific security or transaction context.
    * 
    * @param methodName
    */
   private void invokeOptionalMethod(String methodName)
   {
      /* EJBTHREE-655 has been postponed
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader(); 
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         Class parameterTypes[] = { };
         Method method = clazz.getMethod(methodName, parameterTypes);
         Object args[] = { };
         method.invoke(singleton, args);
      }
      catch(NoSuchMethodException e)
      {
         // ignore
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
         throw new RuntimeException(e.getCause());
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
      */
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
         MethodInfo info = (MethodInfo) methodInterceptors.get(hash);
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call: " + method.toString());
         }
         EJBContainerInvocation nextInvocation = new EJBContainerInvocation(info);
         nextInvocation.setAdvisor(this);
         nextInvocation.setArguments(args);

         nextInvocation = populateInvocation(nextInvocation);

         if (provider != null)
         {
            nextInvocation.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH, AsynchronousInterceptor.INVOKE_ASYNCH, "YES", PayloadKey.AS_IS);
            nextInvocation.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH, AsynchronousInterceptor.FUTURE_HOLDER, provider, PayloadKey.AS_IS);
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

   public InvocationResponse dynamicInvoke(Object target, Invocation invocation) throws Throwable
   {
      long start = System.currentTimeMillis();
      
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      EJBContainerInvocation newSi = null;
      
      MethodInvocation si = (MethodInvocation) invocation;
      MethodInfo info = (MethodInfo) methodInterceptors.get(si.getMethodHash());
      Method method = info.getUnadvisedMethod();
      try
      {
         invokeStats.callIn();
         
         Thread.currentThread().setContextClassLoader(classloader);
         
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call");
         }
         newSi = new EJBContainerInvocation(info);
         newSi.setArguments(si.getArguments());
         newSi.setMetaData(si.getMetaData());
         newSi.setAdvisor(this);

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
         InvocationResponse response = SessionContainer.marshallResponse(invocation, rtn, newSi.getResponseContextInfo());

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
         synchronized(singleton)
         {
            if (beanContext == null)
            {
               beanContext  = createBeanContext();
               beanContext.initialiseInterceptorInstances();
            }
         }
      }
   }

   public BeanContext<?> peekContext()
   {
      return beanContext;
   }
   
   @Override
   protected EJBContainerInvocation populateInvocation(EJBContainerInvocation invocation)
   {
      invocation.setTargetObject(singleton);
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
      injected = true;
   }

   // Dynamic MBean implementation --------------------------------------------------

   public Object getAttribute(String attribute) throws AttributeNotFoundException,
                                                       MBeanException, ReflectionException
   {
      return delegate.getAttribute(attribute);
   }

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
                                                        InvalidAttributeValueException, MBeanException, ReflectionException
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

   public Object invoke(String actionName, Object params[], String signature[])
           throws MBeanException, ReflectionException
   {
      return delegate.invoke(actionName, params, signature);
   }

   @Override
   protected Object invokeEJBObjectMethod(ProxyFactory factory, Object id, MethodInfo info, Object[] args) throws Exception
   {
      throw new RuntimeException("NYI");
   }

   public MBeanInfo getMBeanInfo()
   {
      return delegate.getMBeanInfo();
   }

   public Object createLocalProxy(Object id, LocalBinding binding) throws Exception
   {
      ServiceLocalProxyFactory factory = new ServiceLocalProxyFactory(this, binding);

      return factory.createProxy(id);
   }
   
   public Object createRemoteProxy(Object id, RemoteBinding binding) throws Exception
   {
      ServiceRemoteProxyFactory factory = new ServiceRemoteProxyFactory(this, binding);

      return factory.createProxy(id);
   }

   private void registerManagementInterface()
   {
      try
      {
         Management annotation = (Management)resolveAnnotation(Management.class);

         Class intf = null;
         if (annotation != null)
            intf = annotation.value();

         if (intf ==null)
         {
            Class[] interfaces = this.getBeanClass().getInterfaces();
            int interfaceIndex = 0;
            while (intf == null && interfaceIndex < interfaces.length)
            {
               if (interfaces[interfaceIndex].getAnnotation(Management.class) != null)
                  intf = interfaces[interfaceIndex];
               else
                  ++interfaceIndex;
            }
         }

         if (intf != null)
         {
            if (mbeanServer == null)
               mbeanServer = org.jboss.mx.util.MBeanServerLocator.locateJBoss();

            if (mbeanServer == null)
               throw new RuntimeException("There is a @Management interface on " + ejbName + " but the MBeanServer has not been initialized for it");

            Service service = (Service)resolveAnnotation(Service.class);

            String objname = service.objectName();
            delegateObjectName = (objname == null || objname.equals("")) ?
                            new ObjectName(getObjectName().getCanonicalName() + ",type=ManagementInterface") : new ObjectName(service.objectName());

            delegate = new ServiceMBeanDelegate(mbeanServer, this, intf, delegateObjectName);

            getDeployment().getKernelAbstraction().installMBean(delegateObjectName, getDependencyPolicy(), delegate);
         }
         else
         {
            Service service = (Service)resolveAnnotation(Service.class);
            if (service.xmbean().length() > 0)
            {
               if (mbeanServer == null)
                  mbeanServer = org.jboss.mx.util.MBeanServerLocator.locateJBoss();

               if (mbeanServer == null)
                  throw new RuntimeException(ejbName + "is defined as an XMBean, but the MBeanServer has not been initialized for it");

               String objname = service.objectName();
               delegateObjectName = (objname == null || objname.equals("")) ?
                               new ObjectName(getObjectName().getCanonicalName() + ",type=ManagementInterface") : new ObjectName(service.objectName());

               delegate = new ServiceMBeanDelegate(mbeanServer, this, service.xmbean(), delegateObjectName);

               getDeployment().getKernelAbstraction().installMBean(delegateObjectName, getDependencyPolicy(), delegate);
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
