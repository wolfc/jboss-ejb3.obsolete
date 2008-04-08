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

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.naming.NamingException;

import org.jboss.aop.Domain;
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
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.ejb3.timerservice.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.TimerServiceFactory;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.proxy.ejb.handle.StatelessHandleImpl;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.ExtensibleWebServiceContext;
import org.jboss.wsf.spi.invocation.InvocationType;
import org.jboss.wsf.spi.invocation.WebServiceContextFactory;
import org.jboss.wsf.spi.invocation.integration.InvocationContextCallback;
import org.jboss.wsf.spi.invocation.integration.ServiceEndpointContainer;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessContainer extends SessionSpecContainer
  implements TimedObjectInvoker, ServiceEndpointContainer
{
   private static final Logger log = Logger.getLogger(StatelessContainer.class);

   protected TimerService timerService;
   private Method timeout;
   private StatelessDelegateWrapper mbean = new StatelessDelegateWrapper(this);

   public StatelessContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
                             Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData) throws ClassNotFoundException
   {
      super(cl, beanClassName, ejbName, domain, ctxProperties, deployment, beanMetaData);
      
      initializeTimeout();
   }

   @Override
   public BeanContext<?> createBeanContext()
   {
      return new StatelessBeanContext(this, construct());
   }
   
   @Override
   protected StatelessLocalProxyFactory getProxyFactory(LocalBinding binding)
   {
      StatelessLocalProxyFactory factory = (StatelessLocalProxyFactory) this.proxyDeployer.getProxyFactory(binding);

      if (factory == null)
      {
         factory = new StatelessLocalProxyFactory(this, binding);
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
   
   @Override
   protected BaseStatelessRemoteProxyFactory getProxyFactory(RemoteBinding binding)
   {
      BaseStatelessRemoteProxyFactory factory = (BaseStatelessRemoteProxyFactory) this.proxyDeployer
            .getProxyFactory(binding);

      if (factory == null)
      {
         Clustered clustered = getAnnotation(Clustered.class);
         if (clustered != null)
            factory = new StatelessClusterProxyFactory(this, binding, clustered);
         else
            factory = new StatelessRemoteProxyFactory(this, binding);
         
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
   
   /**
    * Create a local proxy (EJBLocalObject) for an enterprise bean identified by id, with
    * the specified LocalBinding
    * 
    * @param id
    * @return
    * @throws Exception
    */
   @Override
   public Object createProxyLocalEjb21(LocalBinding binding) throws Exception
   {
      StatelessLocalProxyFactory proxyFactory = this.getProxyFactory(binding);
      return proxyFactory.createProxyEjb21();
   }
   
   @Override
   public Object createProxyRemoteEjb21(RemoteBinding binding) throws Exception
   {
      BaseStatelessRemoteProxyFactory proxyFactory = this.getProxyFactory(binding);
      return proxyFactory.createProxyEjb21();
   }
   
   public Object createSession(Class<?> initTypes[], Object initArgs[])
   {
      if((initTypes != null && initTypes.length > 0) || (initArgs != null && initArgs.length > 0))
         throw new IllegalArgumentException("stateless bean create method must take no arguments (EJB3 4.5)");
      // a stateless bean has no sessions
      // TODO: pool stuff
      return null;
   }
   
   public Object getMBean()
   {
      return mbean;
   }
   
   private void initializeTimeout()
   {
      JBossSessionBeanMetaData metaData = getMetaData();
      NamedMethodMetaData timeoutMethodMetaData = null;
      if(metaData != null)
         timeoutMethodMetaData = metaData.getTimeoutMethod();
      this.timeout = getTimeoutCallback(timeoutMethodMetaData, getBeanClass());
   }
   
   public void start() throws Exception
   {
      try
      {
         super.start();
         
         timerService = TimerServiceFactory.getInstance().createTimerService(this, this);
         
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
      if (timeout == null) throw new EJBException("No method has been annotated with @Timeout");
      Object[] args = {timer};
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsFlags.IN_EJB_TIMEOUT);
         try
         {
            MethodInfo info = super.getMethodInfo(timeout);
            EJBContainerInvocation nextInvocation = new EJBContainerInvocation(info);
            nextInvocation.setAdvisor(getAdvisor());
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
   
   public Object localInvoke(Object id, Method method, Object[] args, FutureHolder provider) throws Throwable
   {
      return localInvoke(method, args, provider);
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
               return localHomeInvoke(unadvisedMethod, args);
            }

            EJBContainerInvocation<StatelessContainer, StatelessBeanContext> nextInvocation = new EJBContainerInvocation<StatelessContainer, StatelessBeanContext>(info);
            nextInvocation.setAdvisor(getAdvisor());
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
         MethodInfo info = getAdvisor().getMethodInfo(si.getMethodHash());
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call " + invocation);
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
               newSi.setAdvisor(getAdvisor());
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
         RemoteBinding remoteBindingAnnotation = this.getAnnotation(RemoteBinding.class);
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

         RemoteBinding remoteBindingAnnotation = this.getAnnotation(RemoteBinding.class);
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

   public Object localHomeInvoke(Method method, Object[] args) throws Throwable
   {
      if (method.getName().equals("create"))
      {
         LocalBinding binding = this.getAnnotation(LocalBinding.class);
         
         // FIXME: why this binding? Could be another one. (there is only one local binding, but that's another bug)
         
         StatelessLocalProxyFactory factory = this.getProxyFactory(binding);

         Object proxy = factory.createProxyEjb21();

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
         RemoteBinding binding = this.getRemoteBinding();
         
         BaseStatelessRemoteProxyFactory factory = this.getProxyFactory(binding);
         
         return factory.createProxyEjb21();
      }
      else // remove
      {
         return null;
      }
   }

   @Override
   public Object getBusinessObject(BeanContext ctx, Class intf)
   {
      assert intf != null : "intf is null";
      
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

   /**
    * WS integration
    * @return
    */
   public Class getServiceImplementationClass()
   {
      return this.getBeanClass();
   }

   /**
    * WS Integration
    * @param method
    * @param args
    * @param invocationContextCallback
    * @return
    * @throws Throwable
    */
   public Object invokeEndpoint(Method method, Object[] args, InvocationContextCallback invocationContextCallback) throws Throwable
   {
      WSCallbackImpl callback = new WSCallbackImpl(invocationContextCallback);
      return this.localInvoke(method, args, null, callback);
   }

   static class WSCallbackImpl implements BeanContextLifecycleCallback
   {
      private javax.xml.ws.handler.MessageContext jaxwsMessageContext;
      private javax.xml.rpc.handler.MessageContext jaxrpcMessageContext;

      public WSCallbackImpl(InvocationContextCallback epInv)
      {
         jaxrpcMessageContext = epInv.get( javax.xml.rpc.handler.MessageContext.class );
         jaxwsMessageContext = epInv.get( javax.xml.ws.handler.MessageContext.class );
      }

      public void attached(BeanContext beanCtx)
      {
         StatelessBeanContext sbc = (StatelessBeanContext)beanCtx;
         sbc.setMessageContextJAXRPC(jaxrpcMessageContext);

         BeanProperty beanProp = sbc.getWebServiceContextProperty();
         if (beanProp != null)
         {
            EJBContext ejbCtx = beanCtx.getEJBContext();
            SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
            ExtensibleWebServiceContext wsContext = spiProvider.getSPI(WebServiceContextFactory.class).newWebServiceContext(InvocationType.JAXWS_EJB3, jaxwsMessageContext);
            wsContext.addAttachment(EJBContext.class, ejbCtx);
            beanProp.set(beanCtx.getInstance(), wsContext);
         }
      }

      public void released(BeanContext beanCtx)
      {
         StatelessBeanContext sbc = (StatelessBeanContext)beanCtx;
         sbc.setMessageContextJAXRPC(null);

         BeanProperty beanProp = sbc.getWebServiceContextProperty();
         if (beanProp != null)
            beanProp.set(beanCtx.getInstance(), null);
      }
   }
}
