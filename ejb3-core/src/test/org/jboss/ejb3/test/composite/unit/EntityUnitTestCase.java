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
package org.jboss.ejb3.test.composite.unit;

import java.util.Set;
import org.jboss.ejb3.test.composite.Customer;
import org.jboss.ejb3.test.composite.EntityTest;
import org.jboss.ejb3.test.composite.FieldCustomer;
import org.jboss.ejb3.test.composite.FieldFlight;
import org.jboss.ejb3.test.composite.FieldTicket;
import org.jboss.ejb3.test.composite.Flight;
import org.jboss.ejb3.test.composite.Ticket;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class EntityUnitTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public EntityUnitTestCase(String name)
   {

      super(name);

   }

   public void testOneToMany() throws Exception
   {
      EntityTest test = (EntityTest) this.getInitialContext().lookup("EntityTestBean/remote");
      Customer c = test.oneToManyCreate("Bill");
      assertNotNull(c);
      assertNotNull(c.getTickets());
      Set<Ticket> tickets = c.getTickets();
      assertTrue(tickets.size() > 0);

      // test find
      c = test.findCustomerByPk(c.getPk());
      assertNotNull(c);
      assertNotNull(c.getTickets());
      tickets = c.getTickets();
      assertTrue(tickets.size() > 0);
      assertEquals("Bill", c.getName());

   }

   public void testManyToOne() throws Exception
   {
      EntityTest test = (EntityTest) this.getInitialContext().lookup("EntityTestBean/remote");
      Flight f = test.manyToOneCreate();
      f = test.findFlightById(f.getId());
      assertTrue(f.getName().equals("AF0101"));
   }

   public void testManyToMany() throws Exception
   {
      EntityTest test = (EntityTest) this.getInitialContext().lookup("EntityTestBean/remote");
      test.manyToManyCreate();

      Flight one = test.findFlightById(new Long(1));
      Flight two = test.findFlightById(new Long(2));

      System.out.println("Air France customers");
      for (Customer c : one.getCustomers())
      {
         System.out.println(c.getName());

      }
      System.out.println("USAir customers");

      for (Customer c : two.getCustomers())
      {
         System.out.println(c.getName());
      }

   }

   public void testFieldOneToMany() throws Exception
   {
      EntityTest test = (EntityTest) this.getInitialContext().lookup("EntityTestBean/remote");
      FieldCustomer c = test.fieldOneToManyCreate("Bill");
      assertNotNull(c);
      assertNotNull(c.getTickets());
      Set<FieldTicket> tickets = c.getTickets();
      assertTrue(tickets.size() > 0);

      // test find
      c = test.fieldFindCustomerByPk(c.getPk());
      assertNotNull(c);
      assertNotNull(c.getTickets());
      tickets = c.getTickets();
      assertTrue(tickets.size() > 0);
      assertEquals("Bill", c.getName());

   }

   public void testFieldManyToOne() throws Exception
   {
      EntityTest test = (EntityTest) this.getInitialContext().lookup("EntityTestBean/remote");
      FieldFlight f = test.fieldManyToOneCreate();
      f = test.fieldFindFlightById(f.getId());
      assertTrue(f.getName().equals("AF0101"));
   }

   public void testFieldManyToMany() throws Exception
   {
      EntityTest test = (EntityTest) this.getInitialContext().lookup("EntityTestBean/remote");
      test.fieldManyToManyCreate();

      FieldFlight one = test.fieldFindFlightById(new Long(1));
      FieldFlight two = test.fieldFindFlightById(new Long(2));

      System.out.println("Air France customers");
      for (FieldCustomer c : one.getCustomers())
      {
         System.out.println(c.getName());

      }
      System.out.println("USAir customers");

      for (FieldCustomer c : two.getCustomers())
      {
         System.out.println(c.getName());
      }

   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityUnitTestCase.class, "composite-test.jar");
   }

}
