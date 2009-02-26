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

import java.util.LinkedList;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * We keep track of the synchronizations ourself.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ControlledTransaction implements Transaction
{
   protected class ControlledSynchronization implements Synchronization
   {
      public void afterCompletion(int status)
      {
         ControlledTransaction.this.afterCompletion(status);
      }

      public void beforeCompletion()
      {
         ControlledTransaction.this.beforeCompletion();
      }
   }
   
   private static final Logger log = Logger.getLogger(ControlledTransaction.class);
   
   private ControlledTransactionManager tm;
   private Transaction delegate;
   private ControlledSynchronization sync = new ControlledSynchronization();
   private LinkedList<Synchronization> syncs = new LinkedList<Synchronization>();

   private boolean reverseSyncRegistration = false;
   
   protected ControlledTransaction(ControlledTransactionManager tm, Transaction tx) throws SystemException, IllegalStateException
   {
      this.tm = tm;
      this.delegate = tx;
      try
      {
         if(tx.getStatus() == Status.STATUS_ACTIVE)
            tx.registerSynchronization(sync);
      }
      catch (RollbackException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected void afterCompletion(int status)
   {
      for(Synchronization sync : syncs)
      {
         try
         {
            sync.afterCompletion(status);
         }
         catch(Exception e)
         {
            log.error("afterCompletion", e);
            tm.report(e);
         }
      }
   }
   
   protected void beforeCompletion()
   {
      for(Synchronization sync : syncs)
      {
         try
         {
            sync.beforeCompletion();
         }
         catch(Exception e)
         {
            log.error("beforeCompletion", e);
            tm.report(e);
         }
      }
   }
   
   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
      SecurityException, IllegalStateException, SystemException
   {
      delegate.commit();
      tm.flush(this);
   }

   public boolean delistResource(XAResource resource, int flag) throws IllegalStateException, SystemException
   {
      return delegate.delistResource(resource, flag);
   }

   public boolean enlistResource(XAResource resource) throws RollbackException, IllegalStateException, SystemException
   {
      return delegate.enlistResource(resource);
   }

   public int getStatus() throws SystemException
   {
      return delegate.getStatus();
   }

   public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException,
      SystemException
   {
      //delegate.registerSynchronization(sync);
      if(reverseSyncRegistration)
         syncs.addFirst(sync);
      else
         syncs.addLast(sync);
   }

   public void rollback() throws IllegalStateException, SystemException
   {
      delegate.rollback();
      tm.flush(this);
   }

   public void setReverseSyncRegistration(boolean flag)
   {
      this.reverseSyncRegistration  = flag;
   }
   
   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      delegate.setRollbackOnly();
   }

}
