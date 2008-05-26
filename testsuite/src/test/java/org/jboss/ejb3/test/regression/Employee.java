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

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToOne;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity(name = "Employee")
@Inheritance(strategy = InheritanceType.JOINED)
public class Employee extends Contact
{

   private String first;
   private String last;
   private String cell;
   private Branch branch;

   public Employee()
   {
   }

   public String getFirst()
   {
      return this.first;
   }

   public void setFirst(String first)
   {
      this.first = first;
   }

   public String getLast()
   {
      return this.last;
   }

   public void setLast(String last)
   {
      this.last = last;
   }

   public String getCell()
   {
      return this.cell;
   }

   public void setCell(String cell)
   {
      this.cell = cell;
   }

   @ManyToOne
   @JoinColumn(name = "branch")
   public Branch getBranch()
   {
      return this.branch;
   }

   public void setBranch(Branch branch)
   {
      this.branch = branch;
   }
}
