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
package org.jboss.ejb3.remoting2.test.remoteref.unit;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.jboss.ejb3.remoting.endpoint.client.RemoteContextDataInterceptor;
import org.jboss.ejb3.remoting.endpoint.client.RemoteInvocationHandlerInvocationHandler;
import org.jboss.ejb3.remoting2.client.RemoteInvocationHandler;
import org.jboss.ejb3.remoting2.test.common.AbstractRemotingTestCaseSetup;
import org.jboss.ejb3.remoting2.test.common.MockInterface;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.junit.Test;

/**
 * What happens if we obtain a reference from inside the remotable.
 * 
 * e.g. simulate ctx.getBusinessObject(interface);
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemoteReferenceTestCase extends AbstractRemotingTestCaseSetup
{
   @Test
   public void testRemoteReference() throws Exception
   {
      InvokerLocator locator = new InvokerLocator("socket://localhost:5783");
      String subsystem = "EJB3_R2D2";
      
      Client client = new Client(locator, subsystem);
      client.setDisconnectTimeout(1);
      client.connect();
      
      RemoteInvocationHandler delegate = new RemoteInvocationHandler(client, "MockRemotableID");
      
      Serializable session = null;
      Class<?> businessInterface = MockInterface.class;
      InvocationHandler handler = new RemoteInvocationHandlerInvocationHandler(delegate, session, businessInterface);
      
      handler = new InterceptorInvocationHandler(handler, new RemoteContextDataInterceptor());
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { businessInterface };
      MockInterface proxy = (MockInterface) Proxy.newProxyInstance(loader, interfaces, handler);
      
      MockInterface newProxy = proxy.getBusinessObject();
      
      String result = newProxy.sayHi("testRemoteReference");
      assertEquals("Hi testRemoteReference", result);
   }
}
