/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.callback.bean;

import javax.ejb.AttributeOverride;
import javax.ejb.Column;
import javax.ejb.Embedded;
import javax.ejb.Entity;
import javax.ejb.GeneratorType;
import javax.ejb.Id;
import javax.ejb.Table;
import javax.ejb.CallbackListener;

@Entity
@Table(name = "CUSTOMER")
@CallbackListener("org.jboss.tutorial.callback.bean.CustomerCallbackListener")      
public class Customer implements java.io.Serializable
{
   private int id;
   private String first;
   private String last;
   private String street;
   private String city;
   private String state;
   private String zip;

   public Customer()
   {
   }

   public Customer(String first, String last, String street, String city, String state, String zip)
   {
      this.first = first;
      this.last = last;
      this.street = street;
      this.city = city;
      this.state = state;
      this.zip = zip;
   }

   @Id(generate = GeneratorType.AUTO)
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   @Column(name = "FIRST")
   public String getFirst()
   {
      return first;
   }

   public void setFirst(String first)
   {
      this.first = first;
   }

   @Column(name = "LAST")
   public String getLast()
   {
      return last;
   }

   public void setLast(String last)
   {
      this.last = last;
   }

   @Column(name = "STREET")
   public String getStreet()
   {
      return street;
   }

   public void setStreet(String street)
   {
      this.street = street;
   }

   @Column(name = "CITY")
   public String getCity()
   {
      return city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   @Column(name = "STATE")
   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   @Column(name = "ZIP")
   public String getZip()
   {
      return zip;
   }

   public void setZip(String zip)
   {
      this.zip = zip;
   }
}
