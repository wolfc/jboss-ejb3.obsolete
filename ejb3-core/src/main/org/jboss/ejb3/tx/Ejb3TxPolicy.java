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
package org.jboss.ejb3.tx;

import java.rmi.RemoteException;

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.transaction.Transaction;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Ejb3TxPolicy extends org.jboss.aspects.tx.TxPolicy
{
   public void throwMandatory(Invocation invocation)
   {
      throw new EJBTransactionRequiredException(((MethodInvocation) invocation).getActualMethod().toString());
   }

   public void handleExceptionInOurTx(Invocation invocation, Throwable t, Transaction tx) throws Throwable
   {
      ApplicationException ae = TxUtil.getApplicationException(t.getClass(), invocation);
      if (ae != null)
      {
         if (ae.rollback()) setRollbackOnly(tx);
         throw t;
      }
      if (!(t instanceof RuntimeException || t instanceof RemoteException))
      {
         throw t;
      }
      setRollbackOnly(tx);
      if (t instanceof RuntimeException && !(t instanceof EJBException))
      {
         throw new EJBException((Exception) t);
      }
      throw t;
   }

   public void handleInCallerTx(Invocation invocation, Throwable t, Transaction tx) throws Throwable
   {
      ApplicationException ae = TxUtil.getApplicationException(t.getClass(), invocation);
   
      if (ae != null)
      {
         if (ae.rollback()) setRollbackOnly(tx);
         throw t;
      }
      if (!(t instanceof RuntimeException || t instanceof RemoteException))
      {
         throw t;
      }
      setRollbackOnly(tx);
      // its either a RuntimeException or RemoteException
      
      if (t instanceof EJBTransactionRolledbackException)
         throw t;
      else
         throw new EJBTransactionRolledbackException((Exception) t);
   }

}
