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

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import javax.naming.InitialContext;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class XMLOnlySLSB implements XMLOnlySLSBRemote
{
   private static final Logger log = Logger.getLogger(XMLOnlySLSB.class);
   
   @Resource
   EJBContext ejbCtx;

   public void methodWithClassLevel()
   {
      System.out.println("XMLOnlySLSB.methodWithClassLevel");
   }

   public void overloadedMethod(long l)
   {
      System.out.println("XMLOnlySLSB.overloadedMethod WithClassLevelExcludingDefault");
   }

   public void overloadedMethod(long l, String[][] s)
   {
      System.out.println("overloadedMethod (WithOwnInterceptors)");
   }

   public void overloadedMethod(int i)
   {
      System.out.println("overloadedMethod (WithOwnInterceptorsExcludeClass)");
   }

   public void overloadedMethod()
   {
      System.out.println("overloadedMethod (WithOwnInterceptorsExcludeClassAndDefault)");
   }

   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("XMLOnlySLSB intercepting!");
      //TODO ejbCtx injection
 //     StatusRemote status = (StatusRemote)ejbCtx.lookup("StatusBean/remote");
      StatusRemote status = (StatusRemote)new InitialContext().lookup("StatusBean/remote");
      status.addInterception(new Interception(this, "intercept"));
      return ctx.proceed();
   }

   void postConstruct()
   {
      System.out.println("XMLOnlySLSB postConstruct");
      StatusBean.addPostConstruct(new Interception(this, "postConstruct"));
   }

   void preDestroy()
   {
      System.out.println("XMLOnlySLSB preDestroy!");
      StatusBean.addPreDestroy(new Interception(this, "preDestroy"));
   }
}
