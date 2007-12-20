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
package org.jboss.ejb3.test.cache.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.jboss.ejb3.test.cache.SimpleStatefulRemote;
import org.jboss.ejb3.test.cache.StatefulRemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: StatefulUnitTestCase.java 65932 2007-10-08 19:55:50Z ALRubinger $
 */

public class StatefulUnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public StatefulUnitTestCase(String name)
   {

      super(name);

   }

   public void testStateful() throws Exception
   {
      StatefulRemote remote = (StatefulRemote) getInitialContext().lookup("StatefulBean/remote");
      remote.reset();
      remote.setState("hello");
      Thread.sleep(11000);
      assertEquals("hello", remote.getState());
      assertTrue(remote.getPostActivate());
      assertTrue(remote.getPrePassivate());
      remote.done(); // remove this
   }

   public void testStatefulLongRunning() throws Exception
   {
      StatefulRemote remote = (StatefulRemote) getInitialContext().lookup("StatefulBean/remote");
      remote.reset();
      remote.setState("hello");
      remote.longRunning();
      // It is in use so not yet passivated
      assertFalse(remote.getPrePassivate());
      Thread.sleep(11000);
      // Now it will be.
      assertEquals("hello", remote.getState());
      assertTrue(remote.getPostActivate());
      assertTrue(remote.getPrePassivate());
      remote.done(); // remove this
   }

   public void testSimpleStatefulLongRunning() throws Exception
   {
      SimpleStatefulRemote remote = (SimpleStatefulRemote) getInitialContext().lookup("SimpleStatefulBean/remote");
      remote.reset();
      remote.setState("hello");
      remote.longRunning();
      assertEquals("hello", remote.getState());
      assertTrue(remote.getPostActivate());
      assertTrue(remote.getPrePassivate());
   }

   public void testSimpleStateful() throws Exception
   {
      SimpleStatefulRemote remote = (SimpleStatefulRemote) getInitialContext().lookup("SimpleStatefulBean/remote");
      remote.reset();
      remote.setState("hello");
      Thread.sleep(5000);
      assertEquals("hello", remote.getState());
      assertTrue(remote.getPostActivate());
      assertTrue(remote.getPrePassivate());
   }

   public void testSimpleLocal() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=cache");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testSimpleLocal", params, sig);
   }

   public void testBench() throws Exception
   {
      StatefulRemote remote = (StatefulRemote) getInitialContext().lookup("StatefulBean/remote");
      System.out.println("bench: " + remote.bench(1000));
      remote.done(); // remove this
   }

   public void testBenchSimple() throws Exception
   {
      SimpleStatefulRemote remote = (SimpleStatefulRemote) getInitialContext().lookup("SimpleStatefulBean/remote");
      System.out.println("simple bench: " + remote.bench(1000));

   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(StatefulUnitTestCase.class, "cache-test.jar, cache-test.sar");
   }

}
