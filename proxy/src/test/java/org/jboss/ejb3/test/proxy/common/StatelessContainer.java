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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.ejb3.proxy.jndiregistrar.JndiRegistrar;
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
public class StatelessContainer
{
   private static final Logger log = Logger.getLogger(StatelessContainer.class);
   
   private JBossSessionBeanMetaData metaData;
   private Class<?> beanClass;
   private Kernel kernel;
   
   private class StatelessInvocationHandler implements InvocationHandler
   {
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         return localInvoke(method, args);
      }
   }
   
   public StatelessContainer(JBossSessionBeanMetaData metaData, Kernel kernel) throws ClassNotFoundException
   {
      this.metaData = metaData;
      this.beanClass = Class.forName(metaData.getEjbClass());
      this.kernel = kernel; //TODO Should be injected?, obtained more gracefully, until then hardcode the thing
   }
   
   private Object createInstance() throws InstantiationException, IllegalAccessException
   {
      return beanClass.newInstance();
   }
   
   public Object localInvoke(Method method, Object args[]) throws Throwable
   {
      Object obj = createInstance();
      return method.invoke(obj, args);
   }
   
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
      registrar.bindEjb(this.metaData, cl);

   }
   
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
}
