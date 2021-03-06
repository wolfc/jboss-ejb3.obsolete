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
package org.jboss.ejb3.test.manytomany;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue; import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToMany;
import java.util.Set;


/**
 * Corporate like Air France
 *
 * @author Emmanuel Bernard
 */
@Entity()
public class Company implements java.io.Serializable
{
   private Integer id;
   private String name;
   private Set<Flight> flights;

   @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
   public Integer getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }


   public void setId(Integer integer)
   {
      id = integer;
   }


   public void setName(String string)
   {
      name = string;
   }


   @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy="company")
   public Set<Flight> getFlights()
   {
      return flights;
   }

   public void setFlights(Set<Flight> flights)
   {
      this.flights = flights;
   }


}
