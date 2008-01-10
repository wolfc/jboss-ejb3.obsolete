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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue; import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue; import javax.persistence.GenerationType;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Entity
public class Customer implements Serializable
{
   private long id;
   private CustomerRecord customerRecord;
   private Set<Order> orders;

   @Id @GeneratedValue(strategy=GenerationType.AUTO)
   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   @OneToOne(cascade={CascadeType.ALL})
   @JoinColumn(name="CUSTREC_ID")
   public CustomerRecord getCustomerRecord()
   {
      return customerRecord;
   }

   public void setCustomerRecord(CustomerRecord customerRecord)
   {
      this.customerRecord = customerRecord;
   }

   @OneToMany(mappedBy="customer", fetch=FetchType.EAGER)
   public Set<Order> getOrders()
   {
      return orders;
   }

   public void setOrders(Set<Order> orders)
   {
      this.orders = orders;
   }


}
