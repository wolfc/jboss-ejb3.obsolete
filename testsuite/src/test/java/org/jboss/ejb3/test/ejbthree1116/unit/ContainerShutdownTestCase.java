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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CountDownLatch;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.aop.DispatcherConnectException;
import org.jboss.ejb3.test.ejbthree1116.MyStateful;
import org.jboss.test.JBossTestCase;

/**
 * Test verifying fix of EJBTHREE-1116.
 * @author Paul Ferraro
 */
public class ContainerShutdownTestCase extends JBossTestCase
{
   private static final String JAR = "ejbthree1116.jar";
   
   /**
    * Create a new ContainerShutdownTestCase.
    * 
    * @param name
    */
   public ContainerShutdownTestCase(String name)
   {
      super(name);
   }
   
   public void testSimple() throws Exception
   {
      MyStateful bean = (MyStateful) new InitialContext().lookup("MyStatefulBean/remote");
      
      this.undeploy(JAR);
      
      try
      {
         bean.increment();
      }
      catch (UndeclaredThrowableException e)
      {
         assertInstanceOf(e.getUndeclaredThrowable(), DispatcherConnectException.class);
      }
      catch (Exception e)
      {
         this.log.error(e.getMessage(), e);
         
         assertTrue(e.toString(), false);
      }
      finally
      {
         this.deploy(JAR);
      }
   }

   public void testConcurrent() throws Exception
   {
      MyStateful bean = (MyStateful) new InitialContext().lookup("MyStatefulBean/remote");
      
      CountDownLatch latch = new CountDownLatch(2);
      
      Thread thread = new Thread(new Undeployer(latch));
      
      thread.start();
      
      latch.countDown();
      
      try
      {
         latch.await();
         
         while (!Thread.currentThread().isInterrupted())
         {
            bean.increment();
         }
         
         this.log.warn("Test was interrupted");
      }
      catch (InterruptedException e)
      {
         this.log.warn("Test was interrupted");
      }
      catch (Exception e)
      {
         this.log.error(e.getMessage(), e);
         
         assertInstanceOf(e, UndeclaredThrowableException.class);
         
         assertInstanceOf(((UndeclaredThrowableException) e).getUndeclaredThrowable(), DispatcherConnectException.class);
      }
      finally
      {
         try
         {
            thread.join();
            
            this.deploy(JAR);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
      }
   }
   
   private class Undeployer implements Runnable
   {
      private CountDownLatch latch;
      
      public Undeployer(CountDownLatch latch)
      {
         this.latch = latch;
      }
      
      public void run()
      {
         try
         {
            this.latch.countDown();
            this.latch.await();

            ContainerShutdownTestCase.this.undeploy(JAR);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         catch (Exception e)
         {
            ContainerShutdownTestCase.this.log.error(e.getMessage(), e);
         }
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ContainerShutdownTestCase.class, JAR);
   }
}
