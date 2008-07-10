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
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.persistence.EntityManagerFactory;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.classic.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.jboss.jpa.deployment.ManagedEntityManagerFactory;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class InjectedSessionFactory implements SessionFactory, Externalizable
{
   private static final long serialVersionUID = 7866450655332120010L;
   
   private transient EntityManagerFactory delegate;
   private transient ManagedEntityManagerFactory managedFactory;

   public InjectedSessionFactory(ManagedEntityManagerFactory factory)
   {
      this.managedFactory = factory;
      this.delegate = factory.getEntityManagerFactory();
   }

   public InjectedSessionFactory() {}

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(managedFactory.getKernelName());
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      String kernelName = in.readUTF();
      managedFactory = ManagedEntityManagerFactoryHelper.getManagedEntityManagerFactory(kernelName);
      if (managedFactory == null) throw new IOException("Unable to find persistence unit in registry: " + kernelName);
      delegate = managedFactory.getEntityManagerFactory();
   }

   private EntityManagerFactory getDelegate()
   {
      return delegate;
   }

   private SessionFactory getSessionFactory()
   {
      return ((HibernateEntityManagerFactory)getDelegate()).getSessionFactory();
   }

   public Set getDefinedFilterNames()
   {
      return getSessionFactory().getDefinedFilterNames();
   }

   public FilterDefinition getFilterDefinition(String filterName) throws HibernateException
   {
      return getSessionFactory().getFilterDefinition(filterName);
   }

   public Session openSession(Connection connection)
   {
      return getSessionFactory().openSession(connection);
   }

   public Session openSession(Interceptor interceptor)
   throws HibernateException
   {
      return getSessionFactory().openSession(interceptor);
   }

   public Session openSession(Connection connection, Interceptor interceptor)
   {
      return getSessionFactory().openSession(connection, interceptor);
   }

   public Session openSession()
   throws HibernateException
   {
      return getSessionFactory().openSession();
   }

   public Session getCurrentSession()
   throws HibernateException
   {
      return getSessionFactory().getCurrentSession();
   }

   public ClassMetadata getClassMetadata(Class persistentClass)
   throws HibernateException
   {
      return getSessionFactory().getClassMetadata(persistentClass);
   }

   public ClassMetadata getClassMetadata(String entityName)
   throws HibernateException
   {
      return getSessionFactory().getClassMetadata(entityName);
   }

   public CollectionMetadata getCollectionMetadata(String roleName)
   throws HibernateException
   {
      return getSessionFactory().getCollectionMetadata(roleName);
   }

   public Map getAllClassMetadata()
   throws HibernateException
   {
      return getSessionFactory().getAllClassMetadata();
   }

   public Map getAllCollectionMetadata()
   throws HibernateException
   {
      return getSessionFactory().getAllCollectionMetadata();
   }

   public Statistics getStatistics()
   {
      return getSessionFactory().getStatistics();
   }

   public void close()
   throws HibernateException
   {
      throw new IllegalStateException("It is illegal to close an injected SessionFactory");
   }

   public boolean isClosed()
   {
      return getSessionFactory().isClosed();
   }

   public void evict(Class persistentClass)
   throws HibernateException
   {
      getSessionFactory().evict(persistentClass);
   }

   public void evict(Class persistentClass, Serializable id)
   throws HibernateException
   {
      getSessionFactory().evict(persistentClass, id);
   }

   public void evictEntity(String entityName)
   throws HibernateException
   {
      getSessionFactory().evictEntity(entityName);
   }

   public void evictEntity(String entityName, Serializable id)
   throws HibernateException
   {
      getSessionFactory().evictEntity(entityName, id);
   }

   public void evictCollection(String roleName)
   throws HibernateException
   {
      getSessionFactory().evictCollection(roleName);
   }

   public void evictCollection(String roleName, Serializable id)
   throws HibernateException
   {
      getSessionFactory().evictCollection(roleName, id);
   }

   public void evictQueries()
   throws HibernateException
   {
      getSessionFactory().evictQueries();
   }

   public void evictQueries(String cacheRegion)
   throws HibernateException
   {
      getSessionFactory().evictQueries(cacheRegion);
   }

   public StatelessSession openStatelessSession()
   {
      return getSessionFactory().openStatelessSession();
   }

   public StatelessSession openStatelessSession(Connection connection)
   {
      return getSessionFactory().openStatelessSession(connection);
   }

   public Reference getReference()
   throws NamingException
   {
      return getSessionFactory().getReference();
   }

}
