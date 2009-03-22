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
package org.jboss.ejb3.remoting2.test.simple.unit;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.endpoint.RemotableEndpoint;
import org.jboss.ejb3.remoting.endpoint.client.RemoteInvocationHandlerInvocationHandler;
import org.jboss.ejb3.remoting2.EJB3ServerInvocationHandler;
import org.jboss.ejb3.remoting2.client.RemoteInvocationHandler;
import org.jboss.ejb3.remoting2.test.clientinterceptor.RemoteContextDataInterceptor;
import org.jboss.ejb3.remoting2.test.common.AbstractRemotingTestCaseSetup;
import org.jboss.ejb3.remoting2.test.common.MockInterface;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleRemotingTestCase extends AbstractRemotingTestCaseSetup
{
   private static final Logger log = Logger.getLogger(SimpleRemotingTestCase.class);
   
   @Test
   public void testRaw() throws Throwable
   {
      System.out.println("Hello world");
      
      InvokerLocator locator = new InvokerLocator("socket://localhost:5783");
      String subsystem = "EJB3_R2D2";
      
      Client client = new Client(locator, subsystem);
      client.setDisconnectTimeout(1);
      client.connect();
      Map<Object, Object> metadata = new HashMap<Object, Object>();
      metadata.put(EJB3ServerInvocationHandler.OID, "MockRemotableID");
      
      // the target of MockRemotable is RemotableEndpoint
      Method realMethod = RemotableEndpoint.INVOKE_METHOD;
      SerializableMethod method = new SerializableMethod(realMethod, RemotableEndpoint.class);
      Serializable session = null;
      Map<String, Object> contextData = null;
      // the remotable endpoint delegates to a MockInterface endpoint
      SerializableMethod businessMethod = new SerializableMethod(MockInterface.class.getDeclaredMethod("sayHi", String.class), MockInterface.class);
      Object args[] = { session, contextData, businessMethod, new Object[] { "y" } };
      Object param = new Object[] { method, args };
      String result = (String) client.invoke(param, metadata);
      client.disconnect();
      
      assertEquals("Hi y", result);
   }
   
   @Test
   public void testRealEndpoint() throws Throwable
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
      String result = proxy.sayHi("there");
      assertEquals("Hi there", result);
   }
   
   @Test
   public void testRemotableEndpoint() throws Throwable
   {
      InvokerLocator locator = new InvokerLocator("socket://localhost:5783");
      String subsystem = "EJB3_R2D2";
      
      Client client = new Client(locator, subsystem);
      client.setDisconnectTimeout(1);
      client.connect();
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { RemotableEndpoint.class };
      InvocationHandler handler = new RemoteInvocationHandler(client, "MockRemotableID");
      RemotableEndpoint endpoint = (RemotableEndpoint) Proxy.newProxyInstance(loader, interfaces, handler);
      
      Serializable session = null;
      Map<String, Object> contextData = null;
      // the remotable endpoint delegates to a MockInterface endpoint
      SerializableMethod businessMethod = new SerializableMethod(MockInterface.class.getDeclaredMethod("sayHi", String.class), MockInterface.class);
      Object args[] = { "me" };
      String result = (String) endpoint.invoke(session, contextData, businessMethod, args);
      assertEquals("Hi me", result);
   }
   
}
