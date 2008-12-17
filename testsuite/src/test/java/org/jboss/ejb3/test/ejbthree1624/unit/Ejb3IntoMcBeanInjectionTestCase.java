/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1624.unit;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.ejbthree1624.AccessBean;
import org.jboss.ejb3.test.ejbthree1624.AccessRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1624.DelegateNotInjectedException;
import org.jboss.test.JBossTestCase;

/**
 * Ejb3IntoMcBeanInjectionTestCase
 * 
 * Test Cases to ensure @EJB Injection into MC Beans succeeds
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Ejb3IntoMcBeanInjectionTestCase extends JBossTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * The arguments to be passed along
    */
   private static final int[] args =
   {1, 2, 3, 9};

   /**
    * The expected result of the tests
    */
   private static final int expectedResult;
   static
   {
      int sum = 0;
      for (int arg : args)
      {
         sum += arg;
      }
      expectedResult = sum;
   }

   /**
    * The hook into the remote container
    */
   private static AccessRemoteBusiness accessBean;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   public Ejb3IntoMcBeanInjectionTestCase(String name)
   {
      super(name);
   }

   // --------------------------------------------------------------------------------||
   // Suite --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   public static Test suite() throws Exception
   {
      /*
       * Get the deploy setup 
       */
      return getDeploySetup(Ejb3IntoMcBeanInjectionTestCase.class, "ejbthree1624.jar");
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * Tests that a Local Business interface can be resolved/injected 
    * into an MC Bean
    */
   public void testLocalBusinessInterfaceInjectionIntoMcBean() throws Throwable
   {
      // Test
      try
      {
         TestCase.assertEquals(expectedResult, getAccessBean().addUsingLocalBusinessView(args));
      }
      catch (EJBException ejbe)
      {
         this.checkForFailCaseException(ejbe);
      }
   }

   /**
    * Tests that a Remote Business interface can be resolved/injected 
    * into an MC Bean
    */
   public void testRemoteBusinessInterfaceInjectionIntoMcBean() throws Throwable
   {
      // Test
      try
      {
         TestCase.assertEquals(expectedResult, getAccessBean().addUsingRemoteBusinessView(args));
      }
      catch (EJBException ejbe)
      {
         this.checkForFailCaseException(ejbe);
      }
   }

   /**
    * Tests that a Local Home interface can be resolved/injected 
    * into an MC Bean
    */
   public void testLocalHomeInterfaceInjectionIntoMcBean() throws Throwable
   {
      // Test
      try
      {
         TestCase.assertEquals(expectedResult, getAccessBean().addUsingLocalComponentView(args));

      }
      catch (EJBException ejbe)
      {
         this.checkForFailCaseException(ejbe);
      }
   }

   /**
    * Tests that a Remote Home interface can be resolved/injected 
    * into an MC Bean
    */
   public void testRemoteHomeInterfaceInjectionIntoMcBean() throws Throwable
   {
      // Test
      try
      {
         TestCase.assertEquals(expectedResult, getAccessBean().addUsingRemoteComponentView(args));
      }
      catch (EJBException ejbe)
      {
         this.checkForFailCaseException(ejbe);
      }
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * Fails the test if any execption signaling failure is thrown
    * (as opposed to erroring out)
    * 
    * @param ejbe The exception
    */
   private void checkForFailCaseException(EJBException ejbe)
   {
      if (ejbe.getCause() instanceof DelegateNotInjectedException)
      {
         TestCase.fail("Delegate was not injected, caught " + DelegateNotInjectedException.class.getName());
      }
      else
      {
         throw ejbe;
      }
   }

   /**
    * Returns the access bean, obtaining from JNDI if necessary 
    */
   private static synchronized AccessRemoteBusiness getAccessBean()
   {
      // If not yet obtained
      if (accessBean == null)
      {
         // Get the naming context
         Context context = null;
         try
         {
            context = new InitialContext();

            // Get the access bean
            accessBean = (AccessRemoteBusiness) context.lookup(AccessBean.class.getSimpleName() + "/remote");
         }
         catch (NamingException ne)
         {
            throw new RuntimeException(ne);
         }
      }

      // Return
      return accessBean;
   }

}
