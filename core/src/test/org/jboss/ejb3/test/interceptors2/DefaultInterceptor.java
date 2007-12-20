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

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class DefaultInterceptor implements Serializable
{
   @Resource
   EJBContext ejbCtx;

   static int currentInstance;
   int instance = ++currentInstance;

   private StatusRemote findStatusRemote()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
         return status;
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }


   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("DefaultInterceptor intercepting!");
      StatusRemote status = findStatusRemote();
      status.addInterception(new Interception(this, "intercept", instance));
      return ctx.proceed();
   }

   public void postConstruct(InvocationContext ctx)
   {
      System.out.println("DefaultInterceptor pc!");
      StatusBean.addPostConstruct(new Interception(this, "postConstruct", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void postActivate(InvocationContext ctx) throws Exception
   {
      System.out.println("DefaultInterceptor pa!");
      StatusBean.addPostActivate(new Interception(this, "postActivate", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void prePassivate(InvocationContext ctx)
   {
      System.out.println("DefaultInterceptor pp!");
      StatusBean.addPrePassivate(new Interception(this, "prePassivate", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void preDestroy(InvocationContext ctx)
   {
      System.out.println("DefaultInterceptor pd!");
      StatusBean.addPreDestroy(new Interception(this, "preDestroy", instance));
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
