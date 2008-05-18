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
package org.jboss.ejb3.test.proxy.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.hack.Hack;
import org.jboss.ejb3.proxy.jndiregistrar.JndiRegistrar;
import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.ejb3.proxy.mc.MicrocontainerBindings;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * A simple stateless container that binds proxies and can be invoked.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatelessContainer implements InvokableContext<ContainerMethodInvocation>
{
   private static final Logger log = Logger.getLogger(StatelessContainer.class);

   private JBossSessionBeanMetaData metaData;

   private Class<?> beanClass;

   private Kernel kernel;

   /**
    * The unique name under which this container has been registered
    */
   private String containerName;

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
   public Object invoke(Object proxy, SerializableMethod method, Object... args) throws Throwable
   {
      // Get the types from the arguments
      List<Class<?>> types = new ArrayList<Class<?>>();
      for (Object arg : args)
      {
         types.add(arg.getClass());
      }
      Class<?>[] argTypes = types.toArray(new Class<?>[]
      {});

      // Obtain the method for invocation
      //TODO Use correct classloader, should probably be an instance of the Container
      Method m = Class.forName(method.getClassName()).getDeclaredMethod(method.getName(), argTypes);

      return localInvoke(m, args);
   }

   public StatelessContainer(String containerName, JBossSessionBeanMetaData metaData) throws ClassNotFoundException
   {
      this.metaData = metaData;
      this.beanClass = Class.forName(metaData.getEjbClass());
      this.kernel = Hack.BOOTSTRAP.getKernel(); //TODO Remove and get properly
      this.setContainerName(containerName);
   }

   private Object createInstance() throws InstantiationException, IllegalAccessException
   {
      return beanClass.newInstance();
   }

   public Object localInvoke(Method method, Object args[]) throws Throwable
   {
      // Mock up a new instance, traditionally this would be obtained from a Pool
      Object obj = createInstance();

      // Invoke
      return method.invoke(obj, args);
   }

   @Start
   public void start() throws Throwable
   {
      log.info("Starting " + this);

      // TODO: a lot
      log.fatal("StatelessContainer.start doesn't really do what's really supposed to happen");

      // Carlo's original code
      //      InitialContext ctx = new InitialContext();
      //      //String jndiName = metaData.determineLocalJndiName();
      //      String jndiName = "MyStatelessBean/local";
      //      ClassLoader classLoader = beanClass.getClassLoader();
      //      Class<?> interfaces[] = { MyStatelessLocal.class };
      //      InvocationHandler handler = new StatelessInvocationHandler();
      //      Object proxy = Proxy.newProxyInstance(classLoader, interfaces, handler);
      //      Util.createSubcontext(ctx, "MyStatelessBean");
      //      // TODO: should no be non-serializable (how to get Kernel instance?)
      //      NonSerializableFactory.rebind(ctx, jndiName, proxy);

      // Obtain registrar
      JndiRegistrar registrar = this.getJndiRegistrar();

      // Obtain the TCL
      //TODO Previously the CL was a member of the Container itself, this should fly for now
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      // Bind all appropriate references/factories to Global JNDI for Client access
      registrar.bindEjb(this.metaData, cl, this.getContainerName());

   }

   @Stop
   public void stop()
   {
      log.info("Stopping " + this);

      //TODO We need to unbind the EJB, something like:
      //registrar.unbindEjb(this.metaData);
      // or some key by which the registrar will keep track of all bindings
   }

   /**
    * Obtains the JndiRegistrar from MC
    * 
    * @return
    */
   protected JndiRegistrar getJndiRegistrar()
   {
      // Lookup
      JndiRegistrar registrar = (JndiRegistrar) this.kernel.getController().getInstalledContext(
            MicrocontainerBindings.MC_BEAN_NAME_JNDI_REGISTRAR).getTarget();

      // Return
      return registrar;
   }

   public String getContainerName()
   {
      return containerName;
   }

   private void setContainerName(String containerName)
   {
      this.containerName = containerName;
   }
}
