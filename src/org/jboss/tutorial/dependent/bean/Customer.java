/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.dependent.bean;

import javax.ejb.AttributeOverride;
import javax.ejb.Column;
import javax.ejb.Embedded;
import javax.ejb.Entity;
import javax.ejb.GeneratorType;
import javax.ejb.Id;
import javax.ejb.Table;

@Entity
@Table(name = "CUSTOMER")
public class Customer implements java.io.Serializable
{
   private int id;
   private Name name;
   private String street;
   private String city;
   private String state;
   private String zip;

   public Customer()
   {
   }

   public Customer(String first, String last, String street, String city, String state, String zip)
   {
      this.name = new Name(first, last);
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


   @Embedded({
   @AttributeOverride(name = "first", column = {@Column(name = "FIRST_NAME")}),
   @AttributeOverride(name = "last", column = {@Column(name = "LAST_NAME")})
   })
   public Name getName()
   {
      return name;
   }

   public void setName(Name name)
   {
      this.name = name;
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
