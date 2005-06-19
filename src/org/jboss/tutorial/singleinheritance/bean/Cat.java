/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.singleinheritance.bean;

import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE, discriminatorType = DiscriminatorType.STRING, discriminatorValue = "CAT")
public class Cat extends Pet
{
   public String getHairball()
   {
      return hairball;
   }

   public void setHairball(String hairball)
   {
      this.hairball = hairball;
   }

   String hairball;

}
