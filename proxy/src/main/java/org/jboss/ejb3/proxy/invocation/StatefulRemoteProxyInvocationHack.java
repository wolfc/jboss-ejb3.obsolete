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
package org.jboss.ejb3.proxy.invocation;

import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.stateful.StatefulRemoteInvocation;
import org.jboss.remoting.InvokerLocator;

/**
 * StatefulRemoteProxyInvocationHack
 * 
 * Constructs a Proxy to the Container using an underlying
 * StatefulRemoteInvocation when invocations are made.
 * 
 * Looking forward, should be using a more flexible
 * invocation mechanism to handle SFSB, SLSB, etc invocations
 * in an agnostic manner.  This is put into place to avoid
 * further refactoring within EJB3 Core 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 * @deprecated 
 */
@Deprecated
public class StatefulRemoteProxyInvocationHack extends PojiProxy
{

   public StatefulRemoteProxyInvocationHack(Object oid, InvokerLocator uri, Interceptor[] interceptors)
   {
      super(oid, uri, interceptors);
   }

   /**
    * Constructs a MethodInvocation from the specified Method and
    * arguments
    * 
    * This implementation uses a StatefulRemoteInvocation as the underlying 
    * Invocation made, in order to support legacy EJB3 Core Containers
    * 
    * @param method
    * @param args
    * @return
    */
   @Override
   protected MethodInvocation constructMethodInvocation(Method method, Object[] args)
   {
      long hash = MethodHashing.calculateHash(method);
      MethodInvocation sri = new StatefulRemoteInvocation(this.getInterceptors(), hash, method, method, null, null);
      return sri;
   }

}
