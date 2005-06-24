package org.jboss.tutorial.extended.bean;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Stateless
@Remote(StatelessRemote.class)
@Local(StatelessLocal.class)
public class StatelessSessionBean implements StatelessLocal, StatelessRemote
{
   @PersistenceContext EntityManager em;

   public void update(Customer c)
   {
      c.setName("Bill Jr.");
   }

   public Customer find(long id)
   {
      System.out.println("*** Stateless find");
      return em.find(Customer.class, id);
   }
}
