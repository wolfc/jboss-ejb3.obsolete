/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.joininheritance.bean;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Entity;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Dog extends Pet
{
   private int numBones;

   public int getNumBones()
   {
      return numBones;
   }

   public void setNumBones(int numBones)
   {
      this.numBones = numBones;
   }
}
