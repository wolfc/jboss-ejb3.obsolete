/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.interceptors.basic;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.test.interceptors.instances.StatefulInterceptor;
import org.jboss.logging.Logger;

/**
 * The basic bean has one class interceptor: BasicInterceptor
 * 
 * On the intercepter method there is a business method interceptor: BasicMethodInterceptor
 * 
 * And it has bean method interceptors: aroundInvoke
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@Interceptors(BasicInterceptor.class)
public class BasicBean
{
   private static final Logger log = Logger.getLogger(BasicBean.class);
   
   public static int constructors = 0, aroundInvokes = 0;
   
   public BasicBean()
   {
      log.debug("BasicBean");
      constructors++;
   }
   
   @AroundInvoke
   Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      log.debug("aroundInvoke " + ctx);
      if(ctx.getTarget() != this)
         throw new IllegalStateException("target is not this");
      if(ctx.getMethod().getDeclaringClass() != getClass())
         throw new IllegalStateException("method " + ctx.getMethod() + " not of this class (" + ctx.getMethod().getDeclaringClass() + " != " +  getClass() + ")");
      aroundInvokes++;
      return ctx.proceed();
   }
   
   public String sayHi(String name)
   {
      log.debug("sayHi");
      return "Hi " + name;
   }
   
   @Interceptors(BasicMethodInterceptor.class)
   public void intercept()
   {
      log.debug("intercept");
   }
   
   // State check
   
   @Interceptors(StatefulInterceptor.class)
   public int getState()
   {
      throw new RuntimeException("Should not get here");
   }
   
   @Interceptors(StatefulInterceptor.class)
   public void setState(int state)
   {
      throw new RuntimeException("Should not get here");
   }
}
