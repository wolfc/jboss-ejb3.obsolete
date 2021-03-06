/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.statefulproxyfactoryoverride;

import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;

/**
 * NoOpSessionProxyInvocationHandler
 * 
 * A Mock No-Op Invocation Handler used in testing that a custom Proxy Factory
 * has been used
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class NoOpSessionProxyInvocationHandler implements SessionProxyInvocationHandler
{
   private static final long serialVersionUID = 1L;

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      throw new NoOpException("Invoked upon " + NoOpSessionProxyInvocationHandler.class.getName());
   }

   public String getBusinessInterfaceType()
   {
      throw new UnsupportedOperationException();
   }

   public String getContainerGuid()
   {
      throw new UnsupportedOperationException();
   }

   public String getContainerName()
   {
      throw new UnsupportedOperationException();
   }

   public Interceptor[] getInterceptors()
   {
      throw new UnsupportedOperationException();
   }

   public Object getTarget()
   {
      throw new UnsupportedOperationException();
   }

   public void setBusinessInterfaceType(String businessInterfaceType)
   {
      throw new UnsupportedOperationException();  
   }

   public void setContainerGuid(String containerGuid)
   {
      throw new UnsupportedOperationException();
   }

   public void setContainerName(String containerName)
   {
      throw new UnsupportedOperationException();
   }

   public void setInterceptors(Interceptor[] interceptors)
   {
      throw new UnsupportedOperationException();      
   }

   public void setTarget(Object target)
   {
      throw new UnsupportedOperationException();
   }

}
