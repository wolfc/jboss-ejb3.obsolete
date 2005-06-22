//$Id$
package org.jboss.tutorial.composite.bean;

import javax.persistence.JoinTable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.persistence.Version;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Entity;

import java.util.Set;

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

   @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
   @JoinTable(table = @Table(name = "flight_customer_table"),
                     joinColumns = {@JoinColumn(name = "FLIGHT_ID")},
                     inverseJoinColumns = {@JoinColumn(name = "CUSTOMER_ID"), @JoinColumn(name = "CUSTOMER_NAME")})
   public Set<Customer> getCustomers()
   {
      return customers;
   }

   public void setCustomers(Set<Customer> customers)
   {
      this.customers = customers;
   }
}
