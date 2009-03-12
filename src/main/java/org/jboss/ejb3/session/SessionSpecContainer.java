package org.jboss.ejb3.session;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.ejb.EJB;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.ejb.SessionContext;

import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.ThreadLocalStack;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.proxy.impl.factory.session.SessionProxyFactory;
import org.jboss.ejb3.proxy.impl.factory.session.SessionSpecProxyFactory;
import org.jboss.ejb3.proxy.impl.remoting.SessionSpecRemotingMetadata;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.ejb3.proxy.spi.intf.SessionProxy;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.stateful.StatefulContainerInvocation;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;

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
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public SessionSpecContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
         Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData)
         throws ClassNotFoundException
   {
      super(cl, beanClassName, ejbName, domain, ctxProperties, deployment, beanMetaData);
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
   public Object invoke(SessionProxy proxy, SerializableMethod method, Object[] args) throws Throwable
   {
      /*
       * Replace the TCL with the CL for this Container
       */
      ClassLoader oldLoader = SecurityActions.getContextClassLoader();
      
      SecurityActions.setContextClassLoader(this.getClassloader());
      
      try
      {
         
         invokedMethod.push(method);
         
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

         // Obtain Session ID
         Serializable sessionId = (Serializable) proxy.getTarget();
         
         /*
          * Invoke directly if this is an EJB2.x Method
          */

         if (unadvisedMethod != null && isHomeMethod(unadvisedSerializableMethod))
         {
            return invokeHomeMethod(actualMethod, args);
         }
         else if (unadvisedMethod != null && this.isEjbObjectMethod(unadvisedSerializableMethod))
         {
            return invokeEJBObjectMethod(sessionId, info, args);
         }

         // FIXME: Ahem, stateful container invocation works on all.... (violating contract though)         
         /*
          * Build an invocation
          */
         
         StatefulContainerInvocation nextInvocation = new StatefulContainerInvocation(info,sessionId);
         nextInvocation.getMetaData().addMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,
               SessionSpecRemotingMetadata.KEY_INVOKED_METHOD, method);
         nextInvocation.setArguments(args);
         
         /*
          * Invoke
          */

         return nextInvocation.invokeNext();

      }
      finally
      {
         invokedMethod.pop();
         SecurityActions.setContextClassLoader(oldLoader);
      }
   }

   /**
    * Fulfills javax.ejb.SessionContext.getInvokedBusinessInterface()
    * 
    * Returns the name of the invoking EJB3 Business Interface
    * 
    * @see EJB 3.0 Core Specification 4.5.2 for allowable context in 
    * which this may be invoked
    * @return
    */
   public Class<?> getInvokedBusinessInterface()
   {
      //TODO Should be getting from current invocation
      SerializableMethod invokedMethod = SessionSpecContainer.invokedMethod.get();
      assert invokedMethod!=null : "Invoked Method has not been set";
      
      // Obtain the name of the invoking interface
      String interfaceName = null;
      if (invokedMethod != null)
      {
         interfaceName = invokedMethod.getActualClassName();
      }
      
      // Test for no invoked business interface
      if(interfaceName==null)
      {
         throw new IllegalStateException(
               "Call to "
                     + SessionContext.class.getName()
               + ".getInvokedBusinessInterface() was made from outside an EJB3 Business Interface "
               + "(possibly an EJB2.x Remote/Local?). " + "EJB 3.0 Specification 4.5.2.");
      }
      
      /*
       * Determine if the specified class is not a valid business
       * interface
       */
      
      // Initialize a check flag
      boolean isValidBusinessInterface = false;
      
      // Get Metadata
      JBossSessionBeanMetaData smd = this.getMetaData();
      
      // Check in business remotes
      BusinessRemotesMetaData businessRemotes = smd.getBusinessRemotes();
      if (businessRemotes != null)
      {
         for (String businessRemote : businessRemotes)
         {
            if (businessRemote.equals(interfaceName))
            {
               isValidBusinessInterface = true;
               break;
            }
         }
      }

      // Check in business locals
      BusinessLocalsMetaData businessLocals = smd.getBusinessLocals();
      if (businessLocals != null)
      {
         for (String businessLocal : businessLocals)
         {
            if (businessLocal.equals(interfaceName))
            {
               isValidBusinessInterface = true;
               break;
            }
         }
      }
      
      // If not found as a business interface, we haven't invoked through EJB3 View
      if(!isValidBusinessInterface)
      {
         throw new IllegalStateException("Cannot invoke " + SessionContext.class.getName()
               + ".getInvokedBusinessInterface() from outside of an EJB3 Business View - "
               + "EJB 3.0 Core Specification 4.5.2; Used: " + interfaceName);
      }
      
      /*
       * Get Invoked Interface
       */
      
      // Attempt to load the invoked interface
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
    * @param factory
    * @param unadvisedMethod
    * @param args
    * @return
    * @throws Exception
    */
   protected Object invokeHomeCreate(Method method, Object args[]) throws Exception
   {

      /*
       * Initialize
       */

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
      assert factory instanceof SessionProxyFactory : "Specified factory " + factory.getClass().getName()
            + " is not of type " + SessionProxyFactory.class.getName() + " as required by "
            + StatefulContainer.class.getName() + ", but was instead " + factory;
      SessionSpecProxyFactory sessionFactory = null;
      sessionFactory = SessionSpecProxyFactory.class.cast(factory);

      // Create Proxy
      Object proxy = sessionFactory.createProxyEjb2x();

      // Return
      return proxy;
   }

   /**
    * TODO: work in progress (refactor both invokeHomeMethod's, localHomeInvoke)
    */
   //TODO
   private Object invokeHomeMethod(Method method, Object args[]) throws Exception
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
         {
            throw new RemoveException(
                  "EJB 3.0 Specification Violation 3.6.2.2: Session beans do not have a primary key");
         }

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

   // ------------------------------------------------------------------------------||
   // Lifecycle Methods ------------------------------------------------------------||
   // ------------------------------------------------------------------------------||
   
   /**
    * Lifecycle Start
    */
   @Override
   protected void lockedStart() throws Exception
   {
      log.info("Starting " + this);

      super.lockedStart();
   }

   /**
    * Lifecycle Stop
    */
   @Override
   protected void lockedStop() throws Exception
   {
      log.info("Stopping " + this);

      super.lockedStop();
   }
}
