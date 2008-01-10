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
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.annotation.CacheConfig;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
@Stateful (name="MixedConfigSFSB")
@Interceptors ({MixedClassInterceptor.class})
@CacheConfig(maxSize = 1)
public class MixedConfigSFSB implements MixedConfigSFSBRemote
{
   @Resource
   EJBContext ejbCtx;

   public void test()
   {
      System.out.println("MixedConfigSFSB.test()");
   }
   
   @Interceptors ({MixedMethodInterceptor.class})
   public void testWithMethodLevel()
   {
      System.out.println();
   }
   
   @Interceptors ({MixedMethodInterceptor.class})
   public void testWithMethodLevelB()
   {
      System.out.println();
   }
   
   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("MixedConfigSFSB intercepting!");
      StatusRemote status = findStatusRemote();
      status.addInterception(new Interception(this, "intercept"));
      return ctx.proceed();
   }

   @Remove
   public void kill()
   {
      
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

   

}
