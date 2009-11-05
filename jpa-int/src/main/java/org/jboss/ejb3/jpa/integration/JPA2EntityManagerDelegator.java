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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class JPA2EntityManagerDelegator extends JPA1EntityManagerDelegator implements EntityManager
{
   private static final long serialVersionUID = 1L;
   
   public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass)
   {
      return getEntityManager().createNamedQuery(name, resultClass);
   }

   public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery)
   {
      return getEntityManager().createQuery(criteriaQuery);
   }

   public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass)
   {
      return getEntityManager().createQuery(qlString, resultClass);
   }

   public void detach(Object entity)
   {
      getEntityManager().detach(entity);
   }

   public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties)
   {
      return getEntityManager().find(entityClass, primaryKey, properties);
   }

   public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode)
   {
      return getEntityManager().find(entityClass, primaryKey, lockMode);
   }

   public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties)
   {
      return getEntityManager().find(entityClass, primaryKey, lockMode, properties);
   }

   public CriteriaBuilder getCriteriaBuilder()
   {
      return getEntityManager().getCriteriaBuilder();
   }
   
   public EntityManagerFactory getEntityManagerFactory()
   {
      return getEntityManager().getEntityManagerFactory();
   }

   public LockModeType getLockMode(Object entity)
   {
      return getEntityManager().getLockMode(entity);
   }

   public Metamodel getMetamodel()
   {
      return getEntityManager().getMetamodel();
   }

   public Map<String, Object> getProperties()
   {
      return getEntityManager().getProperties();
   }

   public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties)
   {
      getEntityManager().lock(entity, lockMode, properties);
   }

   public void refresh(Object entity, Map<String, Object> properties)
   {
      getEntityManager().refresh(entity, properties);
   }

   public void refresh(Object entity, LockModeType lockMode)
   {
      getEntityManager().refresh(entity, lockMode);
   }

   public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties)
   {
      getEntityManager().refresh(entity, lockMode, properties);
   }

   public void setProperty(String propertyName, Object value)
   {
      getEntityManager().setProperty(propertyName, value);
   }

   public <T> T unwrap(Class<T> cls)
   {
      return getEntityManager().unwrap(cls);
   }

}
