/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.secondary.bean;

import javax.persistence.EntityManager;
import javax.ejb.Inject;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;

import java.util.List;

@Stateless
public class CustomerDAOBean implements CustomerDAO
{
   @Inject
   private EntityManager manager;


   public int create(String first, String last, String street, String city, String state, String zip)
   {
      Customer customer = new Customer(first, last, street, city, state, zip);
      manager.create(customer);
      return customer.getId();
   }

   public Customer find(int id)
   {
      return manager.find(Customer.class, id);
   }

   public List findByLastName(String name)
   {
      return manager.createQuery("from Customer c where c.last = :name").setParameter("name", name).listResults();
   }

   public void merge(Customer c)
   {
      manager.merge(c);
   }
}
