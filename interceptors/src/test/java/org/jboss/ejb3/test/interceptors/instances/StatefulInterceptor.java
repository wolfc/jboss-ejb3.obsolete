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
package org.jboss.ejb3.test.interceptors.instances;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * The stateful interceptor will retain state, so we can make
 * sure we have one instance per bean.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatefulInterceptor
{
   public static int postConstructs = 0;
   private boolean constructed = false;
   private int state;
   
   @AroundInvoke
   public Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      Method method = ctx.getMethod();
      String methodName = method.getName();
      if(methodName.equals("getState"))
         return getState();
      else if(methodName.equals("setState"))
      {
         setState((Integer) ctx.getParameters()[0]);
         return null;
      }
      else
         return ctx.proceed();
   }

   private int getState()
   {
      return state;
   }

   @PostConstruct
   public void postConstruct(InvocationContext ctx) throws Exception
   {
      // Make sure postConstruct only gets called once (else it will probably be called on the wrong instance)
      if(constructed)
         throw new IllegalStateException(this + " is already constructed");
      constructed = true;
      
      postConstructs++;
   }
   
   private void setState(int state)
   {
      this.state = state;
   }
}
