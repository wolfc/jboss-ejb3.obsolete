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
package org.jboss.ejb3.sis;

import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorAssembly implements Interceptor
{
   private Interceptor[] interceptors;
   
   public InterceptorAssembly(Interceptor interceptors[])
   {
      assert interceptors != null : "interceptors = null";
      this.interceptors = interceptors;
   }
   
   public Object invoke(final InvocationContext context) throws Exception
   {
      InvocationContext current = new InvocationContext() {
         private int currentInterceptor = 0;
         
         public Map<String, Object> getContextData()
         {
            return context.getContextData();
         }
         
         public Method getMethod()
         {
            return context.getMethod();
         }
         
         public Object[] getParameters()
         {
            return context.getParameters();
         }
         
         public Object getTarget()
         {
            return context.getTarget();
         }
         
         public Object proceed() throws Exception
         {
            if(currentInterceptor < interceptors.length)
            {
               try
               {
                  return interceptors[currentInterceptor++].invoke(this);
               }
               finally
               {
                  // so that interceptors like clustering can reinvoke down the chain
                  currentInterceptor--;
               }
            }
            return context.proceed();
         }
         
         public void setParameters(Object[] params)
         {
            context.setParameters(params);
         }
      };
      return current.proceed();
   }
}
