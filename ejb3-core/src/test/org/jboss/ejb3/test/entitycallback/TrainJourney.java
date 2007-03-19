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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("TRAIN")
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
public class TrainJourney extends Journey
{
   private String train;

   public TrainJourney()
   {

   }

   public TrainJourney(String start, String dest, String train)
   {
      super(start, dest);
      this.train = train;
   }

   public String getTrain()
   {
      return train;
   }

   public void setTrain(String train)
   {
      this.train = train;
   }

   @PrePersist
   public void doPreCreate()
   {
      System.out.println("TrainJourney doPreCreate");
      CallbackCounterBean.addCallback("TrainJourney", PrePersist.class);
   }

   @PostPersist
   public void doPostCreate()
   {
      System.out.println("TrainJourney doPostCreate");
      CallbackCounterBean.addCallback("TrainJourney", PostPersist.class);
   }

   @PreRemove
   public void doPreRemove()
   {
      System.out.println("TrainJourney doPreRemove");
      CallbackCounterBean.addCallback("TrainJourney", PreRemove.class);
   }

   @PostRemove
   public void doPostRemove()
   {
      System.out.println("TrainJourney doPostRemove");
      CallbackCounterBean.addCallback("TrainJourney", PostRemove.class);
   }

   @PreUpdate
   public void doPreUpdate()
   {
      System.out.println("TrainJourney doPreUpdate");
      CallbackCounterBean.addCallback("TrainJourney", PreUpdate.class);
   }

   @PostUpdate
   public void doPostUpdate()
   {
      System.out.println("TrainJourney doPostUpdate");
      CallbackCounterBean.addCallback("TrainJourney", PostUpdate.class);
   }

   @PostLoad
   public void doPostLoad()
   {
      System.out.println("TrainJourney doPostLoad");
      CallbackCounterBean.addCallback("TrainJourney", PostLoad.class);
   }

}
