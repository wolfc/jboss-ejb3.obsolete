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

import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.jboss.ejb3.jpa.integration.AbstractEntityManagerDelegator;
import org.jboss.ejb3.stateful.StatefulBeanContext;

/**
 * EntityManager a managed extended persistence context.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class ExtendedEntityManager extends AbstractEntityManagerDelegator implements EntityManager, HibernateSession, Serializable, ExtendedPersistenceContext
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

   public void close()
   {
      throw new IllegalStateException("It is illegal to close an injected EntityManager");
   }

   @Override
   protected EntityManager getEntityManager()
   {
      return getPersistenceContext();
   }
   
   public Session getHibernateSession()
   {
      if (getPersistenceContext() instanceof HibernateEntityManager)
      {
         return ((HibernateEntityManager) getPersistenceContext()).getSession();
      }
      throw new RuntimeException("ILLEGAL ACTION:  Not a Hibernate persistence provider");
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
}

