//$Id$
package org.jboss.tutorial.composite.bean;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Transient;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import java.util.Set;

/**
 *
 */
@Entity
public class Customer implements java.io.Serializable
{
   CustomerPK pk;
   Set<Flight> flights;

   public Customer()
   {
   }

   @EmbeddedId({
   @AttributeOverride(name = "id"),
   @AttributeOverride(name = "name")
   })
   public CustomerPK getPk()
   {
      return pk;
   }

   public void setPk(CustomerPK pk)
   {
      this.pk = pk;
   }

   @Transient
   public String getName()
   {
      return pk.getName();
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

