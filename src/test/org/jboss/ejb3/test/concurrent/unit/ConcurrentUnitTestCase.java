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
package org.jboss.ejb3.test.concurrent.unit;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.ejb.ConcurrentAccessException;

import org.jboss.ejb3.test.concurrent.MyStateful;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Test concurrent access on a stateful bean (EJB3 4.3.13 3rd paragraph / EJBTHREE-666)
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ConcurrentUnitTestCase
   extends JBossTestCase
{
   public ConcurrentUnitTestCase(String name)
   {
      super(name);
   }
   
   private static class MyUncaughtExceptionHandler implements UncaughtExceptionHandler
   {
      private Throwable uncaught;
      
      Throwable getUncaughtException()
      {
         return uncaught;
      }
      
      public void uncaughtException(Thread t, Throwable e)
      {
         this.uncaught = e;
      }
   }
   
   public void testConcurrentAccess() throws Exception
   {
      final MyStateful session = (MyStateful) getInitialContext().lookup("MyStatefulBean/remote");
      
      Runnable r = new Runnable()
      {
         public void run()
         {
            session.waitAndSee();
         }
      };

      // do a call first (see EJBTHREE-697)
      session.doNothing();
      
      // just use one 1 handler, the exception will be in there.
      MyUncaughtExceptionHandler eh = new MyUncaughtExceptionHandler();
      
      Thread t1 = new Thread(r);
      t1.setUncaughtExceptionHandler(eh);
      Thread t2 = new Thread(r);
      t2.setUncaughtExceptionHandler(eh);
      
      t1.start();
      t2.start();
      
      t1.join();
      t2.join();
      
      Throwable t = eh.getUncaughtException();
      assertNotNull(t);
      assertTrue("Expected a javax.ejb.ConcurrentAccessException", t instanceof ConcurrentAccessException);
   }
   
   /**
    * EJBTHREE-697: concurrency on proxies doesn't work
    */
   public void testConcurrentProxyAccess() throws Exception
   {
      final MyStateful session = (MyStateful) getInitialContext().lookup("MyStatefulBean/remote");
      
      Runnable r = new Runnable()
      {
         public void run()
         {
            session.waitAndSee();
         }
      };

      // don't call proxy yet
      //session.doNothing();
      
      // just use one 1 handler, the exception will be in there.
      MyUncaughtExceptionHandler eh = new MyUncaughtExceptionHandler();
      
      Thread t1 = new Thread(r);
      t1.setUncaughtExceptionHandler(eh);
      Thread t2 = new Thread(r);
      t2.setUncaughtExceptionHandler(eh);
      
      t1.start();
      t2.start();
      
      t1.join();
      t2.join();
  
      Throwable t = eh.getUncaughtException();
      assertNotNull("No exception occured during a concurrent call", t);
      fail("never comes here");
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(ConcurrentUnitTestCase.class, "concurrent.jar");
   }

}
