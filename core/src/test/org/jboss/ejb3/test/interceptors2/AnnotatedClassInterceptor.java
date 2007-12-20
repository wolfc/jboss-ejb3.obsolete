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

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.PostActivate;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class AnnotatedClassInterceptor extends AnnotatedClassInterceptor2
{
   @AroundInvoke
   public Object intercept(InvocationContext inv) throws Exception
   {
      System.out.println("AnnotatedClassInterceptor intercepting!");
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      status.addInterception(new Interception(this, "intercept", instance));
      return inv.proceed();
   }

   @PostConstruct
   public void postConstruct(InvocationContext inv)
   {
      StatusRemote status = null;
      status = findStatusRemote();
      status.addLifecycle(PostConstruct.class, new Interception(this, "postConstruct", instance));
      try
      {
         inv.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @PostActivate
   public void postActivate(InvocationContext inv)
   {
      StatusRemote status = null;
      status = findStatusRemote();
      status.addLifecycle(PostActivate.class, new Interception(this, "postActivate", instance));
      try
      {
         inv.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private StatusRemote findStatusRemote()
   {
      StatusRemote status;
      try
      {
         InitialContext ctx = new InitialContext();
         status = (StatusRemote)ctx.lookup("StatusBean/remote");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      return status;
   }

   @PrePassivate
   public void prePassivate(InvocationContext ctx)
   {
      StatusRemote status = findStatusRemote();
      status.addLifecycle(PrePassivate.class, new Interception(this, "prePassivate", instance));
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
      StatusRemote status = findStatusRemote();
      status.addLifecycle(PreDestroy.class, new Interception(this, "preDestroy", instance));
      try
      {
         ctx.proceed();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public Object intercept2(InvocationContext ctx) throws Exception
   {
      throw new RuntimeException("Should not be called");
   }

   public void postConstruct2(InvocationContext ctx)
   {
      throw new RuntimeException("Should not be called");
   }

   public void postActivate2(InvocationContext ctx)
   {
      throw new RuntimeException("Should not be called");
   }

   public void prePassivate2(InvocationContext ctx)
   {
      throw new RuntimeException("Should not be called");
   }

   public void preDestroy2(InvocationContext ctx)
   {
      throw new RuntimeException("Should not be called");
   }

}
