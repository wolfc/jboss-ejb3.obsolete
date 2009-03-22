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
package org.jboss.ejb3.sis.test.assembly.unit;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.ejb3.sis.Interceptor;
import org.jboss.ejb3.sis.InterceptorAssembly;
import org.jboss.ejb3.sis.NoopInterceptor;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.jboss.ejb3.sis.test.assembly.BouncingInterceptor;
import org.jboss.ejb3.sis.test.common.RegisteringInterceptor;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorAssemblyTestCase
{
   @Test
   public void test1() throws Throwable
   {
      InvocationHandler handler = new InvocationHandler() {
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            return "nothing";
         }
      };
      Interceptor interceptors[] = { new NoopInterceptor() };
      Interceptor interceptor = new InterceptorAssembly(interceptors);
      handler = new InterceptorInvocationHandler(handler, interceptor);
      Object proxy = null;
      Method method = Object.class.getDeclaredMethod("toString");
      Object args[] = null;
      String result = (String) handler.invoke(proxy, method, args);
      assertEquals("nothing", result);
   }  

   @Test
   public void test2() throws Throwable
   {
      InvocationHandler handler = new InvocationHandler() {
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            return "nothing";
         }
      };
      Interceptor interceptors[] = { new RegisteringInterceptor("A"), new RegisteringInterceptor("B") };
      Interceptor interceptor = new InterceptorAssembly(interceptors);
      handler = new InterceptorInvocationHandler(handler, interceptor);
      Object proxy = null;
      Method method = Object.class.getDeclaredMethod("toString");
      Object args[] = null;
      String result = (String) handler.invoke(proxy, method, args);
      assertEquals("ABnothing", result);
   }  

   @Test
   public void testBounce() throws Throwable
   {
      InvocationHandler handler = new InvocationHandler() {
         int numInvokes = 0;
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            numInvokes++;
            if(numInvokes < 2)
               throw new Exception("Again!");
            return "nothing" + numInvokes;
         }
      };
      Interceptor interceptors[] = { new RegisteringInterceptor("A"), new BouncingInterceptor(), new RegisteringInterceptor("B") };
      Interceptor interceptor = new InterceptorAssembly(interceptors);
      handler = new InterceptorInvocationHandler(handler, interceptor);
      Object proxy = null;
      Method method = Object.class.getDeclaredMethod("toString");
      Object args[] = null;
      String result = (String) handler.invoke(proxy, method, args);
      assertEquals("ABnothing2", result);
   }  
}
