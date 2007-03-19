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
package org.jboss.ejb3.test.singletableinheritance;

import java.math.BigDecimal;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;

/**
 * @author Gavin King
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("EMPLOYEE")
public class Employee extends Person
{
   private String title;
   private BigDecimal salary;
   private Employee manager;

   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * @param title The title to set.
    */
   public void setTitle(String title)
   {
      this.title = title;
   }

   @OneToOne
   @JoinColumn(name = "manager")
   public Employee getManager()
   {
      return manager;
   }

   /**
    * @param manager The manager to set.
    */
   public void setManager(Employee manager)
   {
      this.manager = manager;
   }

   /**
    * @return Returns the salary.
    */
   public BigDecimal getSalary()
   {
      return salary;
   }

   /**
    * @param salary The salary to set.
    */
   public void setSalary(BigDecimal salary)
   {
      this.salary = salary;
   }
}
