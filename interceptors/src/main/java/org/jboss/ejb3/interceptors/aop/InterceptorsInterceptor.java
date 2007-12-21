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

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;

/**
 * Invoke the correct spec interceptors.
 * 
 * See EJB 3 12.3.1.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorsInterceptor
{
   private static final Logger log = Logger.getLogger(InterceptorsInterceptor.class);
   
   public InterceptorsInterceptor()
   {
      //log.debug("InterceptorsInterceptor");
   }
   
   public Object invokeBusinessMethodInterceptors(Invocation invocation) throws Throwable
   {
      assert invocation instanceof MethodInvocation : "Can only have business method interceptors on a method invocation";
      Interceptor interceptors[] = InterceptorsFactory.getBusinessMethodInterceptors((MethodInvocation) invocation);
      return invocation.getWrapper(interceptors).invokeNext();
   }
   
   public Object invokeClassInterceptors(Invocation invocation) throws Throwable
   {
      Interceptor interceptors[] = InterceptorsFactory.getClassInterceptors(invocation);
      return invocation.getWrapper(interceptors).invokeNext();
   }
   
   public Object invokeDefaultInterceptors(Invocation invocation) throws Throwable
   {
      log.warn("Invoking defaults interceptors is NYI");
      log.debug("advisor " + invocation.getAdvisor().getName());
      return invocation.invokeNext();
   }
   
   public Object postConstruct(Invocation invocation) throws Throwable
   {
      log.warn("postConstruct is NYI");
      return null;
   }
}
