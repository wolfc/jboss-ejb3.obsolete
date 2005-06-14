/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.joininheritance.bean;

import java.util.List;
import javax.annotation.Resource;
import javax.ejb.RemoteInterface;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;

@Stateless
@RemoteInterface(PetDAO.class)
public class PetDAOBean implements PetDAO
{
   @Resource
   private EntityManager manager;

   public void createDog(String name, double weight, int bones)
   {
      Dog dog = new Dog();
      dog.setName(name);
      dog.setWeight(weight);
      dog.setNumBones(bones);
      manager.persist(dog);
   }

   public void createCat(String name, double weight, int lives)
   {
      Cat cat = new Cat();
      cat.setName(name);
      cat.setWeight(weight);
      cat.setLives(lives);
      manager.persist(cat);
   }

   public List findByWeight(double weight)
   {
      return manager.createQuery("from Pet p where p.weight < :weight").setParameter("weight", weight).getResultList();
   }
}
