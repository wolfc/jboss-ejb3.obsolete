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
package org.jboss.ejb3.test.interceptors2;

import javax.interceptor.InvocationContext;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class XMLClassInterceptor2 extends XMLClassInterceptor3
{
   public Object intercept2(InvocationContext ctx) throws Exception
   {
      StatusBean.addInterceptionStatic(new Interception(this, "intercept2"));
      return ctx.proceed();
   }

   public void postConstruct2(InvocationContext ctx)
   {
      StatusBean.addPostConstruct(new Interception(this, "postConstruct2", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void postActivate2(InvocationContext ctx)
   {
      StatusBean.addPostActivate(new Interception(this, "postActivate2", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void prePassivate2(InvocationContext ctx)
   {
      StatusBean.addPrePassivate(new Interception(this, "prePassivate2", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void preDestroy2(InvocationContext ctx)
   {
      System.out.println("XMLClassInterceptor pd!");
      StatusBean.addPreDestroy(new Interception(this, "preDestroy2", instance));
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
