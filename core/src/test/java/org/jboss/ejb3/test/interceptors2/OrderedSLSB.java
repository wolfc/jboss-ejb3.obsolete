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

import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.ejb.Stateless;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
@Interceptors({AnnotatedClassInterceptor3.class, XMLClassInterceptor3.class})
@Stateless(name="OrderedSLSB")
public class OrderedSLSB implements OrderedSLSBRemote
{

   public void methodWithClassLevel()
   {
      
   }

   @ExcludeClassInterceptors
   @ExcludeDefaultInterceptors
   @Interceptors({AnnotatedMethodInterceptor.class, XMLMethodInterceptor.class})
   public void methodWithOwn(String s, int i)
   {
      
   }
   
   @Interceptors({AnnotatedMethodInterceptor.class, XMLMethodInterceptor.class, MixedMethodInterceptor.class})
   public void overLoadedMethod(String s)
   {
      
   }
   
   @Interceptors({AnnotatedMethodInterceptor.class, XMLMethodInterceptor.class, MixedMethodInterceptor.class})
   public void overLoadedMethod(long l)
   {
      
   }
   
   @ExcludeClassInterceptors
   @Interceptors({MixedMethodInterceptor.class})
   public void overLoadedMethod()
   {
      
   }
   
   @Interceptors({AnnotatedMethodInterceptor.class, XMLMethodInterceptor.class, MixedMethodInterceptor.class})
   public void methodNotSpecifyingAll()
   {
      
   }
}
