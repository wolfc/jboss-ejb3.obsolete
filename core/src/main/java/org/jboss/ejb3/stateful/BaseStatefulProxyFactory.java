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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.jboss.ejb3.JndiProxyFactory;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class BaseStatefulProxyFactory extends org.jboss.ejb3.session.BaseSessionProxyFactory implements ProxyFactory
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(BaseStatefulProxyFactory.class);

//   protected Class proxyClass;
   private Constructor proxyConstructor;
//   protected Context proxyFactoryContext;
   protected String jndiName;

   public static final String PROXY_FACTORY_NAME = "StatefulProxyFactory";
   
   /**
    * Do not call, only for externalizable
    */
   protected BaseStatefulProxyFactory()
   {
      super();
   }

   public BaseStatefulProxyFactory(SessionContainer container, String jndiName)
   {
      super(container);
      
      assert jndiName != null : "jndiName is null";
      
      this.jndiName = jndiName;
   }
   
   protected Object constructProxy(InvocationHandler handler)
   {
      try
      {
         return proxyConstructor.newInstance(handler);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if(t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw new RuntimeException(t);
      }
   }
   
   public void init() throws Exception
   {
      Class[] interfaces = getInterfaces();
      Class proxyClass = java.lang.reflect.Proxy.getProxyClass(getContainer().getBeanClass().getClassLoader(), interfaces);
      proxyConstructor = proxyClass.getConstructor(InvocationHandler.class);
   }

   public void start() throws Exception
   {
      init();

      Context ctx = getContainer().getInitialContext();
      Name name = ctx.getNameParser("").parse(jndiName);
      ctx = Util.createSubcontext(ctx, name.getPrefix(name.size() - 1));
      String atom = name.get(name.size() - 1);
      RefAddr refAddr = new StringRefAddr(JndiProxyFactory.FACTORY, jndiName + PROXY_FACTORY_NAME);
      Reference ref = new Reference("java.lang.Object", refAddr, JndiProxyFactory.class.getName(), null);
      try 
      {
         log.debug("Binding reference for " + getContainer().getEjbName() + " in JNDI at " + atom);
         Util.rebind(ctx, atom, ref);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + ctx.getNameInNamespace() + "/" + atom);
         namingException.setRootCause(e);
         throw namingException;
      }
   }

   public void stop() throws Exception
   {
      Util.unbind(getContainer().getInitialContext(), jndiName);
   }

   protected abstract Class<?>[] getInterfaces();

   protected final void initializeJndiName() {};
   
   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      try
      {
         init();
      }
      catch(Exception e)
      {
         log.error(e.getMessage(), e);
         throw new IOException(e.getMessage());
      }
      this.jndiName = in.readUTF();
   }
   
   @Override
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeUTF(jndiName);
   }
}
