/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.entity.bean;

import javax.ejb.CascadeType;
import javax.ejb.Entity;
import javax.ejb.FetchType;
import javax.ejb.GeneratorType;
import javax.ejb.Id;
import javax.ejb.JoinColumn;
import javax.ejb.OneToMany;
import javax.ejb.Table;

import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "PURCHASE_ORDER")
public class Order implements java.io.Serializable
{
   private int id;
   private double total;
   private Collection<LineItem> lineItems;

   @Id(generate = GeneratorType.AUTO)
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public double getTotal()
   {
      return total;
   }

   public void setTotal(double total)
   {
      this.total = total;
   }

   public void addPurchase(String product, int quantity, double price)
   {
      if (lineItems == null) lineItems = new ArrayList<LineItem>();
      LineItem item = new LineItem();
      item.setOrder(this);
      item.setProduct(product);
      item.setQuantity(quantity);
      item.setSubtotal(quantity * price);
      lineItems.add(item);
      total += quantity * price;
   }

   @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
   @JoinColumn(name = "order_id")
   public Collection<LineItem> getLineItems()
   {
      return lineItems;
   }

   public void setLineItems(Collection<LineItem> lineItems)
   {
      this.lineItems = lineItems;
   }
}
