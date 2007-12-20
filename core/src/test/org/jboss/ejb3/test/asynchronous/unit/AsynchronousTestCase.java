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
package org.jboss.ejb3.test.asynchronous.unit;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import javax.ejb.EJBAccessException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;
import org.jboss.aspects.asynch.AsynchProvider;
import org.jboss.aspects.asynch.Future;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.asynchronous.Asynch;
import org.jboss.ejb3.test.asynchronous.SecuredStatelessRemote;
import org.jboss.ejb3.test.asynchronous.ServiceRemote;
import org.jboss.ejb3.test.asynchronous.StatefulClusteredRemote;
import org.jboss.ejb3.test.asynchronous.StatefulRemote;
import org.jboss.ejb3.test.asynchronous.StatelessClusteredRemote;
import org.jboss.ejb3.test.asynchronous.StatelessRemote;
import org.jboss.ejb3.test.asynchronous.TxSessionRemote;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class AsynchronousTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(AsynchronousTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public AsynchronousTestCase(String name)
   {
      super(name);
   }

   public void testSLRemoteAsynchronous() throws Exception
   {
      StatelessRemote tester =
            (StatelessRemote) getInitialContext().lookup("StatelessBean/remote");
      assertEquals("Wrong return for stateless remote", 11, tester.method(11));

      StatelessRemote asynchTester = (StatelessRemote)Asynch.getAsynchronousProxy(tester);
      assertEquals("Wrong return value for stateless remote", 0, asynchTester.method(12));
      Future future = Asynch.getFutureResult(asynchTester);
      int ret = (Integer) future.get();
      assertEquals("Wrong async return value for stateless remote", ret, 12);
   }

   public void testSLClusteredAsynchronous() throws Exception
   {
      StatelessClusteredRemote tester =
            (StatelessClusteredRemote) getInitialContext().lookup("StatelessClusteredBean/remote");
      assertEquals("Wrong return for stateless clustered", 21, tester.method(21));

      StatelessClusteredRemote asynchTester = (StatelessClusteredRemote)((JBossProxy)tester).getAsynchronousProxy();
      assertEquals("Wrong return value for stateless clustered", 0, asynchTester.method(22));
      AsynchProvider ap = (AsynchProvider) asynchTester;
      Future future = ap.getFuture();
      int ret = (Integer) future.get();
      assertEquals("Wrong async return value for stateless clustered", ret, 22);
   }

   public void testSLLocalAsynchrounous() throws Exception
   {
	  MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=asynchronous");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testSLLocalAsynchronous", params, sig);
   }

   public void testSFRemoteAsynchronous() throws Exception
   {
      StatefulRemote tester =
            (StatefulRemote) getInitialContext().lookup("StatefulBean/remote");
      assertEquals("Wrong return for stateful remote", 31, tester.method(31));

      StatefulRemote asynchTester = (StatefulRemote)((JBossProxy)tester).getAsynchronousProxy();
      assertEquals("Wrong return value for stateful remote", 0, asynchTester.method(32));
      AsynchProvider ap = (AsynchProvider) asynchTester;
      Future future = ap.getFuture();
      int ret = (Integer) future.get();
      assertEquals("Wrong async return value for stateful remote", ret, 32);
   }

   public void testSFClusteredAsynchronous() throws Exception
   {
      StatefulClusteredRemote tester =
            (StatefulClusteredRemote) getInitialContext().lookup("StatefulClusteredBean/remote");
      assertEquals("Wrong return for stateful clustered", 41, tester.method(41));

      StatefulClusteredRemote asynchTester = (StatefulClusteredRemote)((JBossProxy)tester).getAsynchronousProxy();
      assertEquals("Wrong return value for stateful clustered", 0, asynchTester.method(42));
      AsynchProvider ap = (AsynchProvider) asynchTester;
      Future future = ap.getFuture();
      int ret = (Integer) future.get();
      assertEquals("Wrong async return value for stateful clustered", ret, 42);
   }

   public void testSFLocalAsynchrounous() throws Exception
   {
	  MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=asynchronous");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testSFLocalAsynchronous", params, sig);
   }

   public void testServiceRemoteAsynchronous() throws Exception
   {
      ServiceRemote tester =
            (ServiceRemote) getInitialContext().lookup("ServiceBean/remote");
      assertEquals("Wrong return for service remote", 51, tester.method(51));

      ServiceRemote asynchTester = (ServiceRemote)((JBossProxy)tester).getAsynchronousProxy();
      assertEquals("Wrong return value for service remote", 0, asynchTester.method(52));
      AsynchProvider ap = (AsynchProvider) asynchTester;
      Future future = ap.getFuture();
      int ret = (Integer) future.get();
      assertEquals("Wrong async return value for service remote", ret, 52);
   }

   public void testServiceLocalAsynchrounous() throws Exception
   {
	  MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=asynchronous");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testServiceLocalAsynchronous", params, sig);
   }

   public void testAsynchSecurity() throws Exception
   {

      SecuredStatelessRemote tester =
            (SecuredStatelessRemote) getInitialContext().lookup("SecuredStatelessBean/remote");
      SecuredStatelessRemote asynchTester = (SecuredStatelessRemote)((JBossProxy)tester).getAsynchronousProxy();
      AsynchProvider ap = (AsynchProvider)asynchTester;

      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());

      asynchTester.method(61);
      Object ret = getReturnOrException(ap);
      assertTrue("SecurityException not thrown: " + ret, ret instanceof EJBAccessException);

      asynchTester.uncheckedMethod(62);
      ret = getReturnOrException(ap);
      assertEquals("Wrong value", 62, ret);

      asynchTester.excludedMethod(63);
      ret = getReturnOrException(ap);
      assertTrue("SecurityException not thrown: " + ret, ret instanceof EJBAccessException);

      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());

      asynchTester.method(64);
      ret = getReturnOrException(ap);
      assertEquals("Wrong return for authorised method", 64, ret);

      SecurityAssociation.setPrincipal(new SimplePrincipal("nosuchuser"));
      SecurityAssociation.setCredential("password".toCharArray());

      asynchTester.method(65);
      ret = getReturnOrException(ap);
      assertTrue("SecurityException not thrown: " + ret, ret instanceof EJBAccessException);
   }

   public void testRemoteAsynchTransaction() throws Exception
   {
      TxSessionRemote tester = (TxSessionRemote) getInitialContext().lookup("TxSessionBean/remote");
      TxSessionRemote asynchTester = (TxSessionRemote)((JBossProxy)tester).getAsynchronousProxy();
      AsynchProvider ap = (AsynchProvider) asynchTester;
      UserTransaction tx = (UserTransaction)getInitialContext().lookup("UserTransaction");

      //Add some entries in different threads and commit
      tx.begin();
      tester.createFruit("apple", false);
      tester.createFruit("pear", false);
      tester.createFruit("tomato", false);

      asynchTester.createVeg("Potato", false);
      waitForProvider(ap);
      asynchTester.createVeg("Turnip", false);
      waitForProvider(ap);
      asynchTester.createVeg("Carrot", false);
      waitForProvider(ap);
      tx.commit();

      tx.begin();
      Collection entries = tester.getEntries();
      tx.commit();
      assertEquals("Wrong number of entries", 6, entries.size());

      //Cleanup synchronously
      tx.begin();
      tester.cleanAll();
      tx.commit();

      tx.begin();
      entries = tester.getEntries();
      tx.commit();
      assertEquals("Wrong number of entries", 0, entries.size());

      //Add some entries in different threads and rollback
      tx.begin();
      tester.createFruit("apple", false);
      tester.createFruit("pear", false);
      tester.createFruit("tomato", false);

      asynchTester.createVeg("Potato", false);
      waitForProvider(ap);
      asynchTester.createVeg("Turnip", false);
      waitForProvider(ap);
      asynchTester.createVeg("Carrot", false);
      waitForProvider(ap);
      tx.rollback();

      tx.begin();
      entries = tester.getEntries();
      tx.commit();

      assertEquals("Wrong number of entries", 0, entries.size());

      //Add some entries in different threads and rollback from within Tx
      tx.begin();
      tester.createFruit("apple", false);
      tester.createFruit("pear", false);
      tester.createFruit("tomato", true);

      asynchTester.createVeg("Potato", false);
      waitForProvider(ap);
      asynchTester.createVeg("Turnip", false);
      waitForProvider(ap);
      asynchTester.createVeg("Carrot", true);
      waitForProvider(ap);

      boolean rollbackException = false;
      try
      {
         tx.commit();
      }
      catch(RollbackException e)
      {
         rollbackException = true;
      }

      assertTrue("RollbackException not picked up", rollbackException);

      tx.begin();
      entries = tester.getEntries();
      tx.commit();
      assertEquals("Wrong number of entries", 0, entries.size());
   }

   public void testLocalAsynchTransaction() throws Exception
   {
	  MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=asynchronous");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testLocalAsynchTransaction", params, sig);

   }

   private Object getReturnOrException(AsynchProvider provider)throws Exception
   {
      try
      {
         Future future = provider.getFuture();

         waitForFuture(future);
         return future.get();
      }
      catch(InvocationTargetException e)
      {
         return e.getCause();
      }
   }

   private void waitForProvider(AsynchProvider provider) throws InterruptedException
   {
      Future future = provider.getFuture();
      waitForFuture(future);
   }

   private void waitForFuture(Future future) throws InterruptedException
   {
      while (!future.isDone())
      {
         Thread.sleep(100);
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(AsynchronousTestCase.class, "asynchronous-test.sar, asynchronous-test.jar");
   }


}
