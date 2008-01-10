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
package org.jboss.ejb3.test.clientinterceptor;

import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
@Stateless
@RemoteBinding(interceptorStack="CustomStatelessSessionClientInterceptors")
public class StatelessBean implements StatelessRemote
{
   boolean interceptorFired;

   public void test()
   {
      System.out.println("test()");
      if (!interceptorFired) throw new RuntimeException("interceptor did not work");
   }

   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("intercept()");
      String asisData = (String)ctx.getContextData().get("as_is");

      if (!"AS_IS".equals(asisData))
      {
         throw new RuntimeException("Wrong test metadata: " + asisData);
      }

      NeedsMarshallingValue marshalledValue = (NeedsMarshallingValue)ctx.getContextData().get("marshalled");
      if (marshalledValue == null)
      {
         throw new RuntimeException("Null marshalled value");
      }

      if (!marshalledValue.getValue().equals("NEEDS MARSHALLING"))
      {
         throw new RuntimeException("Wrong marshalled value: " + marshalledValue.getValue());
      }

      System.out.println("values ok");
      interceptorFired = true;
      return ctx.proceed();
   }
}
