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
package org.jboss.ejb3.tx;

import javax.ejb.EJBException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;

/**
 * EJB 3 13.6.1:
 * If a stateless session bean instance starts a transaction in a business method or interceptor method, it
 * must commit the transaction before the business method (or all its interceptor methods) returns.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatelessBMTInterceptor extends BMTInterceptor
{
   private static final Logger log = Logger.getLogger(StatelessBMTInterceptor.class);
   
   /**
    * @param tm
    */
   public StatelessBMTInterceptor(TransactionManager tm)
   {
      super(tm);
   }

   private void checkStatelessDone(String ejbName, Exception ex)
   {
      int status = Status.STATUS_NO_TRANSACTION;
   
      try
      {
         status = tm.getStatus();
      }
      catch (SystemException sex)
      {
         log.error("Failed to get status", sex);
      }
   
      switch (status)
      {
         case Status.STATUS_ACTIVE :
         case Status.STATUS_COMMITTING :
         case Status.STATUS_MARKED_ROLLBACK :
         case Status.STATUS_PREPARING :
         case Status.STATUS_ROLLING_BACK :
            try
            {
               tm.rollback();
            }
            catch (Exception sex)
            {
               log.error("Failed to rollback", sex);
            }
         // fall through...
         case Status.STATUS_PREPARED :
            String msg = "Application error: BMT stateless bean " + ejbName
                         + " should complete transactions before" + " returning (ejb1.1 spec, 11.6.1)";
            log.error(msg);
   
            // the instance interceptor will discard the instance
            if (ex != null)
            {
               if (ex instanceof EJBException)
                  throw (EJBException)ex;
               else
                  throw new EJBException(msg, ex);
            }
            else throw new EJBException(msg);
      }
   }

   public Object handleInvocation(Invocation invocation) throws Throwable
   {
      assert tm.getTransaction() == null : "can't handle BMT transaction, there is a transaction active";
      
      String ejbName = invocation.getAdvisor().getName();
      boolean exceptionThrown = false;
      try
      {
         return invocation.invokeNext();
      }
      catch (Exception ex)
      {
         exceptionThrown = true;
         checkStatelessDone(ejbName, ex);
         throw ex;
      }
      finally
      {
         try
         {
            if (!exceptionThrown) checkStatelessDone(ejbName, null);
         }
         finally
         {
            tm.suspend();
         }
      }
   }
   
}
