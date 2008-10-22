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
package org.jboss.ejb3.test.initial;

import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.tm.TransactionManagerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class TxTester implements TxTesterMBean
{
   public void testTransactions() throws Exception
   {
      InitialContext ctx = new InitialContext();
      TransactionManager tm = TransactionManagerLocator.locateTransactionManager();
      TestLocal test = (TestLocal) ctx.lookup("initial-ejb3-test/TestBean/local");
      callNever(tm, test);
      callNotSupported(tm, test);
      callSupportsWithTx(tm, test);
      callSupportsWithoutTx(tm, test);
      test.required();
      callMandatoryNoTx(tm, test);
      callMandatoryWithTx(tm, test);
      callRequiresNew(tm, test);
   }

   public void callRequiresNew(TransactionManager tm, TestLocal test) throws Exception
   {
      tm.begin();
      Transaction tx = tm.getTransaction();
      test.requiresNew(tx);
      tm.commit();
   }

   public void callNever(TransactionManager tm, TestLocal test) throws Exception
   {
      boolean exceptionThrown = false;
      tm.begin();
      try
      {
         test.never();
      }
      catch (Exception ex)
      {
         //System.out.println("**************");
         //ex.printStackTrace();
         //System.out.println("******************");
         exceptionThrown = true;
      }
      tm.rollback();
      if (!exceptionThrown) throw new Exception("failed on mandatory no tx call");
   }

   public void callNotSupported(TransactionManager tm, TestLocal test) throws Exception
   {
      tm.begin();
      test.notSupported();
      tm.commit();
   }

   public void callSupportsWithTx(TransactionManager tm, TestLocal test) throws Exception
   {
      tm.begin();
      Transaction tx = tm.getTransaction();
      test.supports(tx);
      tm.commit();
   }

   public void callSupportsWithoutTx(TransactionManager tm, TestLocal test) throws Exception
   {
      test.supports(null);
   }

   public void callMandatoryNoTx(TransactionManager tm, TestLocal test) throws Exception
   {
      boolean exceptionThrown = false;
      try
      {
         test.mandatory();
      }
      catch (Exception ex)
      {
         exceptionThrown = true;
      }
      if (!exceptionThrown) throw new Exception("failed on mandatory no tx call");
   }

   public void callMandatoryWithTx(TransactionManager tm, Test test) throws Exception
   {
      tm.begin();
      test.mandatory();
      tm.commit();
   }
}



