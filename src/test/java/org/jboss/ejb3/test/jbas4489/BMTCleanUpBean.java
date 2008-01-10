/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.jbas4489;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 65875 $
 */
@Stateless
@Remote(BMTCleanUp.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class BMTCleanUpBean implements BMTCleanUp
{
   private static final Logger log = Logger.getLogger(BMTCleanUpBean.class);
   
   @Resource
   private SessionContext sessionCtx;
   
   private void checkTransaction()
   {
      TransactionManager tm = TransactionManagerLocator.getInstance().locate();
      try
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
            throw new IllegalStateException("There should be no transaction context: " + tx);
      }
      catch (Exception e)
      {
         throw new EJBException("Error", e);
      }
   }

   public void doIncomplete()
   {
      UserTransaction ut = sessionCtx.getUserTransaction();
      try
      {
         ut.begin();
      }
      catch (Exception e)
      {
         throw new EJBException("Error", e);
      }
   }

   public void doNormal()
   {
      UserTransaction ut = sessionCtx.getUserTransaction();
      try
      {
         ut.begin();
         ut.commit();
      }
      catch (Exception e)
      {
         throw new EJBException("Error", e);
      }
   }

   public void doTimeout()
   {
      UserTransaction ut = sessionCtx.getUserTransaction();
      try
      {
         ut.setTransactionTimeout(5);
         ut.begin();
         Thread.sleep(10000);
         log.info("tx status: " + ut.getStatus());
      }
      catch (InterruptedException ignored)
      {
      }
      catch (Exception e)
      {
         throw new EJBException("Error", e);
      }
   }

   public void testIncomplete()
   {
      BMTCleanUp remote = sessionCtx.getBusinessObject(BMTCleanUp.class);
      try
      {
         remote.doIncomplete();
         throw new RuntimeException("Expected an EJBException for incomplete transaction");
      }
      catch (EJBException expected)
      {
         // expected
         log.debug("Expected exception", expected);
      }
      checkTransaction();
      remote.doNormal();
   }

   public void testTxTimeout()
   {
      BMTCleanUp remote = sessionCtx.getBusinessObject(BMTCleanUp.class);
      remote.doTimeout();
      checkTransaction();
      remote.doNormal();
   }

}
