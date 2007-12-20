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
package org.jboss.ejb3.test.composite;

import java.util.HashSet;
import java.util.Set;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
@Stateless
@Remote(EntityTest.class)
public class EntityTestBean implements EntityTest
{
   private @PersistenceContext EntityManager manager;
   private static long genid = 0;

   public Customer oneToManyCreate(String name) throws Exception
   {
      Ticket t = new Ticket();
      t.setNumber("33A");
      Customer c = new Customer();
      CustomerPK pk = new CustomerPK(genid++, name);
      c.setPk(pk);
      Set<Ticket> tickets = new HashSet<Ticket>();
      tickets.add(t);
      t.setCustomer(c);
      c.setTickets(tickets);
      manager.persist(c);
      return c;
   }

   public Customer findCustomerByPk(CustomerPK pk) throws Exception
   {
      return manager.find(Customer.class, pk);
   }

   public Flight manyToOneCreate() throws Exception
   {
      Flight firstOne = new Flight();
      firstOne.setId(new Long(1));
      firstOne.setName("AF0101");
      manager.persist(firstOne);
      return firstOne;
   }

   public void manyToManyCreate() throws Exception
   {

      Flight firstOne = findFlightById(new Long(1));
      Flight second = new Flight();
      second.setId(new Long(2));
      second.setName("US1");

      Set<Customer> customers1 = new HashSet<Customer>();
      Set<Customer> customers2 = new HashSet<Customer>();


      Customer will = new Customer();
      CustomerPK willPk = new CustomerPK(genid++, "Will");
      will.setPk(willPk);
      customers1.add(will);

      Customer monica = new Customer();
      CustomerPK moPK = new CustomerPK(genid++, "Monica");
      monica.setPk(moPK);
      customers1.add(monica);

      Customer molly = new Customer();
      CustomerPK mollyPK = new CustomerPK(genid++, "Molly");
      molly.setPk(mollyPK);
      customers2.add(molly);

      firstOne.setCustomers(customers1);
      second.setCustomers(customers2);

      manager.persist(second);
   }


   public Flight findFlightById(Long id) throws Exception
   {
      return manager.find(Flight.class, id);
   }


   public FieldCustomer fieldOneToManyCreate(String name) throws Exception
   {
      FieldTicket t = new FieldTicket();
      t.setNumber("33A");
      FieldCustomer c = new FieldCustomer();
      FieldCustomerPK pk = new FieldCustomerPK(genid++, name);
      c.setPk(pk);
      Set<FieldTicket> tickets = new HashSet<FieldTicket>();
      tickets.add(t);
      t.setCustomer(c);
      c.setTickets(tickets);
      manager.persist(c);
      return c;
   }

   public FieldCustomer fieldFindCustomerByPk(FieldCustomerPK pk) throws Exception
   {
      return manager.find(FieldCustomer.class, pk);
   }

   public FieldFlight fieldManyToOneCreate() throws Exception
   {
      FieldFlight firstOne = new FieldFlight();
      firstOne.setId(new Long(1));
      firstOne.setName("AF0101");
      manager.persist(firstOne);
      return firstOne;
   }

   public void fieldManyToManyCreate() throws Exception
   {

      FieldFlight firstOne = fieldFindFlightById(new Long(1));
      FieldFlight second = new FieldFlight();
      second.setId(new Long(2));
      second.setName("US1");

      Set<FieldCustomer> customers1 = new HashSet<FieldCustomer>();
      Set<FieldCustomer> customers2 = new HashSet<FieldCustomer>();


      FieldCustomer will = new FieldCustomer();
      FieldCustomerPK willPk = new FieldCustomerPK(genid++, "Will");
      will.setPk(willPk);
      customers1.add(will);

      FieldCustomer monica = new FieldCustomer();
      FieldCustomerPK moPK = new FieldCustomerPK(genid++, "Monica");
      monica.setPk(moPK);
      customers1.add(monica);

      FieldCustomer molly = new FieldCustomer();
      FieldCustomerPK mollyPK = new FieldCustomerPK(genid++, "Molly");
      molly.setPk(mollyPK);
      customers2.add(molly);

      firstOne.setCustomers(customers1);
      second.setCustomers(customers2);

      manager.persist(second);
   }


   public FieldFlight fieldFindFlightById(Long id) throws Exception
   {
      return manager.find(FieldFlight.class, id);
   }


}
