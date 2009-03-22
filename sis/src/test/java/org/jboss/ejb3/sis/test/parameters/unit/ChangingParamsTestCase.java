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
package org.jboss.ejb3.sis.test.parameters.unit;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jboss.ejb3.sis.Interceptor;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.jboss.ejb3.sis.test.parameters.ChangingParamsInterceptor;
import org.jboss.ejb3.sis.test.parameters.Greeter;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ChangingParamsTestCase
{
   @Test
   public void test1() throws Throwable
   {
      InvocationHandler handler = new InvocationHandler() {
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            return "Hi " + args[0];
         }
      };
      Interceptor interceptor = new ChangingParamsInterceptor();
      handler = new InterceptorInvocationHandler(handler, interceptor);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { Greeter.class };
      Greeter greeter = (Greeter) Proxy.newProxyInstance(loader, interfaces, handler);
      String result = greeter.sayHi("Fizz");
      assertEquals("Hi *Fizz*", result);
   }
}
