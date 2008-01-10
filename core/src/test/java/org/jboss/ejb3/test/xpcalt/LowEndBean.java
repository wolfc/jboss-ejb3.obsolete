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
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@Stateful
@CacheConfig(idleTimeoutSeconds=5)
public class LowEndBean implements LowEnd
{
   private static Logger log = Logger.getLogger(LowEndBean.class);
   
   //@PersistenceContext(type=EXTENDED)
   // This time it's manual labour
   private EntityManager em;
   
   @TransactionAttribute(NEVER)
   public void checkThingy(long id)
   {
      Thingy thingy = em.find(Thingy.class, id);
      if(thingy == null || thingy.getId() != id)
         throw new IllegalStateException("can't find thingy with id " + id);
   }
   
   @TransactionAttribute(NEVER)
   public long createThingy(long id)
   {
      log.info("em = " + em);
      
      Thingy thingy = new Thingy(id);
      em.persist(thingy);
      return thingy.getId();
   }
   
   public void doSomething()
   {
      log.info("doing something");
   }
   
   @PostConstruct
   protected void postConstruct()
   {
      log.info("postConstruct");
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
   }
   
   /**
    * Normally we inherit the entity manager, but this is all manual labour.
    */
   public void setEntityManager(EntityManager em)
   {
      this.em = em;
   }
}
