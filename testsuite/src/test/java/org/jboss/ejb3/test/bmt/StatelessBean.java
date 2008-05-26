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
package org.jboss.ejb3.test.bmt;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.ejb3.annotation.JndiInject;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class StatelessBean implements StatelessLocal
{
   @Resource UserTransaction ut;
   @JndiInject(jndiName="java:/TransactionManager") TransactionManager tm;

   public void beginNoEnd() throws Exception
   {
      if (tm.getTransaction() != null) throw new TestException("THERE IS AN EXISTING TRANSACTION");
      ut.begin();
   }

   public void beginCommitEnd() throws Exception
   {
      if (tm.getTransaction() != null) throw new TestException("THERE IS AN EXISTING TRANSACTION");
      ut.begin();
      ut.commit();
   }

   public void beginRollbackEnd() throws Exception
   {
      if (tm.getTransaction() != null) throw new TestException("THERE IS AN EXISTING TRANSACTION");
      ut.begin();
      ut.rollback();
   }

}
