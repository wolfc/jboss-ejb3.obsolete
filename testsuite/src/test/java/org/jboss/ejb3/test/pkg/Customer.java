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
package org.jboss.ejb3.test.pkg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SecondaryTable;

/**
 * Company customer
 *
 * @author Emmanuel Bernard
 */
@Entity
@SecondaryTable(name = "EMBEDDED_ADDRESS")
@NamedQuery(name="customerById", query="from Customer c where c.id = :id")
public class Customer implements java.io.Serializable
{
   Long id;
   String name;
   String street;
   String city;
   String state;
   String zip;

   public
   Customer()
   {
   }

   @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
   public
   Long getId()
   {
      return id;
   }

   public
   String getName()
   {
      return name;
   }

   public
   void setId(Long long1)
   {
      id = long1;
   }

   public
   void setName(String string)
   {
      name = string;
   }

   @Column(name = "street", table="EMBEDDED_ADDRESS")
   public String getStreet()
   {
      return street;
   }

   public void setStreet(String street)
   {
      this.street = street;
   }

   @Column(name = "city", table="EMBEDDED_ADDRESS")
   public String getCity()
   {
      return city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   @Column(name = "state", table="EMBEDDED_ADDRESS")
   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   @Column(name = "zip", table = "EMBEDDED_ADDRESS")
   public String getZip()
   {
      return zip;
   }

   public void setZip(String zip)
   {
      this.zip = zip;
   }

}

