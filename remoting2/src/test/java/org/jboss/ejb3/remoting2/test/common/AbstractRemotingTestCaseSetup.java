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
package org.jboss.ejb3.remoting2.test.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.ejb3.remoting.endpoint.client.RemoteInvocationHandlerInvocationHandler;
import org.jboss.ejb3.remoting2.client.RemoteInvocationHandler;
import org.jboss.ejb3.remoting2.test.clientinterceptor.InterceptedMockRemotable;
import org.jboss.ejb3.remoting2.test.clientinterceptor.RemoteContextDataInterceptor;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.EventSet;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AbstractRemotingTestCaseSetup
{
   private static final Logger log = Logger.getLogger(AbstractRemotingTestCaseSetup.class);
   
   private static VirtualMachine vm;
   private static RemoteKernelController controller;
   
   @AfterClass
   public static void afterClass()
   {
      try
      {
         saveCobertura();
      }
      catch(Throwable t)
      {
         log.error("failed to save Cobertura", t);
      }
      vm.exit(0);
   }
   
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      VirtualMachineManager manager = Bootstrap.virtualMachineManager();
      LaunchingConnector connector = manager.defaultConnector();
      Map<String, ? extends Argument> args = connector.defaultArguments();
      for(Argument arg : args.values())
      {
         System.out.println("  " + arg);
      }
      //String cobertura = " -Dnet.sourceforge.cobertura.datafile=target/cobertura/cobertura-server.ser";
      String cobertura = "";
      args.get("options").setValue("-classpath " + getClassPath() + cobertura);
      args.get("main").setValue("org.jboss.kernel.plugins.bootstrap.standalone.StandaloneBootstrap");
      log.info(args);
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
      
      initRemoteKernelController();
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
   
   private static void initRemoteKernelController() throws Exception
   {
      InvokerLocator locator = new InvokerLocator("socket://localhost:5783");
      String subsystem = "EJB3_R2D2";
      
      Client client = new Client(locator, subsystem);
      client.setDisconnectTimeout(1);
      client.connect();

      Class<?> businessInterface = RemoteKernelController.class;
      RemoteInvocationHandler delegate = new RemoteInvocationHandler(client, businessInterface.getName());
      
      // RemoteKernelController is a singleton, so no session
      Serializable session = null;
      InvocationHandler handler = new RemoteInvocationHandlerInvocationHandler(delegate, session, businessInterface);

      handler = new InterceptorInvocationHandler(handler, new RemoteContextDataInterceptor());
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { businessInterface };
      controller = (RemoteKernelController) Proxy.newProxyInstance(loader, interfaces, handler);
   }
   
   /**
    * Install a bean in the server.
    * @param cls
    */
   protected static void install(Class<?> cls) throws Throwable
   {
      AbstractBeanMetaData beanMetaData = new AbstractBeanMetaData(InterceptedMockRemotable.class.getName(), InterceptedMockRemotable.class.getName());
      controller.install(beanMetaData);
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
   
   private static void saveCobertura() throws InterruptedException
   {
      controller.dumpCobertura();
      /*
      String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
      String methodName = "saveGlobalProjectData";
      
      vm.eventQueue().remove(1);
      vm.setDebugTraceMode(VirtualMachine.TRACE_ALL);
      
      List<ThreadReference> threads = vm.allThreads();
      System.out.println(threads);
      ThreadReference thread = threads.get(2);
      System.out.println(thread);
      //thread.suspend();
      vm.suspend();
      StepRequest stepRequest = vm.eventRequestManager().createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
      stepRequest.addCountFilter(1);
      //stepRequest.addClassFilter("*");
      //stepRequest.setSuspendPolicy(StepRequest.SUSPEND_EVENT_THREAD);
      stepRequest.enable();
      
      System.out.println(vm.eventRequestManager().stepRequests());
      vm.resume();
      thread.interrupt();
      
      waitForEvent();
      //thread.suspend();
      
      List<ReferenceType> classes = vm.classesByName(className);
      if(classes.size() == 0)
      {
         log.info("Cobertura was not found");
         return;
      }
      ClassType cls = (ClassType) classes.get(0);
      List<Method> methods = cls.methodsByName(methodName);
      Method method = methods.get(0);
      List<? extends Value> arguments = new ArrayList<Value>();
      int options = 0;
      try {
         cls.invokeMethod(thread, method, arguments, options);
         log.info("Cobertura data successfully saved");
      } catch (Throwable t) {
         t.printStackTrace();
      }
      */
   }
   
   private static void waitForEvent()
   {
      long end = System.currentTimeMillis() + 5000;
      while(System.currentTimeMillis() < end)
      {
         EventSet events;
         try
         {
            events = vm.eventQueue().remove(end - System.currentTimeMillis());
         }
         catch(InterruptedException e)
         {
            log.info("interrupted");
            return;
         }
         System.out.println(events);
      }
      throw new IllegalStateException();
   }
}
