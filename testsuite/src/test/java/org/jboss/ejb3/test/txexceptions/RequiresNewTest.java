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

import org.jboss.ejb3.tx.TxUtil;

import javax.persistence.EntityManager;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class RequiresNewTest
{
   public static void doit() throws Exception
   {
      InitialContext ctx = new InitialContext();
      EntityManager em = (EntityManager)ctx.lookup("java:/SimpleEntityManager");
      TransactionManager tm = TxUtil.getTransactionManager();

      tm.begin();
      SimpleEntity entity = new SimpleEntity();
      entity.setStuff("hello");
      entity.setId(11223123);
      em.persist(entity);
      em.flush();

      Transaction tx = tm.suspend();
      tm.begin();
      if (em.contains(entity)) throw new RuntimeException("entity should not be managed as we should have a different session");
      tm.commit();
      tm.resume(tx);
      tm.commit();
   }

   public static void daoCreateThrowRollbackError() throws Exception
   {
      TransactionManager tm = TxUtil.getTransactionManager();
      tm.begin();

      InitialContext ctx = new InitialContext();
      Dao dao = (Dao) ctx.lookup("DaoBean/remote");

      try
      {
         dao.createThrowRollbackError(1);
         throw new RuntimeException("Expected error not thrown");
      }
      catch (EJBException e)
      {
         // AFAIK, the spec doesn't define how the causing error should be delivered
         // Currently, it's done the same way our EJB2 containers handle errors,
         // i.e. the msg is formatted including the stacktrace of the error
         // and re-thrown as the EJBException
      }
      finally
      {
         tm.rollback();
      }
      
      SimpleEntity entity = dao.get(1);
      
      if (entity != null)
         dao.remove(1);
      
      if(entity != null)
         throw new RuntimeException("Entity is there");
   }
}
