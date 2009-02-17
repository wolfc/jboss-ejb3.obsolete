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
package org.jboss.ejb3.test.ejbthree1647.unit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.ejbthree1647.ResourceInjectionUsingMappedNameIntoPrimitiveBean;
import org.jboss.ejb3.test.ejbthree1647.ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness;
import org.jboss.test.JBossTestCase;

/**
 * ResourceInjectionIntoPrimitivesTestCase
 * 
 * Test Cases to ensure @Resource.mappedName Injection into primitive/wrapper/String
 * targets succeeds
 * 
 * EJBTHREE-1647
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ResourceInjectionIntoPrimitivesTestCase extends JBossTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * The hook into the remote container
    */
   private static ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness bean;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   public ResourceInjectionIntoPrimitivesTestCase(String name)
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
      return getDeploySetup(ResourceInjectionIntoPrimitivesTestCase.class, "ejbthree1647.jar");
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * Ensures that @Resource.mappedName injection into a String 
    * target succeeds as expected
    */
   public void testStringInjectionUsingResourceMappedName()
   {
      // Get the bean
      ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness bean = getBean();

      // Get the value
      String value = bean.getStringValue();

      // Ensure expected
      TestCase.assertEquals("String injection not as expected",
            ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_STRING, value);
   }

   /**
    * Ensures that @Resource.mappedName injection into an int 
    * target succeeds as expected
    */
   public void testIntInjectionUsingResourceMappedName()
   {
      // Get the bean
      ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness bean = getBean();

      // Get the value
      int value = bean.getIntValue();

      // Ensure expected
      TestCase.assertEquals("int injection not as expected",
            ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INT, value);
   }

   /**
    * Ensures that @Resource.mappedName injection into an Integer 
    * target succeeds as expected
    */
   public void testIntegerInjectionUsingResourceMappedName()
   {
      // Get the bean
      ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness bean = getBean();

      // Get the value
      Integer value = bean.getIntegerValue();

      // Ensure expected
      TestCase.assertEquals("Integer injection not as expected",
            ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INTEGER, value);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /**
    * Returns the access bean, obtaining from JNDI if necessary 
    */
   private static synchronized ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness getBean()
   {
      // If not yet obtained
      if (bean == null)
      {
         // Define JNDI Name
         String jndiName = ResourceInjectionUsingMappedNameIntoPrimitiveBean.class.getSimpleName() + "/remote";

         try
         {
            // Get the naming context
            Context context = new InitialContext();

            // Get the bean
            bean = (ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness) context.lookup(jndiName);
         }
         catch (NamingException ne)
         {
            TestCase.fail("Could not get test bean from JNDI at " + jndiName);
         }
      }

      // Return
      return bean;
   }

}
