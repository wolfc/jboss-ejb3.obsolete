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
package org.jboss.ejb3.stateless;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.Container;
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
public abstract class BaseStatelessProxyFactory extends org.jboss.ejb3.session.BaseSessionProxyFactory implements ProxyFactory
{
   private static final Logger log = Logger.getLogger(BaseStatelessProxyFactory.class);

//   protected Class proxyClass;
//   protected Context proxyFactoryContext;
   protected String jndiName;
   
   private Constructor proxyConstructor;
   
   public BaseStatelessProxyFactory(SessionContainer container, String jndiName)
   {
      super(container);
      
      assert jndiName != null : "jndiName is null";
      
      this.jndiName = jndiName;
   }
   
   /**
    * Adapt the JDK to cglib.
    * 
    * This is a named class because it implements both InvocationHandler and Serializable.
    */
   /* TODO: fix EJBTHREE-485 without cglib 
   private static class CGLibInvocationHandlerAdapter implements net.sf.cglib.proxy.InvocationHandler, Serializable
   {
      private static final long serialVersionUID = 1L;

      private InvocationHandler delegate;
      
      private CGLibInvocationHandlerAdapter(InvocationHandler delegate)
      {
         if(delegate == null)
            throw new IllegalArgumentException("delegate must not be null");
         this.delegate = delegate;
      }
      
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         return delegate.invoke(proxy, method, args);
      }
      
   }
   */
   
   /**
    * Adapt the InvocationHandler to MethodHandler.
    * 
    * This is a named class because it implements both MethodHandler and Serializable.
    */
   private static class MethodHandlerAdapter implements MethodHandler, Serializable
   {
      private static final long serialVersionUID = 1L;
      
      private InvocationHandler delegate;
      
      private MethodHandlerAdapter(InvocationHandler delegate)
      {
         if(delegate == null)
            throw new IllegalArgumentException("delegate must not be null");
         this.delegate = delegate;
      }
      
      public Object invoke(Object self, Method thisMethod, Method process, Object[] args) throws Throwable
      {
         return delegate.invoke(self, thisMethod, args);
      }         
   }
   
   /**
    * Hide the fact that I'm now using javassist.
    * 
    * @param handler    a JDK proxy InvocationHandler
    * @return           a true proxy
    */
   protected Object constructProxy(final InvocationHandler handler)
   {
      try
      {
         /* plain jdk */
         Object args[] = { handler };
         Object proxy = proxyConstructor.newInstance(args);
         
         /* javassist */
         /*
         MethodHandler realHandler = new MethodHandlerAdapter(handler);
//         ProxyObject proxy = (ProxyObject) proxyConstructor.newInstance((Object[]) null);
//         proxy.setHandler(realHandler);
         JavassistProxy proxy = (JavassistProxy) proxyConstructor.newInstance((Object[]) null);
         proxy.setMethodHandler(realHandler);
         JavassistProxy.pokeInterfaces(proxy, getInterfaces());
         */
         
         /* cglib */
         /*
         Object args[] = { new CGLibInvocationHandlerAdapter(handler) };
         Object proxy = proxyConstructor.newInstance(args);
         */
         
         return proxy;
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);
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
         throw new RuntimeException(e.getTargetException());
      }
   }
   
   public final Object createProxy(Object id)
   {
      assert id == null : "stateless bean must not have an id";
      return createProxy();
   }
   
   public void init() throws Exception
   {
      initializeJndiName();
      Class<?>[] interfaces = getInterfaces();
      
      
      
      /* plain jdk */
      Class<?> proxyClass = java.lang.reflect.Proxy.getProxyClass(getContainer().getBeanClass().getClassLoader(), interfaces);
      final Class<?>[] constructorParams =
              {InvocationHandler.class};
      proxyConstructor = proxyClass.getConstructor(constructorParams);
      
      /* javassist */
      /*
      proxyFactory = new javassist.util.proxy.ProxyFactory()
      {
         @Override
         protected ClassLoader getClassLoader()
         {
            return container.getBeanClass().getClassLoader();
         }
      };
      proxyFactory.setInterfaces(interfaces);
      proxyFactory.setSuperclass(JavassistProxy.class);
      proxyClass = proxyFactory.createClass();
      proxyConstructor = proxyClass.getConstructor((Class[]) null);
      */
      
      /* cglib */
      /*
      proxyClass = net.sf.cglib.proxy.Proxy.getProxyClass(container.getBeanClass().getClassLoader(), interfaces);
      final Class[] constructorParams = {net.sf.cglib.proxy.InvocationHandler.class};
      proxyConstructor = proxyClass.getConstructor(constructorParams);
      */
   }

   /* for debugging purposes * /
   private static void describeClass(Class cls) {
      System.err.println("class " + cls + " has the following:");
      for(Class i : cls.getInterfaces()) {
         System.err.println("  interface: " + i);
      }
      for(Method m : cls.getDeclaredMethods()) {
         System.err.println("  method: " + m);
      }
      System.err.println("  classloader = " + cls.getClassLoader());
      if(cls.getSuperclass() != null)
         describeClass(cls.getSuperclass());
   }
   */
   
   public void start() throws Exception
   {
      init();

      Object proxy = createProxy();
      //describeClass(proxy.getClass());
      bindProxy(proxy);
   }

   public void stop() throws Exception
   {
      Util.unbind(getContainer().getInitialContext(), jndiName);
   }

   protected abstract Class<?>[] getInterfaces();

   protected final void initializeJndiName() {};

   protected void bindProxy(Object proxy) throws NamingException
   {
      try
      {
         log.debug("Binding proxy for " + getContainer().getEjbName() + " in JNDI at " + jndiName);
         Util.rebind(getContainer().getInitialContext(), jndiName, proxy);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateless proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName);
         namingException.setRootCause(e);
         throw namingException;
      }
   }
}
