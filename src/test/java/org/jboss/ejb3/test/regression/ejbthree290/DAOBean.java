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
package org.jboss.ejb3.test.regression.ejbthree290;

import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.ejb.Remote;
import javax.ejb.EJB;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(DAO.class)
public class DAOBean implements DAO
{
   @PersistenceContext EntityManager manager;
   @EJB DeleteLocal local;

   public MyEntity create()
   {
      MyEntity e = new MyEntity();
      e.name = "Bill";
      manager.persist(e);
      return e;
   }

   public void findAndDelete(int id) throws Exception
   {
      MyEntity e = manager.find(MyEntity.class, id);
      local.removeEntity(e.id);
      try
      {
         manager.refresh(e);
      }
      catch (EntityNotFoundException e1)
      {
         return; // correct
      }
      throw new RuntimeException("Expected EntityNotFoundException");
   }

   public void merge(MyEntity e) throws Exception
   {
      //local.removeEntity(e.id);
	  //the spec says IllegalArgumentException if the entity is removed (ie a scheduled for remove as per the spec)
	  MyEntity managedEntity = manager.find(MyEntity.class, e.id);
	  managedEntity.name="Joe";
	  manager.remove(managedEntity);
      try
      {
         manager.merge(managedEntity);
      }
      catch (IllegalArgumentException e1)
      {
         return;
      }
      throw new RuntimeException("expected IllegalArgumentException");
   }

   public void mergeAfterRemove(MyEntity e) throws Exception
   {
      e = manager.merge(e);
      manager.remove(e);
      try
      {
         manager.merge(e);
      }
      catch (IllegalArgumentException e1)
      {
         return;
      }
      throw new RuntimeException("expected IllegalArgumentException");
   }

}
