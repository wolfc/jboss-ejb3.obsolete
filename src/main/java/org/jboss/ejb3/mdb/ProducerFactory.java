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

import java.util.HashMap;
import java.util.Hashtable;

import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.annotation.MessageProperties;
import org.jboss.ejb3.annotation.Producer;
import org.jboss.ejb3.annotation.Producers;
import org.jboss.ejb3.annotation.impl.ProducerImpl;
import org.jboss.ejb3.proxy.JndiSessionProxyObjectFactory;
import org.jboss.ejb3.proxy.ProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public abstract class ProducerFactory implements ProxyFactory
{
   private static final Logger log = Logger.getLogger(ProducerFactory.class);
   
   protected Class<?> producer;
   protected MessageProperties props;
   protected Destination dest;
   protected HashMap<?,?> methodMap;
   protected ProducerImpl pImpl;
   protected String jndiName;
   protected InitialContext ctx;
   protected Hashtable<?,?> initialContextProperties;

   public static final String PROXY_FACTORY_NAME = "PRODUCER_FACTORY";


   protected ProducerFactory(ConsumerContainer container, Class<?> producer, MessageProperties props, Destination dest,
         InitialContext ctx, Hashtable<?, ?> ctxProperties)
   {
      this.producer = producer;
      this.props = props;
      this.dest = dest;
      this.ctx = ctx;
      this.initialContextProperties = ctxProperties;


      methodMap = new HashMap();
//      Method[] methods = producer.getMethods();
//      for (int i = 0 ; i < methods.length ; ++i)
//      {
//         MessageProperties mProps = (MessageProperties)methods[i].getAnnotation(MessageProperties.class);
//         if (mProps != null)
//         {
//            try
//            {
//               methodMap.put(new Long(MethodHashing.methodHash(methods[i])), new MessagePropertiesImpl(mProps));
//            }
//            catch (Exception e)
//            {
//               throw new RuntimeException(e);
//            }
//         }
//      }
  
      Producer p = (Producer) producer.getAnnotation(Producer.class);
      if (p == null)
         p = (Producer)container.resolveAnnotation(Producer.class);
      if (p == null)
      {
         Producers annotation = (Producers)container.resolveAnnotation(Producers.class);
         Producer[] producers = annotation.value();
         for (int i = 0 ; i < producers.length ; ++i)
         {
            if (producers[i].producer() != null && producers[i].producer().equals(producer))
               p = producers[i];
         }
      }
     
      pImpl = new ProducerImpl(p);
      jndiName = producer.getName();
   }

   public Object createHomeProxy()
   {
      throw new UnsupportedOperationException("producer can't have a home interface");
   }
   
   public Object createProxyBusiness(Object id)
   {
      if(id != null)
         throw new IllegalArgumentException("producer proxy must not have an id");
      return createProxyBusiness();
   }
   
   public void setContainer(Container container)
   {
   }

   public void start() throws Exception
   {
      Context baseCtx = ctx;
      Name name = baseCtx.getNameParser("").parse(jndiName);
      baseCtx = Util.createSubcontext(baseCtx, name.getPrefix(name.size() - 1));
      String atom = name.get(name.size() - 1);
      RefAddr refAddr = new StringRefAddr(JndiSessionProxyObjectFactory.REF_ADDR_NAME_JNDI_BINDING_DELEGATE_PROXY_FACTORY, atom + PROXY_FACTORY_NAME);
      Reference ref = new Reference("java.lang.Object", refAddr, JndiSessionProxyObjectFactory.class.getName(), null);
     
      try
      {
         Util.rebind(baseCtx, atom, ref);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind producer factory into JNDI under jndiName: " + baseCtx.getNameInNamespace() + "/" + atom);
         namingException.setRootCause(e);
         throw namingException;
      }
   }

   public void stop() throws Exception
   {
      Util.unbind(ctx, jndiName);
   }
}
