/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.callback.bean;

import javax.ejb.PreCreate;
import javax.ejb.PostCreate;
import javax.ejb.PreRemove;
import javax.ejb.PostRemove;
import javax.ejb.PreUpdate;
import javax.ejb.PostUpdate;
import javax.ejb.PostLoad;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class CustomerCallbackListener
{
   @PreCreate
   public void doPreCreate(Customer customer)
   {
      System.out.println("doPreCreate: About to create Customer: " + customer.getFirst() + " " + customer.getLast());
   }

   @PostCreate
   public void doPostCreate(Object customer)
   {
      System.out.println("doPostCreate: Created Customer: " + ((Customer)customer).getFirst() + " " + ((Customer)customer).getLast());
   }

   @PreRemove
   public void doPreRemove(Customer customer)
   {
      System.out.println("doPreRemove: About to delete Customer: " + customer.getFirst() + " " + customer.getLast());
   }

   @PostRemove
   public void doPostRemove(Customer customer)
   {
      System.out.println("doPostRemove: Deleted Customer: " + customer.getFirst() + " " + customer.getLast());
   }

   @PreUpdate
   public void doPreUpdate(Customer customer)
   {
      System.out.println("doPreUpdate: About to update Customer: " + customer.getFirst() + " " + customer.getLast());
   }

   @PostUpdate
   public void doPostUpdate(Customer customer)
   {
      System.out.println("doPostUpdate: Updated Customer: " + customer.getFirst() + " " + customer.getLast());
   }

   @PostLoad
   public void doPostLoad(Customer customer)
   {
      System.out.println("doPostLoad: Loaded Customer: " + customer.getFirst() + " " + customer.getLast());
   }


}
