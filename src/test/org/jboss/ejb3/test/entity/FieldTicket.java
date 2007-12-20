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
package org.jboss.ejb3.test.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Flight ticket
 *
 * @author Emmanuel Bernard
 */
@Entity
public class FieldTicket implements java.io.Serializable
{
   @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
   Long id;

   String number;

   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "CUSTOMER_ID")
   FieldCustomer customer;

   public FieldTicket()
   {
   }

   public Long getId()
   {
      return id;
   }

   public String getNumber()
   {
      return number;
   }

   public void setId(Long long1)
   {
      id = long1;
   }

   public void setNumber(String string)
   {
      number = string;
   }

   public FieldCustomer getCustomer()
   {
      return customer;
   }

   public void setCustomer(FieldCustomer customer)
   {
      this.customer = customer;
   }

}

