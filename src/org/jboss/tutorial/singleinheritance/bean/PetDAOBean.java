/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.singleinheritance.bean;

import javax.persistence.EntityManager;
import javax.ejb.Inject;
import javax.ejb.Stateless;

import java.util.List;

@Stateless
public class PetDAOBean implements PetDAO
{
   @Inject
   private EntityManager manager;

   public void createDog(String name, double weight, int bones)
   {
      Dog dog = new Dog();
      dog.setName(name);
      dog.setWeight(weight);
      dog.setNumBones(bones);
      manager.create(dog);
   }

   public void createCat(String name, double weight, int lives)
   {
      Dog dog = new Dog();
      dog.setName(name);
      dog.setWeight(weight);
      dog.setNumBones(lives);
      manager.create(dog);
   }

   public List findByWeight(double weight)
   {
      return manager.createQuery("from Pet p where p.weight < :weight").setParameter("weight", weight).listResults();
   }
}
