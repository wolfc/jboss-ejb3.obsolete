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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * Comment
 *
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision$
 */
public class JavassistProxy implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private MethodHandler handler;
   private Class interfaces[];
   
//   /**
//    * Public because of classloading? 
//    */
//   public static class JavassistProxyReplacement extends JavassistProxy
//   {
//      public void setHandler(MethodHandler handler)
//      {
//         throw new RuntimeException("should never happen");
//      }
//   }
//   
   /**
    * With this method you can directly poke interfaces into the
    * proxy without setting of the method handler.
    * 
    * @param proxy
    * @param interfaces
    */
   protected static void pokeInterfaces(JavassistProxy proxy, Class interfaces[]) {
      proxy.interfaces = interfaces;
   }
   
   private Object readResolve() throws ObjectStreamException
   {
      System.err.println("JavassistProxy.readResolve");
      try {
         ProxyFactory proxyFactory = new ProxyFactory() {
            protected ClassLoader getClassLoader()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         };
         proxyFactory.setInterfaces(interfaces);
         proxyFactory.setSuperclass(JavassistProxy.class);
         Class proxyClass = proxyFactory.createClass();
         Constructor proxyConstructor = proxyClass.getConstructor((Class[]) null);
         JavassistProxy proxy = (JavassistProxy) proxyConstructor.newInstance((Object[]) null);
         proxy.setMethodHandler(this.handler);
         proxy.interfaces = this.interfaces;
         return proxy;
      }
      catch (NoSuchMethodException e)
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
   
   protected void setInterfaces(Class interfaces[])
   {
      // TODO: check whether I get true interfaces
      this.interfaces = interfaces;
   }
   
   protected void setMethodHandler(MethodHandler handler)
   {
      // LOL
      ((ProxyObject) this).setHandler(handler);
      this.handler = handler;
   }
   
   private Object writeReplace() throws ObjectStreamException
   {
      System.err.println("JavassistProxy.writeReplace");
      JavassistProxy replacement = new JavassistProxyReplacement();
      replacement.handler = this.handler;
      replacement.interfaces = this.interfaces;
      return replacement;
   }
}
