//$Id$
package org.jboss.tutorial.relationships.bean;

import javax.ejb.CascadeType;
import javax.ejb.Entity;
import javax.ejb.FetchType;
import javax.ejb.GeneratorType;
import javax.ejb.Id;
import javax.ejb.JoinColumn;
import javax.ejb.ManyToMany;
import javax.ejb.OneToOne;
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

   @ManyToMany(cascade = {CascadeType.CREATE, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy="custoemrs")
   public Set<Flight> getFlights()
   {
      return flights;
   }

   public void setFlights(Set<Flight> flights)
   {
      this.flights = flights;
   }


}

