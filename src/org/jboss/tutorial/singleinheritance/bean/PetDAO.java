/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.singleinheritance.bean;

import javax.ejb.Remote;

import java.util.List;

@Remote
        public interface PetDAO
{
   void createDog(String name, double weight, int bones);

   void createCat(String name, double weight, int lives);

   List findByWeight(double weight);
}
