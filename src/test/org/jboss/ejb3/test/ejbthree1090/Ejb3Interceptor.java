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
package org.jboss.ejb3.test.ejbthree1090;

import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 57207 $
 */
public class Ejb3Interceptor implements Serializable
{
   private static final Logger log = Logger.getLogger(Ejb3Interceptor.class);
   
   private static boolean called = false;
   
   public Ejb3Interceptor()
   {
   }
   
   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      try
      {
         log.info("*** Intercepting in Ejb3Interceptor");
         // This is to skip the second instance of the interceptor due to @Interceptors being TYPE and METHOD
         if (called)
         {
            return ctx.proceed();
         }
         else
         {
            called = true;
            return "Ejb3 Intercepted" + ctx.proceed();
         }
      }
      finally
      {
      }
   }
}
