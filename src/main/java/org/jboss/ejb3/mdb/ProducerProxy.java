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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ProducerProxy implements InvocationHandler, Serializable
{
   private static final long serialVersionUID = -2077140072365253007L;
   
   protected ProducerManager producer;
   protected Interceptor[] interceptors;

   public ProducerProxy(ProducerManager producer, Interceptor[] interceptors)
   {
      this.producer = producer;
      this.interceptors = interceptors;
   }

   public ProducerProxy()
   {
   }


   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      String methodName = method.getName();
      if (methodName.equals("getProducerManager"))
      {
         return producer;
      }
      else if(methodName.equals("toString"))
         return toString();
      long hash = MethodHashing.calculateHash(method);
      MethodInvocation sri = new MethodInvocation(interceptors, hash, method, method, null);
      sri.setArguments(args);
      return sri.invokeNext();
   }
}
