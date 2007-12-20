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
package org.jboss.ejb3.test.txexceptions;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public interface Dao
{
   SimpleEntity get(int id);

   void createThrowAnnotatedAppException(int id) throws AnnotatedAppException;

   void createThrowAppException(int id) throws AppException;

   void createThrowDeploymentDescriptorAppException(int id) throws DeploymentDescriptorAppException;

   void createThrowCheckedRollbackException(int id) throws CheckedRollbackException;

   void createThrowDeploymentDescriptorCheckedRollbackException(int id) throws DeploymentDescriptorCheckedRollbackException;

   void createThrowNoRollbackRemoteException(int id) throws NoRollbackRemoteException;

   void createThrowNoRollbackRuntimeException(int id) throws NoRollbackRuntimeException;

   void createThrowRollbackRemoteException(int id) throws RollbackRemoteException;

   void createThrowRollbackRuntimeException(int id) throws RollbackRuntimeException;

   void remove(int id);

   void testRequiresNewWithLookedUpEntityManager() throws Exception;
}
