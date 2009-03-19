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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.endpoint.RemotableEndpoint;
import org.jboss.ejb3.remoting.endpoint.client.RemoteInvocationHandlerInvocationHandler;
import org.jboss.ejb3.remoting2.EJB3ServerInvocationHandler;
import org.jboss.ejb3.remoting2.client.RemoteInvocationHandler;
import org.jboss.ejb3.remoting2.test.common.MockInterface;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.Connector.Argument;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleRemotingTestCase
{
   private static final Logger log = Logger.getLogger(SimpleRemotingTestCase.class);
   
   private static VirtualMachine vm;
   
   @AfterClass
   public static void afterClass()
   {
      vm.exit(0);
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      VirtualMachineManager manager = Bootstrap.virtualMachineManager();
      LaunchingConnector connector = manager.defaultConnector();
      Map<String, ? extends Argument> args = connector.defaultArguments();
      for(Argument arg : args.values())
      {
         System.out.println("  " + arg);
      }
      args.get("options").setValue("-classpath " + getClassPath());
      args.get("main").setValue("org.jboss.kernel.plugins.bootstrap.standalone.StandaloneBootstrap");
      System.out.println(args);
      vm = connector.launch(args);
      
      Process p = vm.process();
      final InputStream in = p.getInputStream();
      Thread inputStreamReader = new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               int b;
               while((b = in.read()) != -1)
                  System.out.write(b);
            }
            catch(IOException e)
            {
               e.printStackTrace();
            }
         }
      };
      inputStreamReader.setDaemon(true);
      inputStreamReader.start();
      
      final InputStream err = p.getErrorStream();
      Thread errorStreamReader = new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               int b;
               while((b = err.read()) != -1)
                  System.err.write(b);
            }
            catch(IOException e)
            {
               e.printStackTrace();
            }
         }
      };
      errorStreamReader.setDaemon(true);
      errorStreamReader.start();
      
      vm.resume();
      
      // This causes an EOFException, which can be ignored.
      waitForSocket(5783, 10, TimeUnit.SECONDS);
   }
   
   private static String getClassPath()
   {
      String sureFireTestClassPath = System.getProperty("surefire.test.class.path");
      log.debug("sureFireTestClassPath = " + sureFireTestClassPath);
      String javaClassPath = System.getProperty("java.class.path");
      log.debug("javaClassPath = " + javaClassPath);
      // make sure we can run under surefire 
      String classPath = sureFireTestClassPath;
      if(classPath == null)
         classPath = javaClassPath;
      log.debug("classPath = " + classPath);
      return classPath;
   }
   
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
      Method realMethod = RemotableEndpoint.class.getDeclaredMethod("invoke", Serializable.class, Map.class, Class.class, SerializableMethod.class, Object[].class);
      SerializableMethod method = new SerializableMethod(realMethod);
      Serializable session = null;
      Map<?, ?> context = null;
      // the remotable endpoint delegates to a MockInterface endpoint
      SerializableMethod businessMethod = new SerializableMethod(MockInterface.class.getDeclaredMethod("sayHi", String.class));
      Object args[] = { session, context, null, businessMethod, new Object[] { "y" } };
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
      Map<?, ?> context = null;
      // the remotable endpoint delegates to a MockInterface endpoint
      SerializableMethod businessMethod = new SerializableMethod(MockInterface.class.getDeclaredMethod("sayHi", String.class));
      Object args[] = { "me" };
      String result = (String) endpoint.invoke(session, context, null, businessMethod, args);
      assertEquals("Hi me", result);
   }
   
   private static void waitForSocket(int port, long duration, TimeUnit unit)
   {
      long end = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(duration, unit);
      while(System.currentTimeMillis() < end)
      {
         try
         {
            Socket socket = new Socket("localhost", port);
            socket.close();
            return;
         }
         catch(ConnectException e)
         {
            // ignore
         }
         catch (UnknownHostException e)
         {
            throw new RuntimeException(e);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
         try
         {
            Thread.sleep(1000);
         }
         catch (InterruptedException e)
         {
            return;
         }
      }
   }
}
