package org.jboss.ejb3.session;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.util.MethodHashing;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulProxyInvocationHandler;
import org.jboss.ejb3.proxy.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.ejb3.stateful.StatefulContainerInvocation;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * SessionSpecContainer
 * 
 * A SessionContainer with support for Session Beans defined 
 * specifically by the EJB3 Specification
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionSpecContainer extends SessionContainer
      implements
         InvokableContext<ContainerMethodInvocation>
{

   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SessionSpecContainer.class);

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

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
    * Obtains the JndiSessionRegistrarBase from MC, null if not found
    * 
    * @return
    */
   protected JndiSessionRegistrarBase getJndiRegistrar()
   {
      // Initialize
      String jndiRegistrarBindName = this.getJndiRegistrarBindName();

      // Obtain Registrar
      Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();

      // Lookup
      Object obj = null;
      try
      {
         obj = registrar.lookup(jndiRegistrarBindName);
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
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         Method clMethod = method.toMethod(this.getClassloader());
         long hash = MethodHashing.calculateHash(clMethod);
         MethodInfo info = getAdvisor().getMethodInfo(hash);
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call: " + method.toString());
         }

         Method unadvisedMethod = info.getUnadvisedMethod();

         //TODO Use Polymorphism to have sessions only in StatefulContainer
         InvocationHandler handler = Proxy.getInvocationHandler(proxy);
         Object sessionId = null;
         if (handler instanceof StatefulProxyInvocationHandler)
         {
            sessionId = ((StatefulProxyInvocationHandler) handler).getSessionId();
         }

         if (unadvisedMethod != null && isHomeMethod(method))
         {
            return invokeHomeMethod(method, args);
         }
         else if (unadvisedMethod != null && this.isEjbObjectMethod(method))
         {
            return invokeEJBObjectMethod(sessionId, info, args);
         }

         // FIXME: Ahem, stateful container invocation works on all.... (violating contract though)
         EJBContainerInvocation nextInvocation = new StatefulContainerInvocation(info, sessionId);
         nextInvocation.setAdvisor(getAdvisor());
         nextInvocation.setArguments(args);

         // allow a container to supplement information into an invocation
         nextInvocation = populateInvocation(nextInvocation);

         //TODO Support Async Invocation
         //         ProxyUtils.addLocalAsynchronousInfo(nextInvocation, provider);
         try
         {
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
    * TODO: Move this to SessionSpecContainer
    */
   //TODO
   private Object invokeHomeMethod(SerializableMethod method, Object args[]) throws Exception
   {
      if (method.getName().equals("create"))
      {
         return this.invokeHomeCreate(method, args);
      }
      else if (method.getName().equals("remove"))
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
      if (method.getDeclaringClass().getName().equals(EJBObject.class.getName()))
         return true;

      if (method.getDeclaringClass().getName().equals(EJBLocalObject.class.getName()))
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
      // Get the Method
      Method invokingMethod = method.toMethod(this.getClassloader());

      // Use legacy
      return this.isEJBObjectMethod(invokingMethod);
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

   // ------------------------------------------------------------------------------||
   // Lifecycle Methods ------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Lifecycle Start
    */
   @Start
   @Override
   public void start() throws Exception
   {
      log.info("Starting " + this);
      
      super.start();

      // Register with Remoting
      Dispatcher.singleton.registerTarget(this.getName(), this);

      // Obtain registrar
      JndiSessionRegistrarBase registrar = this.getJndiRegistrar();

      // Bind all appropriate references/factories to Global JNDI for Client access, if a JNDI Registrar is present
      if (registrar != null)
      {
         registrar.bindEjb(this.getMetaData(), this.getClassloader(), this.getName());
      }
      else
      {
         log.warn("No " + JndiSessionRegistrarBase.class.getSimpleName()
               + " was found; byassing binding of Proxies to " + this.getName() + " in Global JNDI.");
      }
   }

   /**
    * Lifecycle Stop
    */
   @Override
   @Stop
   public void stop() throws Exception
   {
      log.info("Stopping " + this);
      
      super.stop();

      // Deregister with Remoting
      Dispatcher.singleton.unregisterTarget(this.getName());

      //TODO We need to unbind the EJB, something like:
      //JndiSessionRegistrarBase.unbindEjb(this.metaData);
      // or some key by which the registrar will keep track of all bindings
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
}
