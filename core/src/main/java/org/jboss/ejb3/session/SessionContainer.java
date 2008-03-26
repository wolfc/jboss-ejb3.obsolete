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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.LocalHome;
import javax.ejb.RemoteHome;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Module;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.ProxyUtils;
import org.jboss.ejb3.ThreadLocalStack;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;
import org.jboss.ejb3.remoting.IsLocalInterceptor;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
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
public abstract class SessionContainer extends EJBContainer
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(SessionContainer.class);

   protected ProxyDeployer proxyDeployer;
   private Map<String, HATarget> clusterFamilies;

   public class InvokedMethod
   {
      public InvokedMethod(boolean localInterface, Method method)
      {
         isLocalInterface = localInterface;
         this.method = method;
      }

      public boolean isLocalInterface;
      public Method method;
   }
   
   /**
    * @param id
    * @return
    * @throws Exception
    * @deprecated       the binding on which this proxy is bound is unspecified
    */
   public Object createLocalProxy(Object id) throws Exception
   {
      LocalBinding binding = getAnnotation(LocalBinding.class);
      return createLocalProxy(id, binding);
   }
   
   /**
    * Create a local proxy for an enterprise bean identified by id on a given binding.
    * 
    * @param id             the identifier of the enterprise bean (null for stateless)
    * @param binding        the binding of the proxy
    * @return               a proxy to an enterprise bean
    * @throws Exception
    */
   public abstract Object createLocalProxy(Object id, LocalBinding binding) throws Exception;
   
   /**
    * @param id
    * @return
    * @throws Exception
    * @deprecated       the binding on which this proxy is bound is unspecified
    */
   @Deprecated
   public Object createRemoteProxy(Object id) throws Exception
   {
      RemoteBinding binding = null;
      RemoteBindings bindings = getAnnotation(RemoteBindings.class);
      if (bindings != null)
         binding = bindings.value()[0];
      else
         binding = getAnnotation(RemoteBinding.class);
      
      return createRemoteProxy(id, binding);
   }
   /**
    * Create a remote proxy for an enterprise bean identified by id on a given binding.
    * 
    * @param id             the identifier of the enterprise bean (null for stateless)
    * @param binding        the binding of the proxy
    * @return               a proxy to an enterprise bean
    * @throws Exception
    */
   public abstract Object createRemoteProxy(Object id, RemoteBinding binding) throws Exception;

   protected ThreadLocalStack<InvokedMethod> invokedMethod = new ThreadLocalStack<InvokedMethod>();

   public SessionContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
                           Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData) throws ClassNotFoundException
   {
      super(Ejb3Module.BASE_EJB3_JMX_NAME + ",name=" + ejbName, domain, cl, beanClassName, ejbName, ctxProperties, deployment, beanMetaData);
      proxyDeployer = new ProxyDeployer(this);
   }

   /**
    * Create a local proxy factory.
    * @return
    */
   protected abstract ProxyFactory createProxyFactory(LocalBinding binding);
   
   /**
    * Create a remote proxy factory on the given binding.
    * 
    * The jndiBinding is set to a value, the factory is set to it's default value.
    * 
    * @param binding
    * @return
    */
   protected abstract RemoteProxyFactory createRemoteProxyFactory(RemoteBinding binding);
   
   public abstract InvocationResponse dynamicInvoke(Object target, Invocation invocation) throws Throwable;
   
   public Class<?> getInvokedBusinessInterface()
   {
      InvokedMethod method = invokedMethod.get();
      if (method == null) throw new IllegalStateException("getInvokedBusinessInterface() being invoked outside of a business invocation");
      if (method.method == null) throw new IllegalStateException("getInvokedBusinessInterface() being invoked outside of a business invocation");
      if (method.isLocalInterface) return method.method.getDeclaringClass();
      Class<?>[] remoteInterfaces = ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(this);
      for (Class<?> intf : remoteInterfaces)
      {
         try
         {
            intf.getMethod(method.method.getName(), method.method.getParameterTypes());
            return intf;
         }
         catch (NoSuchMethodException ignored)
         {
            // continue
         }
      }
      throw new IllegalStateException("Unable to find geInvokedBusinessInterface()");
   }

   protected JBossSessionBeanMetaData getMetaData()
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
   
   public void start() throws Exception
   {
      super.start();
      // So that Remoting layer can reference this container easily.
      Dispatcher.singleton.registerTarget(getObjectName().getCanonicalName(), new ClassProxyHack(this));
      proxyDeployer.start();
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
      if(clusterFamilies != null)
         return clusterFamilies;
      
      synchronized (this)
      {
         if(clusterFamilies == null)
            clusterFamilies = new HashMap<String, HATarget>();
      }
      return clusterFamilies;
   }

   public void stop() throws Exception
   {
      try
      {
         proxyDeployer.stop();
      }
      catch (Exception ignore)
      {
         log.trace("Proxy deployer stop failed", ignore);
      }
      try
      {
         Dispatcher.singleton.unregisterTarget(getObjectName().getCanonicalName());
      }
      catch (Exception ignore)
      {
         log.trace("Dispatcher unregister target failed", ignore);
      }
      super.stop();
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
            for(Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);

            declaredMethods = javax.ejb.EJBObject.class.getMethods();
            for(Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);
         }

         LocalHome localHome = getAnnotation(LocalHome.class);
         if (localHome != null)
         {
            Method[] declaredMethods = localHome.value().getMethods();
            for(Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);

            declaredMethods = javax.ejb.EJBLocalObject.class.getMethods();
            for(Method declaredMethod : declaredMethods)
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

   protected boolean isHomeMethod(Method method)
   {
      if (javax.ejb.EJBHome.class.isAssignableFrom(method.getDeclaringClass())) return true;
      if (javax.ejb.EJBLocalHome.class.isAssignableFrom(method.getDeclaringClass())) return true;
      return false;
   }

   protected boolean isEJBObjectMethod(Method method)
   {
      if (method.getDeclaringClass().getName().equals(EJBObject.class.getName()))
         return true;

      if (method.getDeclaringClass().getName().equals(EJBLocalObject.class.getName()))
         return true;

      return false;
   }

   protected boolean isHandleMethod(Method method)
   {
      if (method.getDeclaringClass().getName().equals(Handle.class.getName()))
         return true;

      return false;
   }

   public static InvocationResponse marshallException(Invocation invocation, Throwable exception, Map responseContext) throws Throwable
   {
      if (invocation.getMetaData(IsLocalInterceptor.IS_LOCAL,IsLocalInterceptor.IS_LOCAL) == null) throw exception;

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
    */
   public Object invoke(ProxyFactory factory, Object id, Method method, Object args[], FutureHolder provider) throws Throwable
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

         Method unadvisedMethod = info.getUnadvisedMethod();

         if (unadvisedMethod != null && isHomeMethod(unadvisedMethod))
         {
            return invokeHomeMethod(factory, info, args);
         }
         else if (unadvisedMethod != null && isEJBObjectMethod(unadvisedMethod))
         {
            return invokeEJBObjectMethod(factory, id, info, args);
         }

         // FIXME: Ahem, stateful container invocation works on all.... (violating contract though)
         EJBContainerInvocation nextInvocation = new StatefulContainerInvocation(info, id);
         nextInvocation.setAdvisor(getAdvisor());
         nextInvocation.setArguments(args);
         
         // allow a container to supplement information into an invocation
         nextInvocation = populateInvocation(nextInvocation);

         ProxyUtils.addLocalAsynchronousInfo(nextInvocation, provider);
         try
         {
            invokedMethod.push(new InvokedMethod(true, method));
            return nextInvocation.invokeNext();
         }
         finally
         {
            invokedMethod.pop();
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }
   
   /**
    * TODO: work in progress (refactor both invokeHomeMethod's, localHomeInvoke)
    */
   private Object invokeHomeMethod(ProxyFactory factory, MethodInfo info, Object args[]) throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if (unadvisedMethod.getName().equals("create"))
      {
         Class[] initParameterTypes = {};
         Object[] initParameterValues = {};
         if (unadvisedMethod.getParameterTypes().length > 0)
         {
            initParameterTypes = unadvisedMethod.getParameterTypes();
            initParameterValues = args;
         }

         Object id = createSession(initParameterTypes, initParameterValues);
         
         Object proxy = factory.createProxy(id);

         return proxy;
      }
      else if (unadvisedMethod.getName().equals("remove"))
      {
         if(args[0] instanceof Handle)
            removeHandle((Handle) args[0]);
         else
            destroySession(args[0]);

         return null;
      }
      else
      {
         throw new IllegalArgumentException("illegal home method " + unadvisedMethod);
      }
   }
   
   /**
    * Create session to an EJB bean.
    * 
    * @param initParameterTypes     the parameter types used by the home's create method
    * @param initParameterValues    the arguments for the home's create method
    * @return   the identifier of the session
    */
   abstract public Object createSession(Class initParameterTypes[], Object initParameterValues[]);
   
   abstract public Object localInvoke(Object id, Method method, Object[] args, FutureHolder provider) throws Throwable;
   
   abstract public Object localHomeInvoke(Method method, Object[] args) throws Throwable;
   
   public Object createSession()
   {
      return createSession(null, null);
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
   
   protected Object invokeEJBObjectMethod(ProxyFactory factory, Object id, MethodInfo info, Object args[]) throws Exception
   {
      Method unadvisedMethod = info.getUnadvisedMethod();
      if(unadvisedMethod.getName().equals("getEJBHome"))
      {
         return factory.createHomeProxy();
      }
      if(unadvisedMethod.getName().equals("getPrimaryKey"))
      {
         return id;
      }
      if(unadvisedMethod.getName().equals("isIdentical"))
      {
         // object has no identity
         if(id == null)
            return false;
         
         EJBObject bean = (EJBObject) args[0];

         Object primaryKey = bean.getPrimaryKey();
         if(primaryKey == null)
            return false;

         boolean isIdentical = id.equals(primaryKey);

         return isIdentical;
      }
      if (unadvisedMethod.getName().equals("remove"))
      {
         destroySession(id);

         return null;
      }
      throw new RuntimeException("NYI");
   }
   
   /**
    * Allow a container sub class to supplement an invocation. Per default nothing to supplement.
    * 
    * @param invocation
    * @return
    */
   protected EJBContainerInvocation populateInvocation(EJBContainerInvocation invocation)
   {
      return invocation;
   }
   
   abstract protected void removeHandle(Handle handle) throws Exception;
}
