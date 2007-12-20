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
package org.jboss.ejb3.test.asynchronous;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.Local;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContext;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
@Stateless
@Remote(TxSessionRemote.class)
@Local(TxSessionLocal.class)
public class TxSessionBean implements TxSessionRemote, TxSessionLocal
{
   @PersistenceContext EntityManager manager;
   @Resource javax.ejb.SessionContext ctx;

   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void createFruit(String name, boolean rollback)
   {
      Fruit fruit = new Fruit(name);
      manager.persist(fruit);
      if (rollback)ctx.setRollbackOnly();
   }

   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void createVeg(String name, boolean rollback)
   {
      Vegetable veg = new Vegetable(name);
      manager.persist(veg);
      if (rollback)ctx.setRollbackOnly();
   }

   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public Collection getEntries()
   {
      Query vegQuery = manager.createQuery("SELECT v FROM Vegetable v");
      List veg = vegQuery.getResultList();
      Query fruitQuery = manager.createQuery("SELECT f FROM Fruit f");
      List fruit = fruitQuery.getResultList();


      ArrayList list = new ArrayList();
      for (Iterator it = veg.iterator() ; it.hasNext() ; )
      {
         list.add(it.next().toString());
      }

      for (Iterator it = fruit.iterator() ; it.hasNext() ; )
      {
         list.add(it.next().toString());
      }


      return list;
   }

   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void cleanAll()
   {
      Query vegQuery = manager.createQuery("SELECT v FROM Vegetable v");
      List veg = vegQuery.getResultList();
      for (Iterator it = veg.iterator() ; it.hasNext() ; )
      {
         manager.remove(it.next());
      }

      Query fruitQuery = manager.createQuery("SELECT f FROM Fruit f");
      List fruit = fruitQuery.getResultList();
      for (Iterator it = fruit.iterator() ; it.hasNext() ; )
      {
         manager.remove(it.next());
      }
   }

}
