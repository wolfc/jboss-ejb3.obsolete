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
package org.jboss.ejb3.test.relationships.unit;

import java.util.Collection;
import java.util.Iterator;
import org.jboss.ejb3.test.relationships.Customer;
import org.jboss.ejb3.test.relationships.LineItem;
import org.jboss.ejb3.test.relationships.Order;
import org.jboss.ejb3.test.relationships.SessionRemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Test reentrant remote call.  There was a bug that remoting didn't route locally
 * and the tx propagation was happening when it shouldn't
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: RelationshipUnitTestCase.java 61136 2007-03-06 09:24:20Z wolfc $
 */

public class RelationshipUnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public RelationshipUnitTestCase(String name)
   {

      super(name);

   }

   public void testUniOneToMany() throws Exception
   {
      SessionRemote remote = (SessionRemote) getInitialContext().lookup("SessionBean/remote");
      remote.createCategory();
      remote.createItem();
      int num = remote.getNumItems();
      assertEquals(1, num);
   }

   public void testOneToOne() throws Exception
   {
      SessionRemote remote = (SessionRemote) getInitialContext().lookup("SessionBean/remote");
      long id = remote.createCustomer();
      Customer cust = remote.getCustomer(id);
      long record = cust.getCustomerRecord().getId();
      remote.testInverse(record);
      cust = remote.getCustomer(id);
      record = cust.getCustomerRecord().getId();

   }

   public void testMappedByOneToMany() throws Exception
   {
      SessionRemote remote = (SessionRemote) getInitialContext().lookup("SessionBean/remote");
      long id1 = remote.createCustomer();
      Customer cust1 = remote.getCustomer(id1);
      long orderId = remote.creatOrder();
      Order order = remote.getOrder(orderId);
      order.setCustomer(cust1);
      remote.merge(order);
      orderId = remote.creatOrder();
      order = remote.getOrder(orderId);
      order.setCustomer(cust1);
      remote.merge(order);
      cust1 = remote.getCustomer(id1);
      assertEquals(cust1.getOrders().size(), 2);
      for (Order orderIn : cust1.getOrders())
      {
         orderIn.setCustomer(null);
         remote.merge(orderIn);
         break;
      }
      cust1 = remote.getCustomer(id1);
      assertEquals(cust1.getOrders().size(), 1);



   }

   public void testRemove() throws Exception
   {

      SessionRemote remote = (SessionRemote) getInitialContext().lookup("SessionBean/remote");
      long id = remote.creatOrder();
      Order order = remote.getOrder(id);
      assertEquals(order.getItems().size(), 2);
      remote.deleteFromCollection(id);
      order = remote.getOrder(id);
      assertEquals(order.getItems().size(), 1);
      for (LineItem item : order.getItems())
      {
         remote.deleteOne(item.getId());
         break;
      }
      order = remote.getOrder(id);
      assertEquals(order.getItems().size(), 0);
   }

   public void testMergeRemove() throws Exception
   {

      SessionRemote remote = (SessionRemote) getInitialContext().lookup("SessionBean/remote");
      long id = remote.creatOrder();
      Order order = remote.getOrder(id);
      Collection<LineItem> items = order.getItems();
      Iterator<LineItem> it = items.iterator();
      while (it.hasNext())
      {
         LineItem item = it.next();
         item.setOrder(null);
         break;
      }
      remote.merge(order);
      order = remote.getOrder(id);
      assertEquals(order.getItems().size(), 1);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(RelationshipUnitTestCase.class, "relationships-test.jar");
   }

}
