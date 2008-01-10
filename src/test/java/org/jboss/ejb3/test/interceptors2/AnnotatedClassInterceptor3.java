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
import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.ejb.EJBContext;
import javax.interceptor.InvocationContext;
import javax.ejb.PostActivate;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class AnnotatedClassInterceptor3 implements Serializable
{
   @Resource
   EJBContext ejbCtx;

   private static int currentInstance = 0;
   int instance = ++currentInstance;

   @AroundInvoke
   public Object intercept3(InvocationContext ctx) throws Exception
   {
      StatusRemote status = findStatusRemote();
      status.addInterception(new Interception(this, "intercept3", instance));
      return ctx.proceed();
   }

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

   @PostConstruct
   public void postConstruct3(InvocationContext ctx)
   {
      StatusRemote status = findStatusRemote();
      status.addLifecycle(PostConstruct.class, new Interception(this, "postConstruct3", instance));
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
   public void postActivate3(InvocationContext ctx)
   {
      StatusRemote status = findStatusRemote();
      status.addLifecycle(PostActivate.class, new Interception(this, "postActivate3", instance));
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
   public void prePassivate3(InvocationContext ctx)
   {
      StatusRemote status = findStatusRemote();
      status.addLifecycle(PrePassivate.class, new Interception(this, "prePassivate3", instance));
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
   public void preDestroy3(InvocationContext ctx)
   {
      StatusRemote status = findStatusRemote();
      status.addLifecycle(PreDestroy.class, new Interception(this, "preDestroy3", instance));
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
