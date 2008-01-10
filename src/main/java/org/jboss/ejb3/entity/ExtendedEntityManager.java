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
package org.jboss.ejb3.entity;

import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.jboss.ejb3.stateful.StatefulBeanContext;

/**
 * EntityManager a managed extended persistence context.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class ExtendedEntityManager implements EntityManager, HibernateSession, Serializable, ExtendedPersistenceContext
{
   private static final long serialVersionUID = -2221892311301499591L;
   
   private String identity;

   public ExtendedEntityManager(String name)
   {
      this.identity = name;
   }

   public ExtendedEntityManager()
   {
   }

   public EntityManager getPersistenceContext()
   {
      StatefulBeanContext beanContext = StatefulBeanContext.currentBean.get();
      EntityManager persistenceContext = beanContext.getExtendedPersistenceContext(identity);
      if (persistenceContext == null)
         throw new RuntimeException("Unable to determine persistenceContext: " + identity
                                    + " in injected SFSB: " + beanContext.getContainer().getObjectName());
      return persistenceContext;
   }

   // delegates

   public Session getHibernateSession()
   {
      if (getPersistenceContext() instanceof HibernateEntityManager)
      {
         return ((HibernateEntityManager) getPersistenceContext()).getSession();
      }
      throw new RuntimeException("ILLEGAL ACTION:  Not a Hibernate persistence provider");
   }

   public void joinTransaction()
   {
      getPersistenceContext().joinTransaction();
   }

   public void clear()
   {
      getPersistenceContext().clear();
   }

   public void lock(Object entity, LockModeType lockMode)
   {
      getPersistenceContext().lock(entity, lockMode);
   }

   public FlushModeType getFlushMode()
   {
      return getPersistenceContext().getFlushMode();
   }

   public <T> T getReference(Class<T> entityClass, Object primaryKey)
   {
      return getPersistenceContext().getReference(entityClass, primaryKey);
   }

   public void persist(Object entity)
   {
      getPersistenceContext().persist(entity);
   }

   public <T> T merge(T entity)
   {
      return getPersistenceContext().merge(entity);
   }

   public void remove(Object entity)
   {
      getPersistenceContext().remove(entity);
   }

   public <T> T find(Class<T> entityClass, Object primaryKey)
   {
      return getPersistenceContext().find(entityClass, primaryKey);
   }

   public void flush()
   {
      getPersistenceContext().flush();
   }

   public javax.persistence.Query createQuery(String ejbqlString)
   {
      return getPersistenceContext().createQuery(ejbqlString);
   }

   public javax.persistence.Query createNamedQuery(String name)
   {
      return getPersistenceContext().createNamedQuery(name);
   }

   public javax.persistence.Query createNativeQuery(String sqlString)
   {
      return getPersistenceContext().createNativeQuery(sqlString);
   }

   public javax.persistence.Query createNativeQuery(String sqlString, Class resultClass)
   {
      return getPersistenceContext().createNativeQuery(sqlString, resultClass);
   }

   public javax.persistence.Query createNativeQuery(String sqlString, String resultSetMapping)
   {
      return getPersistenceContext().createNativeQuery(sqlString, resultSetMapping);
   }

   public void refresh(Object entity)
   {
      getPersistenceContext().refresh(entity);
   }

   public boolean contains(Object entity)
   {
      return getPersistenceContext().contains(entity);
   }

   public void close()
   {
      throw new IllegalStateException("It is illegal to close an injected EntityManager");
   }

   public boolean isOpen()
   {
      return getPersistenceContext().isOpen();
   }

   public EntityTransaction getTransaction()
   {
      return getPersistenceContext().getTransaction();
   }

   public void setFlushMode(FlushModeType flushMode)
   {
      getPersistenceContext().setFlushMode(flushMode);
   }

   public Object getDelegate()
   {
      return getPersistenceContext().getDelegate();
   }

}

