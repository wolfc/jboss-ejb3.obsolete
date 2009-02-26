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
package org.jboss.ejb3.core.test.common.tx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * A transaction manager of which we have a better control.
 * 
 * Note that we delegate to a true transaction manager for ease.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ControlledTransactionManager implements TransactionManager
{
   private TransactionManager delegate;
   private ThreadLocal<ControlledTransaction> currentTx = new ThreadLocal<ControlledTransaction>();
   private List<Exception> reports = new ArrayList<Exception>();
   
   protected ControlledTransactionManager(TransactionManager delegate)
   {
      this.delegate = delegate;
   }
   
   public void begin() throws NotSupportedException, SystemException
   {
      delegate.begin();
   }

   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
      SecurityException, IllegalStateException, SystemException
   {
      currentTx.remove();
      delegate.commit();
   }

   protected void flush(ControlledTransaction tx)
   {
      if(currentTx.get().equals(tx))
         currentTx.remove();
   }
   
   public void flushReports()
   {
      reports.clear();
   }
   
   public ControlledTransaction getControlledTransaction() throws SystemException
   {
      ControlledTransaction tx = currentTx.get();
      if(tx == null)
      {
         Transaction delegateTx = delegate.getTransaction();
         if(delegateTx != null)
         {
            tx = new ControlledTransaction(this, delegateTx);
            currentTx.set(tx);
         }
      }
      return tx;
   }
   
   public List<Exception> getReports()
   {
      return Collections.unmodifiableList(reports);
   }
   
   public int getStatus() throws SystemException
   {
      return delegate.getStatus();
   }

   public Transaction getTransaction() throws SystemException
   {
      return getControlledTransaction();
   }

   protected void report(Exception e)
   {
      reports.add(e);
   }
   
   public void resume(Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException
   {
      throw new RuntimeException("NYI");
   }

   public void rollback() throws IllegalStateException, SecurityException, SystemException
   {
      currentTx.remove();
      delegate.rollback();
   }

   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      throw new RuntimeException("NYI");
   }

   public void setTransactionTimeout(int timeout) throws SystemException
   {
      throw new RuntimeException("NYI");
   }

   public Transaction suspend() throws SystemException
   {
      throw new RuntimeException("NYI");
   }
   
}
