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
package org.jboss.ejb3.test.entityexception.unit;

import org.jboss.ejb3.test.entityexception.ExceptionTest;
import org.jboss.ejb3.test.entityexception.Person;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

public class EntityExceptionTestCase extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();
   
   Person kabir;
   
   public EntityExceptionTestCase(String name)
   {
      super(name);
   }

   public void testTransactionRequiredException() throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      test.testTransactionRequiredException();
   }
   
   public void testPersistExceptions() throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      kabir = new Person(1, "Kabir");
      kabir = test.createEntry(kabir);
      
      test.testEMPersistExceptions();
   }

   public void testEMFindExceptions() throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      assertTrue(test.testEMFindExceptions());
   }

   public void testEMMergeExceptions() throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      test.testEMMergeExceptions();
   }
   
   public void testEMCreateQueryExceptions() throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      test.testEMCreateQueryExceptions();
   }
   
   public void testEMRefreshExceptions() throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      test.testEMRefreshExceptions();
   }
   
   public void testEMContainsExceptions() throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      test.testEMContainsExceptions();
   }
   
   public void testQuerySingleResultExceptions()throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      test.testQuerySingleResultExceptions();
   }

   public void testQuerySetHintAndParameter()throws Exception
   {
      ExceptionTest test = 
         (ExceptionTest)getInitialContext().lookup("ExceptionTestBean/remote");
      test.testQuerySetHintAndParameter();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityExceptionTestCase.class, "entityexception-test.jar");
   }


}
