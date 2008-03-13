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

package org.jboss.ejb3.cache.spi;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 * Coordinates order of transaction {@link Synchronization} execution, allowing
 * different elements of the caching system to ensure their synchronization
 * executes either relatively early or relatively late in the transaction 
 * commit process.
 * <p>
 * Note that a <code>SynchronizationCoordinator</code> has no effect on the
 * order of execution of synchronizations it doesn't know about (e.g. those
 * registered by code external to the caching subsystem.)
 * </p>
 * 
 * @author Brian Stansberry
 */
public interface SynchronizationCoordinator
{
   /**
    * Register the given Synchronization with the given Transaction, 
    * ensuring that during transaction commit the synchronization executes 
    * before any other synchronizations <strong>previously</strong> added to 
    * this coordinator.
    * 
    * @param tx   the transaction
    * @param sync the synchronization
    * 
    * @throws RollbackException
    * @throws SystemException
    */
   void addSynchronizationFirst(Transaction tx, Synchronization sync) 
      throws RollbackException, SystemException;
   
   /**
    * Register the given Synchronization with the given Transaction, 
    * ensuring that during transaction commit the synchronization executes 
    * after any other synchronizations <strong>previously</strong> added to 
    * this coordinator.
    * 
    * @param tx   the transaction
    * @param sync the synchronization
    * 
    * @throws RollbackException
    * @throws SystemException
    */
   void addSynchronizationLast(Transaction tx, Synchronization sync) 
      throws RollbackException, SystemException;
}
