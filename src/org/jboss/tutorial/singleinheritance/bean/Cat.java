/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.singleinheritance.bean;

import javax.ejb.DiscriminatorType;
import javax.ejb.Entity;
import javax.ejb.Inheritance;
import javax.ejb.InheritanceType;

@Entity
        @Inheritance(strategy = InheritanceType.SINGLE_TABLE, discriminatorType = DiscriminatorType.STRING, discriminatorValue = "CAT")
        public class Cat extends Pet
{
   int lives;

   public int getLives()
   {
      return lives;
   }

   public void setLives(int lives)
   {
      this.lives = lives;
   }
}
