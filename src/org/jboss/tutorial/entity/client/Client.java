/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.entity.client;


import org.jboss.tutorial.entity.bean.LineItem;
import org.jboss.tutorial.entity.bean.Order;
import org.jboss.tutorial.entity.bean.ShoppingCart;

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
      ShoppingCart cart = (ShoppingCart) ctx.lookup(ShoppingCart.class.getName());

      System.out.println("Buying 2 memory sticks");
      cart.buy("Memory stick", 2, 500.00);
      System.out.println("Buying a laptop");
      cart.buy("Laptop", 1, 2000.00);

      System.out.println("Print cart:");
      Order order = cart.getOrder();
      System.out.println("Total: $" + order.getTotal());
      for (LineItem item : order.getLineItems())
      {
         System.out.println(item.getQuantity() + "     " + item.getProduct() + "     " + item.getSubtotal());
      }

      System.out.println("Checkout");
      cart.checkout();

   }
}
