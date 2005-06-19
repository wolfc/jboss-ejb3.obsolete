/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.entity.bean;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;


@Stateful
@Remote(ShoppingCart.class)
public class ShoppingCartBean implements ShoppingCart, java.io.Serializable
{
   @Resource
   private EntityManager manager;
   private Order order;

   public void buy(String product, int quantity, double price)
   {
      if (order == null) order = new Order();
      order.addPurchase(product, quantity, price);
   }

   public Order getOrder()
   {
      return order;
   }

   @Remove
   public void checkout()
   {
      manager.persist(order);
   }
}
