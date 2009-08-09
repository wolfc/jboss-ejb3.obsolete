/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.proxy.impl.remoting;

import java.io.Serializable;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.logging.Logger;

/**
 * Routes the call to the local container, bypassing further client-side
 * interceptors and any remoting layer, if this interceptor was created
 * in this JVM.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 *
 * @version $Revision: 61667 $
 */
public class IsLocalProxyFactoryInterceptor implements Interceptor, Serializable
{
   private static final long serialVersionUID = -1264055696758370812L;

   // Important implementation note : The order of the class field initialization
   // (i.e. static fields) is important. This "stamp" field needs to be initialized
   // *before* the static "singleton" field.
   //
   // If singleton is declared before "stamp" then it will cause the following bug
   //
   // public static final IsLocalProxyFactoryInterceptor singleton = new IsLocalProxyFactoryInterceptor();
   // private static final long stamp = System.currentTimeMillis();
   // private long marshalledStamp = stamp;
   //
   // 1) Default class initialization occurs and all static fields including "stamp" are set to default values
   // 2) So at this point stamp = 0
   // 3) Then class field intializer blocks are executed. Since "singleton" field intialization comes
   //   before "stamp" field initialization, the new IsLocalProxyFactotyInterceptor gets called.
   //   This results in object field initialization where the "marshalledStamp" is first set to
   //   default value of 0 and then the object field initializer is called. The object field initializer
   //   then sets the "marshalledStamp" to the value of "stamp" which is 0 (as per #2) because the static field
   //   initializer for "stamp" has not yet executed.
   // 4) So ultimately "marshalledStamp" will hold 0.
   // 5) After the class field intializer block for "singleton" field completes, it moves onto class field
   //   initialization of "stamp" and sets the value of "stamp" to the current system time (=xxxxx)
   // 6) So ultimately after this complete initialization process, the values of marshalledStamp = 0
   //   and that of stamp = xxxx
   // 7) At a later point of time when isLocal() compares these 2 values(through stamp == marshalledStamp) it always returns
   //   false and hence all calls are considered remote.
   //
   // So the rule is - declare class field "stamp" before the class field "singleton"
   private static final long stamp = System.currentTimeMillis();

   public static final IsLocalProxyFactoryInterceptor singleton = new IsLocalProxyFactoryInterceptor();

   private static final Logger log = Logger.getLogger(IsLocalProxyFactoryInterceptor.class);

   private long marshalledStamp = stamp;

   public String getName()
   {
      return getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      if (isLocal())
      {
         Object oid = invocation.getMetaData(Dispatcher.DISPATCHER, Dispatcher.OID);
         if (Dispatcher.singleton.isRegistered(oid))
         {
            InvocationResponse response = Dispatcher.singleton.invoke(invocation);
            invocation.setResponseContextInfo(response.getContextInfo());
            log.debug("Local invocation, handling locally via current Dispatcher");
            return response.getResponse();
         }
      }
      log.debug("NOT a local invocation, passing the control to next interceptor");
      return invocation.invokeNext();
   }

   private boolean isLocal()
   {
      return stamp == marshalledStamp;
   }
}
