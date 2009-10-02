/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.jpa.integration;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class JPA1EntityManagerDelegator
{
   private static final long serialVersionUID = 1L;

   public void clear()
   {
      getEntityManager().clear();
   }
   
   public void close()
   {
      getEntityManager().close();
   }
   
   public boolean contains(Object entity)
   {
      return getEntityManager().contains(entity);
   }
   
   public Query createNamedQuery(String name)
   {
      return getEntityManager().createNamedQuery(name);
   }
   
   @SuppressWarnings("unchecked")
   public Query createNativeQuery(String sqlString, Class resultClass)
   {
      return getEntityManager().createNativeQuery(sqlString, resultClass);
   }
   
   public Query createNativeQuery(String sqlString, String resultSetMapping)
   {
      return getEntityManager().createNativeQuery(sqlString, resultSetMapping);
   }
   
   public Query createNativeQuery(String sqlString)
   {
      return getEntityManager().createNativeQuery(sqlString);
   }
   
   public Query createQuery(String ejbqlString)
   {
      return getEntityManager().createQuery(ejbqlString);
   }
   
   public <T> T find(Class<T> entityClass, Object primaryKey)
   {
      return getEntityManager().find(entityClass, primaryKey);
   }
   
   public void flush()
   {
      getEntityManager().flush();
   }
   
   public Object getDelegate()
   {
      return getEntityManager().getDelegate();
   }

   protected abstract EntityManager getEntityManager();
   
   public FlushModeType getFlushMode()
   {
      return getEntityManager().getFlushMode();
   }
   
   public <T> T getReference(Class<T> entityClass, Object primaryKey)
   {
      return getEntityManager().getReference(entityClass, primaryKey);
   }
   
   public EntityTransaction getTransaction()
   {
      return getEntityManager().getTransaction();
   }
   
   public boolean isOpen()
   {
      return getEntityManager().isOpen();
   }
   
   public void joinTransaction()
   {
      getEntityManager().joinTransaction();
   }
   
   public void lock(Object entity, LockModeType lockMode)
   {
      getEntityManager().lock(entity, lockMode);
   }
   
   public <T> T merge(T entity)
   {
      return getEntityManager().merge(entity);
   }
   
   public void persist(Object entity)
   {
      getEntityManager().persist(entity);
   }
   
   public void refresh(Object entity)
   {
      getEntityManager().refresh(entity);
   }
   
   public void remove(Object entity)
   {
      getEntityManager().remove(entity);
   }
   
   public void setFlushMode(FlushModeType flushMode)
   {
      getEntityManager().setFlushMode(flushMode);
   }
}
