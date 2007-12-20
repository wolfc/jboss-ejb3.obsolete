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
package org.jboss.ejb3.test.initial;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.annotation.CacheConfig;


/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 67628 $
 */
@Stateful
@Interceptors(FirstInterceptor.class)
@CacheConfig(maxSize = 1)
@Remote(InterceptedSFTest.class)
public class InterceptedSFTestBean implements InterceptedSFTest, Serializable
{
   int val;
   
   @Resource
   EJBContext ejbCtx;

   public int testMethod(int i)
   {
      System.out.println("InterceptedSFTestBean testMethod");
      System.out.println("val: " + i);
      val = i;
      return i;
   }

   public int getVal()
   {
      return val;
   }

   @Remove
   public void clear()
   {

   }

   @AroundInvoke
   public Object myInterceptor(InvocationContext ctx) throws Exception
   {
      if (ctx.getMethod().getName().equals("testMethod"))
      {
         System.out.println("Intercepting in InterceptedSFTestBean.myInterceptor()");
         int val = (Integer) ctx.getContextData().get("DATA");

         int ret = (Integer) ctx.proceed();

         ejbCtx.setRollbackOnly();
         val = (Integer) ctx.getContextData().get("DATA");
         ret += val;
         if (ctx.getTarget() != this) throw new RuntimeException("ctx.getBean() != this: " + ctx.getTarget() + " != " + this);
         return ret;
      }

      return ctx.proceed();
   }
}
