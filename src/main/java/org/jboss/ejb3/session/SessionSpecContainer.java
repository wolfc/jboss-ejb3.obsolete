package org.jboss.ejb3.session;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.jboss.aop.Advisor;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.ThreadLocalStack;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;
import org.jboss.ejb3.annotation.RemoteHomeBinding;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.remoting.SessionSpecRemotingMetadata;
import org.jboss.ejb3.stateful.StatefulContainerInvocation;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.RemoteBindingMetaData;

/**
 * SessionSpecContainer
 * 
 * A SessionContainer with support for Session Beans defined 
 * specifically by the EJB3 Specification
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionSpecContainer extends SessionContainer implements InvokableContext
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SessionSpecContainer.class);
   
   /**
    * The method invoked upon by the client
    */
   //TODO: Remove when CurrentInvocation is completely sorted out
   @Deprecated 
   protected static ThreadLocalStack<SerializableMethod> invokedMethod = new ThreadLocalStack<SerializableMethod>();

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private JndiSessionRegistrarBase jndiRegistrar;

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public SessionSpecContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
         Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData)
         throws ClassNotFoundException
   {
      super(cl, beanClassName, ejbName, domain, ctxProperties, deployment, beanMetaData);
   }

   /**
    * Create a remote proxy (EJBObject) for an enterprise bean identified by id
    * 
    * @param id
    * @return
    * @throws Exception
    */
   public Object createProxyRemoteEjb21(String businessInterfaceType) throws Exception
   {
      RemoteBinding binding = this.getRemoteBinding();
      return this.createProxyRemoteEjb21(binding, businessInterfaceType);
   }

   /**
    * Create a remote proxy (EJBObject) for an enterprise bean identified by id on a given binding
    * 
    * @param id
    * @param binding
    * @return
    * @throws Exception
    */
   public abstract Object createProxyRemoteEjb21(RemoteBinding binding, String businessInterfaceType) throws Exception;

   /**
    * Create a local proxy (EJBLocalObject) for an enterprise bean identified by id
    * 
    * @param id
    * @return
    * @throws Exception
    */
   public Object createProxyLocalEjb21(String businessInterfaceType) throws Exception
   {
      LocalBinding binding = this.getAnnotation(LocalBinding.class);
      return this.createProxyLocalEjb21(binding, businessInterfaceType);
   }

   /**
    * Create a local proxy (EJBLocalObject) for an enterprise bean identified by id, with
    * the specified LocalBinding
    * 
    * @param id
    * @return
    * @throws Exception
    */
   public abstract Object createProxyLocalEjb21(LocalBinding binding, String businessInterfaceType) throws Exception;

   /**
    * Invokes the method described by the specified serializable method
    * as called from the specified proxy, using the specified arguments
    * 
    * @param proxy The proxy making the invocation
    * @param method The method to be invoked
    * @param args The arguments to the invocation
    * @throws Throwable A possible exception thrown by the invocation
    * @return
    */
   public Object invoke(Object proxy, SerializableMethod method, Object[] args) throws Throwable
   {
      /*
       * Replace the TCL with the CL for this Container
       */
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(this.getClassloader());
      
      // Push the ENC onto the Stack
      pushEnc();
      
      try
      {
         
         /*
          * Obtain the target method (advised)
          */
         Method actualMethod = method.toMethod(this.getClassloader());
         long hash = MethodHashing.calculateHash(actualMethod);
         MethodInfo info = getAdvisor().getMethodInfo(hash);
         if (info == null)
         {
            throw new RuntimeException("Method invocation via Proxy could not be found handled for EJB "
                  + this.getEjbName() + " : " + method.toString()
                  + ", probable error in virtual method registration w/ Advisor for the Container");
         }
         Method unadvisedMethod = info.getUnadvisedMethod();
         SerializableMethod unadvisedSerializableMethod = new SerializableMethod(unadvisedMethod);

         // Obtain Invocation Handler
         //TODO Ugly, use polymorphism and get Session ID for SFSB only
         assert Proxy.isProxyClass(proxy.getClass());
         SessionProxyInvocationHandler handler = (SessionProxyInvocationHandler)Proxy.getInvocationHandler(proxy);
         Object sessionId = null;
         if (handler instanceof StatefulProxyInvocationHandlerBase)
         {
            sessionId = ((StatefulProxyInvocationHandlerBase) handler).getSessionId();
         }
         
         /*
          * Invoke directly if this is an EJB2.x Method
          */

         if (unadvisedMethod != null && isHomeMethod(unadvisedSerializableMethod))
         {
            return invokeHomeMethod(method, args);
         }
         else if (unadvisedMethod != null && this.isEjbObjectMethod(unadvisedSerializableMethod))
         {
            return invokeEJBObjectMethod(sessionId, info, args);
         }

         // FIXME: Ahem, stateful container invocation works on all.... (violating contract though)
         //TODO Use Polymorphism to have sessions only in StatefulContainer
         
//         Interceptor[] interceptors, long methodHash, Method advisedMethod,
//         Method unadvisedMethod, SerializableMethod invokedMethod, Advisor advisor
         
//         StatefulSessionContainerMethodInvocation nextInvocation = new StatefulSessionContainerMethodInvocation(info
//               .getInterceptors(), hash, info.getAdvisedMethod(), info.getUnadvisedMethod(), method, getAdvisor());
         
         /*
          * Build an invocation
          */
         
         StatefulContainerInvocation nextInvocation = new StatefulContainerInvocation(info,sessionId);
         nextInvocation.getMetaData().addMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,
               SessionSpecRemotingMetadata.KEY_INVOKED_METHOD, method);
         nextInvocation.setArguments(args);
         
         
         //nextInvocation.setAdvisor(getAdvisor());
         //nextInvocation.setSessionId(sessionId);
//         EJBContainerInvocation nextInvocation = new StatefulContainerInvocation(info, sessionId);
//         nextInvocation.setAdvisor(getAdvisor());
//         nextInvocation.setArguments(args);

         // allow a container to supplement information into an invocation
         //nextInvocation = populateInvocation(nextInvocation);

         //TODO Support Async Invocation
         //         ProxyUtils.addLocalAsynchronousInfo(nextInvocation, provider);
        
         
         try
         {
            /*
             * Invoke
             */
            
            invokedMethod.push(method);
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

   public Class<?> getInvokedBusinessInterface()
   {
      //TODO Should be getting from current invocation
      SerializableMethod invokedMethod = SessionSpecContainer.invokedMethod.get();
      assert invokedMethod!=null : "Invoked Method has not been set";
      String interfaceName = invokedMethod.getActualClassName();
      assert interfaceName !=null && interfaceName.trim().length()>0 : "Target Business Interface is not available on invoked method";
      Class<?> invokedInterface = null;
      
      try
      {
         invokedInterface = Class.forName(interfaceName, false, this.getClassloader());
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Invoked Business Interface on Proxy was set to " + interfaceName
               + ", but this could not be loaded by the " + ClassLoader.class.getSimpleName() + " for " + this);
      }
//      if (method == null) throw new IllegalStateException("getInvokedBusinessInterface() being invoked outside of a business invocation");
//      if (method.getName() == null || method.getName().equals("")) throw new IllegalStateException("getInvokedBusinessInterface() being invoked outside of a business invocation");
      
//      String invokedBusinessInterfaceClassName = method.getActualClassName();
//      Class<?> invokedBusinessInterface = null;
//      try
//      {
//         invokedBusinessInterface = this.getClassloader().loadClass(invokedBusinessInterfaceClassName);
//      }
//      catch (ClassNotFoundException e)
//      {
//         throw new RuntimeException("Invoked Business Interface on Proxy was set to "
//               + invokedBusinessInterfaceClassName + ", but this could not be loaded by the "
//               + ClassLoader.class.getSimpleName() + " for " + this);
//      }
      
      return invokedInterface;
   }
   
   /**
    * Provides implementation for this bean's EJB 2.1 Home.create() method 
    * 
    * @param method
    * @param args
    * @return
    * @throws Exception
    */
   protected abstract Object invokeHomeCreate(SerializableMethod method, Object args[]) throws Exception;

   /**
    * TODO: work in progress (refactor both invokeHomeMethod's, localHomeInvoke)
    */
   //TODO
   private Object invokeHomeMethod(SerializableMethod method, Object args[]) throws Exception
   {
      if (method.getName().equals(Ejb2xMethodNames.METHOD_NAME_HOME_CREATE))
      {
         return this.invokeHomeCreate(method, args);
      }
      else if (method.getName().equals(Ejb2xMethodNames.METHOD_NAME_HOME_REMOVE))
      {
         if (args[0] instanceof Handle)
            removeHandle((Handle) args[0]);
         else
            destroySession(args[0]);

         return null;
      }
      else
      {
         throw new IllegalArgumentException("illegal home method " + method);
      }
   }

   /**
    * @deprecated Use isHomeMethod(SerializableMethod method) in SessionSpecContainer
    */
   @Deprecated
   protected boolean isHomeMethod(Method method)
   {
      if (javax.ejb.EJBHome.class.isAssignableFrom(method.getDeclaringClass()))
         return true;
      if (javax.ejb.EJBLocalHome.class.isAssignableFrom(method.getDeclaringClass()))
         return true;
      return false;
   }

   /**
    * Determines whether the specified method is an EJB2.x Home Method
    * 
    * @param method
    * @return
    */
   protected boolean isHomeMethod(SerializableMethod method)
   {
      // Get the Method
      Method invokingMethod = method.toMethod(this.getClassloader());

      // Use legacy
      return this.isHomeMethod(invokingMethod);
   }

   /**
    * @param method
    * @return
    * @deprecated Use isEjbObjectMethod(SerializableMethod method)
    */
   @Deprecated
   protected boolean isEJBObjectMethod(Method method)
   {
      /*
       * Initialize
       */
      
      // Get the declaring class
      Class<?> declaringClass = method.getDeclaringClass();
      
      /*
       * Test if declared by EJBObject/EJBLocalObject
       */
      
      if (declaringClass.getName().equals(EJBObject.class.getName()))
         return true;

      if (declaringClass.getName().equals(EJBLocalObject.class.getName()))
         return true;

      return false;
   }

   /**
    * Determines whether the specified method is an EJB2.x Local 
    * or Remote Method
    * 
    * @param method
    * @return
    */
   protected boolean isEjbObjectMethod(SerializableMethod method)
   {
      
      /*
       * Initialize
       */
      
      // Get the declaring class
      Class<?> declaringClass = null;
      String declaringClassName = method.getDeclaringClassName();
      try
      {
         declaringClass = Class.forName(declaringClassName, false, this.getClassloader());
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Invoked Method specifies a declaring class that could not be loaded by the "
               + ClassLoader.class.getSimpleName() + " for EJB " + this.getEjbName());
      }
      
      /*
       * Test if declared by EJBObject/EJBLocalObject
       */
      
      if (declaringClass.getName().equals(EJBObject.class.getName()))
         return true;

      if (declaringClass.getName().equals(EJBLocalObject.class.getName()))
         return true;

      // If we've reached here, not EJBObject/EJBLocalObject
      return false;
   }

   /**
    * 
    * @param method
    * @return
    * @deprecated Use isHandleMethod(SerializableMethod method)
    */
   @Deprecated
   protected boolean isHandleMethod(Method method)
   {
      if (method.getDeclaringClass().getName().equals(Handle.class.getName()))
         return true;

      return false;
   }

   /**
    * Determines if the specified Method is a Handle Method
    * @param method
    * @return
    */
   protected boolean isHandleMethod(SerializableMethod method)
   {
      // Get the Method
      Method invokingMethod = method.toMethod(this.getClassloader());

      // Use legacy
      return this.isHandleMethod(invokingMethod);
   }
   
   /**
    * Registers this Container with Remoting / AOP Dispatcher
    */
   @Override
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

   // ------------------------------------------------------------------------------||
   // Lifecycle Methods ------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public static ClassLoader TMP_CL = null;
   
   /**
    * Lifecycle Start
    */
   @Override
   protected void lockedStart() throws Exception
   {
      log.info("Starting " + this);

      super.lockedStart();

      //TODO
      /*
       * Temporary Hack Alert
       * 
       * Populate JBoss-specific metadata until this is done by
       * AnnotationMetaDataDeployer and MergedJBossMetaDataDeployer
       * 
       * http://jira.jboss.com/jira/browse/JBMETA-45
       * http://www.jboss.com/index.html?module=bb&op=viewtopic&p=4157770
       */
      log.warn("Populating JBoss-specific annotation metadata manually until done by deployers: " + this);

      // Obtain annotations 
      RemoteBindings remoteBindings = this.getAnnotation(RemoteBindings.class);
      RemoteBinding remoteBinding = this.getAnnotation(RemoteBinding.class);
      RemoteHomeBinding remoteHomeBinding = this.getAnnotation(RemoteHomeBinding.class);
      LocalHomeBinding localHomeBinding = this.getAnnotation(LocalHomeBinding.class);
      //      LocalBinding localBinding = this.getAnnotation(LocalBinding.class); // < No LocalBindingMetaData?

      // Create a Set to hold RemoteBindings
      Set<RemoteBinding> remoteBindingsSet = new HashSet<RemoteBinding>();

      // Populate Set with Remote Bindings
      if (remoteBindings != null)
      {
         for (RemoteBinding binding : remoteBindings.value())
         {
            remoteBindingsSet.add(binding);
         }
      }
      if (remoteBinding != null)
      {
         remoteBindingsSet.add(remoteBinding);
      }

      // Ensure remote bindings metadata is not null
      List<RemoteBindingMetaData> rbmd = this.getMetaData().getRemoteBindings();
      if (rbmd == null || rbmd.size() == 0)
      {
         rbmd = new ArrayList<RemoteBindingMetaData>();
         this.getMetaData().setRemoteBindings(rbmd);
      }

      // For each remote binding, populate metadata
      for (RemoteBinding binding : remoteBindingsSet)
      {
         RemoteBindingMetaData md = new RemoteBindingMetaData();
         md.setClientBindUrl(binding.clientBindUrl());
         md.setInterceptorStack(binding.interceptorStack());
         md.setJndiName(binding.jndiBinding());
         md.setProxyFactory(binding.factory());
         //TODO binding.invokerName?
         rbmd.add(md);
      }

      // Populate metadata for @RemoteHomeBinding
      if (remoteHomeBinding != null)
      {
         this.getMetaData().setHomeJndiName(remoteHomeBinding.jndiBinding());
      }

      // Populate metadata for @LocalHomeBinding
      if (localHomeBinding != null)
      {
         this.getMetaData().setLocalHomeJndiName(localHomeBinding.jndiBinding());
      }

      /*
       * End Temporary Hack
       */

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
      
      //TODO Remove
      TMP_CL = this.getClassloader();
   }

   /**
    * Lifecycle Stop
    */
   @Override
   protected void lockedStop() throws Exception
   {
      log.info("Stopping " + this);

      super.lockedStop();

      // Deregister with Remoting
      Dispatcher.singleton.unregisterTarget(this.getName());

      // Unbind applicable JNDI Entries
      JndiSessionRegistrarBase jndiRegistrar = this.getJndiRegistrar();
      if (jndiRegistrar != null)
      {
         jndiRegistrar.unbindEjb(this.getInitialContext(), this.getMetaData());
      }

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
}
