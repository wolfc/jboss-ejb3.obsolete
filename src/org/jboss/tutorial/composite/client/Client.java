/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.composite.client;

import org.jboss.tutorial.composite.bean.Customer;
import org.jboss.tutorial.composite.bean.EntityTest;
import org.jboss.tutorial.composite.bean.Flight;

import javax.naming.InitialContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      EntityTest test = (EntityTest) ctx.lookup(EntityTest.class.getName());
      test.manyToManyCreate();

      Flight one = test.findFlightById(new Long(1));

      Flight two = test.findFlightById(new Long(2));

      System.out.println("Air France customers");
      for (Customer c : one.getCustomers())
      {
         System.out.println(c.getName());

      }
      System.out.println("USAir customers");

      for (Customer c : two.getCustomers())
      {
         System.out.println(c.getName());
      }

   }
}
