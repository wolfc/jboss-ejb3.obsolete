/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.dependent.bean;

import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@Remote(CustomerDAO.class)
public class CustomerDAOBean implements CustomerDAO
{
   @PersistenceContext
   private EntityManager manager;


   public int create(String first, String last, String street, String city, String state, String zip)
   {
      Customer customer = new Customer(first, last, street, city, state, zip);
      manager.persist(customer);
      return customer.getId();
   }

   public Customer find(int id)
   {
      return manager.find(Customer.class, id);
   }

   public List findByLastName(String name)
   {
      return manager.createQuery("from Customer c where c.name.last = :name").setParameter("name", name).getResultList();
   }

   public void merge(Customer c)
   {
      manager.merge(c);
   }
}
