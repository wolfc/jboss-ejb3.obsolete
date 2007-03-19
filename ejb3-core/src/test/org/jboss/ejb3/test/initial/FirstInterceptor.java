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

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.PostActivate;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class FirstInterceptor implements Serializable{

   @AroundInvoke
   public Object oneMethod(InvocationContext ctx) throws Exception
   {
      if (ctx.getMethod().getName().equals("testMethod"))
      {
         System.out.println("Intercepting in FirstInterceptor.oneMethod()");
         int i = (Integer)ctx.getParameters()[0];
         ctx.getContextData().put("DATA", new Integer(i + 1000));
      }
      return ctx.proceed();
   }

   @PostConstruct
   public void postConstruct(InvocationContext ctx)
   {
      TestStatusBean.postConstruct = true;
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @PreDestroy
   public void preDestroy(InvocationContext ctx)
   {
      TestStatusBean.preDestroy = true;
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @PostActivate
   public void postActivate(InvocationContext ctx)
   {
      TestStatusBean.postActivate = true;
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @PrePassivate
   public void prePassivate(InvocationContext ctx)
   {
      TestStatusBean.prePassivate = true;
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
