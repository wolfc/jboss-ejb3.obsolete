/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.callback.bean;

import javax.persistence.EntityManager;
import javax.ejb.Inject;
import javax.ejb.Stateless;
import javax.ejb.PostConstruct;
import javax.ejb.PreDestroy;
import javax.persistence.EntityManager;

import java.util.List;
import java.util.Iterator;

@Stateless
public class CustomerDAOBean implements CustomerDAO
{
   @Inject
   private EntityManager manager;

   public int create(String first, String last, String street, String city, String state, String zip)
   {
      System.out.println("-- CustomerDAOBean.create()");
      Customer customer = new Customer(first, last, street, city, state, zip);
      manager.create(customer);
      return customer.getId();
   }

   public Customer find(int id)
   {
      System.out.println("-- CustomerDAOBean.find()");
      return manager.find(Customer.class, id);
   }

   public List findByLastName(String last)
   {
      System.out.println("-- CustomerDAOBean.findByLastName(id)");
      return manager.createQuery("from Customer c where c.last = :last").setParameter("last", last).listResults();
   }

   public void merge(Customer c)
   {
      System.out.println("-- CustomerDAOBean.merge()");
      manager.merge(c);
   }

   public void delete(List l)
   {
      System.out.println("-- CustomerDAOBean.delete()");
      for (Iterator it = l.iterator() ; it.hasNext() ; )
      {
         Customer c = (Customer)it.next();
         manager.remove(c);
      }
   }

   // Callbacks ----------------------------------------------------------------
   @PostConstruct
   public void postConstructCallback()
   {
      System.out.println("PostConstruct - Have EntityManager: " + (manager != null));
   }

   @PreDestroy
   public void preDestroyCallback()
   {
      System.out.println("PreDestory - Have EntityManager: " + (manager != null));
   }
}
