//$Id$
package org.jboss.tutorial.relationships.bean;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratorType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import javax.persistence.GeneratorType;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Company customer
 *
 * @author Emmanuel Bernard
 */
@Entity
public class Customer implements java.io.Serializable
{
   Long id;
   String name;
   Set<Flight> flights;
   Address address;

   public Customer()
   {
   }

   @Id(generate = GeneratorType.IDENTITY)
   public Long getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public void setId(Long long1)
   {
      id = long1;
   }

   public void setName(String string)
   {
      name = string;
   }

   @OneToOne(cascade = {CascadeType.ALL})
   @JoinColumn(name = "ADDRESS_ID")
   public Address getAddress()
   {
      return address;
   }

   public void setAddress(Address address)
   {
      this.address = address;
   }

   @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy="customers")
   public Set<Flight> getFlights()
   {
      return flights;
   }

   public void setFlights(Set<Flight> flights)
   {
      this.flights = flights;
   }


}

