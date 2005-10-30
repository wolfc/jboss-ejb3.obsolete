/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.tutorial.entity.bean;

import javax.persistence.Entity;
import javax.persistence.GeneratorType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Entity;

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
