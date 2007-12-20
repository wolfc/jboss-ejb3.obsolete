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
package org.jboss.ejb3.test.initial;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.JndiInject;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 67628 $
 */
@Stateless
public class TestBean implements TestLocal, TestRemote
{
   private TransactionManager tm;
   public static Map obj = new HashMap();

   public Map getObject()
   {
      return obj;
   }

   public Object echo(Object e)
   {
      return e;
   }

   @JndiInject(jndiName = "java:/TransactionManager")
   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
      System.out.println("TransactionManager set: " + tm);
   }

   public String testMe(String echo)
   {
      System.out.println("JDK15 testMe worked");
      return echo;
   }

   @TransactionAttribute(TransactionAttributeType.NEVER)
   public void never()
   {
   }

   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public void notSupported() throws Exception
   {
      if (tm.getTransaction() != null) throw new Exception("notsupported() method has tx set");
   }

   @TransactionAttribute(TransactionAttributeType.SUPPORTS)
   public void supports(Transaction tx) throws Exception
   {
      Transaction tmTx = tm.getTransaction();
      if (tx != tmTx) throw new Exception("supports didn't work");
   }

   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void required() throws Exception
   {
      if (tm.getTransaction() == null) throw new Exception("rquired() method has no tx set");
   }


   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public void requiresNew(Transaction tx) throws Exception
   {
      Transaction tmTx = tm.getTransaction();
      if (tx == tmTx || (tx != null && tx.equals(tmTx)))
         throw new Exception("transactions shouldn't be equal");
      if (tmTx == null) throw new Exception("tx is null in RequiresNew");
   }


   @TransactionAttribute(TransactionAttributeType.MANDATORY)
   public void mandatory()
   {
   }
}



