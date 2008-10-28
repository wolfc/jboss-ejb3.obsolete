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
package org.jboss.ejb3.test.interceptors.common;

import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

/**
 * A common interceptor without annotation, does nothing useful.
 * 
 * Can be used by an annotated interceptor, or one from metadata.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class CommonInterceptor
{
   private static final Logger log = Logger.getLogger(CommonInterceptor.class);
   
   public static int preDestroys = 0, postConstructs = 0, aroundInvokes = 0;
   
   public void preDestroy(InvocationContext ctx) throws Exception
   {
      log.debug("preDestroy " + ctx);
      InterceptorChain.add(getClass());
      preDestroys++;
      ctx.proceed();
   }
   
   public void postConstruct(InvocationContext ctx) throws Exception
   {
      log.debug("postConstruct " + ctx);
      if(ctx.getTarget() == null)
         throw new IllegalStateException("target is null");
      InterceptorChain.add(getClass());
      postConstructs++;
      ctx.proceed();
   }
   
   public Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      log.debug("aroundInvoke " + ctx);
      InterceptorChain.add(getClass());
      aroundInvokes++;
      return ctx.proceed();
   }
}
