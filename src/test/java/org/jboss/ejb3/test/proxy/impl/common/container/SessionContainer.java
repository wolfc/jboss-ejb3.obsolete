/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.impl.common.container;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.impl.remoting.StatefulSessionRemotingMetadata;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;

/**
 * SessionContainer
 *
 * A Mock Session Container for use in Proxy Testing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionContainer implements InvokableContext
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SessionContainer.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The unique name under which this container has been registered
    */
   private String name;

   /**
    * The CL for this container and all classes associated with this EJB
    */
   private ClassLoader classLoader;

   /**
    * Metadata for this Container
    */
   private JBossSessionBeanMetaData metaData;

   /**
    * The Bean Implementation Class
    */
   private Class<?> beanClass;

   /**
    * The optional JNDI Registrar
    */
   private JndiSessionRegistrarBase jndiRegistrar;

   /**
    * The JNDI Context to use for binding
    */
   private Context jndiContext;

   /**
    * The AOP Advisor
    */
   private Advisor advisor;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param metaData
    * @param classLoader
    */
   protected SessionContainer(JBossSessionBeanMetaData metaData, ClassLoader classLoader)
   {
      // Set Metadata
      this.setMetaData(metaData);

      // Set CL
      this.setClassLoader(classLoader);

      // Set name
      this.setName(this.createContainerName());

      // Set Bean Class
      String beanClassName = this.getMetaData().getEjbClass();
      try
      {
         this.setBeanClass(this.getClassLoader().loadClass(beanClassName));
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Could not find Bean Implementation class \"" + beanClassName + "\" in the "
               + ClassLoader.class.getSimpleName() + " for " + this);
      }

      // Set Advisor
      AspectManager aspectManager = AspectManager.instance(this.getClassLoader());
      Advisor advisor = new ProxyTestClassAdvisor(this, aspectManager);
      this.setAdvisor(advisor);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

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
      Method m = method.toMethod(this.getClassLoader());

      // Invoke on the bean
      return invokeBean(proxy, m, args);
   }

   /**
    * Invocation point of entry for Remoting
    * 
    * @param invocation
    * @return
    * @throws Throwable
    */
   public InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable
   {
      /*
       * Set the proper TCL
       */

      // Hold a reference to the existing TCL
      ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();

      // Set the Container's CL as TCL, required to unmarshall methods from the bean impl class
      Thread.currentThread().setContextClassLoader(this.getClassLoader());

      try
      {

         /*
          * Obtain the target method (unmarshall from invocation)
          */

         // Cast
         assert invocation instanceof MethodInvocation : SessionContainer.class.getName()
               + ".dynamicInoke supports only " + MethodInvocation.class.getSimpleName() + ", but has been passed: "
               + invocation;
         MethodInvocation mi = (MethodInvocation) invocation;

         // Get the method hash
         long methodHash = mi.getMethodHash();
         log.debug("Received dynamic invocation for method with hash: " + methodHash);

         // Get the Method via MethodInfo from the Advisor
         Advisor advisor = this.getAdvisor();
         MethodInfo info = advisor.getMethodInfo(mi.getMethodHash());

         /*
          * Build a new Invocation
          */

         // Construct the invocation
         MethodInvocation newInvocation = new MethodInvocation(info, new Interceptor[]
         {});
         Object[] args = mi.getArguments();
         newInvocation.setArguments(args);
         newInvocation.setMetaData(mi.getMetaData());
         newInvocation.setAdvisor(advisor);

         // Obtain the Session ID
         Serializable sessionId = null;
         Object objSessionId = mi.getMetaData(StatefulSessionRemotingMetadata.TAG_SFSB_INVOCATION,
               StatefulSessionRemotingMetadata.KEY_SESSION_ID);
         if (objSessionId != null)
         {
            assert objSessionId instanceof Serializable : "Session IDs must be " + Serializable.class.getSimpleName();
            sessionId = (Serializable) objSessionId;
         }

         // Get the target, and set on the invocation
         Object target = this.getBeanInstance(sessionId);
         newInvocation.setTargetObject(target);

         // Create an Object reference to hold the return value
         Object returnValue = null;

         // Create a reference to the Invocation's response
         InvocationResponse response = null;

         /*
          * Invoke
          */

         // Invoke
         returnValue = newInvocation.invokeNext();

         // Create a Response
         response = new InvocationResponse(returnValue);
         Map<Object, Object> responseContext = newInvocation.getResponseContextInfo();
         response.setContextInfo(responseContext);

         // Return
         return response;
      }
      finally
      {
         // Reset the TCL to original
         Thread.currentThread().setContextClassLoader(originalLoader);
      }
   }

   protected Object createInstance() throws InstantiationException, IllegalAccessException
   {
      return this.getBeanClass().newInstance();
   }

   //FIXME: Should be agnostic to Session IDs, SLSBs have none
   public Object invokeBean(Object proxy, Method method, Object args[]) throws Throwable
   {
      // Precondition checks
      assert Proxy.isProxyClass(proxy.getClass()) : "Unexpected proxy, was expecting type " + Proxy.class.getName();
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof SessionProxyInvocationHandler : InvocationHandler.class.getSimpleName()
            + " must be of type " + SessionProxyInvocationHandler.class.getName();
      SessionProxyInvocationHandler sHandler = (SessionProxyInvocationHandler) handler;

      // Get the Target (Session ID)
      Object sessionId = sHandler.getTarget();

      // Get the appropriate instance
      Object obj = this.getBeanInstance((Serializable) sessionId);

      // Invoke
      return method.invoke(obj, args);
   }

   @Start
   public void start() throws Throwable
   {
      log.info("Starting " + this);

      // Register with Remoting
      Dispatcher.singleton.registerTarget(this.getName(), new ProxyTestClassProxyHack(this));

      // Obtain registrar
      JndiSessionRegistrarBase registrar = this.getJndiRegistrar();

      // Bind all appropriate references/factories to Global JNDI for Client access, if a JNDI Registrar is present
      if (registrar != null)
      {
         this.setJndiRegistrar(registrar);
         registrar.bindEjb(this.getJndiContext(), this.getMetaData(), this.getClassLoader(), this.getName(), this
               .getName(), this.getAdvisor());
      }
      else
      {
         log.warn("No " + JndiSessionRegistrarBase.class.getSimpleName()
               + " was found; byassing binding of Proxies to " + this.getName() + " in Global JNDI.");
      }

   }

   @Stop
   public void stop()
   {
      log.info("Stopping " + this);

      // Deregister with Remoting
      Dispatcher.singleton.unregisterTarget(this.getName());

      // Obtain registrar
      JndiSessionRegistrarBase registrar = this.getJndiRegistrar();

      // If the registrar has been used for this container, unbind all JNDI references
      if (registrar != null)
      {
         registrar.unbindEjb(this.getJndiContext(), this.getMetaData());
      }
   }

   /**
    * Obtains the JndiSessionRegistrarBase from MC, null if not found
    * 
    * @return
    */
   protected JndiSessionRegistrarBase getJndiRegistrar()
   {
      // If the JNDI Registrar has not yet been set
      if (this.jndiRegistrar == null)
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

         // Cast and set
         this.setJndiRegistrar((JndiSessionRegistrarBase) obj);
      }

      // Return
      return jndiRegistrar;
   }

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a unique name for this container
    * 
    * @return
    */
   protected abstract String createContainerName();

   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   protected abstract String getJndiRegistrarBindName();

   /**
    * Obtains the appropriate bean instance for invocation
    * as called from the specified proxy
    * 
    * @param sessionId
    * @return
    */
   protected abstract Object getBeanInstance(Serializable sessionId);

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||   

   /**
    * Obtains a List of all methods handled by the bean class
    * 
    * @return The methods handled by the bean class directly
    */
   public Set<Method> getVirtualMethods()
   {
      // Initialize
      Set<Method> virtualMethods = new HashSet<Method>();

      // Obtain Metadata
      JBossSessionBeanMetaData smd = this.getMetaData();

      // Obtain CL
      ClassLoader cl = this.getClassLoader();

      /*
       * Business Remotes
       */

      // Obtain all specified business remotes
      BusinessRemotesMetaData businessRemotes = smd.getBusinessRemotes();
      if (businessRemotes != null)
      {
         // For each business remote
         for (String businessRemote : businessRemotes)
         {
            // Load the Class
            Class<?> businessRemoteClass = null;
            try
            {
               businessRemoteClass = Class.forName(businessRemote, true, cl);
            }
            catch (ClassNotFoundException e)
            {
               throw new RuntimeException("Could not find specified business remote class: " + businessRemote, e);
            }

            // Obtain all methods declared by the class
            Method[] declaredMethods = businessRemoteClass.getMethods();

            // Add each method
            for (Method declaredMethod : declaredMethods)
            {
               virtualMethods.add(declaredMethod);
            }
         }
      }

      /*
       * Business Locals
       */

      // Obtain all specified business locals
      BusinessLocalsMetaData businessLocals = smd.getBusinessLocals();
      if (businessLocals != null)
      {
         // For each business local
         for (String businessLocal : businessLocals)
         {
            // Load the Class
            Class<?> businessLocalClass = null;
            try
            {
               businessLocalClass = Class.forName(businessLocal, true, cl);
            }
            catch (ClassNotFoundException e)
            {
               throw new RuntimeException("Could not find specified business local class: " + businessLocal, e);
            }

            // Obtain all methods declared by the class
            Method[] declaredMethods = businessLocalClass.getMethods();

            // Add each method
            for (Method declaredMethod : declaredMethods)
            {
               virtualMethods.add(declaredMethod);
            }
         }
      }

      // Remote Home
      String remoteHomeClassName = smd.getHome();
      if (remoteHomeClassName != null)
      {
         Class<?> remoteHomeClass = null;
         try
         {
            remoteHomeClass = Class.forName(remoteHomeClassName, true, cl);
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException("Could not find specified Remote Home Class: " + remoteHomeClassName, e);
         }
         if (remoteHomeClass != null)
         {
            Method[] declaredMethods = remoteHomeClass.getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);

            declaredMethods = javax.ejb.EJBObject.class.getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);
         }
      }

      // Local Home
      String localHomeClassName = smd.getLocalHome();
      if (localHomeClassName != null)
      {
         Class<?> localHomeClass = null;
         try
         {
            localHomeClass = Class.forName(localHomeClassName, true, cl);
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException("Could not find specified Local Home Class: " + localHomeClass, e);
         }
         if (localHomeClass != null)
         {
            Method[] declaredMethods = localHomeClass.getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);

            declaredMethods = javax.ejb.EJBLocalObject.class.getMethods();
            for (Method declaredMethod : declaredMethods)
               virtualMethods.add(declaredMethod);
         }
      }

      return virtualMethods;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public String getName()
   {
      return name;
   }

   protected void setName(String name)
   {
      this.name = name;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   private void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   public JBossSessionBeanMetaData getMetaData()
   {
      return metaData;
   }

   private void setMetaData(JBossSessionBeanMetaData metaData)
   {
      this.metaData = metaData;
   }

   public Class<?> getBeanClass()
   {
      return beanClass;
   }

   private void setBeanClass(Class<?> beanClass)
   {
      this.beanClass = beanClass;
   }

   public void setJndiRegistrar(JndiSessionRegistrarBase jndiRegistrar)
   {
      this.jndiRegistrar = jndiRegistrar;
   }

   protected Context getJndiContext()
   {
      if (this.jndiContext == null)
      {
         try
         {
            this.setJndiContext(new InitialContext());
         }
         catch (NamingException e)
         {
            throw new RuntimeException("Could not create new default JNDI Context for Container: " + this.getName(), e);
         }
      }
      return jndiContext;
   }

   private void setJndiContext(Context jndiContext)
   {
      this.jndiContext = jndiContext;
   }

   public Advisor getAdvisor()
   {
      return advisor;
   }

   public void setAdvisor(Advisor advisor)
   {
      this.advisor = advisor;
   }
}
