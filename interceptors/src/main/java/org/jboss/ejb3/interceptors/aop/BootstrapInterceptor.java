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
package org.jboss.ejb3.interceptors.aop;

import javax.interceptor.Interceptors;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

/**
 * The mother of interceptors
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BootstrapInterceptor implements Interceptor
{
   private static final Logger log = Logger.getLogger(BootstrapInterceptor.class);
   
   public String getName()
   {
      return "BootstrapInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      log.debug("invoke " + invocation);
      log.debug("  " + invocation.getAdvisor());
      log.debug("  " + invocation.getTargetObject());
      Interceptors interceptors = (Interceptors) invocation.getAdvisor().resolveAnnotation(Interceptors.class);
      assert interceptors != null : "interceptors annotation not found";
      return invocation.invokeNext();
   }

}
