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

package org.jboss.ejb3.test.cache.integrated;

import org.jboss.ejb3.test.cache.mock.MockRegistry;
import org.jboss.ejb3.test.cache.mock.tm.MockTransactionManager;

import junit.framework.TestCase;

/**
 * @author Brian Stansberry
 *
 */
public class Ejb3CacheTestCaseBase extends TestCase
{

   /**
    * Create a new Ejb3CacheTestCaseBase. 
    */
   public Ejb3CacheTestCaseBase()
   {
      super();
   }

   /**
    * Create a new Ejb3CacheTestCaseBase.
    * 
    * @param name
    */
   public Ejb3CacheTestCaseBase(String name)
   {
      super(name);
   }

   @Override
   protected void tearDown() throws Exception
   {
      cleanSystem();
      super.tearDown();
   }
   
   protected void cleanSystem()
   {
      MockRegistry.clear();
      MockTransactionManager.cleanupTransactions();
      MockTransactionManager.cleanupTransactionManagers();
   }
   
   protected static void wait(Object obj) throws InterruptedException
   {
      synchronized (obj)
      {
         obj.wait(5000);
      }
   }
   
   protected static void sleep(long micros)
   {
      try
      {
         Thread.sleep(micros);
      }
      catch (InterruptedException e)
      {
         // ignore
      }
   }

}
