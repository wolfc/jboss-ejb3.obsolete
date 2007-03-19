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
package org.jboss.ejb3.test.regression;

import java.io.Serializable;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.persistence.PersistenceContext;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.ejb3.Container;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Stateful
@CacheConfig(maxSize = 1000, idleTimeoutSeconds = 1)
@Remote(PassivationTest.class)
public class PassivationTestBean implements PassivationTest, Serializable
{
   @PersistenceContext private EntityManager em;


   public String echo(String echo)
   {
      System.out.println("***: " + echo);
      try
      {
         InitialContext ctx = new InitialContext();
         UserTransaction ut = (UserTransaction)ctx.lookup(Container.ENC_CTX_NAME + "/UserTransaction");
         Context env = (Context)ctx.lookup(Container.ENC_CTX_NAME + "/env");
         EntityManager em2 = (EntityManager)env.lookup("EntityManager");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      return echo;
   }

   public void createContact()
   {
      Contact c = new Contact();
      c.setEmail("bill@jboss.org");
      c.setName("Bill");
      c.setPhone("666-666-6666");
      em.persist(c);
   }

   @PrePassivate
   public void prePassivate()
   {
      System.out.println("PASSIVATING!!!");
   }
}
