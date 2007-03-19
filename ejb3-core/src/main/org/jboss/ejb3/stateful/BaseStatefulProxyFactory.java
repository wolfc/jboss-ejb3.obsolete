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
package org.jboss.ejb3.stateful;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.jboss.ejb3.JndiProxyFactory;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.naming.Util;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class BaseStatefulProxyFactory extends org.jboss.ejb3.session.BaseSessionProxyFactory implements ProxyFactory
{
   private static final Logger log = Logger.getLogger(BaseStatefulProxyFactory.class);

   protected Class proxyClass;
   protected Constructor proxyConstructor;
   protected Context proxyFactoryContext;
   protected String jndiName;

   public static final String PROXY_FACTORY_NAME = "StatefulProxyFactory";

   public void init() throws Exception
   {
      initializeJndiName();
      Class[] interfaces = getInterfaces();
      Class proxyClass = java.lang.reflect.Proxy.getProxyClass(container.getBeanClass().getClassLoader(), interfaces);
      final Class[] constructorParams =
              {InvocationHandler.class};
      proxyConstructor = proxyClass.getConstructor(constructorParams);
   }

   public void start() throws Exception
   {
      init();

      Context ctx = container.getInitialContext();
      Name name = ctx.getNameParser("").parse(jndiName);
      ctx = Util.createSubcontext(ctx, name.getPrefix(name.size() - 1));
      String atom = name.get(name.size() - 1);
      RefAddr refAddr = new StringRefAddr(JndiProxyFactory.FACTORY, jndiName + PROXY_FACTORY_NAME);
      Reference ref = new Reference("java.lang.Object", refAddr, JndiProxyFactory.class.getName(), null);
      try 
      {
         Util.rebind(ctx, atom, ref);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful proxy with ejb name " + container.getEjbName() + " into JNDI under jndiName: " + ctx.getNameInNamespace() + "/" + atom);
         namingException.setRootCause(e);
         throw namingException;
      }
   }

   public void stop() throws Exception
   {
      Util.unbind(container.getInitialContext(), jndiName);
   }

   protected abstract Class[] getInterfaces();

   protected abstract void initializeJndiName();
}
