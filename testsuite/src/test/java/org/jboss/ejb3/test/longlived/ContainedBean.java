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
package org.jboss.ejb3.test.longlived;

import java.io.Serializable;

import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.ejb3.annotation.CacheConfig;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateful
@CacheConfig(maxSize = 1000, idleTimeoutSeconds = 2)
public class ContainedBean implements Contained, Serializable
{
   @PersistenceContext(type= PersistenceContextType.EXTENDED) EntityManager em;

   Customer customer;

   public Customer find(long id)
   {
      return em.find(Customer.class, id);
   }

   public void setCustomer(long id)
   {
      customer = find(id);
   }

   public Customer getCustomer()
   {
      return customer;
   }

   public void updateCustomer()
   {
      customer.setName("contained modified");
   }

   public boolean isActivated()
   {
      return activated;
   }

   public static boolean destroyed = false;
   public static boolean passivated = false;
   public static boolean activate = false;
   private boolean activated = false;

   @PrePassivate
   public void passivate()
   {
      passivated = true;
   }

   @PostActivate
   public void activate()
   {
      System.out.println("*********** ACTIVATED *****************");
      if (activated) throw new RuntimeException("ACTIVATED TWICE");
      activated = true;
      if (activate) throw new RuntimeException("ACTIVATED TWIC IN TWO DIFFERENT INSTANCES");
      activate = true;

   }

   @PreDestroy
   public void destroy()
   {
      destroyed = true;
   }
   
   @Remove
   public void remove()
   {  
   }
}
