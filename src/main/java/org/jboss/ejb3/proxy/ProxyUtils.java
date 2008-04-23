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
package org.jboss.ejb3.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.asynch.AsynchMixin;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.asynch.AsynchProxyInterceptor;
import org.jboss.aspects.asynch.FutureHolder;
import org.jboss.ejb3.asynchronous.AsynchronousInterceptor;
import org.jboss.ejb3.proxy.handler.ProxyInvocationHandler;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class ProxyUtils
{
   public final static Class<AsynchProvider> ASYNCH_PROVIDER_CLASS = AsynchProvider.class;
   public final static long GET_ASYNCHRONOUS;
   public static final long TO_STRING;
   public static final long EQUALS;
   public static final long HASHCODE;
   public static final Method GET_PRIMARY_KEY;
   public static final Method GET_HANDLE;
   public static final Method GET_EJB_HOME;
   public static final Method IS_IDENTICAL;
   public static final Method GET_HOME_HANDLE;
   public static final Method GET_EJB_METADATA;
   public static final Method REMOVE;

   static
   {
      try
      {
         Class<?>[] empty = {};
         
         Method method = JBossProxy.class.getMethod("getAsynchronousProxy", empty);
         GET_ASYNCHRONOUS = MethodHashing.calculateHash(method);
         TO_STRING = MethodHashing.calculateHash(Object.class.getDeclaredMethod("toString", empty));
         EQUALS = MethodHashing.calculateHash(Object.class.getDeclaredMethod("equals", new Class<?>[]{Object.class}));
         HASHCODE = MethodHashing.calculateHash(Object.class.getDeclaredMethod("hashCode", empty));
               
         GET_PRIMARY_KEY = EJBObject.class.getMethod("getPrimaryKey", empty);
         GET_HANDLE = EJBObject.class.getMethod("getHandle", empty);
         GET_EJB_HOME = EJBObject.class.getMethod("getEJBHome", empty);
         IS_IDENTICAL = EJBObject.class.getMethod("isIdentical", new Class<?>[] { EJBObject.class });
         REMOVE = EJBObject.class.getMethod("remove", empty);
                 
         GET_HOME_HANDLE = EJBHome.class.getMethod("getHomeHandle", empty);
         GET_EJB_METADATA = EJBHome.class.getMethod("getEJBMetaData", empty);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static boolean isAsynchronous(Class<?>[] infs)
   {
      for (int i = 0; i < infs.length; i++)
      {
         if (infs[i] == ASYNCH_PROVIDER_CLASS)
         {
            return true;
         }
      }
      return false;
   }

   public static Class<?>[] addAsynchProviderInterface(Class<?>[] infs)
   {
      ArrayList<Class<?>> interfaces = new ArrayList<Class<?>>();

      for (int i = 0; i < infs.length; i++)
      {
         if (infs[i] == ASYNCH_PROVIDER_CLASS)
         {
            //This should not happen
            continue;
         }
         interfaces.add(infs[i]);
      }

      interfaces.add(ASYNCH_PROVIDER_CLASS);
      return (Class<?>[]) interfaces.toArray(new Class<?>[interfaces.size()]);
   }

   public static Interceptor[] addAsynchProxyInterceptor(AsynchMixin mixin, Interceptor[] interceptors)
   {
      AsynchProxyInterceptor interceptor = new AsynchProxyInterceptor(mixin);
      Interceptor[] newInterceptors = null;
      newInterceptors = new Interceptor[interceptors.length + 1];
      newInterceptors[0] = interceptor;
      System.arraycopy(interceptors, 0, newInterceptors, 1, interceptors.length);
      return newInterceptors;
   }

   public static void addLocalAsynchronousInfo(MethodInvocation invocation, FutureHolder provider)
   {
      if (provider != null)
      {
         invocation.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH, AsynchronousInterceptor.INVOKE_ASYNCH, "YES", PayloadKey.AS_IS);
         invocation.getMetaData().addMetaData(AsynchronousInterceptor.ASYNCH, AsynchronousInterceptor.FUTURE_HOLDER, provider, PayloadKey.AS_IS);
      }
   }

   public static Object handleCallLocally(Object jbproxy, ProxyInvocationHandler ih, Method m, Object[] args)
   {
      long hash = MethodHashing.calculateHash(m);
      return handleCallLocally(hash, jbproxy, ih, m, args);
   }

   public static Object handleCallLocally(long hash, Object jbproxy, ProxyInvocationHandler ih, Method m, Object[] args)
   {
      if (hash == ProxyUtils.GET_ASYNCHRONOUS)
      {
         return ih.getAsynchronousProxy((JBossProxy)jbproxy);
      }
      else if (hash == TO_STRING)
      {
         return ih.toString();
      }
      else if (hash == HASHCODE)
      {
         return new Integer(ih.toString().hashCode());
      }
      else if (hash == EQUALS)
      {
         return new Boolean(ih.toString().equals(args[0].toString()));
      } 
      return null;
   }
}
