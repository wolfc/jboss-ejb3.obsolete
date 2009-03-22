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
package org.jboss.ejb3.sis.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.jboss.ejb3.sis.Interceptor;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorInvocationHandler implements InvocationHandler
{
   private InvocationHandler handler;
   private Interceptor interceptor;
   
   public InterceptorInvocationHandler(InvocationHandler handler, Interceptor interceptor)
   {
      assert handler != null : "handler is null";
      assert interceptor != null : "interceptor is null";
      
      this.handler = handler;
      this.interceptor = interceptor;
   }
   
   /* (non-Javadoc)
    * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
   {
      final Map<String, Object> contextData = new HashMap<String, Object>();
      InvocationContext context = new InvocationContext() {
         private Object[] parameters = args;
         
         public Map<String, Object> getContextData()
         {
            return contextData;
         }

         public Method getMethod()
         {
            return method;
         }

         public Object[] getParameters()
         {
            return args;
         }

         public Object getTarget()
         {
            return proxy;
         }

         public Object proceed() throws Exception
         {
            try
            {
               return handler.invoke(proxy, method, parameters);
            }
            catch(Error e)
            {
               throw e;
            }
            catch(RuntimeException e)
            {
               throw e;
            }
            catch(Exception e)
            {
               throw e;
            }
            catch(Throwable t)
            {
               // should not happen
               throw new RuntimeException(t);
            }
         }

         public void setParameters(Object[] params)
         {
            this.parameters = params;
         }
      };
      return interceptor.invoke(context);
   }
}
