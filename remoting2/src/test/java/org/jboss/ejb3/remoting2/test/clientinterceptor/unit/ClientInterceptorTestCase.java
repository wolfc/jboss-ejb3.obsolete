/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.remoting2.test.clientinterceptor.unit;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.jboss.ejb3.remoting.endpoint.client.RemoteInvocationHandlerInvocationHandler;
import org.jboss.ejb3.remoting2.client.RemoteInvocationHandler;
import org.jboss.ejb3.remoting2.test.clientinterceptor.Current;
import org.jboss.ejb3.remoting2.test.clientinterceptor.InterceptedMockRemotable;
import org.jboss.ejb3.remoting2.test.clientinterceptor.RemoteContextDataInterceptor;
import org.jboss.ejb3.remoting2.test.clientinterceptor.SimpleInterceptorClientSide;
import org.jboss.ejb3.remoting2.test.common.AbstractRemotingTestCaseSetup;
import org.jboss.ejb3.remoting2.test.common.MockInterface;
import org.jboss.ejb3.sis.Interceptor;
import org.jboss.ejb3.sis.InterceptorAssembly;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ClientInterceptorTestCase extends AbstractRemotingTestCaseSetup
{
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      AbstractRemotingTestCaseSetup.beforeClass();
      
      install(InterceptedMockRemotable.class);
   }
   
   private <T> T createRemoteProxy(Client client, Serializable oid, Class<T> businessInterface, org.jboss.ejb3.sis.Interceptor interceptor)
   {
      RemoteInvocationHandler delegate = new RemoteInvocationHandler(client, oid);
      
      // assume we're talking to a singleton
      Serializable session = null;
      InvocationHandler handler = new RemoteInvocationHandlerInvocationHandler(delegate, session, businessInterface);

      Interceptor interceptors[] = new Interceptor[] { new RemoteContextDataInterceptor(), interceptor };
      interceptor = new InterceptorAssembly(interceptors);
      
      handler = new InterceptorInvocationHandler(handler, interceptor);
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { businessInterface };
      return businessInterface.cast(Proxy.newProxyInstance(loader, interfaces, handler));
   }
   
   @Test
   public void test1() throws Throwable
   {
      Interceptor interceptor = new SimpleInterceptorClientSide();
      
      InvokerLocator locator = new InvokerLocator("socket://localhost:5783");
      String subsystem = "EJB3_R2D2";
      
      Client client = new Client(locator, subsystem);
      client.setDisconnectTimeout(1);
      client.connect();
      
      String oid = InterceptedMockRemotable.class.getSimpleName();
      MockInterface bean = createRemoteProxy(client, oid, MockInterface.class, interceptor);
      
      Current.setState("something");
      
      String result = bean.sayHi("me");
      
      assertEquals("Hi me something", result);
   }
}
