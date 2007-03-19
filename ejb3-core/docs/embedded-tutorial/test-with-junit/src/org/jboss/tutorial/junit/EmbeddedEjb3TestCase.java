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
package org.jboss.tutorial.junit;

import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class EmbeddedEjb3TestCase extends TestCase
{
   public EmbeddedEjb3TestCase()
   {
      super("EmbeddedEjb3TestCase");
   }


   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(EmbeddedEjb3TestCase.class);


      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            startupEmbeddedJboss();
         }

         protected void tearDown()
         {
            shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
         EJB3StandaloneBootstrap.boot(null);
         EJB3StandaloneBootstrap.scanClasspath("tutorial.jar");
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }

   public void testEJBs() throws Exception
   {

      InitialContext ctx = getInitialContext();
      CustomerDAOLocal local = (CustomerDAOLocal) ctx.lookup("CustomerDAOBean/local");
      CustomerDAORemote remote = (CustomerDAORemote) ctx.lookup("CustomerDAOBean/remote");

      System.out.println("----------------------------------------------------------");
      System.out.println("This test scans the System Property java.class.path for all annotated EJB3 classes");
      System.out.print("    ");

      int id = local.createCustomer("Gavin");
      Customer cust = local.findCustomer(id);
      assertNotNull(cust);
      System.out.println("Successfully created and found Gavin from @Local interface");

      id = remote.createCustomer("Emmanuel");
      cust = remote.findCustomer(id);
      assertNotNull(cust);
      System.out.println("Successfully created and found Emmanuel from @Remote interface");
      System.out.println("----------------------------------------------------------");
   }

   public void testEntityManager() throws Exception
   {
      // This is a transactionally aware EntityManager and must be accessed within a JTA transaction
      // Why aren't we using javax.persistence.Persistence?  Well, our persistence.xml file uses
      // jta-datasource which means that it is created by the EJB container/embedded JBoss.
      // using javax.persistence.Persistence will just cause us an error
      EntityManager em = (EntityManager) getInitialContext().lookup("java:/EntityManagers/custdb");

      // Obtain JBoss transaction
      TransactionManager tm = (TransactionManager) getInitialContext().lookup("java:/TransactionManager");

      tm.begin();

      Customer cust = new Customer();
      cust.setName("Bill");
      em.persist(cust);

      assertTrue(cust.getId() > 0);

      int id = cust.getId();

      System.out.println("created bill in DB with id: " + id);

      tm.commit();

      tm.begin();
      cust = em.find(Customer.class, id);
      assertNotNull(cust);
      tm.commit();
   }

   public static InitialContext getInitialContext() throws Exception
   {
      Hashtable props = getInitialContextProperties();
      return new InitialContext(props);
   }

   private static Hashtable getInitialContextProperties()
   {
      Hashtable props = new Hashtable();
      props.put("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      return props;
   }
}
