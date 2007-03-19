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
package org.jboss.ejb3.stateless;


import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.naming.NamingException;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.aop.AspectManager;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.AllowedOperationsFlags;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.BeanContextLifecycleCallback;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.ejb3.SessionContainer;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.timerservice.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.TimerServiceFactory;
import org.jboss.logging.Logger;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.proxy.ejb.handle.StatelessHandleImpl;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessContainer extends SessionContainer implements TimedObjectInvoker
{
   private static final Logger log = Logger.getLogger(StatelessContainer.class);

   protected TimerService timerService;

   public StatelessContainer(ClassLoader cl, String beanClassName, String ejbName, AspectManager manager,
                             Hashtable ctxProperties, InterceptorInfoRepository interceptorRepository,
                             Ejb3Deployment deployment)
   {
      super(cl, beanClassName, ejbName, manager, ctxProperties, interceptorRepository, deployment);
      beanContextClass = StatelessBeanContext.class;
   }

   public Object createSession(Class initTypes[], Object initArgs[])
   {
      if((initTypes != null && initTypes.length > 0) || (initArgs != null && initArgs.length > 0))
         throw new IllegalArgumentException("stateless bean create method must take no arguments (EJB3 4.5)");
      // a stateless bean has no sessions
      // TODO: pool stuff
      return null;
   }
   
   public void start() throws Exception
   {
      try
      {
         super.start();
         
         timerService = TimerServiceFactory.getInstance().createTimerService(this.getObjectName(), this);
         
         TimerServiceFactory.getInstance().restoreTimerService(timerService);
      }
      catch (Exception e)
      {
         try
         {
            stop();
         }
         catch (Exception ignore)
         {
            log.debug("Failed to cleanup after start() failure", ignore);
         }
         throw e;
      }
   }

   public void stop() throws Exception
   {
      if (timerService != null)
      {
         TimerServiceFactory.getInstance().removeTimerService(timerService);
         timerService = null;
      }
      
      super.stop();
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
   
   public void callTimeout(Timer timer) throws Exception
   {
      Method timeout = callbackHandler.getTimeoutCallback();
      if (timeout == null) throw new EJBException("No method has been annotated with @Timeout");
      Object[] args = {timer};
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsFlags.IN_EJB_TIMEOUT);
         try
         {
            MethodInfo info = (MethodInfo) methodInterceptors.get(callbackHandler.getTimeoutCalllbackHash());
            EJBContainerInvocation nextInvocation = new EJBContainerInvocation(info);
            nextInvocation.setAdvisor(this);
            nextInvocation.setArguments(args);
            nextInvocation.invokeNext();
         }
         catch (Throwable throwable)
         {
            if (throwable instanceof Exception) throw (Exception) throwable;
            throw new RuntimeException(throwable);
         }
         finally
         {
            AllowedOperationsAssociation.popInMethodFlag();
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
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
      return localInvoke(method, args, provider, null);
   }
   
   public Object localInvoke(Method method, Object[] args, FutureHolder provider, BeanContextLifecycleCallback<StatelessBeanContext> callback) throws Throwable
   {
      long start = System.currentTimeMillis();
      
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         MethodInfo info = getMethodInfo(method);
         Method unadvisedMethod = info.getUnadvisedMethod();

         try
         {
            invokeStats.callIn();
            
            invokedMethod.push(new InvokedMethod(true, unadvisedMethod));

            if (unadvisedMethod != null && isHomeMethod(unadvisedMethod))
            {
               return invokeLocalHomeMethod(info, args);
            }

            EJBContainerInvocation<StatelessContainer, StatelessBeanContext> nextInvocation = new EJBContainerInvocation<StatelessContainer, StatelessBeanContext>(info);
            nextInvocation.setAdvisor(this);
            nextInvocation.setArguments(args);
            nextInvocation.setContextCallback(callback);

            ProxyUtils.addLocalAsynchronousInfo(nextInvocation, provider);
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
            
            invokedMethod.pop();
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }

   public InvocationResponse dynamicInvoke(Object target, Invocation invocation) throws Throwable
   {
      long start = System.currentTimeMillis();
      
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         MethodInvocation si = (MethodInvocation) invocation;
         MethodInfo info = (MethodInfo) methodInterceptors.get(si.getMethodHash());
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call");
         }

         Method unadvisedMethod = info.getUnadvisedMethod();
         try
         {
            invokeStats.callIn();
            
            invokedMethod.push(new InvokedMethod(false, unadvisedMethod));
            Map responseContext = null;
            Object rtn = null;
            if (unadvisedMethod != null && isHomeMethod(unadvisedMethod))
            {
               rtn = invokeHomeMethod(info, si);
            }
            else if (info != null && unadvisedMethod != null && isEJBObjectMethod(unadvisedMethod))
            {
               rtn = invokeEJBObjectMethod(info, si);
            }
            else
            {

               EJBContainerInvocation newSi = null;

               newSi = new EJBContainerInvocation(info);
               newSi.setArguments(si.getArguments());
               newSi.setMetaData(si.getMetaData());
               newSi.setAdvisor(this);
               try
               {
                  rtn = newSi.invokeNext();
                  responseContext = newSi.getResponseContextInfo();
               }
               catch (Throwable throwable)
               {
                  responseContext = newSi.getResponseContextInfo();
                  return marshallException(invocation, throwable, responseContext);
               }
            }

            InvocationResponse response = marshallResponse(invocation, rtn, responseContext);
            return response;
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
            
            invokedMethod.pop();
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }

   }


   protected Object invokeEJBObjectMethod(MethodInfo info, MethodInvocation invocation) throws Throwable
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("getHandle"))
      {
         StatelessHandleImpl handle = null;
         RemoteBinding remoteBindingAnnotation = (RemoteBinding) resolveAnnotation(RemoteBinding.class);
         if (remoteBindingAnnotation != null)
            handle = new StatelessHandleImpl(remoteBindingAnnotation.jndiBinding());

         return handle;
      }
      else if (unadvisedMethod.getName().equals("remove"))
      {
         return null;
      }
      else if (unadvisedMethod.getName().equals("getEJBHome"))
      {
         HomeHandleImpl homeHandle = null;

         RemoteBinding remoteBindingAnnotation = (RemoteBinding) resolveAnnotation(RemoteBinding.class);
         if (remoteBindingAnnotation != null)
            homeHandle = new HomeHandleImpl(ProxyFactoryHelper.getHomeJndiName(this));

         return homeHandle.getEJBHome();
      }
      else if (unadvisedMethod.getName().equals("getPrimaryKey"))
      {
         return null;
      }
      else if (unadvisedMethod.getName().equals("isIdentical"))
      {
         return false;
      }
      else
      {
         return null;
      }
   }

   private Object invokeLocalHomeMethod(MethodInfo info, Object[] args) throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("create"))
      {
         LocalBinding binding = (LocalBinding) resolveAnnotation(LocalBinding.class);

         StatelessLocalProxyFactory factory = new StatelessLocalProxyFactory();
         factory.setContainer(this);
         factory.init();

         Object proxy = factory.createProxy();

         return proxy;
      }
      else // remove
      {
         return null;
      }
   }

   protected Object invokeHomeMethod(MethodInfo info, MethodInvocation invocation) throws Throwable
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("create"))
      {
         RemoteBinding binding = null;

         RemoteBindings bindings = (RemoteBindings) resolveAnnotation(RemoteBindings.class);
         if (bindings != null)
            binding = bindings.value()[0];
         else
            binding = (RemoteBinding) resolveAnnotation(RemoteBinding.class);

         StatelessRemoteProxyFactory factory = new StatelessRemoteProxyFactory();
         factory.setContainer(this);
         factory.setRemoteBinding(binding);
         factory.init();

         return factory.createProxy();
      }
      else // remove
      {
         return null;
      }
   }

   @Override
   public Object getBusinessObject(BeanContext ctx, Class intf)
   {
      try
      {
         String jndiName = ProxyFactoryHelper.getJndiName(this, intf);
         if (jndiName == null) throw new IllegalStateException("Cannot find BusinessObject for interface: " + intf.getName());
         return getInitialContext().lookup(ProxyFactoryHelper.getJndiName(this, intf));
      }
      catch (NamingException e)
      {
         throw new RuntimeException("failed to invoke getBusinessObject", e);
      }
   }

   protected void removeHandle(Handle handle)
   {
      throw new RuntimeException("NYI");
   }
}
