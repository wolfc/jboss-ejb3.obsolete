/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1020.unit;

import java.lang.Thread.UncaughtExceptionHandler;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1020.MyStateful;
import org.jboss.test.JBossTestCase;

/**
 * Test to see if resources of type URL work.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 64465 $
 */
public class EjbContextCheckTestCase extends JBossTestCase
{
   private static class MyUncaughtExceptionHandler implements UncaughtExceptionHandler
   {
      private Throwable uncaught;
      
      Throwable getUncaughtException()
      {
         return uncaught;
      }
      
      public void uncaughtException(Thread t, Throwable e)
      {
         e.printStackTrace();
         this.uncaught = e;
      }
   }
   
   public EjbContextCheckTestCase(String name)
   {
      super(name);
   }

   private MyStateful lookupBean() throws Exception
   {
      return (MyStateful) getInitialContext().lookup("MyStatefulBean/remote");
   }
   
   public void test1() throws Exception
   {
      final MyStateful bean1 = lookupBean();
      bean1.create(1);
      
      MyStateful bean2 = lookupBean();
      bean2.create(2);
      
      MyUncaughtExceptionHandler handler = new MyUncaughtExceptionHandler();
      Thread thread = new Thread()
      {
         @Override
         public void run()
         {
            MyStateful beans[] = bean1.method1(1);
            for(MyStateful bean : beans)
            {
               assertEquals(1, bean.getId());
            }
         }
      };
      thread.setUncaughtExceptionHandler(handler);
      thread.start();
      
      Thread.sleep(1000);
      
      MyStateful bean = bean2.method2(2);
      assertEquals(2, bean.getId());
      
      thread.join(5000);
      assertNull(handler.getUncaughtException());
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EjbContextCheckTestCase.class, "ejbthree1020.jar");
   }
}
