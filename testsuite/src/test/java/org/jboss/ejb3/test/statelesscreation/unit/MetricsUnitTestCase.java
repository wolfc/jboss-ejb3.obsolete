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
package org.jboss.ejb3.test.statelesscreation.unit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.ejb3.test.statelesscreation.DestroyRemote;
import org.jboss.ejb3.test.statelesscreation.StatelessRemote;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MetricsUnitTestCase
   extends JBossTestCase
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(MetricsUnitTestCase.class);

   public MetricsUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testSLSBCount() throws Exception
   {
      DestroyRemote slsb = (DestroyRemote)getInitialContext().lookup("DestroyStatelessBean/remote");
      assertNotNull(slsb);
      assertEquals(1, slsb.getBeanCount());
      
      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {
               InitialContext jndiContext = getInitialContext();
               for (int i = 0 ; i < 100 ; ++i)
               {                  
                  DestroyRemote slsb = (DestroyRemote)jndiContext.lookup("DestroyStatelessBean/remote");              
                  slsb.getBeanCount();
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      };
      Thread threads[] = new Thread[50];
      for (int i = 0 ; i < threads.length ; ++i)
      {
         threads[i] = new Thread(r);
         threads[i].start();
      }
      
      for(Thread t : threads)
      {
         t.join(1 * 60 * 1000);
      }
      
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:jar=statelesscreation-test.jar,name=DestroyStatelessBean,service=EJB3");
   
      int currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");
      System.out.println("CurrentSize=" + currentSize);
      
      int availableCount = (Integer)server.getAttribute(testerName, "AvailableCount");
      System.out.println("AvailableCount=" + availableCount);
      
      int maxSize = (Integer)server.getAttribute(testerName, "MaxSize");
      System.out.println("MaxSize=" + maxSize);
      
      int removeCount = (Integer)server.getAttribute(testerName, "RemoveCount");
      System.out.println("RemoveCount=" + removeCount);
      
      int createCount = (Integer)server.getAttribute(testerName, "CreateCount");
      System.out.println("CreateCount=" + createCount);
      
      
      assertEquals(20, availableCount);
      assertEquals(20, maxSize);
      assertEquals(currentSize, createCount);
      assertEquals(0, removeCount);
      
   }
   
   public void testThreadLocalPoolJmxMetrics() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:jar=statelesscreation-test.jar,name=ThreadLocalPoolStatelessBean,service=EJB3");
      int size = 0;
      
      // For a detailed explanation about what each metric value
      // means when a ThreadLocalPool is used, check the comments
      // from carlo in this JIRA https://jira.jboss.org/jira/browse/EJBTHREE-1703
      
      int currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");
      assertEquals(0, size);
      
      size = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(0, size);
      
      StatelessRemote stateless = (StatelessRemote)getInitialContext().lookup("ThreadLocalPoolStatelessBean/remote");
      assertNotNull(stateless);
      stateless.test();     
      
      currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");
      assertEquals(1, currentSize);
      
      size = (Integer)server.getAttribute(testerName, "AvailableCount");
      assertEquals(1, size);
      
      size = (Integer)server.getAttribute(testerName, "MaxSize");
      assertEquals(1, size);
      
      size = (Integer)server.getAttribute(testerName, "CreateCount");
      assertEquals(1, size);
      
      runConcurrentTests(30, 1);
      
      currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");
      
      checkMetrics(server, testerName, currentSize, 20, 20, currentSize, 0);
     
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
      
      runConcurrentTests(30, 1);
      
      currentSize = (Integer)server.getAttribute(testerName, "CurrentSize");
      
      checkMetrics(server, testerName, currentSize, 20, 20, currentSize + 10, 10);
      
      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {
               StatelessRemote stateless = (StatelessRemote)getInitialContext().lookup("ThreadLocalPoolStatelessBean/remote");                                            
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
      
      int availableCount = (Integer)server.getAttribute(testerName, "AvailableCount");
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
      
      assertEquals("CurrentSize", current, currentSize);
      assertEquals("AvailableCount", available, availableCount);
      assertEquals("MaxSize", max, maxSize);
      assertEquals("CreateCount", create, createCount);
      assertEquals("RemoveCount", remove, removeCount);
      
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
                  StatelessRemote stateless = (StatelessRemote)getInitialContext().lookup("ThreadLocalPoolStatelessBean/remote");                              
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
      return getDeploySetup(MetricsUnitTestCase.class, "statelesscreation-connectors-service.xml,statelesscreation-test.jar");
   }
}
