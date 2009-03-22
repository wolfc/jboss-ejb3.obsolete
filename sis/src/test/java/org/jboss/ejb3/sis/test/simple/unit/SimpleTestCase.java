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
package org.jboss.ejb3.sis.test.simple.unit;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.ejb3.sis.Interceptor;
import org.jboss.ejb3.sis.NoopInterceptor;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleTestCase
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
      Interceptor interceptor = new NoopInterceptor();
      handler = new InterceptorInvocationHandler(handler, interceptor);
      Object proxy = null;
      Method method = Object.class.getDeclaredMethod("toString");
      Object args[] = null;
      String result = (String) handler.invoke(proxy, method, args);
      assertEquals("nothing", result);
   }
}
