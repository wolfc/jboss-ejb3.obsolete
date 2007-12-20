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

import javax.persistence.PrePersist;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;
import javax.persistence.PostUpdate;
import javax.persistence.PostLoad;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class JourneyCallbackListener
{
   @PrePersist
   public void doPreCreate(Object bean)
   {
      System.out.println("Journey doPreCreate");
      CallbackCounterBean.addCallback("Journey", PrePersist.class);
   }

   @PostPersist
   public void doPostCreate(Object bean)
   {
      System.out.println("Journey doPostCreate");
      CallbackCounterBean.addCallback("Journey", PostPersist.class);
   }

   @PreRemove
   public void doPreRemove(Object bean)
   {
      System.out.println("Journey doPreRemove");
      CallbackCounterBean.addCallback("Journey", PreRemove.class);
   }

   @PostRemove
   public void doPostRemove(Object bean)
   {
      System.out.println("Journey doPostRemove");
      CallbackCounterBean.addCallback("Journey", PostRemove.class);
   }

   @PreUpdate
   public void doPreUpdate(Object bean)
   {
      System.out.println("Journey doPreUpdate");
      CallbackCounterBean.addCallback("Journey", PreUpdate.class);
   }

   @PostUpdate
   public void doPostUpdate(Object bean)
   {
      System.out.println("Journey doPostUpdate");
      CallbackCounterBean.addCallback("Journey", PostUpdate.class);
   }

   @PostLoad
   public void doPostLoad(Object bean)
   {
      System.out.println("Journey doPostLoad");
      CallbackCounterBean.addCallback("Journey", PostLoad.class);
   }
   
}
