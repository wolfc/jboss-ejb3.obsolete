package org.jboss.tutorial.entity.security.bean;

import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.annotation.ejb.AspectDomain;
import org.jboss.tutorial.entity.security.bean.AllEntity;
import org.jboss.tutorial.entity.security.bean.SomeEntity;
import org.jboss.tutorial.entity.security.bean.StarEntity;
import org.jboss.tutorial.entity.security.bean.Stateless;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@javax.ejb.Stateless
@Remote (Stateless.class)
@SecurityDomain ("other")
@AspectDomain("JACC Stateless Bean")
public class StatelessBean implements Stateless
{
   @PersistenceContext
   EntityManager em;

   @PermitAll
   public int unchecked(int i)
   {
      System.out.println("stateless unchecked");
      return i;
   }

   @RolesAllowed ("allowed")
   public int checked(int i)
   {
      System.out.println("stateless checked");
      return i;
   }

   @PermitAll
   public AllEntity insertAllEntity()
   {
      AllEntity e = new AllEntity();
      e.val = "x";
      em.persist(e);
      return e;
   }

   @PermitAll
   public AllEntity readAllEntity(int key)
   {
      AllEntity e = em.find(AllEntity.class, key);
      return e;
   }

   @PermitAll
   public void updateAllEntity(AllEntity e)
   {
      em.merge(e);
   }

   @PermitAll
   public void deleteAllEntity(AllEntity e)
   {
      em.remove(e);
   }

   @PermitAll
   public StarEntity insertStarEntity()
   {
      StarEntity e = new StarEntity();
      e.val = "x";
      em.persist(e);
      return e;
   }

   @PermitAll
   public StarEntity readStarEntity(int key)
   {
      StarEntity e = em.find(StarEntity.class, key);
      return e;
   }

   @PermitAll
   public void updateStarEntity(StarEntity e)
   {
      em.merge(e);
   }

   @PermitAll
   public void deleteStarEntity(StarEntity e)
   {
      em.remove(e);
   }


   @PermitAll
   public SomeEntity insertSomeEntity()
   {
      SomeEntity e = new SomeEntity();
      e.val = "x";
      em.persist(e);
      return e;
   }

   @PermitAll
   public SomeEntity readSomeEntity(int key)
   {
      SomeEntity e = em.find(SomeEntity.class, key);
      return e;
   }

   @PermitAll
   public void updateSomeEntity(SomeEntity e)
   {
      em.merge(e);
   }

   @PermitAll
   public void deleteSomeEntity(SomeEntity e)
   {
      em.remove(e);
   }
}
