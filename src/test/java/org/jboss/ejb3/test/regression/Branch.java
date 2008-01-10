/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ejb3.test.regression;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Collection;

@Entity(name = "Branch")
@Inheritance(strategy = InheritanceType.JOINED)
public class Branch extends Contact
{

   private String city;
   private String country;
   private Collection<Employee> employees;

   public Branch()
   {
      this.employees = new ArrayList<Employee>();
   }

   public String getCity()
   {
      return this.city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   public String getCountry()
   {
      return this.country;
   }

   public void setCountry(String country)
   {
      this.country = country;
   }

   public void addEmployee(Employee employee)
   {
      if (employees == null) employees = new ArrayList<Employee>();
      employees.add(employee);
      employee.setBranch(this);
   }

   @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy="branch")
   public Collection<Employee> getEmployees()
   {
      return this.employees;
   }

   public void setEmployees(Collection<Employee> employees)
   {
      this.employees = employees;
   }

}
