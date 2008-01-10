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
package org.jboss.ejb3.test.relationships;

import java.util.Collection;
import java.util.Iterator;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(SessionRemote.class)
public class SessionBean implements SessionRemote
{
   @PersistenceContext
   private EntityManager em;

   public void createCategory()
   {
      Category category = new Category();
      category.setId(1);
      em.persist(category);
   }

   public void createItem()
   {
      System.out.println("++++++++++++++++++++++");
      Category category = em.find(Category.class, 1L);
      Item item = new Item();
      item.setId(1);
      item.setName("hello");
      category.getItems().add(item);
   }

   public int getNumItems()
   {
      Category category = em.find(Category.class, 1L);
      for (Item i : category.getItems())
      {
         System.out.println(i.getName());
      }
      return category.getItems().size();
   }

   public long createCustomer()
   {
      Customer cust = new Customer();
      CustomerRecord record = new CustomerRecord();
      cust.setCustomerRecord(record);
      //record.setCustomer(cust);
      em.persist(cust);
      return cust.getId();
   }

   public void testInverse(long id)
   {
      CustomerRecord record = em.find(CustomerRecord.class, id);
      record.setCustomer(null);
   }


   public Customer getCustomer(long id)
   {
      return em.find(Customer.class, id);
   }

   public long creatOrder()
   {
      Order order = new Order();
      em.persist(order);
      LineItem beer = new LineItem();
      beer.setProduct("beer");
      LineItem wine = new LineItem();
      wine.setProduct("wine");

      beer.setOrder(order);
      em.persist(beer);
      wine.setOrder(order);
      em.persist(wine);
      return order.getId();
   }

   public Order getOrder(long id)
   {
      return em.find(Order.class, id);
   }

   public void deleteOne(long itemId)
   {
      LineItem item = em.find(LineItem.class, itemId);
      item.getOrder().getItems().remove(item);
      em.remove(item);
   }

   public void deleteFromCollection(long orderId)
   {
      Order order = em.find(Order.class, orderId);
      Collection<LineItem> items = order.getItems();
      Iterator<LineItem> it = items.iterator();
      while (it.hasNext())
      {
         LineItem item = it.next();
         it.remove();
         item.setOrder(null);
         break;
      }
      System.out.println("****** " + order.getItems().size());
   }

   public void merge(Order order)
   {
      em.merge(order);
   }
}
