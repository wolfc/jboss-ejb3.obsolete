//$Id$
package org.jboss.tutorial.composite.bean;

import javax.ejb.AttributeOverride;
import javax.ejb.CascadeType;
import javax.ejb.EmbeddedId;
import javax.ejb.Entity;
import javax.ejb.FetchType;
import javax.ejb.ManyToMany;
import javax.ejb.Transient;
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

   @ManyToMany(cascade = {CascadeType.CREATE, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy="customers")
   public Set<Flight> getFlights()
   {
      return flights;
   }

   public void setFlights(Set<Flight> flights)
   {
      this.flights = flights;
   }

}

