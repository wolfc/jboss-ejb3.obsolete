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
package org.jboss.tutorial.entity.security.client;

import org.jboss.tutorial.entity.security.bean.AllEntity;
import org.jboss.tutorial.entity.security.bean.SomeEntity;
import org.jboss.tutorial.entity.security.bean.StarEntity;
import org.jboss.tutorial.entity.security.bean.Stateless;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class Client
{

   public static void main(String[] args) throws Exception
   {
      testAllEntity();
      testSomeEntity();
      testStarEntity();
   }

   public static InitialContext getInitialContext(String username, String password) throws Exception
   {
      Properties env = new Properties();
      env.setProperty(Context.SECURITY_PRINCIPAL, username);
      env.setProperty(Context.SECURITY_CREDENTIALS, password);
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      return new InitialContext(env);

   }

   public static void testAllEntity()throws Exception
   {
      InitialContext ctx = getInitialContext("somebody", "password");
      Stateless stateless = (Stateless)ctx.lookup("StatelessBean/remote");

      System.out.println("Good role");
      System.out.println("Inserting...");
      AllEntity e = stateless.insertAllEntity();
      System.out.println("Reading...");
      e = stateless.readAllEntity(e.id);
      e.val += "y";
      System.out.println("Updating...");
      stateless.updateAllEntity(e);
      System.out.println("Deleting...");
      stateless.deleteAllEntity(e);
      System.out.println("Inserting...");
      e = stateless.insertAllEntity();

      System.out.println("Bad role");
      getInitialContext("rolefail", "password");

      AllEntity ae2 = null;
      try
      {
         System.out.println("Inserting...");
         ae2 = stateless.insertAllEntity();
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         System.out.println("Reading...");
         ae2 = stateless.readAllEntity(e.id);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         e.val += "y";
         stateless.updateAllEntity(e);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         stateless.deleteAllEntity(e);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         e = stateless.insertAllEntity();
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }
   }


   public static void testStarEntity()throws Exception
   {
      InitialContext ctx = getInitialContext("somebody", "password");
      Stateless stateless = (Stateless)ctx.lookup("StatelessBean/remote");

      System.out.println("Good role");
      System.out.println("Inserting...");
      StarEntity e = stateless.insertStarEntity();
      System.out.println("Reading...");
      e = stateless.readStarEntity(e.id);
      e.val += "y";
      System.out.println("Updating...");
      stateless.updateStarEntity(e);
      System.out.println("Deleting...");
      stateless.deleteStarEntity(e);
      System.out.println("Inserting...");
      e = stateless.insertStarEntity();

      System.out.println("Bad role");
      getInitialContext("rolefail", "password");

      StarEntity ae2 = null;
      try
      {
         System.out.println("Inserting...");
         ae2 = stateless.insertStarEntity();
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         System.out.println("Reading...");
         ae2 = stateless.readStarEntity(e.id);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         e.val += "y";
         stateless.updateStarEntity(e);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         stateless.deleteStarEntity(e);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         e = stateless.insertStarEntity();
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }
   }

   public static void testSomeEntity()throws Exception
   {
      InitialContext ctx = getInitialContext("somebody", "password");
      Stateless stateless = (Stateless)ctx.lookup("StatelessBean/remote");

      System.out.println("Good role");
      System.out.println("Inserting...");
      SomeEntity e = stateless.insertSomeEntity();

      try
      {
         System.out.println("Reading...");
         e = stateless.readSomeEntity(e.id);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         e.val += "y";
         System.out.println("Updating...");
         stateless.updateSomeEntity(e);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }


      System.out.println("Deleting...");
      stateless.deleteSomeEntity(e);
      System.out.println("Inserting...");
      e = stateless.insertSomeEntity();

      System.out.println("Bad role");
      getInitialContext("rolefail", "password");

      SomeEntity ae2 = null;
      try
      {
         System.out.println("Inserting...");
         ae2 = stateless.insertSomeEntity();
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         stateless.deleteSomeEntity(e);
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }

      try
      {
         e = stateless.insertSomeEntity();
         throw new RuntimeException("security exception should have been thrown!");
      }
      catch(Exception ex)
      {
         System.out.println("Expected failure: " + ex.getMessage());
      }
   }
}
