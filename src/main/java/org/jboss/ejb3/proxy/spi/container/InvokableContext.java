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
package org.jboss.ejb3.proxy.spi.container;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.ejb3.common.lang.SerializableMethod;

/**
 * InvokableContext
 * 
 * Represents any object capable of carrying out 
 * generic Invocations as described by a 
 * ContainerMethodInvocation descriptor
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 * @deprecated In favor of ejb3-endpoint
 */
@Deprecated
public interface InvokableContext
{
   /**
    * Invokes the method described by the specified serializable method
    * as called from the specified proxy, using the specified arguments
    * 
    * @param proxy The proxy making the invocation
    * @param method The method to be invoked
    * @param args The arguments to the invocation
    * @throws Throwable A possible exception thrown by the invocation
    * @return
    */
   Object invoke(Object proxy, SerializableMethod method, Object[] args) throws Throwable;

   /**
    * Invocation point of entry for Remoting
    * 
    * @param invocation
    * @return
    * @throws Throwable
    */
   InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable;
}
