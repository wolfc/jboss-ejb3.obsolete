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
package org.jboss.ejb3.remoting2.test.clientinterceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting2.test.common.MockRemotable;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
//@Interceptors(SimpleInterceptorServerSide.class)
public class InterceptedMockRemotable extends MockRemotable
{
   private Interceptor interceptor = new SimpleInterceptorServerSide();
   
   @Override
   public Serializable getId()
   {
      return "InterceptedMockRemotable";
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.remoting2.test.common.MockRemotable#invoke(java.io.Serializable, java.util.Map, org.jboss.ejb3.common.lang.SerializableMethod, java.lang.Object[])
    */
   @Override
   public Object invoke(final Serializable session, final Map<String, Object> contextData, SerializableMethod method, final Object[] args)
      throws Throwable
   {
      final Method realMethod = method.toMethod(getClassLoader());
      // emulate an interception
      InvocationContext context = new InvocationContext() {

         public Map<String, Object> getContextData()
         {
            return contextData;
         }

         public Method getMethod()
         {
            return realMethod;
         }

         public Object[] getParameters()
         {
            return args;
         }

         public Object getTarget()
         {
            // TODO: for real?
            return session;
         }

         public Object proceed() throws Exception
         {
            try
            {
               return realMethod.invoke(InterceptedMockRemotable.this, args);
            }
            catch(IllegalArgumentException e)
            {
               throw new IllegalArgumentException("can't invoke " + realMethod + " on " + this, e);
            }
         }

         public void setParameters(Object[] params)
         {
            throw new RuntimeException("NYI");
         }
      };
      return interceptor.invoke(context);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.remoting2.test.common.MockRemotable#sayHi(java.lang.String)
    */
   @Override
   public String sayHi(String name)
   {
      return "Hi " + name + " " + Current.getState();
   }
}
