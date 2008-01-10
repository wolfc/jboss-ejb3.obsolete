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

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class FieldCustomer implements java.io.Serializable
{
   @EmbeddedId
   FieldCustomerPK pk;

   @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy="customer")
   Set<FieldTicket> tickets;

   @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy="customers")
   Set<FieldFlight> flights;

   public FieldCustomer()
   {
   }

   public FieldCustomerPK getPk()
   {
      return pk;
   }

   public void setPk(FieldCustomerPK pk)
   {
      this.pk = pk;
   }

   public String getName()
   {
      return pk.name;
   }

   public Set<FieldTicket> getTickets()
   {
      return tickets;
   }

   public void setTickets(Set<FieldTicket> tickets)
   {
      this.tickets = tickets;
   }

   public Set<FieldFlight> getFlights()
   {
      return flights;
   }

   public void setFlights(Set<FieldFlight> flights)
   {
      this.flights = flights;
   }


}

