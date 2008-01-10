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
package org.jboss.ejb3.test.standalone;

import org.jboss.ejb3.Container;

import javax.ejb.Stateless;
import javax.ejb.SessionContext;
import javax.persistence.*;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@PersistenceUnit(name="emf/pu", unitName="tempdb")
@PersistenceContext(name="em/pc", unitName="tempdb")
@PersistenceUnits(
        @PersistenceUnit(name="emf/pus", unitName="tempdb")
)
@PersistenceContexts(
        @PersistenceContext(name="em/pcs", unitName="tempdb")
)
public class CustomerDAOBean implements CustomerDAO
{
   @PersistenceContext private EntityManager manager;
   @Resource SessionContext ctx;

   public long createCustomer()
   {
      Customer cust = new Customer();
      cust.setName("Bill");
      manager.persist(cust);
      return cust.getId();
   }

   public Customer findCustomer(long id)
   {
      EntityManagerFactory emf = null;
      Hashtable props = new Hashtable();
      props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.LocalOnlyContextFactory");
      try
      {
         InitialContext initialContext = new InitialContext(props);
         emf = (EntityManagerFactory)initialContext.lookup(Container.ENC_CTX_NAME + "/env/emf/pu");
         emf = (EntityManagerFactory)initialContext.lookup(Container.ENC_CTX_NAME + "/env/emf/pus");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      emf = (EntityManagerFactory)ctx.lookup("emf/pu");
      if (emf == null) throw new RuntimeException("Could not locate enc registered pu");
      emf = (EntityManagerFactory)ctx.lookup("emf/pus");
      if (emf == null) throw new RuntimeException("Could not locate enc registered pu");

      EntityManager em = null;
      try
      {
         InitialContext initialContext = new InitialContext(props);
         em = (EntityManager)initialContext.lookup(Container.ENC_CTX_NAME + "/env/em/pc");
         em = (EntityManager)initialContext.lookup(Container.ENC_CTX_NAME + "/env/em/pcs");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      em = (EntityManager)ctx.lookup("em/pc");
      if (em == null) throw new RuntimeException("Could not locate enc registered pc");
      em = (EntityManager)ctx.lookup("em/pcs");
      if (em == null) throw new RuntimeException("Could not locate enc registered pc");


      return manager.find(Customer.class, id);
   }
}
