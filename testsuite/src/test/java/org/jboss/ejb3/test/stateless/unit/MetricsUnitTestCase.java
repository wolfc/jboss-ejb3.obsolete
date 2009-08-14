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
package org.jboss.ejb3.test.stateless.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.ejb3.test.stateless.StatelessRemote;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;


/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MetricsUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(MetricsUnitTestCase.class);

   public MetricsUnitTestCase(String name)
   {
      super(name);
   }

   public void testDefaultJmxMetrics() throws Exception
   {
      // The default pool used by a SLSB is the ThreadLocalPool
      // For a detailed explanation about what each metric value
      // means when a ThreadLocalPool is used, check the comments
      // from carlo in this JIRA https://jira.jboss.org/jira/browse/EJBTHREE-1703
      
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:jar=stateless-test.jar,name=DefaultPoolStatelessBean,service=EJB3");

      int size = 0;

      size = (Integer)server.getAttribute(testerName, "CurrentSize");
      assertEquals(0, size);

      size = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(0, size);

      StatelessRemote stateless = (StatelessRemote)getInitialContext().lookup("DefaultPoolStatelessBean/remote");
      assertNotNull(stateless);
      stateless.test();

      size = (Integer)server.getAttribute(testerName, "CurrentSize");
      assertEquals(1, size);

      size = (Integer)server.getAttribute(testerName, "AvailableCount");
      assertEquals(1, size);

      size = (Integer)server.getAttribute(testerName, "MaxSize");
      assertEquals(1, size);

      size = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(1, size);

      size = (Integer)server.getAttribute(testerName, "RemoveCount");
      assertEquals(0, size);

   }

   public void testStrictMaxPoolJmxMetrics() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:jar=stateless-test.jar,name=StrictMaxPoolStatelessBean,service=EJB3");
      int size = 0;

      size = (Integer)server.getAttribute(testerName, "CurrentSize");
      assertEquals(0, size);

      size = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(0, size);

      StatelessRemote stateless = (StatelessRemote)getInitialContext().lookup("StrictMaxPoolStatelessBean/remote");
      assertNotNull(stateless);
      stateless.test();

      size = (Integer)server.getAttribute(testerName, "CurrentSize");
      assertEquals(1, size);

      size = (Integer)server.getAttribute(testerName, "AvailableCount");
      assertEquals(3, size);

      size = (Integer)server.getAttribute(testerName, "MaxSize");
      assertEquals(3, size);

      int createCount = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(1, createCount);

      runConcurrentTests(20, 1);

      int currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");

      checkMetrics(server, testerName, currentSize, 3, 3, currentSize, 0);

      createCount = (Integer)server.getAttribute(testerName, "CreateCount");

      for (int i = 1 ; i <= 10 ; ++i)
      {
         try
         {
            stateless.testException();
            fail("should have caught EJBException");
         }
         catch (javax.ejb.EJBException e)
         {
            int removeCount = (Integer)server.getAttribute(testerName, "RemoveCount");
            System.out.println("RemoveCount=" + removeCount);
            assertEquals(i, removeCount);
         }
      }
      //  New instances should have been created in the loop above,
      // since 10 bean instances were discarded because of exception.
      int createCountAfterExceptionOnBeanInstances = (Integer)server.getAttribute(testerName, "CreateCount");
      assertTrue("New instances not created", createCountAfterExceptionOnBeanInstances > createCount);


      // 10 method calls led to exceptions so 10 bean instances would be removed
      int removedCount = (Integer)server.getAttribute(testerName, "RemoveCount");
      assertEquals("Removed count does not match", 10, removedCount);

      // current size (created - removed) should now be zero
      // since all the created ones have been removed after exception
      currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");
      assertEquals("Current size is incorrect", 0, currentSize);

      // none of the instances are in use, so available count should be
      // max (=3)
      int availableCount = (Integer) server.getAttribute(testerName, "AvailableCount");
      assertEquals("Available count does not match", 3, availableCount);


      // one more round of concurrent invocations
      runConcurrentTests(20, 1);

      // again there should be no "inUse" instances and hence available count
      // should be equal to max (=3)
      availableCount = (Integer) server.getAttribute(testerName, "AvailableCount");
      assertEquals("Available count not matching", 3, availableCount);

      // some new instances should have been created in runconcurrent tests,
      // since all the bean instances were earlier removed because of exception.
      // We won't know the exact count because of the thread concurrency involved
      // but the create count should definitely be greater than the earlier
      // create count
      createCount = (Integer)server.getAttribute(testerName, "CreateCount");
      assertTrue("Create count does not match after concurrent test",createCount > createCountAfterExceptionOnBeanInstances);


      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {
               StatelessRemote stateless = (StatelessRemote)getInitialContext().lookup("StrictMaxPoolStatelessBean/remote");
               stateless.delay();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      };

      new Thread(r).start();

      Thread.sleep(10 * 1000);

      int maxSize = (Integer)server.getAttribute(testerName, "MaxSize");
      System.out.println("MaxSize=" + maxSize);

      availableCount = (Integer)server.getAttribute(testerName, "AvailableCount");
      System.out.println("AvailableCount=" + availableCount);

      assertEquals(maxSize - 1, availableCount);
   }

   protected void checkMetrics(MBeanServerConnection server, ObjectName testerName, int current, int available, int max, int create, int remove)
      throws Exception
   {

      int currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");
      System.out.println("CurrentSize=" + currentSize);

      int availableCount = (Integer)server.getAttribute(testerName, "AvailableCount");
      System.out.println("AvailableCount=" + availableCount);

      int maxSize = (Integer)server.getAttribute(testerName, "MaxSize");
      System.out.println("MaxSize=" + maxSize);

      int createCount = (Integer)server.getAttribute(testerName, "CreateCount");
      System.out.println("CreateCount=" + createCount);

      int removeCount = (Integer)server.getAttribute(testerName, "RemoveCount");
      System.out.println("RemoveCount=" + removeCount);

      if (availableCount != maxSize)
      {
         System.out.println("Waiting to stabilize ... " + availableCount + "<" + maxSize);
         Thread.sleep(1 * 60 * 1000);
         availableCount = (Integer)server.getAttribute(testerName, "AvailableCount");
      }

      assertEquals(current, currentSize);
      assertEquals(available, availableCount);
      assertEquals(max, maxSize);
      assertEquals(create, createCount);
      assertEquals(remove, removeCount);

   }

   protected void runConcurrentTests(int numThreads, int sleepSecs) throws Exception
   {
      for (int i = 0 ; i < numThreads ; ++i)
      {
         Runnable r = new Runnable()
         {
            public void run()
            {
               try
               {
                  StatelessRemote stateless = (StatelessRemote)getInitialContext().lookup("StrictMaxPoolStatelessBean/remote");
                  for (int i = 0 ; i < 25 ; ++i)
                  {
                     stateless.test();
                  }
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }
            }
         };

         new Thread(r).start();
      }

      Thread.sleep(sleepSecs * 60 * 1000);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(MetricsUnitTestCase.class, "stateless-test.jar");
   }

}
