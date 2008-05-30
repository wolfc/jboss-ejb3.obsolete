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
package org.jboss.ejb3.test.proxy.common.container;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aop.Dispatcher;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.proxy.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * SessionContainer
 *
 * A Mock Session Container for use in Proxy Testing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionContainer
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
      // Initialize
      Class<?>[] argTypes = new Class<?>[]
      {};

      // Get the types from the arguments, if present
      if (args != null)
      {
         List<Class<?>> types = new ArrayList<Class<?>>();
         for (Object arg : args)
         {
            types.add(arg.getClass());
         }
         argTypes = types.toArray(new Class<?>[]
         {});
      }

      // Obtain the method for invocation
      Method m = this.getClassLoader().loadClass(method.getClassName()).getDeclaredMethod(method.getName(), argTypes);

      // Invoke on the bean
      return invokeBean(proxy, m, args);
   }

   protected Object createInstance() throws InstantiationException, IllegalAccessException
   {
      return this.getBeanClass().newInstance();
   }

   public Object invokeBean(Object proxy, Method method, Object args[]) throws Throwable
   {
      // Get the appropriate instance
      Object obj = this.getBeanInstance(proxy);

      // Invoke
      return method.invoke(obj, args);
   }

   @Start
   public void start() throws Throwable
   {
      log.info("Starting " + this);
      
      // Register with Remoting
      Dispatcher.singleton.registerTarget(this.getName(), this);

      // Obtain registrar
      JndiSessionRegistrarBase registrar = this.getJndiRegistrar();

      // Bind all appropriate references/factories to Global JNDI for Client access, if a JNDI Registrar is present
      if (registrar != null)
      {
         registrar.bindEjb(this.getMetaData(), this.getClassLoader(), this.getName());
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

      //TODO We need to unbind the EJB, something like:
      //JndiSessionRegistrarBase.unbindEjb(this.metaData);
      // or some key by which the registrar will keep track of all bindings
   }

   /**
    * Obtains the JndiSessionRegistrarBase from MC, null if not found
    * 
    * @return
    */
   protected JndiSessionRegistrarBase getJndiRegistrar()
   {
      // Initialize
      String jndiRegistrarBindName = this.getJndiRegistrarBindName();

      // Lookup
      Object obj = null;
      try
      {
         obj = Ejb3RegistrarLocator.locateRegistrar().lookup(jndiRegistrarBindName);
      }
      // If not installed, warn and return null
      catch (NotBoundException e)
      {
         log.warn("No " + JndiSessionRegistrarBase.class.getName()
               + " was found installed in the ObjectStore (Registry) at " + jndiRegistrarBindName);
         return null;

      }

      // Cast
      JndiSessionRegistrarBase registrar = (JndiSessionRegistrarBase) obj;

      // Return
      return registrar;
   }

   // --------------------------------------------------------------------------------||
   // contracts ----------------------------------------------------------------------||
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
    * @param proxy
    * @return
    */
   protected abstract Object getBeanInstance(Object proxy);

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
}
