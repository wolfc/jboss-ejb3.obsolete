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

import javax.persistence.Entity;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue; import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Entity
public class Customer
{
   Long id;
   String name;
   Set<Journey> journeys = new HashSet<Journey>();

   public Customer()
   {

   }

   public Customer(String name)
   {
      this.name = name;
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

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, mappedBy="customer")
   public Set<Journey> getJourneys()
   {
      return journeys;
   }

   public void setJourneys(Set<Journey> journeys)
   {
      this.journeys = journeys;
   }

   public void addJourney(Journey journey)
   {
      journeys.add(journey);
      journey.setCustomer(this);
   }

   @PrePersist
   public void doPreCreate()
   {
      System.out.println("Customer doPreCreate");
      CallbackCounterBean.addCallback("Customer", PrePersist.class);
   }

   @PostPersist
   public void doPostCreate()
   {
      System.out.println("Customer doPostCreate");
      CallbackCounterBean.addCallback("Customer", PostPersist.class);
   }

   @PreRemove
   public void doPreRemove()
   {
      System.out.println("Customer doPreRemove");
      CallbackCounterBean.addCallback("Customer", PreRemove.class);
   }

   @PostRemove
   public void doPostRemove()
   {
      System.out.println("Customer doPostRemove");
      CallbackCounterBean.addCallback("Customer", PostRemove.class);
   }

   @PreUpdate
   public void doPreUpdate()
   {
      System.out.println("Customer doPreUpdate");
      CallbackCounterBean.addCallback("Customer", PreUpdate.class);
   }

   @PostUpdate
   public void doPostUpdate()
   {
      System.out.println("Customer doPostUpdate");
      CallbackCounterBean.addCallback("Customer", PostUpdate.class);
   }

   @PostLoad
   public void doPostLoad()
   {
      System.out.println("Customer doPostLoad");
      CallbackCounterBean.addCallback("Customer", PostLoad.class);
   }
}
