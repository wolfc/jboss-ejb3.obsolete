package org.jboss.tutorial.extended.bean;

import javax.annotation.EJB;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.FlushMode;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Stateful
@Remote(ShoppingCart.class)
public class ShoppingCartBean implements ShoppingCart
{
   @PersistenceContext(type=PersistenceContextType.EXTENDED) EntityManager em;

   @EJB StatelessLocal stateless;

   private Customer customer;

   public long createCustomer()
   {
      customer = new Customer();
      customer.setName("William");
      em.persist(customer);
      return customer.getId();
   }

   public void update()
   {
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
      return em.find(Customer.class, id);
   }

   @FlushMode(FlushModeType.NEVER)
   public void never()
   {
      customer.setName("Bob");
   }

   

   @Remove
   public void checkout()
   {
      em.flush();  
   }
}
