/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.entity.bean;

import javax.ejb.Entity;
import javax.persistence.GeneratorType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class LineItem implements java.io.Serializable
{
   private int id;
   private double subtotal;
   private int quantity;
   private String product;
   private Order order;


   @Id(generate = GeneratorType.AUTO)
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public double getSubtotal()
   {
      return subtotal;
   }

   public void setSubtotal(double subtotal)
   {
      this.subtotal = subtotal;
   }

   public int getQuantity()
   {
      return quantity;
   }

   public void setQuantity(int quantity)
   {
      this.quantity = quantity;
   }

   public String getProduct()
   {
      return product;
   }

   public void setProduct(String product)
   {
      this.product = product;
   }

   @ManyToOne
   @JoinColumn(name = "order_id")
   public Order getOrder()
   {
      return order;
   }

   public void setOrder(Order order)
   {
      this.order = order;
   }
}
