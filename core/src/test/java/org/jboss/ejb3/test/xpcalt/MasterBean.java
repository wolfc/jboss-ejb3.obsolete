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

import static javax.ejb.TransactionAttributeType.NEVER;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.JndiInject;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@Stateful
@CacheConfig(idleTimeoutSeconds=5)
public class MasterBean implements Master
{
   private static Logger log = Logger.getLogger(MasterBean.class);
   
   @EJB
   private LowEnd lowEnd;
   
   //@PersistenceContext(type=EXTENDED)
   @EJB(beanName="XPCAltBean")
   private EntityManager em;
   
   @JndiInject(jndiName="java:/TransactionManager")
   private TransactionManager tm;
   
   @TransactionAttribute(NEVER)
   public long createThingy(long id)
   {
      log.info("em = " + em);
      
      long realId = lowEnd.createThingy(id);
      Thingy thingy = em.find(Thingy.class, realId);
      if(thingy == null || thingy.getId() != realId)
         throw new IllegalStateException("can't find thingy with id " + realId);
      return id;
   }
   
   @TransactionAttribute(NEVER)
   public void checkThingy(long id)
   {
      Thingy thingy = em.find(Thingy.class, id);
      if(thingy == null || thingy.getId() != id)
         throw new IllegalStateException("can't find thingy with id " + id);
      
      lowEnd.checkThingy(id);
   }
   
   public void doSomething()
   {
      lowEnd.doSomething();
   }
   
   @PostConstruct
   protected void postConstruct()
   {
      log.info("postConstruct");
      // manual labour
      lowEnd.setEntityManager(em);
   }
   
   @PostActivate
   protected void postActivate()
   {
      log.info("postActivate");   
   }
   
   @PreDestroy
   protected void preDestroy()
   {
      log.info("preDestroy");
   }
   
   @PrePassivate
   protected void prePassivate()
   {
      log.info("prePassivate");
   }
   
   @Remove
   public void remove()
   {
      lowEnd.remove();
      em.close();
   }
   
   public void save()
   {
      try
      {
         if(tm.getTransaction() == null)
            throw new RuntimeException("where is my transaction?");
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
      
      em.flush();
   }
}
