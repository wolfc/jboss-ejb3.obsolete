/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.singleinheritance.bean;

import javax.ejb.DiscriminatorColumn;
import javax.ejb.DiscriminatorType;
import javax.ejb.Entity;
import javax.ejb.Id;
import javax.ejb.Inheritance;
import javax.ejb.InheritanceType;

@Entity
        @Inheritance(strategy = InheritanceType.SINGLE_TABLE, discriminatorType = DiscriminatorType.STRING)
        @DiscriminatorColumn(name = "ANIMAL_TYPE", nullable = true)
        public class Pet implements java.io.Serializable
{
   private int id;
   private String name;
   private double weight;

   @Id
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
