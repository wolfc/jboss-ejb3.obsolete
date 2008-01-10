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
package org.jboss.ejb3.test.tableperinheritance;

import java.util.Iterator;
import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(EntityTest.class)
public class EntityTestBean implements EntityTest
{
   private @PersistenceContext EntityManager manager;
   private static long genid;

   private void assertTrue(boolean value)
   {
      if (!value) throw new RuntimeException("assert failed");
   }

   private void assertFalse(boolean value)
   {
      if (value) throw new RuntimeException("assert failed");
   }

   private void assertEquals(int x, int y)
   {
      if (x != y) throw new RuntimeException("assert failed");
   }

   private void assertEquals(String x, String y)
   {
      if (x == y) return;
      if (x == null && y != null) throw new RuntimeException("assert failed");
      if (x != null && y == null) throw new RuntimeException("assert failed");
      if (!x.equals(y)) throw new RuntimeException("assert failed");
   }

   public long[] createBeans() throws Exception
   {
      Employee mark = new Employee();
      mark.setId(genid++);
      mark.setName("Mark");
      mark.setTitle("internal sales");
      mark.setSex('M');
      mark.setAddress("buckhead");
      mark.setZip("30305");
      mark.setCountry("USA");

      Customer joe = new Customer();
      joe.setId(genid++);
      joe.setName("Joe");
      joe.setAddress("San Francisco");
      joe.setZip("XXXXX");
      joe.setCountry("USA");
      joe.setComments("Very demanding");
      joe.setSalesperson(mark);

      Person yomomma = new Person();
      yomomma.setId(genid++);
      yomomma.setName("mum");
      yomomma.setSex('F');

      manager.persist(mark);
      manager.persist(joe);
      manager.persist(yomomma);
      long[] ids = {mark.getId(), joe.getId(), yomomma.getId()};
      return ids;
   }

   public void test1() throws Exception
   {
      assertEquals(manager.createQuery("from java.io.Serializable").getResultList().size(), 0);

      assertEquals(manager.createQuery("from Person").getResultList().size(), 3);
   }

   public void test2() throws Exception
   {
      List customers = manager.createQuery("from Customer c left join fetch c.salesperson").getResultList();
      for (Iterator iter = customers.iterator(); iter.hasNext();)
      {
         Customer c = (Customer) iter.next();
         assertEquals(c.getSalesperson().getName(), "Mark");
      }
      assertEquals(customers.size(), 1);
   }

   public void test3() throws Exception
   {
      List customers = manager.createQuery("from Customer").getResultList();
      for (Iterator iter = customers.iterator(); iter.hasNext();)
      {
         Customer c = (Customer) iter.next();
         assertEquals(c.getSalesperson().getName(), "Mark");
      }
      assertEquals(customers.size(), 1);
   }

   public void test4(long[] ids) throws Exception
   {
      Employee mark = manager.find(Employee.class, new Long(ids[0]));
      Customer joe = (Customer) manager.find(Customer.class, new Long(ids[1]));
      Person yomomma = manager.find(Person.class, new Long(ids[2]));

      mark.setZip("30306");
      assertEquals(manager.createQuery("from Person p where p.zip = '30306'").getResultList().size(), 1);
      manager.remove(mark);
      manager.remove(joe);
      manager.remove(yomomma);
      assertTrue(manager.createQuery("from Person").getResultList().isEmpty());
   }
}
