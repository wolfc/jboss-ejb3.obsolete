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

package org.jboss.ejb3.test.jacc;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.annotation.SecurityDomain;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@javax.ejb.Stateless
@Remote (Stateless.class)
@SecurityDomain ("other")
//@AspectDomain("JACC Stateless Bean")
public class StatelessBean implements Stateless
{
   //TODO: put this in again once we update the hibernate jars (changes already made to hibernate)
   //@PersistenceContext(unitName="jacc-test")
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
      if (!em.contains(e))
      {
         e = em.merge(e);
      }
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
      if (!em.contains(e))
      {
         e = em.merge(e);
      }
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
      if (!em.contains(e))
      {
         e = em.merge(e);
      }
      em.remove(e);
   }
}
