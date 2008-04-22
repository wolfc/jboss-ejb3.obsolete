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
package org.jboss.ejb3.test.interceptors.inheritance;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;


/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Interceptors(ClassInterceptor.class)
public class AnnotatedBean extends AnnotatedBase implements MyInterface
{
   @Interceptors(MethodInterceptor.class)
   public void method()
   {
      
   }
   
   @AroundInvoke
   public Object around(InvocationContext ctx) throws Exception
   {
      Interceptions.addAroundInvoke(AnnotatedBean.class);
      return ctx.proceed();
   }
   
   @PostConstruct
   public void postConstruct()
   {
      Interceptions.addPostConstruct(AnnotatedBean.class);
   }
   
   @PreDestroy
   public void preDestroy()
   {
      Interceptions.addPreDestroy(AnnotatedBean.class);
   }
}
