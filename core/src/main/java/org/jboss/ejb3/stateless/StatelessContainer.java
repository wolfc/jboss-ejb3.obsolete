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


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoteHome;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.naming.NamingException;

import org.jboss.aop.Advisor;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.AllowedOperationsFlags;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.BeanContextLifecycleCallback;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteHomeBinding;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.clustered.objectstore.ClusteredObjectStoreBindings;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.session.SessionSpecProxyFactory;
import org.jboss.ejb3.proxy.factory.session.stateless.StatelessSessionProxyFactoryBase;
import org.jboss.ejb3.proxy.impl.EJBMetaDataImpl;
import org.jboss.ejb3.proxy.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.objectstore.ObjectStoreBindings;
import org.jboss.ejb3.proxy.remoting.SessionSpecRemotingMetadata;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory;
import org.jboss.ejb3.util.CollectionHelper;
import org.jboss.injection.WebServiceContextProxy;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.util.NotImplementedException;
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
  implements TimedObjectInvoker, ServiceEndpointContainer, InvokableContext
{
   private static final Logger log = Logger.getLogger(StatelessContainer.class);

   protected TimerService timerService;
   private Method timeout;
   private StatelessDelegateWrapper mbean = new StatelessDelegateWrapper(this);

   private TimerServiceFactory timerServiceFactory;

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
   
   public Object createProxyLocalEjb21() throws Exception
   {
      return this.createProxyLocalEjb21(this.getAnnotation(LocalBinding.class));
   }
   
   /**
    * Create a local proxy (EJBLocalObject) for an enterprise bean with
    * the specified LocalBinding
    * 
    * @param id
    * @return
    * @throws Exception
    */
   public Object createProxyLocalEjb21(LocalBinding binding) throws Exception
   {
      
      SessionSpecProxyFactory proxyFactory = (SessionSpecProxyFactory) this.getProxyFactory(binding);
      return proxyFactory.createProxyEjb2x();
   }
   
   public Object createProxyRemoteEjb21() throws Exception
   {
      return this.createProxyRemoteEjb21(this.getRemoteBinding());
   }
   
   public Object createProxyRemoteEjb21(RemoteBinding binding) throws Exception
   {
      SessionSpecProxyFactory proxyFactory = (SessionSpecProxyFactory) this.getProxyFactory(binding);
      return proxyFactory.createProxyEjb2x();
   }
   
   public Serializable createSession(Class<?> initTypes[], Object initArgs[])
   {
      if((initTypes != null && initTypes.length > 0) || (initArgs != null && initArgs.length > 0))
         throw new IllegalArgumentException("stateless bean create method must take no arguments (EJB3 4.5)");
      // a stateless bean has no sessions
      // TODO: pool stuff
      return null;
   }
   
   public boolean isClustered()
   {
      return isAnnotationPresent(Clustered.class);
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
   
   @Override
   protected void lockedStart() throws Exception
   {
      try
      {
         super.lockedStart();
         
         timerService = timerServiceFactory.createTimerService(this);
         
         timerServiceFactory.restoreTimerService(timerService);
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
      if (timerService != null)
      {
         timerServiceFactory.suspendTimerService(timerService);
         timerService = null;
      }
      
      super.lockedStop();
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

   public Object localInvoke(Object id, Method method, Object[] args) throws Throwable
   {
      return localInvoke(method, args);
   }
   
   public Object localInvoke(Method method, Object[] args, BeanContextLifecycleCallback<StatelessBeanContext> callback) throws Throwable
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
            
            //invokedMethod.push(new SerializableMethod(unadvisedMethod, unadvisedMethod.getClass()));

            if (unadvisedMethod != null && isHomeMethod(unadvisedMethod))
            {
               return localHomeInvoke(unadvisedMethod, args);
            }

            EJBContainerInvocation<StatelessContainer, StatelessBeanContext> nextInvocation = new EJBContainerInvocation<StatelessContainer, StatelessBeanContext>(info);
            nextInvocation.setAdvisor(getAdvisor());
            nextInvocation.setArguments(args);
            nextInvocation.setContextCallback(callback);

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
      }
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
      EJBContainerInvocation newSi = null;

      // Create a pointer to the response we'll return
      InvocationResponse response = null;
      

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
         assert invocation instanceof MethodInvocation : SessionContainer.class.getName()
               + ".dynamicInoke supports only " + MethodInvocation.class.getSimpleName()
               + ", but has been passed: " + invocation;
         MethodInvocation si = (MethodInvocation) invocation;

         // Get the method hash
         long methodHash = si.getMethodHash();
         log.debug("Received dynamic invocation for method with hash: " + methodHash);

         // Get the Method via MethodInfo from the Advisor
         Advisor advisor = this.getAdvisor();
         MethodInfo info = advisor.getMethodInfo(methodHash);
         Method unadvisedMethod = info.getMethod();
         SerializableMethod unadvisedMethodSerializable = new SerializableMethod(unadvisedMethod);
         
         try
         {
            invokeStats.callIn();
            
            /*
             * Set the invoked method
             */
            //TODO Remove when CurrentInvocation is ironed out
            
            // Get the invoked method from invocation metadata
            Object objInvokedMethod = si.getMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,SessionSpecRemotingMetadata.KEY_INVOKED_METHOD);
            assert objInvokedMethod !=null : "Invoked Method must be set on invocation metadata";
            assert objInvokedMethod instanceof SerializableMethod : "Invoked Method set on invocation metadata is not of type " + SerializableMethod.class.getName() + ", instead: " + objInvokedMethod;
            SerializableMethod invokedMethod = (SerializableMethod)objInvokedMethod;
            
            // Set onto stack
            SessionSpecContainer.invokedMethod.push(invokedMethod);

            //invokedMethod.push(new SerializableMethod(unadvisedMethod, unadvisedMethod.getClass()));
            Map responseContext = null;
            Object rtn = null;
            if (unadvisedMethod != null && isHomeMethod(unadvisedMethodSerializable))
            {
               rtn = invokeHomeMethod(info, si);
            }
            else if (info != null && unadvisedMethod != null && isEjbObjectMethod(unadvisedMethodSerializable))
            {
               rtn = invokeEJBObjectMethod(info, si);
            }
            else
            {

               newSi = new EJBContainerInvocation<StatelessContainer, StatelessBeanContext>(info);
               newSi.setArguments(si.getArguments());
               newSi.setMetaData(si.getMetaData());
               //newSi.setAdvisor(getAdvisor());               
               
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
               finally
               {
                  SessionSpecContainer.invokedMethod.pop();
               }
            }

            response = marshallResponse(invocation, rtn, responseContext);
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
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(originalLoader);
      }
   }
 
   protected Object invokeEJBObjectMethod(MethodInfo info, MethodInvocation invocation) throws Throwable
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("getHandle"))
      {
         // Get JNDI Registrar
         JndiSessionRegistrarBase slsbJndiRegistrar = this.getJndiRegistrar();

         // Determine if local/remote
         boolean isLocal = EJBLocalObject.class.isAssignableFrom(unadvisedMethod.getDeclaringClass());
         
         // Get the metadata
         JBossSessionBeanMetaData smd = this.getMetaData();

         // Get the appropriate JNDI Name
         String jndiName = isLocal ? smd.getLocalJndiName() : smd.getJndiName();

         // Find the Proxy Factory Key for this SLSB
         String proxyFactoryKey = slsbJndiRegistrar.getProxyFactoryRegistryKey(jndiName, smd, isLocal);

         // Lookup the Proxy Factory in the Object Store
         StatelessSessionProxyFactoryBase proxyFactory = Ejb3RegistrarLocator.locateRegistrar().lookup(proxyFactoryKey,
               StatelessSessionProxyFactoryBase.class);

         // Create a new EJB2.x Proxy
         EJBObject proxy = (EJBObject) proxyFactory.createProxyEjb2x();

         // Create a Handle
         StatelessHandleRemoteImpl handle = new StatelessHandleRemoteImpl(proxy);

         // Return
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
      throw new NotImplementedException("EJBTHREE-1641");
//      if (method.getName().equals("create"))
//      {
//         LocalBinding binding = this.getAnnotation(LocalBinding.class);
//
//         // FIXME: why this binding? Could be another one. (there is only one local binding, but that's another bug)
//
//         StatelessLocalProxyFactory factory = this.getProxyFactory(binding);
//
//         Object proxy = factory.createProxyEjb21(method.getReturnType().getName());
//
//         return proxy;
//      }
//      else
//      // remove
//      {
//         return null;
//      }
   }

   protected Object invokeHomeMethod(MethodInfo info, MethodInvocation invocation) throws Throwable
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      String methodName = unadvisedMethod.getName();
      if (methodName.equals("create"))
      {
         Object[] arguments = invocation.getArguments();
         // EJBTHREE-1512 Pass along the request to the proper home create method
         Object proxy = this.invokeHomeCreate(unadvisedMethod, arguments);
         return proxy;
      }
      // TODO: should be handled locally
      else if(methodName.equals("getEJBMetaData"))
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

         return metadata;
      }
      // TODO: should be handled locally
      else if(methodName.equals("getHomeHandle"))
      {
         RemoteHomeBinding remoteHomeBinding = this.getAnnotation(RemoteHomeBinding.class);
         assert remoteHomeBinding != null : "remoteHomeBinding is null";
         return new HomeHandleImpl(remoteHomeBinding.jndiBinding());
      }
      else
      {
         // remove
         return null;
      }
   }

   @Override
   public <T> T getBusinessObject(BeanContext<?> ctx, Class<T> intf)
   {
      assert intf != null : "intf is null";
      
      try
      {
         
         /*
          * Get all business interfaces
          */
         Set<String> businessInterfaceNames = new HashSet<String>();
         JBossSessionBeanMetaData smd= (JBossSessionBeanMetaData)this.getXml();
         CollectionHelper.addAllIfSet(businessInterfaceNames, smd.getBusinessRemotes());
         CollectionHelper.addAllIfSet(businessInterfaceNames, smd.getBusinessLocals());
         
         String interfaceName = intf.getName();
         
         if (!businessInterfaceNames.contains(interfaceName))
            throw new IllegalStateException("Cannot find BusinessObject for interface: " + interfaceName);
         
         String jndiName = this.getXml().determineResolvedJndiName(interfaceName);
         return intf.cast(getInitialContext().lookup(jndiName));
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
    * @param invCtxCallback
    * @return
    * @throws Throwable
    */
   public Object invokeEndpoint(Method method, Object[] args, InvocationContextCallback invCtxCallback) throws Throwable
   {
      // JAX-RPC message context
      javax.xml.rpc.handler.MessageContext jaxrpcContext = invCtxCallback.get(javax.xml.rpc.handler.MessageContext.class);

      // JAX-WS webservice context
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      WebServiceContextFactory contextFactory = spiProvider.getSPI(WebServiceContextFactory.class);
      ExtensibleWebServiceContext jaxwsContext = contextFactory.newWebServiceContext(
        InvocationType.JAXWS_EJB3,
        invCtxCallback.get(javax.xml.ws.handler.MessageContext.class)
      );

      // ThreadLocal association
      WebServiceContextProxy.associateMessageContext(jaxwsContext);

      // EJB3 Injection Callbacks
      WSCallbackImpl ejb3Callback = new WSCallbackImpl( jaxrpcContext, jaxwsContext );

      // Actual invocation
      return this.localInvoke(method, args, ejb3Callback);
   }

   public String getContainerName()
   {
      String name = this.getObjectName() != null ? this.getObjectName().getCanonicalName() : null;
      return name;
   }
   
   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   protected String getJndiRegistrarBindName()
   {
      return isClustered() ? ClusteredObjectStoreBindings.CLUSTERED_OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SLSB
                           : ObjectStoreBindings.OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SLSB;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.timerservice.spi.TimedObjectInvoker#getTimedObjectId()
    */
   public String getTimedObjectId()
   {
      return getDeploymentQualifiedName();
   }
   
   @Inject
   public void setTimerServiceFactory(TimerServiceFactory factory)
   {
      this.timerServiceFactory = factory;
   }
   
   static class WSCallbackImpl implements BeanContextLifecycleCallback
   {
      private ExtensibleWebServiceContext jaxwsContext;
      private javax.xml.rpc.handler.MessageContext jaxrpcMessageContext;

      public WSCallbackImpl(javax.xml.rpc.handler.MessageContext jaxrpc, ExtensibleWebServiceContext jaxws)
      {
         jaxrpcMessageContext = jaxrpc;
         jaxwsContext = jaxws;
      }

      public void attached(BeanContext beanCtx)
      {
         // JAX-RPC MessageContext
         StatelessBeanContext sbc = (StatelessBeanContext)beanCtx;
         sbc.setMessageContextJAXRPC(jaxrpcMessageContext);

         // JAX-WS MessageContext
         BeanProperty beanProp = sbc.getWebServiceContextProperty();
         if (beanProp != null)
         {
            EJBContext ejbCtx = beanCtx.getEJBContext();            
            jaxwsContext.addAttachment(EJBContext.class, ejbCtx);
            beanProp.set(beanCtx.getInstance(), jaxwsContext);
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
