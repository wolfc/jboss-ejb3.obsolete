/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.xpcalt;

import static javax.ejb.TransactionAttributeType.SUPPORTS;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.JndiInject;
import org.jboss.logging.Logger;

/**
 * An alternative to extended persistence context.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@Stateful
@Local(EntityManager.class)
@TransactionAttribute(SUPPORTS)
@CacheConfig(idleTimeoutSeconds=5)
public class XPCAltBean implements EntityManager
{
   private static final Logger log = Logger.getLogger(XPCAltBean.class);
   
   @PersistenceUnit
   private EntityManagerFactory emf;
   
   private EntityManager actualEntityManager;
   
   @JndiInject(jndiName="java:/TransactionManager")
   private TransactionManager tm;
   
   @PostConstruct
   protected void postConstruct()
   {
      this.actualEntityManager = emf.createEntityManager();
   }
   
   @PostActivate
   protected void postActivate()
   {
      log.info("postActivate");   
   }
   
   @PreDestroy
   protected void preDestroy()
   {
      this.actualEntityManager.close();
   }
   
   @PrePassivate
   protected void prePassivate()
   {
      log.info("prePassivate");
   }
   
   public void clear()
   {
      actualEntityManager.clear();
   }
   
   @Remove
   public final void close()
   {
      // Normally this is illegal, but since we manage the lifecycle of XPCAlt ourselves this will remove the bean.
      //throw new IllegalStateException("Closing a container managed entity manager is not allowed (EJB3 persistence 5.9.1)");
   }
   
   public boolean contains(Object entity)
   {
      return actualEntityManager.contains(entity);
   }
   
   public Query createNamedQuery(String name)
   {
      joinOptionalTransaction();
      return actualEntityManager.createNamedQuery(name);
   }
   
   public Query createNativeQuery(String sqlString)
   {
      joinOptionalTransaction();
      return actualEntityManager.createNativeQuery(sqlString);
   }
   
   public Query createNativeQuery(String sqlString, Class resultClass)
   {
      joinOptionalTransaction();
      return actualEntityManager.createNativeQuery(sqlString, resultClass);
   }
   
   public Query createNativeQuery(String sqlString, String resultSetMapping)
   {
      joinOptionalTransaction();
      return actualEntityManager.createNativeQuery(sqlString, resultSetMapping);
   }
   
   public Query createQuery(String qlString)
   {
      joinOptionalTransaction();
      return actualEntityManager.createQuery(qlString);
   }
   
   public <T> T find(Class<T> entityClass, Object primaryKey)
   {
      joinOptionalTransaction();
      return actualEntityManager.find(entityClass, primaryKey);
   }
   
   public void flush()
   {
      verifyInTx();
      actualEntityManager.flush();
   }
   
   public Object getDelegate()
   {
      return actualEntityManager.getDelegate();
   }
   
   public FlushModeType getFlushMode()
   {
      return actualEntityManager.getFlushMode();
   }
   
   public <T> T getReference(Class<T> entityClass, Object primaryKey)
   {
      joinOptionalTransaction();
      return actualEntityManager.getReference(entityClass, primaryKey);
   }
   
   public final EntityTransaction getTransaction()
   {
      throw new IllegalStateException("Not allowed on a JTA entity manager (EJB3 Persistence 3.1.1)");
   }
   
   public boolean isOpen()
   {
      return actualEntityManager.isOpen();
   }
   
   private void joinOptionalTransaction()
   {
      try
      {
         if(tm.getStatus() != Status.STATUS_NO_TRANSACTION)
            actualEntityManager.joinTransaction();
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public void joinTransaction()
   {
      actualEntityManager.joinTransaction();
   }
   
   public void lock(Object entity, LockModeType lockMode)
   {
      verifyInTx();
      actualEntityManager.lock(entity, lockMode);
   }
   
   public <T> T merge(T entity)
   {
      joinOptionalTransaction();
      return actualEntityManager.merge(entity);
   }
   
   public void persist(Object entity)
   {
      joinOptionalTransaction();
      actualEntityManager.persist(entity);
   }
   
   public void refresh(Object entity)
   {
      joinOptionalTransaction();
      actualEntityManager.refresh(entity);
   }
   
   public void remove(Object entity)
   {
      joinOptionalTransaction();
      actualEntityManager.remove(entity);
   }
   
   public void setFlushMode(FlushModeType flushMode)
   {
      actualEntityManager.setFlushMode(flushMode);
   }
   
   private void verifyInTx()
   {
      try
      {
         log.info("tx status = " + tm.getStatus());
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
      joinTransaction();
   }
}
