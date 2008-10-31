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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.annotation.Pool;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
@Stateless
@Remote(AnnotatedOnlySLSBRemote.class)
@Interceptors(AnnotatedClassInterceptor.class)
@ExcludeDefaultInterceptors
// The default is ThreadlocalPool, which could lead to new constructs. Same goes for maxSize.
@Pool(value="StrictMaxPool", maxSize=1)
public class AnnotatedOnlySLSB implements AnnotatedOnlySLSBRemote
{
   @EJB
   StatusRemote status;

   public void methodWithClassLevel()
   {
      System.out.println("AnnotatedOnlySLSB.methodWithClassLevel");
   }

   @ExcludeClassInterceptors
   public void methodExcludingClassInterceptors()
   {
      System.out.println("AnnotatedOnlySLSB.methodExcludingClassInterceptors");
   }

   @Interceptors(AnnotatedMethodInterceptor.class)
   public void methodWithOwnInterceptors()
   {
      System.out.println("AnnotatedOnlySLSB.methodWithOwnInterceptors");
   }

   @Interceptors(AnnotatedMethodInterceptor.class)
   @ExcludeClassInterceptors
   public void methodWithOwnInterceptorsExcludeClass()
   {
      System.out.println("AnnotatedOnlySLSB.methodWithOwnInterceptors");
   }

   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("AnnotatedOnlySLSB intercepting!");
      status.addInterception(new Interception(this, "intercept"));
      return ctx.proceed();
   }

   @PostConstruct
   void postConstruct()
   {
      System.out.println("AnnotatedOnlySLSB postConstruct " + this);
      StatusBean.addPostConstruct(new Interception(this, "postConstruct"));
   }

   @PreDestroy()
   void preDestroy()
   {
      System.out.println("AnnotatedOnlySLSB preDestroy! " + this);
      StatusBean.addPreDestroy(new Interception(this, "preDestroy"));
   }
}
