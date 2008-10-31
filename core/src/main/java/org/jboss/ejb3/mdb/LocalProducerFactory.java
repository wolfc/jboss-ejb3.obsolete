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

import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.NonSerializableFactory;
import org.jboss.ejb3.annotation.MessageProperties;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class LocalProducerFactory extends ProducerFactory
{
   protected String factoryName;

   public LocalProducerFactory(ConsumerContainer container, Class producer, MessageProperties props, Destination dest, InitialContext ctx, Hashtable icProperties)
   {
      super(container, producer, props, dest, ctx, icProperties);

      try
      {
         factoryName = pImpl.connectionFactory();
         if (factoryName.equals("")) factoryName = "java:/ConnectionFactory";
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public Object createProxyBusiness()
   {
      Class[] interfaces = {producer, ProducerObject.class};

      ProducerManagerImpl mImpl = null;

      mImpl = new ProducerManagerImpl(dest, factoryName, props.delivery(), props.timeToLive(), props.priority(),
            methodMap, initialContextProperties, pImpl.transacted(), pImpl.acknowledgeMode());

      Interceptor[] interceptors = {mImpl};
      ProducerProxy ih = new ProducerProxy(mImpl, interceptors);
      return java.lang.reflect.Proxy.newProxyInstance(producer.getClassLoader(), interfaces, ih);
   }

   public void start() throws Exception
   {
      super.start();
      try{
         NonSerializableFactory.rebind(ctx, jndiName + PROXY_FACTORY_NAME, this);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind local producer factory with name " + factoryName + " into JNDI under jndiName: " + ctx.getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }
   }

   public void stop() throws Exception
   {
      super.stop();
      NonSerializableFactory.unbind(ctx, jndiName + PROXY_FACTORY_NAME);
   }
}
