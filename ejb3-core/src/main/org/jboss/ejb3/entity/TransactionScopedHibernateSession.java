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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.sql.Connection;
import javax.persistence.EntityManager;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.stat.SessionStatistics;
import org.jboss.ejb3.PersistenceUnitRegistry;


/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class TransactionScopedHibernateSession implements Session, Externalizable
{
   private transient ManagedEntityManagerFactory factory;

   protected Session getHibernateSession()
   {
      if (getSession() instanceof HibernateEntityManager)
      {
         return ((HibernateEntityManager) getSession()).getSession();
      }
      throw new RuntimeException("ILLEGAL ACTION:  Not a Hibernate pe" +
                                 "rsistence provider");
   }

   protected EntityManager getSession()
   {
      return factory.getTransactionScopedEntityManager();
   }

   public TransactionScopedHibernateSession(ManagedEntityManagerFactory factory)
   {
      if (factory == null) throw new NullPointerException("factory must not be null");
      this.factory = factory;
   }

   public TransactionScopedHibernateSession()
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

   public EntityMode getEntityMode()
   {
      return getHibernateSession().getEntityMode();
   }

   public Session getSession(EntityMode entityMode)
   {
      return getHibernateSession().getSession(entityMode);
   }

   public void flush()
   throws HibernateException
   {
      getHibernateSession().flush();
   }

   public void setFlushMode(FlushMode flushMode)
   {
      getHibernateSession().setFlushMode(flushMode);
   }

   public FlushMode getFlushMode()
   {
      return getHibernateSession().getFlushMode();
   }

   public void setCacheMode(CacheMode cacheMode)
   {
      getHibernateSession().setCacheMode(cacheMode);
   }

   public CacheMode getCacheMode()
   {
      return getHibernateSession().getCacheMode();
   }

   public SessionFactory getSessionFactory()
   {
      return getHibernateSession().getSessionFactory();
   }

   public Connection connection()
   throws HibernateException
   {
      return getHibernateSession().connection();
   }

   public Connection disconnect()
   throws HibernateException
   {
      return getHibernateSession().disconnect();
   }

   public void reconnect()
   throws HibernateException
   {
      getHibernateSession().reconnect();
   }

   public void reconnect(Connection connection)
   throws HibernateException
   {
      getHibernateSession().reconnect(connection);
   }

   public Connection close()
   throws HibernateException
   {
      throw new IllegalStateException("Illegal to call this method from injected, managed EntityManager");
   }

   public void cancelQuery()
   throws HibernateException
   {
      getHibernateSession().cancelQuery();
   }

   public boolean isOpen()
   {
      return getHibernateSession().isOpen();
   }

   public boolean isConnected()
   {
      return getHibernateSession().isConnected();
   }

   public boolean isDirty()
   throws HibernateException
   {
      return getHibernateSession().isDirty();
   }

   public Serializable getIdentifier(Object object)
   throws HibernateException
   {
      return getHibernateSession().getIdentifier(object);
   }

   public boolean contains(Object object)
   {
      return getHibernateSession().contains(object);
   }

   public void evict(Object object)
   throws HibernateException
   {
      getHibernateSession().evict(object);
   }

   public Object load(Class theClass, Serializable id, LockMode lockMode)
   throws HibernateException
   {
      return getHibernateSession().load(theClass, id, lockMode);
   }

   public Object load(String entityName, Serializable id, LockMode lockMode)
   throws HibernateException
   {
      return getHibernateSession().load(entityName, id, lockMode);
   }

   public Object load(Class theClass, Serializable id)
   throws HibernateException
   {
      return getHibernateSession().load(theClass, id);
   }

   public Object load(String entityName, Serializable id)
   throws HibernateException
   {
      return getHibernateSession().load(entityName, id);
   }

   public void load(Object object, Serializable id)
   throws HibernateException
   {
      getHibernateSession().load(object, id);
   }

   public void replicate(Object object, ReplicationMode replicationMode)
   throws HibernateException
   {
      getHibernateSession().replicate(object, replicationMode);
   }

   public void replicate(String entityName, Object object, ReplicationMode replicationMode)
   throws HibernateException
   {
      getHibernateSession().replicate(entityName, object, replicationMode);
   }

   public Serializable save(Object object)
   throws HibernateException
   {
      return getHibernateSession().save(object);
   }

   public Serializable save(String entityName, Object object)
   throws HibernateException
   {
      return getHibernateSession().save(entityName, object);
   }

   public void saveOrUpdate(Object object)
   throws HibernateException
   {
      getHibernateSession().saveOrUpdate(object);
   }

   public void saveOrUpdate(String entityName, Object object)
   throws HibernateException
   {
      getHibernateSession().saveOrUpdate(entityName, object);
   }

   public void update(Object object)
   throws HibernateException
   {
      getHibernateSession().update(object);
   }

   public void update(String entityName, Object object)
   throws HibernateException
   {
      getHibernateSession().update(entityName, object);
   }

   public Object merge(Object object)
   throws HibernateException
   {
      return getHibernateSession().merge(object);
   }

   public Object merge(String entityName, Object object)
   throws HibernateException
   {
      return getHibernateSession().merge(entityName, object);
   }

   public void persist(Object object)
   throws HibernateException
   {
      getHibernateSession().persist(object);
   }

   public void persist(String entityName, Object object)
   throws HibernateException
   {
      getHibernateSession().persist(entityName, object);
   }

   public void delete(Object object)
   throws HibernateException
   {
      getHibernateSession().delete(object);
   }

   public void delete(String entityName, Object object)
   throws HibernateException
   {
      getHibernateSession().delete(entityName, object);
   }

   public void lock(Object object, LockMode lockMode)
   throws HibernateException
   {
      getHibernateSession().lock(object, lockMode);
   }

   public void lock(String entityName, Object object, LockMode lockMode)
   throws HibernateException
   {
      getHibernateSession().lock(entityName, object, lockMode);
   }

   public void refresh(Object object)
   throws HibernateException
   {
      getHibernateSession().refresh(object);
   }

   public void refresh(Object object, LockMode lockMode)
   throws HibernateException
   {
      getHibernateSession().refresh(object, lockMode);
   }

   public LockMode getCurrentLockMode(Object object)
   throws HibernateException
   {
      return getHibernateSession().getCurrentLockMode(object);
   }

   public Transaction beginTransaction()
   throws HibernateException
   {
      return getHibernateSession().beginTransaction();
   }

   public Criteria createCriteria(Class persistentClass)
   {
      return getHibernateSession().createCriteria(persistentClass);
   }

   public Criteria createCriteria(Class persistentClass, String alias)
   {
      return getHibernateSession().createCriteria(persistentClass, alias);
   }

   public Criteria createCriteria(String entityName)
   {
      return getHibernateSession().createCriteria(entityName);
   }

   public Criteria createCriteria(String entityName, String alias)
   {
      return getHibernateSession().createCriteria(entityName, alias);
   }

   public org.hibernate.Query createQuery(String queryString)
   throws HibernateException
   {
      return getHibernateSession().createQuery(queryString);
   }

   public SQLQuery createSQLQuery(String queryString)
   throws HibernateException
   {
      return getHibernateSession().createSQLQuery(queryString);
   }

   public org.hibernate.Query createFilter(Object collection, String queryString)
   throws HibernateException
   {
      return getHibernateSession().createFilter(collection, queryString);
   }

   public org.hibernate.Query getNamedQuery(String queryName)
   throws HibernateException
   {
      return getHibernateSession().getNamedQuery(queryName);
   }

   public void clear()
   {
      getHibernateSession().clear();
   }

   public Object get(Class clazz, Serializable id)
   throws HibernateException
   {
      return getHibernateSession().get(clazz, id);
   }

   public Object get(Class clazz, Serializable id, LockMode lockMode)
   throws HibernateException
   {
      return getHibernateSession().get(clazz, id, lockMode);
   }

   public Object get(String entityName, Serializable id)
   throws HibernateException
   {
      return getHibernateSession().get(entityName, id);
   }

   public Object get(String entityName, Serializable id, LockMode lockMode)
   throws HibernateException
   {
      return getHibernateSession().get(entityName, id, lockMode);
   }

   public String getEntityName(Object object)
   throws HibernateException
   {
      return getHibernateSession().getEntityName(object);
   }

   public Filter enableFilter(String filterName)
   {
      return getHibernateSession().enableFilter(filterName);
   }

   public Filter getEnabledFilter(String filterName)
   {
      return getHibernateSession().getEnabledFilter(filterName);
   }

   public void disableFilter(String filterName)
   {
      getHibernateSession().disableFilter(filterName);
   }

   public SessionStatistics getStatistics()
   {
      return getHibernateSession().getStatistics();
   }

   public void setReadOnly(Object entity, boolean readOnly)
   {
      getHibernateSession().setReadOnly(entity, readOnly);
   }

   public Transaction getTransaction()
   {
      return getHibernateSession().getTransaction();
   }
}
