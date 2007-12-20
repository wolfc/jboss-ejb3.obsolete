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
package org.jboss.ejb3.test.standalone.unit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * POJO Environment tests
 * 
 * @TODO XMLKernelDeployer -> BeanXMLDeployer
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 61136 $
 */
public class POJOEnvironmentTestCase extends TestCase
{
   private static boolean booted = false;

   public POJOEnvironmentTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      // set bad properties to make sure that we're injecting InitialContext correct
//      System.setProperty("java.naming.factory.initial", "ERROR");
//      System.setProperty("java.naming.factory.url.pkgs", "ERROR");

      super.setUp();
      long start = System.currentTimeMillis();
      try
      {
         if (!booted)
         {
            booted = true;
            EJB3StandaloneBootstrap.boot("");
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      EJB3StandaloneBootstrap.shutdown();
   }


   protected void configureLoggingAfterBootstrap()
   {
   }

   protected InitialContext getInitialContext() throws Exception
   {
      return new InitialContext(getInitialContextProperties());
   }

   protected Hashtable getInitialContextProperties()
   {
      return EJB3StandaloneBootstrap.getInitialContextProperties();
   }


   public void testTxDataSource() throws Throwable
   {
      InitialContext ctx = getInitialContext();
      DataSource ds = (DataSource) ctx.lookup("java:/DefaultDS");
      TransactionManager tm = (TransactionManager) ctx.lookup("java:/TransactionManager");

      Connection c = ds.getConnection();
      try
      {
         Statement s = c.createStatement();
         s.execute("create table test (key integer, value char(50))");
      }
      finally
      {
         c.close();
      }

      tm.begin();
      try
      {
         c = ds.getConnection();
         try
         {
            Statement s = c.createStatement();
            s.execute("insert into test (key, value) values(1, 'Hello')");
         }
         finally
         {
            c.close();
         }
      }
      finally
      {
         tm.rollback();
      }

      c = ds.getConnection();
      try
      {
         Statement s = c.createStatement();
         ResultSet r = s.executeQuery("select count(*) from test");
         if (r.next())
         {
            assertEquals(0, r.getInt(1));
         }
         else
            fail("Should not be here");
      }
      finally
      {
         c.close();
      }

      tm.begin();
      try
      {
         c = ds.getConnection();
         try
         {
            Statement s = c.createStatement();
            s.execute("insert into test (key, value) values(1, 'Goodbye')");
         }
         finally
         {
            c.close();
         }
      }
      finally
      {
         tm.commit();
      }

      c = ds.getConnection();
      try
      {
         Statement s = c.createStatement();
         ResultSet r = s.executeQuery("select value from test where key=1");
         if (r.next())
         {
            assertEquals("Goodbye", r.getString(1));
         }
         else
            fail("Should not be here");
      }
      finally
      {
         c.close();
      }
   }

   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("POJOEnvironment");
      suite.addTestSuite(POJOEnvironmentTestCase.class);
      return suite;
   }


}