/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.endpoint.deployers.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.endpoint.SessionFactory;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.ejb3.proxy.spi.container.StatefulSessionFactory;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EndpointImpl implements Endpoint, SessionFactory
{
   private static final Logger log = Logger.getLogger(EndpointImpl.class);
   
   private InvokableContext container;
   private StatefulSessionFactory factory;
   
   public Serializable createSession(Class<?>[] initTypes, Object[] initValues)
   {
      if(initTypes != null && initTypes.length != 0)
         throw new UnsupportedOperationException("SessionFactory " + this + " does not support arguments");
      if(initValues != null && initValues.length != 0)
         throw new UnsupportedOperationException("SessionFactory " + this + " does not support arguments");
      return factory.createSession();
   }
   
   public void destroySession(Serializable session)
   {
      log.debug("Session destruction is not supported");
   }
   
   public SessionFactory getSessionFactory() throws IllegalStateException
   {
      if(factory == null)
         throw new IllegalStateException("Endpoint " + this + " is not session aware");
      return this;
   }

   public Object invoke(final Serializable session, Class<?> invokedBusinessInterface, Method method, Object[] args)
      throws Throwable
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { invokedBusinessInterface };
      SessionProxyInvocationHandler handler = new SessionProxyInvocationHandler() {
         private static final long serialVersionUID = 1L;

         public String getBusinessInterfaceType()
         {
            throw new UnsupportedOperationException();
         }

         public String getContainerGuid()
         {
            throw new UnsupportedOperationException();
         }

         public String getContainerName()
         {
            throw new UnsupportedOperationException();
         }

         public Interceptor[] getInterceptors()
         {
            throw new UnsupportedOperationException();
         }

         public Object getTarget()
         {
            return session;
         }

         public void setBusinessInterfaceType(String businessInterfaceType)
         {
            throw new UnsupportedOperationException();
         }

         public void setContainerGuid(String containerGuid)
         {
            throw new UnsupportedOperationException();
         }

         public void setContainerName(String containerName)
         {
            throw new UnsupportedOperationException();
         }

         public void setInterceptors(Interceptor[] interceptors)
         {
            throw new UnsupportedOperationException();
         }

         public void setTarget(Object target)
         {
            throw new UnsupportedOperationException();
         }

         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            throw new UnsupportedOperationException();
         }
      };
      Object proxy = Proxy.newProxyInstance(loader, interfaces, handler);
      SerializableMethod businessMethod = new SerializableMethod(method, invokedBusinessInterface);
      return container.invoke(proxy, businessMethod, args);
   }

   public boolean isSessionAware()
   {
      return factory != null;
   }
   
   //@Inject
   public void setContainer(InvokableContext container)
   {
      this.container = container;
      log.info("container " + container.getClass().getName());
      if(container instanceof StatefulSessionFactory)
         this.factory = (StatefulSessionFactory) container;
   }
}
