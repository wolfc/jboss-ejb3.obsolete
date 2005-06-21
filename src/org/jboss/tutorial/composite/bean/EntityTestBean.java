/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.composite.bean;

import java.util.HashSet;
import java.util.Set;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(EntityTest.class)
public class EntityTestBean implements EntityTest
{
   private @PersistenceContext EntityManager manager;
   private static long genid = 0;

   public void manyToManyCreate() throws Exception
   {

      Flight firstOne = new Flight();
      firstOne.setId(new Long(1));
      firstOne.setName("AF0101");
      manager.persist(firstOne);
      Flight second = new Flight();
      second.setId(new Long(2));
      second.setName("US1");

      Set<Customer> customers1 = new HashSet<Customer>();
      Set<Customer> customers2 = new HashSet<Customer>();


      Customer bill = new Customer();
      CustomerPK pk = new CustomerPK(genid++, "Bill");
      bill.setPk(pk);
      customers1.add(bill);

      Customer monica = new Customer();
      CustomerPK moPK = new CustomerPK(genid++, "Monica");
      monica.setPk(moPK);
      customers1.add(monica);

      Customer molly = new Customer();
      CustomerPK mollyPK = new CustomerPK(genid++, "Molly");
      molly.setPk(mollyPK);
      customers2.add(molly);

      firstOne.setCustomers(customers1);
      second.setCustomers(customers2);

      manager.persist(second);
   }


   public Flight findFlightById(Long id) throws Exception
   {
      return manager.find(Flight.class, id);
   }

}
