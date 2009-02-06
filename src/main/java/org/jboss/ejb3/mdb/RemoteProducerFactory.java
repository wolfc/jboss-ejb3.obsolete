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
package org.jboss.ejb3.mdb;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.Remoting;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.annotation.MessageProperties;
import org.jboss.ejb3.proxy.ProxyFactory;
import org.jboss.ejb3.proxy.remoting.ProxyRemotingUtils;
import org.jboss.util.naming.Util;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class RemoteProducerFactory extends ProducerFactory
{
   protected ConnectionFactory factory;

   public RemoteProducerFactory(ConsumerContainer container, Class producer, MessageProperties props, Destination dest, InitialContext ctx, Hashtable initialContextProperties)
   {
      super(container, producer, props, dest, ctx, initialContextProperties);
      try
      {
         String factoryName = pImpl.connectionFactory();
         if (factoryName.equals("")) factoryName = "ConnectionFactory";
         factory = (ConnectionFactory) ctx.lookup(factoryName);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void setContainer(Container container)
   {
   }

   public Object createProxyBusiness()
   {
      Class[] interfaces = {producer, ProducerObject.class};

      ProducerManagerImpl mImpl = null;

      mImpl = new ProducerManagerImpl(dest, factory, props.delivery(), props.timeToLive(), props.priority(), methodMap,
            initialContextProperties, pImpl.transacted(), pImpl.acknowledgeMode());

      Interceptor[] interceptors = {mImpl};
      ProducerProxy ih = new ProducerProxy(mImpl, interceptors);
      return java.lang.reflect.Proxy.newProxyInstance(producer.getClassLoader(), interfaces, ih);
   }

   public void start() throws Exception
   {
      super.start();
      Class[] interfaces = {ProxyFactory.class};
      Object factoryProxy = Remoting.createPojiProxy(jndiName + PROXY_FACTORY_NAME, interfaces, ProxyRemotingUtils.getDefaultClientBinding());
      try
      {
         Util.rebind(ctx, jndiName + PROXY_FACTORY_NAME, factoryProxy);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind remote producer factory into JNDI under jndiName: " + ctx.getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }
      Dispatcher.singleton.registerTarget(jndiName + PROXY_FACTORY_NAME, this);
   }

   public void stop() throws Exception
   {
      super.stop();
      Util.unbind(ctx, jndiName + PROXY_FACTORY_NAME);
      Dispatcher.singleton.unregisterTarget(jndiName + PROXY_FACTORY_NAME);
   }
}
