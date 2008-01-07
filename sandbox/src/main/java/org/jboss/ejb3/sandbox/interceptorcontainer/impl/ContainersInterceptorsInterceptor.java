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
package org.jboss.ejb3.sandbox.interceptorcontainer.impl;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.ejb3.sandbox.interceptorcontainer.ContainerInterceptors;
import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.logging.Logger;

/**
 * This is the interceptor that checks for ContainerInterceptors
 * and executes them.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ContainersInterceptorsInterceptor
{
   private static final Logger log = Logger.getLogger(ContainersInterceptorsInterceptor.class);
   
   private ContainerInterceptorsExecutor executor;
   
   @AroundInvoke
   public Object arroundInvoke(InvocationContext ctx) throws Exception
   {
      log.debug("ctx = " + ctx);
      return executor.aroundInvoke(ctx);
   }
   
   // FIXME: should be protected (bug in ejb3-interceptors)
   @PostConstruct
   public void postConstruct(InvocationContext ctx) throws Exception
   {
      InterceptorContainer container = (InterceptorContainer) ctx.getTarget();
      ContainerInterceptors containerInterceptors = container.getAnnotation(ContainerInterceptors.class);
      if(containerInterceptors != null)
      {
         log.debug("containerInterceptors = " + Arrays.toString(containerInterceptors.value()));
         //throw new RuntimeException("NYI");
         //container.addInterceptors(containerInterceptors.value());
         executor = new ContainerInterceptorsExecutor(container, containerInterceptors.value());
         executor.postConstruct(ctx);
      }
   }
}
