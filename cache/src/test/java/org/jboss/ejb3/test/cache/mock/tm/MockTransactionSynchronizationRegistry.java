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

package org.jboss.ejb3.test.cache.mock.tm;

import java.util.LinkedList;
import java.util.List;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * @author Brian Stansberry
 *
 */
public class MockTransactionSynchronizationRegistry implements TransactionSynchronizationRegistry
{
   private final MockTransaction tx;
   private LinkedList<Synchronization> synchronizations = new LinkedList<Synchronization>();
   
   public MockTransactionSynchronizationRegistry(MockTransaction tx)
   {
      assert tx != null : "tx is null";
      this.tx = tx;
   }
   
   public Object getResource(Object key) throws IllegalStateException
   {
      throw new UnsupportedOperationException("unsupported");
   }

   public boolean getRollbackOnly() throws IllegalStateException
   {      
      return false;
   }

   public Object getTransactionKey()
   {
      return tx.getId();
   }

   public int getTransactionStatus()
   {
      return tx.getStatus();
   }

   public void putResource(Object key, Object value) throws IllegalStateException
   {
      throw new UnsupportedOperationException("unsupported");
   }

   public void registerInterposedSynchronization(Synchronization sync) throws IllegalStateException
   {
      synchronizations.add(sync);
   }

   public void setRollbackOnly() throws IllegalStateException
   {
      try
      {
         tx.setRollbackOnly();
      }
      catch (SystemException e)
      {
         throw new IllegalStateException(e);
      }
   }
   
   public List<Synchronization> getSynchronizations()
   {
      return synchronizations;
   }

}
