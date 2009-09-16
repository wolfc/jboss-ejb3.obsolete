/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1116.unit;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.aop.DispatcherConnectException;
import org.jboss.aop.NotFoundInDispatcherException;
import org.jboss.ejb3.test.ejbthree1116.MyStateful;
import org.jboss.remoting.CannotConnectException;
import org.jboss.test.JBossTestCase;

/**
 * Test verifying fix of EJBTHREE-1116 & EJBTHREE-1894.
 * @author Paul Ferraro
 */
public class ContainerShutdownTestCase extends JBossTestCase
{
   private static final String JAR = "ejbthree1116.jar";
   private static final int ITERATIONS = 100;
   
   private final Set<Class<? extends Exception>> allowedExceptions = new HashSet<Class<? extends Exception>>();
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(ContainerShutdownTestCase.class, JAR);
   }
   
   /**
    * Create a new ContainerShutdownTestCase.
    * 
    * @param name
    */
   public ContainerShutdownTestCase(String name)
   {
      super(name);
      
      this.allowedExceptions.add(CannotConnectException.class);
      this.allowedExceptions.add(DispatcherConnectException.class);
      this.allowedExceptions.add(NotFoundInDispatcherException.class);
   }
   
   public void testSimpleRemoteInvocation() throws Exception
   {
      MyStateful bean = (MyStateful) new InitialContext().lookup("MyStatefulBean/remote");
      
      this.undeploy(JAR);
      
      try
      {
         bean.increment();
      }
      catch (RuntimeException e)
      {
         Throwable cause = e.getCause();

         if ((cause == null) || !this.allowedExceptions.contains(cause.getClass())) throw e;
      }
      finally
      {
         this.deploy(JAR);
      }
   }

   public void testConcurrentRemoteInvocation() throws Exception
   {
      for (int i = 0; i < ITERATIONS; ++i)
      {
         MyStateful bean = (MyStateful) new InitialContext().lookup("MyStatefulBean/remote");
         
         final CountDownLatch latch = new CountDownLatch(2);
         
         Thread thread = new Thread(new UndeployTask(latch));
         
         thread.start();
         
         latch.countDown();
         
         try
         {
            latch.await();
            
            while (!Thread.currentThread().isInterrupted())
            {
               bean.increment();
            }
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         catch (RuntimeException e)
         {
            Throwable cause = e.getCause();
            
            if ((cause == null) || !this.allowedExceptions.contains(cause.getClass())) throw e;
         }
         finally
         {
            try
            {
               thread.join();
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
            
            this.deploy(JAR);
         }
      }
   }
   
   public void testSimpleRemove() throws Exception
   {
      MyStateful bean = (MyStateful) new InitialContext().lookup("MyStatefulBean/remote");
      
      this.undeploy(JAR);
      
      try
      {
         bean.remove();
      }
      catch (RuntimeException e)
      {
         Throwable cause = e.getCause();
         
         if ((cause == null) || !this.allowedExceptions.contains(cause.getClass())) throw e;
      }
      finally
      {
         this.deploy(JAR);
      }
   }

   public void testConcurrentRemove() throws Exception
   {
      for (int i = 0; i < ITERATIONS; ++i)
      {
         final CountDownLatch latch = new CountDownLatch(2);
         
         Thread thread = new Thread(new UndeployTask(latch));
         
         thread.start();
         
         try
         {
            MyStateful[] beans = new MyStateful[ITERATIONS];
            
            for (int j = 0; j < ITERATIONS; ++j)
            {
               beans[j] = (MyStateful) new InitialContext().lookup("MyStatefulBean/remote");
            }
            
            latch.countDown();
            latch.await();
            
            for (int j = 0; j < ITERATIONS; ++j)
            {
               beans[j].remove();
            }
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         catch (RuntimeException e)
         {
            Throwable cause = e.getCause();
            
            if ((cause == null) || !this.allowedExceptions.contains(cause.getClass())) throw e;
         }
         finally
         {
            try
            {
               thread.join();
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
            
            this.deploy(JAR);
         }
      }
   }
   
   public void testSimpleCreate() throws Exception
   {
      this.undeploy(JAR);
      
      try
      {
         new InitialContext().lookup("MyStatefulBean/remote");         
      }
      catch (NamingException e)
      {
         // Expected
      }
      finally
      {
         this.deploy(JAR);
      }
   }

   public void testConcurrentCreate() throws Exception
   {
      for (int i = 0; i < ITERATIONS; ++i)
      {
         final CountDownLatch latch = new CountDownLatch(2);
         
         Thread thread = new Thread(new UndeployTask(latch));
         
         thread.start();
         
         latch.countDown();
         
         try
         {
            latch.await();
            
            while (!Thread.currentThread().isInterrupted())
            {
               new InitialContext().lookup("MyStatefulBean/remote");
            }
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         catch (NamingException e)
         {
            // Expected
         }
         catch (RuntimeException e)
         {
            Throwable cause = e.getCause();
            
            if ((cause == null) || !this.allowedExceptions.contains(cause.getClass())) throw e;
         }
         finally
         {
            try
            {
               thread.join();
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
            
            this.deploy(JAR);
         }
      }
   }

   private class UndeployTask implements Runnable
   {
      private final CountDownLatch latch;
      
      UndeployTask(CountDownLatch latch)
      {
         this.latch = latch;
      }
      
      public void run()
      {
         latch.countDown();
         
         try
         {
            latch.await();
            
            ContainerShutdownTestCase.this.undeploy(JAR);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         catch (Exception e)
         {
            e.printStackTrace(System.err);
         }
      }
   }
}
