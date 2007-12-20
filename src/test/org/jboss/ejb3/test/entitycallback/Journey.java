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
package org.jboss.ejb3.test.entitycallback;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "JOURNEY_TYPE", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(JourneyCallbackListener.class)
public class Journey
{
   Long id;
   private String start;
   private String dest;

   private Customer customer;

   public Journey()
   {

   }

   public Journey(String start, String dest)
   {
      this.start = start;
      this.dest = dest;
   }

   @Id @GeneratedValue(strategy=GenerationType.AUTO)
   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   public String getStart()
   {
      return start;
   }

   public void setStart(String start)
   {
      this.start = start;
   }

   public String getDest()
   {
      return dest;
   }

   public void setDest(String dest)
   {
      this.dest = dest;
   }

   public void setCustomer(Customer customer)
   {
      this.customer = customer;
   }

   @ManyToOne
   @JoinColumn(name = "CUSTOMER_ID")
   public Customer getCustomer()
   {
      return customer;
   }
}
