/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.txsync.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.NoSuchEJBException;
import javax.naming.NamingException;
import javax.transaction.RollbackException;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.tx.ControlledTransactionManager;
import org.jboss.ejb3.core.test.txsync.TxSync;
import org.jboss.ejb3.core.test.txsync.TxSyncBean;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class TxSyncTestCase extends AbstractEJB3TestCase
{
   private ControlledTransactionManager tm = getTransactionManager();
   
   private static void assertEmpty(Collection<?> collection)
   {
      if(!collection.isEmpty())
         fail("collection was not empty, but " + collection);
   }
   
   @Before
   public void before()
   {
      TxSyncBean.afterBeginCalls = 0;
      TxSyncBean.afterCompletionCalls = 0;
      TxSyncBean.beforeCompletionCalls = 0;
      TxSyncBean.setThrowInAfterBegin(false);
      tm.flushReports();
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      deploy("controlled-transactionmanager-beans.xml");

      AbstractEJB3TestCase.deploySessionEjb(TxSyncBean.class);
   }
   
   protected static ControlledTransactionManager getTransactionManager()
   {
      try
      {
         return lookup("java:/TransactionManager", ControlledTransactionManager.class);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Test
   public void testAfterBegin() throws Exception
   {
      tm.begin();
      try
      {
         TxSyncBean.setThrowInAfterBegin(true);
         TxSync bean = lookup("TxSyncBean/local", TxSync.class);
         try
         {
            bean.sayHi("me");
            fail("Should have thrown EJBTransactionRolledbackException");
         }
         catch(EJBTransactionRolledbackException e)
         {
            assertEquals("afterBegin", e.getCause().getMessage());
         }
      }
      finally
      {
         tm.rollback();
      }
      assertEquals(1, TxSyncBean.afterBeginCalls);
      assertEquals(0, TxSyncBean.beforeCompletionCalls);
      assertEquals(0, TxSyncBean.afterCompletionCalls);
      assertEmpty(tm.getReports());
   }
   
   @Test
   public void testAfterBeginCommit() throws Exception
   {
      tm.begin();
      try
      {
         TxSyncBean.setThrowInAfterBegin(true);
         TxSync bean = lookup("TxSyncBean/local", TxSync.class);
         try
         {
            bean.sayHi("me");
            fail("Should have thrown EJBTransactionRolledbackException");
         }
         catch(EJBTransactionRolledbackException e)
         {
            assertEquals("afterBegin", e.getCause().getMessage());
         }
      }
      finally
      {
         // force commit
         try
         {
            tm.commit();
            fail("Expected RollbackException");
         }
         catch(RollbackException e)
         {
            // good
         }
      }
      assertEquals(1, TxSyncBean.afterBeginCalls);
      assertEquals(0, TxSyncBean.beforeCompletionCalls);
      assertEquals(0, TxSyncBean.afterCompletionCalls);
      assertEmpty(tm.getReports());
   }
   
   @Test
   public void testAfterCompletion() throws Exception
   {
      tm.begin();
      try
      {
         TxSync bean = lookup("TxSyncBean/local", TxSync.class);
         bean.setThrowInAfterCompletion(true);
         bean.sayHi("me");
      }
      finally
      {
         tm.rollback();
      }
      assertEquals(1, TxSyncBean.afterBeginCalls);
      // we rollback, so no beforeCompletion
      assertEquals(0, TxSyncBean.beforeCompletionCalls);
      assertEquals(1, TxSyncBean.afterCompletionCalls);
      assertEquals(1, tm.getReports().size());
      Exception report = tm.getReports().get(0);
      assertEquals("afterCompletion", report.getMessage());
   }
   
   @Test
   public void testAfterCompletionCommit() throws Exception
   {
      tm.begin();
      try
      {
         TxSync bean = lookup("TxSyncBean/local", TxSync.class);
         bean.setThrowInAfterCompletion(true);
         bean.sayHi("me");
      }
      finally
      {
         tm.commit();
      }
      assertEquals(1, TxSyncBean.afterBeginCalls);
      assertEquals(1, TxSyncBean.beforeCompletionCalls);
      assertEquals(1, TxSyncBean.afterCompletionCalls);
      assertEquals(1, tm.getReports().size());
      Exception report = tm.getReports().get(0);
      assertEquals("afterCompletion", report.getMessage());
   }
   
   @Test
   public void testBeforeCompletion() throws Exception
   {
      tm.begin();
      try
      {
         TxSync bean = lookup("TxSyncBean/local", TxSync.class);
         bean.setThrowInBeforeCompletion(true);
         bean.sayHi("me");
      }
      finally
      {
         tm.commit();
      }
      assertEquals(1, TxSyncBean.afterBeginCalls);
      assertEquals(1, TxSyncBean.beforeCompletionCalls);
      // ignore any discrepancies here, because ControlledTM isn't compliant
      //assertEquals(0, TxSyncBean.afterCompletionCalls);
      assertEquals(1, tm.getReports().size());
      Exception report = tm.getReports().get(0);
      assertEquals("beforeCompletion", report.getMessage());
   }
   
   @Test
   public void testRemove() throws Exception
   {
      TxSync bean;
      tm.begin();
      try
      {
         bean = lookup("TxSyncBean/local", TxSync.class);
         bean.remove();
      }
      finally
      {
         tm.commit();
      }
      tm.begin();
      try
      {
         try
         {
            bean.sayHi("me");
            fail("Should have throw NoSuchEJBException");
         }
         catch(NoSuchEJBException e)
         {
            // good
         }
      }
      finally
      {
         tm.rollback();
      }
      assertEquals(1, TxSyncBean.afterBeginCalls);
      assertEquals(1, TxSyncBean.beforeCompletionCalls);
      assertEquals(1, TxSyncBean.afterCompletionCalls);
      assertEmpty(tm.getReports());
   }
   
   // Don't test this yet, because it's another issue
   //@Test
   public void testReverseSyncRemove() throws Exception
   {
      TxSync bean;
      tm.begin();
      tm.getControlledTransaction().setReverseSyncRegistration(true);
      try
      {
         bean = lookup("TxSyncBean/local", TxSync.class);
         bean.remove();
      }
      finally
      {
         tm.commit();
      }
      tm.begin();
      try
      {
         try
         {
            bean.sayHi("me");
            fail("Should have throw NoSuchEJBException");
         }
         catch(NoSuchEJBException e)
         {
            // good
         }
      }
      finally
      {
         tm.rollback();
      }
      assertEquals(1, TxSyncBean.afterBeginCalls);
      assertEquals(1, TxSyncBean.beforeCompletionCalls);
      assertEquals(1, TxSyncBean.afterCompletionCalls);
      assertEmpty(tm.getReports());
   }
}
