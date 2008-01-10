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

import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.jboss.ejb3.PersistenceUnitRegistry;

import javax.persistence.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class TransactionScopedEntityManager implements EntityManager, HibernateSession, Externalizable
{
   private static final long serialVersionUID = 4260828563883650376L;
   
   private transient ManagedEntityManagerFactory factory;

   public Session getHibernateSession()
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (em instanceof HibernateEntityManager)
      {
         return ((HibernateEntityManager) em).getSession();
      }
      throw new RuntimeException("ILLEGAL ACTION:  Not a Hibernate pe" +
              "rsistence provider");
   }

   public TransactionScopedEntityManager(ManagedEntityManagerFactory factory)
   {
      if (factory == null) throw new NullPointerException("factory must not be null");
      this.factory = factory;
   }

   public TransactionScopedEntityManager()
   {
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(factory.getKernelName());
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      String kernelName = in.readUTF();
      PersistenceUnitDeployment deployment = PersistenceUnitRegistry.getPersistenceUnit(kernelName);
      if (deployment == null) throw new IOException("Unable to find persistence unit in registry: " + kernelName);
      factory = deployment.getManagedFactory();
   }

   public Object getDelegate()
   {
      return factory.getTransactionScopedEntityManager().getDelegate();
   }

   public void joinTransaction()
   {
      factory.verifyInTx();
      factory.getTransactionScopedEntityManager().joinTransaction();
   }

   public void clear()
   {
      factory.getTransactionScopedEntityManager().clear();
   }

   public FlushModeType getFlushMode()
   {
      return factory.getTransactionScopedEntityManager().getFlushMode();
   }

   public void lock(Object entity, LockModeType lockMode)
   {
      factory.verifyInTx();
      factory.getTransactionScopedEntityManager().lock(entity, lockMode);
   }

   public <T> T getReference(Class<T> entityClass, Object primaryKey)
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      try
      {
         return em.getReference(entityClass, primaryKey);
      }
      finally
      {
         if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      }
   }

   public void setFlushMode(FlushModeType flushMode)
   {
      factory.getTransactionScopedEntityManager().setFlushMode(flushMode);
   }

   public Query createQuery(String ejbqlString)
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      return em.createQuery(ejbqlString);
   }

   public Query createNamedQuery(String name)
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      return em.createNamedQuery(name);
   }

   public Query createNativeQuery(String sqlString)
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      return em.createNativeQuery(sqlString);
   }

   public Query createNativeQuery(String sqlString, Class resultClass)
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      return em.createNativeQuery(sqlString, resultClass);
   }

   public Query createNativeQuery(String sqlString, String resultSetMapping)
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      return em.createNativeQuery(sqlString, resultSetMapping);
   }

   public <A> A find(Class<A> entityClass, Object primaryKey)
   {
      EntityManager em = factory.getTransactionScopedEntityManager();
      if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      try
      {
         return em.find(entityClass, primaryKey);
      }
      finally
      {
         if (!factory.isInTx()) em.clear(); // em will be closed by interceptor
      }
   }

   public void persist(Object entity)
   {
      factory.verifyInTx();
      factory.getTransactionScopedEntityManager().persist(entity);
   }

   public <A> A merge(A entity)
   {
      factory.verifyInTx();
      return (A) factory.getTransactionScopedEntityManager().merge(entity);
   }

   public void remove(Object entity)
   {
      factory.verifyInTx();
      factory.getTransactionScopedEntityManager().remove(entity);
   }

   public void refresh(Object entity)
   {
      factory.verifyInTx();
      factory.getTransactionScopedEntityManager().refresh(entity);
   }

   public boolean contains(Object entity)
   {
      return factory.getTransactionScopedEntityManager().contains(entity);
   }

   public void flush()
   {
      factory.verifyInTx();
      factory.getTransactionScopedEntityManager().flush();
   }

   public void close()
   {
      throw new IllegalStateException("Illegal to call this method from injected, managed EntityManager");
   }

   public boolean isOpen()
   {
      throw new IllegalStateException("Illegal to call this method from injected, managed EntityManager");
   }

   public EntityTransaction getTransaction()
   {
      throw new IllegalStateException("Illegal to call this method from injected, managed EntityManager");
   }

}
