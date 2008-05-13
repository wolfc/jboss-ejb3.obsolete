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
import java.lang.reflect.Proxy;

import javax.naming.InitialContext;

import org.jboss.ejb3.test.proxy.session.MyStatelessLocal;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.naming.Util;
import org.jboss.util.naming.NonSerializableFactory;

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
   
   private class StatelessInvocationHandler implements InvocationHandler
   {
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         return localInvoke(method, args);
      }
   }
   
   public StatelessContainer(JBossSessionBeanMetaData metaData) throws ClassNotFoundException
   {
      this.metaData = metaData;
      this.beanClass = Class.forName(metaData.getEjbClass());
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
      
      InitialContext ctx = new InitialContext();
      //String jndiName = metaData.determineLocalJndiName();
      String jndiName = "MyStatelessBean/local";
      ClassLoader classLoader = beanClass.getClassLoader();
      Class<?> interfaces[] = { MyStatelessLocal.class };
      InvocationHandler handler = new StatelessInvocationHandler();
      Object proxy = Proxy.newProxyInstance(classLoader, interfaces, handler);
      Util.createSubcontext(ctx, "MyStatelessBean");
      // TODO: should no be non-serializable (how to get Kernel instance?)
      NonSerializableFactory.rebind(ctx, jndiName, proxy);
   }
   
   public void stop()
   {
      log.info("Stopping " + this);
   }
}
