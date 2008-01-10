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
package org.jboss.ejb3.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.ejb3.ProxyFactory;
import org.jboss.naming.Util;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public abstract class BaseServiceProxyFactory implements ProxyFactory
{
   protected Constructor<?> proxyConstructor;
   protected String jndiName;
   protected ServiceContainer container;

   protected BaseServiceProxyFactory(ServiceContainer container, String jndiName)
   {
      assert container != null : "container is null";
      assert jndiName != null : "jndiName is null";
      
      this.container = container;
      this.jndiName = jndiName;
   }
   
   public Object createHomeProxy()
   {
      throw new UnsupportedOperationException("service can't have a home interface");
   }
   
   public Object createProxy(Object id)
   {
      if(id != null)
         throw new IllegalArgumentException("service proxy must not have an id");
      return createProxy();
   }
   
   public void start() throws Exception
   {
      Class[] interfaces = getInterfaces();
      Class<?> proxyClass = java.lang.reflect.Proxy.getProxyClass(container.getBeanClass().getClassLoader(), interfaces);
      proxyConstructor = proxyClass.getConstructor(InvocationHandler.class);

      try
      {
         Util.rebind(container.getInitialContext(), jndiName, createProxy());
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind service proxy factory for EJB container with ejb name " + container.getEjbName() + " into JNDI under jndiName: " + container.getInitialContext().getNameInNamespace() + "/" + jndiName);
         namingException.setRootCause(e);
         throw namingException;
      }
   }

   public void stop() throws Exception
   {
      Util.unbind(container.getInitialContext(), jndiName);
   }

   protected abstract Class[] getInterfaces();

   protected final void initializeJndiName() {};
}
