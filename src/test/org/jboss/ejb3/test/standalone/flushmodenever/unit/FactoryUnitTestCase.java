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
package org.jboss.ejb3.test.standalone.flushmodenever.unit;

import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.test.standalone.flushmodenever.Entity1;
import org.jboss.ejb3.test.standalone.flushmodenever.Entity2;
import org.jboss.ejb3.test.standalone.flushmodenever.Session2;
import org.jboss.ejb3.test.standalone.flushmodenever.Stateful1;
import org.jboss.ejb3.test.standalone.flushmodenever.Util;
import org.jboss.ejb3.test.standalone.flushmodenever.Session1;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: FactoryUnitTestCase.java 61136 2007-03-06 09:24:20Z wolfc $
 */

public class FactoryUnitTestCase
        extends TestCase
{

   public FactoryUnitTestCase(String name)
   {

      super(name);

   }

   protected InitialContext getInitialContext() throws Exception
   {
      return new InitialContext(getInitialContextProperties());
   }

   protected Hashtable getInitialContextProperties()
   {
      return EJB3StandaloneBootstrap.getInitialContextProperties();
   }

   public void testUserTransaction() throws Exception
   {
      UserTransaction tx = (UserTransaction)getInitialContext().lookup("UserTransaction");
      EntityManagerFactory factory1 = (EntityManagerFactory)getInitialContext().lookup("java:/Manager1Factory");

      tx.begin();
      Entity1 one = new Entity1();
      one.setString("UserTransaction");
      EntityManager em1 = factory1.createEntityManager();
      em1.persist(one);
      tx.commit();
      em1.close();

      EntityManager em = factory1.createEntityManager();
      one = em.find(Entity1.class, one.getId());
      assertNotNull(one);
      em.close();
      
   }

   public void testMe() throws Exception
   {
      Session1 session1 = (Session1) this.getInitialContext().lookup("Session1Bean/remote");
      Session2 session2 = (Session2) this.getInitialContext().lookup("Session2Bean/remote");

      int oneF = session1.create1FromFactory();
      int oneM = session1.create1FromManager();
      int twoF = session1.create2FromFactory();
      int twoM = session1.create2FromManager();
      session1.doUtil(new Util());

      session2.find1FromFactory(oneF);
      assertNotNull(session2.find1FromManager(oneM));
      session2.find2FromFactory(twoF);
      assertNotNull(session2.find2FromManager(twoM));
      assertNotNull(session2.findUtil1FromManager(1));
      assertNotNull(session2.findUtil2FromManager(2));

   }

   public void testExtended() throws Exception
   {
      Stateful1 stateful1 = (Stateful1) this.getInitialContext().lookup("Stateful1Bean/remote");
      Session2 session2 = (Session2) this.getInitialContext().lookup("Session2Bean/remote");

      int oneId = stateful1.create1();
      int twoId = stateful1.create2();

      stateful1.update1();
      stateful1.update2();

      {
         Entity1 one = session2.find1FromManager(oneId);
         assertEquals(one.getString(), "changed");

         Entity2 two = session2.find2FromManager(twoId);
         assertEquals(two.getString(), "changed");
      }

      stateful1.never();

      {
         Entity1 one = session2.find1FromManager(oneId);
         assertEquals(one.getString(), "changed");

         Entity2 two = session2.find2FromManager(twoId);
         assertEquals(two.getString(), "changed");
      }

      stateful1.checkout();

      {
         Entity1 one = session2.find1FromManager(oneId);
         assertEquals(one.getString(), "never");

         Entity2 two = session2.find2FromManager(twoId);
         assertEquals(two.getString(), "never");
      }
   }

   public void testExtended2() throws Exception
   {
      Stateful1 stateful1 = (Stateful1) this.getInitialContext().lookup("Stateful1Bean/remote");
      Session2 session2 = (Session2) this.getInitialContext().lookup("Session2Bean/remote");

      int oneId = stateful1.create1();
      int twoId = stateful1.create2();

      stateful1.update1();
      stateful1.update2();

      Entity1 one = session2.find1FromManager(oneId);
      assertEquals(one.getString(), "changed");

      Entity2 two = session2.find2FromManager(twoId);
      assertEquals(two.getString(), "changed");
      one.setString("never2");
      two.setString("never2");
      stateful1.never2(one, two);



      {
         Entity1 uno = session2.find1FromManager(oneId);
         assertEquals(uno.getString(), "changed");

         Entity2 dos = session2.find2FromManager(twoId);
         assertEquals(dos.getString(), "changed");
      }

      stateful1.checkout();

      {
         Entity1 uno = session2.find1FromManager(oneId);
         assertEquals(uno.getString(), "never2");

         Entity2 dos = session2.find2FromManager(twoId);
         assertEquals(dos.getString(), "never2");
      }
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(FactoryUnitTestCase.class);


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
         EJB3StandaloneBootstrap.scanClasspath("flushmodenever-session1.jar, flushmodenever-session2.jar");
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }

}
