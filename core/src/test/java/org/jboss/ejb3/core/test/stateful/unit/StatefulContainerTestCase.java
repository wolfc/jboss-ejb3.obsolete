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
package org.jboss.ejb3.core.test.stateful.unit;

import static org.junit.Assert.assertEquals;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.stateful.StatefulBean;
import org.jboss.ejb3.core.test.stateful.StatefulLocalBusiness;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * StatefulContainerTestCase
 * 
 * A Collection of Simple SFSB Tests
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulContainerTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(StatefulContainerTestCase.class);

   /**
    * Defines the SFSB Bean Implementation Class to use for testing
    */
   private static Class<?> testClass = StatefulBean.class;

   /**
    * A Reference to the Session Container to be created
    */
   private static SessionContainer container;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that SFSB session integrity is kept inbetween invocations
    */
   @Test
   public void testSfsbSessionPersistenceBetweenInvocations() throws Throwable
   {
      // Lookup a new instance
      StatefulLocalBusiness bean1 = this.lookupLocalSfsb();

      // Initialize a counter
      int counter1 = bean1.getNextCounter();

      // Ensure invocation succeeded
      assertEquals("Counter should have been started at 1", 1, counter1);

      // Ensure counter is incremented in same session
      counter1 = bean1.getNextCounter();
      assertEquals("Counter should have been incremented", 2, counter1);

      // Lookup a new instance
      StatefulLocalBusiness bean2 = this.lookupLocalSfsb();

      // Initialize a new counter for this session
      int counter2 = bean2.getNextCounter();

      // Ensure sessions have not overlapped
      assertEquals("Counter for new session should have been started at 1", 1, counter2);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains a new SFSB instance
    */
   protected StatefulLocalBusiness lookupLocalSfsb() throws NamingException
   {
      String jndiName = this.getBaseJndiName() + "local";
      StatefulLocalBusiness obj = null;
      return this.lookupBean(obj, jndiName);
   }

   /**
    * Obtains the JNDI Context to use
    * @return
    */
   protected Context getNamingContext() throws NamingException
   {
      return new InitialContext();

   }

   /**
    * Looks up the bean instance at the specified JNDI name, casting
    * to the type specified
    * 
    * @param <T>
    * @param type
    * @param jndiName
    * @return
    * @throws NamingException
    */
   @SuppressWarnings("unchecked")
   protected <T> T lookupBean(T type, String jndiName) throws NamingException
   {
      return (T) this.getNamingContext().lookup(jndiName);
   }

   /**
    * Obtains the base JNDI name for the test EJB
    * @return
    */
   protected String getBaseJndiName()
   {
      return testClass.getSimpleName() + "/";
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   @Before
   public void before()
   {
      StatefulBean.preDestroys = 0;
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();

      // Deploy the test SLSB
      container = AbstractEJB3TestCase.deploySessionEjb(testClass);
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      // Undeploy the test SLSB
      AbstractEJB3TestCase.undeployEjb(container);

      AbstractEJB3TestCase.afterClass();
   }
   
   /**
    * EJBTHREE-1496: preDestroy must be called on remove.
    */
   @Test
   public void testPreDestroy() throws NamingException
   {
      StatefulLocalBusiness bean = lookupLocalSfsb();
      bean.remove();
      
      assertEquals("Wrong number of PreDestroy invocations", 1, StatefulBean.preDestroys);
   }
}
