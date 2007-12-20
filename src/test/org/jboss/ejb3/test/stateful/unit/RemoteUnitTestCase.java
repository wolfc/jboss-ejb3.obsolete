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
package org.jboss.ejb3.test.stateful.unit;

import java.util.Map;
import java.util.Random;

import javax.ejb.NoSuchEJBException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.ClientKernelAbstraction;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.ejb3.test.stateful.ClusteredStateful;
import org.jboss.ejb3.test.stateful.ConcurrentStateful;
import org.jboss.ejb3.test.stateful.Entity;
import org.jboss.ejb3.test.stateful.EntityFacade;
import org.jboss.ejb3.test.stateful.ExtendedState;
import org.jboss.ejb3.test.stateful.ProxyFactoryInterface;
import org.jboss.ejb3.test.stateful.RemoteBindingInterceptor;
import org.jboss.ejb3.test.stateful.ServiceRemote;
import org.jboss.ejb3.test.stateful.SmallCacheStateful;
import org.jboss.ejb3.test.stateful.State;
import org.jboss.ejb3.test.stateful.Stateful;
import org.jboss.ejb3.test.stateful.StatefulHome;
import org.jboss.ejb3.test.stateful.StatefulInvoker;
import org.jboss.ejb3.test.stateful.StatefulLocal;
import org.jboss.ejb3.test.stateful.StatefulTimeout;
import org.jboss.ejb3.test.stateful.StatefulTx;
import org.jboss.ejb3.test.stateful.Stateless;
import org.jboss.ejb3.test.stateful.Tester;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: RemoteUnitTestCase.java 68202 2007-12-12 21:01:31Z bdecoste $
 */

public class RemoteUnitTestCase
extends JBossTestCase
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(RemoteUnitTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public RemoteUnitTestCase(String name)
   {

      super(name);

   }
   
   private class ConcurrentInvocation extends Thread
   {
      SmallCacheStateful small = null;
      public Exception ex;
      private int id;
      
      public ConcurrentInvocation(int id)
      {
         this.id = id;
         try
         {
            small = (SmallCacheStateful)getInitialContext().lookup("SmallCacheStatefulBean/remote");
            small.setId(id);
         }
         catch (Exception e)
         {
         }
      }
      
      public void run()
      {
         for (int i = 0; i < 5; i++)
         {
            try
            {
               assertEquals(small.doit(id),i);
            }
            catch (Exception e)
            {
               ex = e;
            }
            catch (Error er)
            {
               ex = new RuntimeException("Failed assert: " + id, er);
            }
         }
      }
   }
   
   private class ConcurrentStatefulTimeoutClient extends Thread
   {
      StatefulTimeout sfsb;
      public Exception ex;
      private int id;
      private int wait;
      public boolean removed = false;
      public Exception failure = null;
      
      public ConcurrentStatefulTimeoutClient(int id, int wait)
      {
         try
         {
            this.id = id;
            this.wait = wait;
            sfsb = (StatefulTimeout)getInitialContext().lookup("StatefulClusteredTimeoutBean/remote");
         }
         catch (Exception e)
         {
         }
      }
      
      public void run()
      {
         Random random = new Random(id);
         
         while (!removed)
         {
            try
            {
               sfsb.test();
               int millis = random.nextInt(wait);
               if (millis <= wait/10)
               {
                  sfsb.remove();
                  removed = true;
                  System.out.println("Bean has been removed " + id);
               }
               else
                  Thread.sleep(millis);
            }
            catch (javax.ejb.NoSuchEJBException e)
            {
               System.out.println("Bean has timed out " + id);
               removed = true;
            }
            catch (Exception e)
            {
               failure = e;
            }
         }
      }
   }
   
   public void atestSmallCache() throws Exception
   {
      ConcurrentInvocation[] threads = new ConcurrentInvocation[5];
      for (int i = 0; i < 5; i++) threads[i] = new ConcurrentInvocation(i);
      for (int i = 0; i < 5; i++) threads[i].start();
      for (int i = 0; i < 5; i++) threads[i].join();
      for (int i = 0; i < 5; i++)
      {
         if (threads[i].ex != null)
         {
            throw new Exception(threads[i].ex);
         }
      }
   }
   
   

   public void atestStatefulSynchronization() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      Tester test = (Tester) getInitialContext().lookup("TesterBean/remote");
      test.testSessionSynchronization();

   }
   
   public void atestEJBObject() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      StatefulHome home = (StatefulHome)getInitialContext().lookup("StatefulBean/home");
      assertNotNull(home);
      javax.ejb.EJBObject stateful = (javax.ejb.EJBObject)home.create(); 
      assertNotNull(stateful);
      stateful = (javax.ejb.EJBObject)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
   }
   
   public void atestStatefulTx() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      StatefulTx stateful = (StatefulTx)getInitialContext().lookup("StatefulTx");
      assertNotNull(stateful);
      
      boolean transacted = stateful.isLocalTransacted();
      assertTrue(transacted);
      transacted = stateful.isGlobalTransacted();
      assertFalse(transacted);
      transacted = stateful.testNewTx();
      assertTrue(transacted);
      
      try
      {
         stateful.testTxRollback();
         fail("should have caught exception");
      }
      catch (javax.ejb.EJBException e)
      {
      }
   }
   
   public void atestTemplateInterfaceTx() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      StatefulTx stateful = (StatefulTx)getInitialContext().lookup("StatefulTx");
      assertNotNull(stateful);
      
      try
      {
         stateful.testMandatoryTx(new State("test"));
         fail("should have caught exception");
      }
      catch (javax.ejb.EJBTransactionRequiredException e)
      {
      }
   }
   
   public void atestLocalSFSB() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      try
      {
         StatefulLocal stateful = (StatefulLocal)getInitialContext().lookup("StatefulBean/local");
         assertNotNull(stateful);
         
         stateful.getState();
         fail("EJBException should be thrown");
      }
      catch (Exception e)
      {
         if (e.getCause() == null || !(e.getCause() instanceof javax.ejb.EJBException))
            fail("EJBException should be thrown as cause");
      }
   }
   
   public void atestNotSerialableSFSB() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      stateful.setState("state");
      String state = stateful.getState();
      assertEquals("state", state);
   }
   
   public void atestSFSBInit() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      StatefulHome home = (StatefulHome)getInitialContext().lookup("StatefulBean/home");
      assertNotNull(home);
      ExtendedState state = new ExtendedState("init");
      Stateful stateful = home.create(state);
      assertNotNull(stateful);
      String s = stateful.getState();
      assertEquals("Extended_init", s);
   }
   
   public void atestStackTrace() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      
      try
      {
         stateful.testThrownException();
         fail("no exception caught");
      } 
      catch (Exception e)
      {
         StackTraceElement[] stackTrace = e.getStackTrace();
         assertTrue(stackTrace[stackTrace.length - 1].getClassName().startsWith("org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner"));
      }
   }
   
   public void atestExceptionCase() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      
      try
      {
         stateful.testExceptionCause();
         fail("no exception caught");
      } 
      catch (Exception e)
      {
         Throwable cause = e.getCause();
         assertNotNull(cause);
         assertEquals(NullPointerException.class.getName(), cause.getClass().getName());
      }
   }
   
   public void atestRemoteBindingInterceptorStack() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      assertFalse(stateful.interceptorAccessed());
      assertTrue(RemoteBindingInterceptor.accessed);
   }

   public void atestUninstantiatedPassivation() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      stateful.lookupStateful();
      Thread.sleep(10 * 1000);
      stateful.testStateful();
      
      stateful.lookupStateful();
      stateful.testStateful();
      Thread.sleep(10 * 1000);
      stateful.testStateful();
   }
   
   public void atestExtendedPersistencePassivation() throws Exception
   {
      EntityFacade stateful = (EntityFacade)getInitialContext().lookup("EntityFacadeBean/remote");
      Entity entity = null;
      entity = stateful.createEntity("Kalin");
      
      //passivate
      Thread.sleep(10 * 1000);
      
      MBeanServerConnection server = getServer();
      ObjectName objectName = new ObjectName("jboss.jca:service=CachedConnectionManager");
      int inUseConnections = (Integer)server.getAttribute(objectName, "InUseConnections");
      System.out.println("inUseConnections \n" + inUseConnections);
      assertEquals(0, inUseConnections);
      
      //activate
      entity = stateful.loadEntity(entity.getId());
      
      inUseConnections = (Integer)server.getAttribute(objectName, "InUseConnections");
      System.out.println("inUseConnections \n" + inUseConnections);
      if (inUseConnections != 0)
         printStats(server);
      assertEquals(0, inUseConnections);
      
      entity = stateful.createEntity("Kalin" + entity.getId());
      
      inUseConnections = (Integer)server.getAttribute(objectName, "InUseConnections");
      System.out.println("inUseConnections \n" + inUseConnections);
      
      if (inUseConnections != 0)
         printStats(server);
      assertEquals(0, inUseConnections);
   }
   
   protected void printStats(MBeanServerConnection server) throws Exception
   {
      ObjectName objectName = new ObjectName("jboss.jca:service=CachedConnectionManager");
      Object[] params = {};
      String[] sig = {};
      Map<?, ?> result = (Map<?, ?>)server.invoke(objectName, "listInUseConnections", params, sig);
      System.out.println(result);
      
      objectName = new ObjectName("jboss:service=TransactionManager");
      long activeTransactions = (Long)server.getAttribute(objectName, "TransactionCount");
      System.out.println("activeTransactions \n" + activeTransactions);
      
      objectName = new ObjectName("jboss.jca:service=ManagedConnectionPool,name=DefaultDS");
      long inUseConnectionCount = (Long)server.getAttribute(objectName, "InUseConnectionCount");
      System.out.println("inUseConnectionCount \n" + inUseConnectionCount);
      
      long maxConnectionsInUseCount = (Long)server.getAttribute(objectName, "MaxConnectionsInUseCount");
      System.out.println("maxConnectionsInUseCount \n" + maxConnectionsInUseCount); 
   }
   
   public void atestTimeoutRemoval() throws Exception
   {
      StatefulTimeout sfsb = (StatefulTimeout)getInitialContext().lookup("StatefulTimeoutBean/remote");
      sfsb.test();
      Thread.sleep(5 * 1000);
      
      try
      {
         sfsb.test();
         fail("SFSB should have been removed via timeout");
      } catch (javax.ejb.NoSuchEJBException e)
      {
      }
      
      sfsb = (StatefulTimeout)getInitialContext().lookup("StatefulTimeoutBean2/remote");
      sfsb.test();
      Thread.sleep(10 * 1000);
      
      try
      {
         sfsb.test();
         fail("SFSB should have been removed via timeout");
      } catch (javax.ejb.NoSuchEJBException e)
      {
      }
   }
   
   public void atestClusteredTimeoutRemoval() throws Exception
   {
      StatefulTimeout sfsb = (StatefulTimeout)getInitialContext().lookup("StatefulClusteredTimeoutBean/remote");
      sfsb.test();
      Thread.sleep(5 * 1000);
      
      try
      {
         sfsb.test();
         fail("SFSB should have been removed via timeout");
      } catch (javax.ejb.NoSuchEJBException e)
      {
      }
      
      sfsb = (StatefulTimeout)getInitialContext().lookup("StatefulClusteredTimeoutBean2/remote");
      sfsb.test();
      Thread.sleep(10 * 1000);
      
      try
      {
         sfsb.test();
         fail("SFSB should have been removed via timeout");
      } catch (javax.ejb.NoSuchEJBException e)
      {
      }
   }
   
   public void atestConcurrentClusteredTimeoutRemoval() throws Exception
   {
      int numThreads = 100;
      ConcurrentStatefulTimeoutClient[] clients = new ConcurrentStatefulTimeoutClient[numThreads];
      for (int i = 0 ; i < numThreads ; ++i)
      {
         clients[i] = new ConcurrentStatefulTimeoutClient(i, 3000);
         clients[i].start();
      }
      
      Thread.sleep(500);
      ObjectName objectName = new ObjectName("jboss.j2ee:jar=stateful-test.jar,name=StatefulClusteredTimeoutBean,service=EJB3");
      MBeanServerConnection server = getServer();
      int size = (Integer)server.getAttribute(objectName, "TotalSize");
      assertTrue(size > 0);
      
      boolean allRemoved = false;
      while (!allRemoved)
      {
         int i = 0;
         while (i < numThreads && clients[i].removed && clients[i].failure == null)
            ++i;
         
         if (i < numThreads && clients[i].failure != null)
            throw clients[i].failure;
         
         System.out.println("----- removed " + i);
         
         if (i == numThreads)
            allRemoved = true;
         
         Thread.sleep(5000);
      }
      
      size = (Integer)server.getAttribute(objectName, "TotalSize");
      assertEquals(0, size);
   }

   public void atestPassivation() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      System.out.println("testPassivation");
      Stateless stateless = (Stateless)getInitialContext().lookup("Stateless");
      stateless.testInjection();
      
      ServiceRemote service = (ServiceRemote) getInitialContext().lookup("ServiceBean/remote");
      service.testInjection();
      
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
      stateful.setState("state");
      assertEquals("state", stateful.getState());
      stateful.testSerializedState("state");
      assertEquals(null, stateful.getInterceptorState());
      stateful.setInterceptorState("hello world");
      assertFalse(stateful.testSessionContext());
      Thread.sleep(10 * 1000);
      assertTrue(stateful.wasPassivated());
      
      assertEquals("state", stateful.getState());
      assertEquals("hello world", stateful.getInterceptorState());

      Stateful another = (Stateful)getInitialContext().lookup("Stateful");
      assertEquals(null, another.getInterceptorState());
      another.setInterceptorState("foo");
      assertEquals("foo", another.getInterceptorState());
      assertEquals("hello world", stateful.getInterceptorState());
      
      assertFalse(stateful.testSessionContext());
      
      stateful.testResources();
      
      stateless.testInjection();
      
      service.testInjection();
   }
   
   public void atestClusteredPassivation() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      System.out.println("testPassivation");
      Stateless stateless = (Stateless)getInitialContext().lookup("Stateless");
      stateless.testInjection();
      
      ServiceRemote service = (ServiceRemote) getInitialContext().lookup("ServiceBean/remote");
      service.testInjection();
      
      ClusteredStateful stateful = (ClusteredStateful)getInitialContext().lookup("ClusteredStateful");
      assertNotNull(stateful);
      stateful.setState("state");
      assertEquals("state", stateful.getState());
      stateful.testSerializedState("state");
      assertEquals(null, stateful.getInterceptorState());
      stateful.setInterceptorState("hello world");
      assertFalse(stateful.testSessionContext());
      Thread.sleep(10 * 1000);
      assertTrue(stateful.wasPassivated());
      
      assertEquals("state", stateful.getState());
      assertEquals("hello world", stateful.getInterceptorState());

      Stateful another = (Stateful)getInitialContext().lookup("Stateful");
      assertEquals(null, another.getInterceptorState());
      another.setInterceptorState("foo");
      assertEquals("foo", another.getInterceptorState());
      assertEquals("hello world", stateful.getInterceptorState());
      
      assertFalse(stateful.testSessionContext());
      
      stateful.testResources();
      
      stateless.testInjection();
      
      service.testInjection();
   }

   
   public void atestRemove() throws Exception
   {
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      System.out.println("testPassivation");
      Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
      assertNotNull(stateful);
 //     stateful.setState("state");
      
      stateful.removeBean();
      
      try
      {
         stateful.getState();
         fail("Bean should have been removed");
      } catch (NoSuchEJBException e)
      {
         
      }
   }

   public void atestRemoveWithRollback() throws Exception
   {
      Tester test = (Tester) getInitialContext().lookup("TesterBean/remote");
      test.testRollback1();
      test.testRollback2();
   }
   
   public void atestConcurrentAccess() throws Exception
   {
      ConcurrentStateful stateful = (ConcurrentStateful) new InitialContext().lookup("ConcurrentStateful");
      stateful.getState();
      
      StatefulInvoker[] invokers = new StatefulInvoker[2];
      for (int i = 0; i < 2 ; ++i)
      {
         invokers[i] = new StatefulInvoker(stateful);
      }
      
      for (StatefulInvoker invoker: invokers)
      {
         invoker.start();
      }
      
      Thread.sleep(10000);
      
      for (StatefulInvoker invoker: invokers)
      {
         if (invoker.getException() != null)
            throw invoker.getException();
      }
      
      stateful = (ConcurrentStateful) new InitialContext().lookup("Stateful");
      
      invokers = new StatefulInvoker[2];
      for (int i = 0; i < 2 ; ++i)
      {
         invokers[i] = new StatefulInvoker(stateful);
      }
      
      for (StatefulInvoker invoker: invokers)
      {
         invoker.start();
      }
      
      Thread.sleep(10000);
      
      boolean wasConcurrentException = false;
      for (StatefulInvoker invoker: invokers)
      {
         if (invoker.getException() != null)
            wasConcurrentException = true;
      }
      
      assertTrue(wasConcurrentException);
   }
   
   public void testOverrideConcurrentAccess() throws Exception
   {
      ConcurrentStateful stateful = (ConcurrentStateful) new InitialContext().lookup("OverrideConcurrentStateful");
      stateful.getState();
      
      StatefulInvoker[] invokers = new StatefulInvoker[2];
      for (int i = 0; i < 2 ; ++i)
      {
         invokers[i] = new StatefulInvoker(stateful);
      }
      
      for (StatefulInvoker invoker: invokers)
      {
         invoker.start();
      }
      
      Thread.sleep(5000);
      
      boolean wasConcurrentException = false;
      for (StatefulInvoker invoker: invokers)
      {
         if (invoker.getException() != null)
            wasConcurrentException = true;
      }

      assertTrue(wasConcurrentException);
   }
   
   public void atestJmxName() throws Exception
   {
      ObjectName deployment = new ObjectName("test.ejb3:name=Bill,service=EJB3");

      ClientKernelAbstraction kernel = KernelAbstractionFactory.getClientInstance();
      kernel.invoke(deployment, "stop", new Object[0], new String[0]);
      kernel.invoke(deployment, "start", new Object[0], new String[0]);
   }
   
   public void atestDestroyException() throws Exception
   {
      EntityFacade stateful = (EntityFacade)getInitialContext().lookup("EntityFacadeBean/remote");
      assertNotNull(stateful);
      stateful.createEntity("Kalin");
      
      try
      {
         stateful.remove();
         fail("should catch RuntimeException");
      }
      catch (RuntimeException e)
      {
         assertEquals("java.lang.RuntimeException: From destroy", e.getMessage());
      }
      
      try
      {
         stateful.createEntity("Nogo");
         fail("expected NoSuchEJBException");
      }
      catch(NoSuchEJBException e)
      {
         // okay
      }
      
      /* Wolf: the stateful is discarded
      stateful.createEntity("Cabernet");
      
      try
      {
         stateful.removeWithTx();
         fail("should catch RuntimeException");
      }
      catch (RuntimeException e)
      {
      }
      
      stateful.createEntity("Bailey");
      */
   }
      
   public void atestDestroyExceptionWithTx() throws Exception
   {
      EntityFacade stateful = (EntityFacade)getInitialContext().lookup("EntityFacadeBean/remote");
      assertNotNull(stateful);
      stateful.createEntity("Cabernet");
      
      try
      {
         stateful.removeWithTx();
         fail("should catch RuntimeException");
      }
      catch (RuntimeException e)
      {
         assertEquals("java.lang.RuntimeException: From destroy", e.getMessage());
      }
      
      try
      {
         stateful.createEntity("Nogo");
         fail("expected NoSuchEJBException");
      }
      catch(NoSuchEJBException e)
      {
         // okay
      }      
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(RemoteUnitTestCase.class, "stateful-test.jar");
   }

}
