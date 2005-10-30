/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.tutorial.joininheritance.bean;

import javax.persistence.Entity;
import javax.persistence.GeneratorType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.GeneratorType;
import javax.persistence.InheritanceType;
import javax.persistence.Entity;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Pet implements java.io.Serializable
{
   private int id;
   private String name;
   private double weight;

   @Id(generate = GeneratorType.AUTO)
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public double getWeight()
   {
      return weight;
   }

   public void setWeight(double weight)
   {
      this.weight = weight;
   }
}
