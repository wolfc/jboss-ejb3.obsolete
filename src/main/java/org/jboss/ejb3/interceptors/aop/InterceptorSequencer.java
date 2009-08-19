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

import java.util.Arrays;
import java.util.List;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

/**
 * Invokes some interceptors in sequence.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class InterceptorSequencer implements Interceptor 
{
   private static final Logger log = Logger.getLogger(InterceptorSequencer.class);
   
   private Interceptor[] interceptors;
   
   public InterceptorSequencer(List<Interceptor> interceptors)
   {
      this(interceptors.toArray(new Interceptor[0]));
   }
   
   public InterceptorSequencer(Interceptor interceptors[])
   {
      assert interceptors != null;
      
      //log.debug("InterceptorSequencer");
      this.interceptors = interceptors;
   }
   
   public Object aroundInvoke(Invocation invocation) throws Throwable
   {
      if (log.isTraceEnabled())
      {
         log.trace("aroundInvoke " + invocation);
      }
      return invoke(invocation);
   }
   
   public String getName()
   {
      // TODO: might need a dynamic name
      return "InterceptorSequence";
   }

   @Deprecated
   public Object invoke(Invocation invocation) throws Throwable
   {
      if(log.isTraceEnabled()) log.trace("interceptors " + Arrays.toString(interceptors));
      Invocation newInvocation = invocation.getWrapper(interceptors);
      return newInvocation.invokeNext();
   }
   
   public Object postConstruct(Invocation invocation) throws Throwable
   {
      return invoke(invocation);
   }
   
   @Override
   public String toString()
   {
      return super.toString() + "[interceptors=" + Arrays.toString(interceptors) + "]";
   }
}
