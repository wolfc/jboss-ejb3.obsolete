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
 * @version $Revision: 61136 $
 */
public class XMLClassInterceptor3
{
   static int currentInstance;
   int instance = ++currentInstance;

   public Object intercept3(InvocationContext ctx) throws Exception
   {
      StatusBean.addInterceptionStatic(new Interception(this, "intercept3"));
      return ctx.proceed();
   }

   public void postConstruct3(InvocationContext ctx)
   {
      StatusBean.addPostConstruct(new Interception(this, "postConstruct3", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void postActivate3(InvocationContext ctx)
   {
      StatusBean.addPostActivate(new Interception(this, "postActivate3", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void prePassivate3(InvocationContext ctx)
   {
      StatusBean.addPrePassivate(new Interception(this, "prePassivate3", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void preDestroy3(InvocationContext ctx)
   {
      System.out.println("XMLClassInterceptor pd!");
      StatusBean.addPreDestroy(new Interception(this, "preDestroy3", instance));
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
