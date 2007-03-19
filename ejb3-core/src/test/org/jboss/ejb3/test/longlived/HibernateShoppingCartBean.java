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

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import org.hibernate.Session;
import org.jboss.annotation.ejb.RemoteBinding;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Stateful
@Remote(ShoppingCart.class)
@RemoteBinding(jndiBinding = "HibernateShoppingCart")
public class HibernateShoppingCartBean implements ShoppingCart
{
   @PersistenceContext(type=PersistenceContextType.EXTENDED) Session em;

   @EJB StatelessLocal stateless;

   @EJB private Contained contained;

   private Customer customer;

   public long createCustomer()
   {
      customer = new Customer();
      customer.setName("William");
      em.save(customer);
      System.out.println("********* created *****");
      return customer.getId();
   }

   public boolean isContainedActivated()
   {
      return false;
   }

   public void update()
   {
      System.out.println("********* update() *****");
      customer.setName("Bill");
   }
   public void update2()
   {
      customer.setName("Billy");
   }

   public void update3()
   {
      stateless.update(customer);
   }

   public Customer find(long id)
   {
      return (Customer)em.get(Customer.class, new Long(id));
   }

   public void findAndUpdateStateless()
   {
      stateless.findAndUpdate(customer.getId());
      if (!customer.getName().equals("stateless modified")) throw new RuntimeException("stateless didn't get propagated pc");
   }

   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public void never()
   {
      customer.setName("Bob");
   }

   public void updateContained()
   {
      contained.updateCustomer();
   }

   public void setContainedCustomer()
   {
      contained.setCustomer(customer.getId());
   }

   public void checkContainedCustomer()
   {
      if (contained.getCustomer() != customer) throw new RuntimeException("not same customer");
   }




   @Remove
   public void checkout() {}
}
