//$Id$
package org.jboss.tutorial.relationships.bean;

import javax.ejb.AssociationTable;
import javax.ejb.Basic;
import javax.ejb.CascadeType;
import javax.ejb.Column;
import javax.ejb.Entity;
import javax.ejb.FetchType;
import javax.ejb.Id;
import javax.ejb.JoinColumn;
import javax.ejb.ManyToMany;
import javax.ejb.Table;
import javax.ejb.Transient;
import javax.ejb.Version;

import java.util.Set;

/**
 * Flight
 *
 * @author Emmanuel Bernard
 */
@Entity()
public class Flight implements java.io.Serializable
{
   Long id;
   String name;
   long duration;
   long durationInSec;
   Integer version;
   Set<Customer> customers;

   @Id
   public Long getId()
   {
      return id;
   }

   public void setId(Long long1)
   {
      id = long1;
   }

   @Column(updatable = false, name = "flight_name", nullable = false, length = 50)
   public String getName()
   {
      return name;
   }

   public void setName(String string)
   {
      name = string;
   }

   @Basic(fetch = FetchType.LAZY)
   public long getDuration()
   {
      return duration;
   }

   public void setDuration(long l)
   {
      duration = l;
      //durationInSec = duration / 1000;
   }

   @Transient
   public long getDurationInSec()
   {
      return durationInSec;
   }

   public void setDurationInSec(long l)
   {
      durationInSec = l;
   }

   @Version
   @Column(name = "OPTLOCK")
   public Integer getVersion()
   {
      return version;
   }

   public void setVersion(Integer i)
   {
      version = i;
   }

   @ManyToMany(cascade = {CascadeType.CREATE, CascadeType.MERGE}, fetch = FetchType.EAGER)
   @AssociationTable(table = @Table(name = "flight_customer_table"),
   joinColumns = {@JoinColumn(name = "FLIGHT_ID")},
   inverseJoinColumns = {@JoinColumn(name = "CUSTOMER_ID")})
   public Set<Customer> getCustomers()
   {
      return customers;
   }

   public void setCustomers(Set<Customer> customers)
   {
      this.customers = customers;
   }
}
