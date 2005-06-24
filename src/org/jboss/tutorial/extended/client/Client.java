/*
 *
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tutorial.extended.client;

import javax.naming.InitialContext;
import org.jboss.tutorial.extended.bean.Customer;
import org.jboss.tutorial.extended.bean.ShoppingCart;
import org.jboss.tutorial.extended.bean.StatelessRemote;

//import java.rmi.*;


/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class Client
{
   public static InitialContext getInitialContext()  throws Exception
   {
      return new InitialContext();
   }

   public static void testLongLivedSession() throws Exception
   {
      ShoppingCart test = (ShoppingCart) getInitialContext().lookup(ShoppingCart.class.getName());
      StatelessRemote remote = (StatelessRemote) getInitialContext().lookup(StatelessRemote.class.getName());
      Customer c;

      long id = test.createCustomer();
      c = remote.find(id);
      System.out.println("Created customer: " + c.getName());

      test.update();
      c = remote.find(id);
      System.out.println("ShoppingCartBean.customer should stay managed because we're in an extended PC: Customer.getName() == " + c.getName());

      test.update3();
      c = remote.find(id);
      System.out.println("Extended persistence contexts are propagated to nested EJB calls: Customer.getName() == " + c.getName());
      test.checkout();
   }

   public static void testWithFlushMode() throws Exception
   {
      ShoppingCart cart = (ShoppingCart) getInitialContext().lookup(ShoppingCart.class.getName());
      StatelessRemote dao = (StatelessRemote) getInitialContext().lookup(StatelessRemote.class.getName());
      Customer c;
      long id;


      id = cart.createCustomer();
      c = dao.find(id);
      System.out.println("Created customer: " + c.getName());

      cart.never();
      c = dao.find(id);
      System.out.println("Customer's name should still be William as pc was not yet flushed:  Customer.getName() == " + c.getName());

      cart.checkout();
      c = dao.find(id);
      System.out.println("Now that the pc has been flushed name should be 'Bob': Customer.getName() == " + c.getName());

   }

   public static void main(String[] args) throws Exception
   {
      testWithFlushMode();
      testLongLivedSession();
   }
}
