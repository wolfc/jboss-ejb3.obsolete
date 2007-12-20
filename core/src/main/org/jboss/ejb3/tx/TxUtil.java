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

import javax.ejb.ApplicationException;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.jboss.aop.Advisor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb.ApplicationExceptionImpl;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.spec.ApplicationExceptionMetaData;
import org.jboss.metadata.ejb.spec.ApplicationExceptionsMetaData;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 67006 $
 */
public class TxUtil
{
   public static TransactionManager getTransactionManager() throws RuntimeException
   {
      try
      {
         //return TxManager.getInstance();
         InitialContext jndiContext = InitialContextFactory.getInitialContext();
         TransactionManager tm = TransactionManagerLocator.getInstance().locate();
         return tm;
      } 
      catch (NamingException e)
      {
         throw new RuntimeException("Unable to lookup TransactionManager", e);
      }
   }

   public static TransactionManagementType getTransactionManagementType(Advisor c)
   {
      TransactionManagement transactionManagement = (TransactionManagement) c.resolveAnnotation(TransactionManagement.class);
      if (transactionManagement == null) return TransactionManagementType.CONTAINER;
      return transactionManagement.value();
   }

   public static ApplicationException getApplicationException(Class<?> exceptionClass, Invocation invocation)
   {
      MethodInvocation ejb = (MethodInvocation) invocation;
      EJBContainer container = (EJBContainer) ejb.getAdvisor();

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
   }
}
