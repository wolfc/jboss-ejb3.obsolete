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

import java.lang.reflect.Method;

import javax.ejb.ApplicationException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.aop.Advisor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision:72368 $
 */
public class TxUtil
{
   private static final Logger log = Logger.getLogger(TxUtil.class);
   
   protected static TransactionManager getTransactionManager()
   {
      return TransactionManagerLocator.getInstance().locate();
   }

   protected static TransactionManagementType getTransactionManagementType(Advisor advisor)
   {
      TransactionManagement transactionManagement =  (TransactionManagement) advisor.resolveAnnotation(TransactionManagement.class);
      if (transactionManagement == null) return TransactionManagementType.CONTAINER;
      return transactionManagement.value();
   }

   public static ApplicationException getApplicationException(Class<?> exceptionClass, Invocation invocation)
   {
      return AbstractInterceptor.resolveAnnotation(invocation, exceptionClass, ApplicationException.class);
      /*
      MethodInvocation ejb = (MethodInvocation) invocation;
      EJBContainer container = AbstractInterceptor.getEJBContainer(invocation);

      // TODO: Wolf: refactor onto a unified metadata view
      
      if (exceptionClass.isAnnotationPresent(ApplicationException.class))
         return (ApplicationException)exceptionClass.getAnnotation(ApplicationException.class);

      JBossAssemblyDescriptorMetaData assembly = container.getAssemblyDescriptor();

      if (assembly != null)
      {
         ApplicationExceptionsMetaData exceptions = assembly.getApplicationExceptions();
         if (exceptions != null)
         {
            for(ApplicationExceptionMetaData exception : exceptions)
            {
               if (exception.getExceptionClass().equals(exceptionClass.getName()))
                  return new ApplicationExceptionImpl(exception.isRollback());
            }
         }

      }
      return null;
      */
      
   }

   /**
    * @param currentInvocation
    * @return
    */
   public static boolean getRollbackOnly(Invocation currentInvocation)
   {
      Advisor advisor = currentInvocation.getAdvisor();
      // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
      TransactionManagementType type = TxUtil.getTransactionManagementType(advisor);
      if (type != TransactionManagementType.CONTAINER)
         throw new IllegalStateException("Container " + advisor.getName() + ": it is illegal to call getRollbackOnly from BMT: " + type);

      // TODO: we should really ask a TxType object to handle getRollbackOnly()
      if(getTxType(currentInvocation) == TransactionAttributeType.SUPPORTS)
         throw new IllegalStateException("getRollbackOnly() not allowed with TransactionAttributeType.SUPPORTS (EJB 3 13.6.2.9)");
      
      try
      {
         TransactionManager tm = TxUtil.getTransactionManager();

         // The getRollbackOnly and setRollBackOnly method of the SessionContext interface should be used
         // only in the session bean methods that execute in the context of a transaction.
         if (tm.getTransaction() == null)
            throw new IllegalStateException("getRollbackOnly() not allowed without a transaction.");

         // EJBTHREE-805, consider an asynchronous rollback due to timeout
         int status = tm.getStatus();
         return status == Status.STATUS_MARKED_ROLLBACK
             || status == Status.STATUS_ROLLING_BACK
             || status == Status.STATUS_ROLLEDBACK;
      }
      catch (SystemException e)
      {
         log.warn("failed to get tx manager status; ignoring", e);
         return true;
      }
   }
   
   protected static TransactionAttributeType getTxType(Advisor advisor, Method method)
   {
      TransactionAttribute tx = (TransactionAttribute) advisor.resolveAnnotation(method, TransactionAttribute.class);

      if (tx == null)
         tx = (TransactionAttribute) advisor.resolveAnnotation(TransactionAttribute.class);

      TransactionAttributeType value = TransactionAttributeType.REQUIRED;
      if (tx != null && tx.value() != null)
      {
         value = tx.value();
      }

      return value;
   }
   
   private static TransactionAttributeType getTxType(Invocation invocation)
   {
      return getTxType(invocation.getAdvisor(), ((MethodInvocation) invocation).getActualMethod());
   }

}
