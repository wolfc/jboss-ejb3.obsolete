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
package org.jboss.ejb3.test.spec_3_2_1.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.test.spec_3_2_1.MyRemote;
import org.jboss.ejb3.test.spec_3_2_1.MyStatefulBean;
import org.jboss.ejb3.test.spec_3_2_1.MyStatelessBean;
import org.jboss.ejb3.test.spec_3_2_1.SimplePojo;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.After;
import org.junit.Test;

/**
 * IntraJvmRemoteInvocationPassByValueTestCase
 * 
 * Tests for EJB 3.0 Core Specification 3.2.1:
 * 
 * "The arguments and results of the methods of 
 * the remote business interface are passed by value."
 * 
 * EJBTHREE-1401
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class IntraJvmRemoteInvocationPassByValueTestCase extends AbstractEJB3TestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(IntraJvmRemoteInvocationPassByValueTestCase.class);

   /**
    * The session container for the beans that are deployed during the tests  
    */
   private SessionContainer sessionContainer;

   /**
    * This method takes care of any cleanup required after each test.
    */
   @After
   public void cleanupAfterEachTest()
   {
      if (this.sessionContainer != null)
      {
         undeployEjb(sessionContainer);
      }
   }

   /**
    * Test that the params that are passed to the method of a remote SLS bean
    * are passed by value.
    *  
    * @throws Throwable
    */
   @Test
   public void testPassByValueForMethodParametersForSlsb() throws Throwable
   {

      this.sessionContainer = deploySessionEjb(MyStatelessBean.class);

      // get metadata
      JBossSessionBeanMetaData metadata = this.sessionContainer.getMetaData();

      // Lookup the remote bean
      Context ctx = new InitialContext();
      MyRemote remote = (MyRemote) ctx.lookup(metadata.getJndiName());

      testPassByValueForMethodParameters(remote);

   }

   /**
    * Tests that the returned object from the remote SLS bean method is 
    * passed by value.
    * 
    * @throws Throwable
    */
   @Test
   public void testPassByValueForReturnedObjectSlsb() throws Throwable
   {
      this.sessionContainer = deploySessionEjb(MyStatelessBean.class);

      // get metadata
      JBossSessionBeanMetaData metadata = this.sessionContainer.getMetaData();

      // Lookup the remote bean
      Context ctx = new InitialContext();
      MyRemote remote = (MyRemote) ctx.lookup(metadata.getJndiName());

      testPassByValueForReturnedObject(remote);

   }

   /**
    * Test that the params that are passed to the method of a remote SFS bean
    * are passed by value.
    *  
    * @throws Throwable
    */
   @Test
   public void testPassByValueForMethodParametersForSfsb() throws Throwable
   {

      this.sessionContainer = deploySessionEjb(MyStatefulBean.class);

      // get metadata
      JBossSessionBeanMetaData metadata = this.sessionContainer.getMetaData();

      // Lookup the remote bean
      Context ctx = new InitialContext();
      MyRemote remote = (MyRemote) ctx.lookup(metadata.getJndiName());

      testPassByValueForMethodParameters(remote);

   }

   /**
    * Tests that the returned object from the remote SFS bean method is 
    * passed by value.
    * 
    * @throws Throwable
    */
   @Test
   public void testPassByValueForReturnedObjectSfsb() throws Throwable
   {
      this.sessionContainer = deploySessionEjb(MyStatefulBean.class);

      // get metadata
      JBossSessionBeanMetaData metadata = this.sessionContainer.getMetaData();

      // Lookup the remote bean
      Context ctx = new InitialContext();
      MyRemote remote = (MyRemote) ctx.lookup(metadata.getJndiName());

      testPassByValueForReturnedObject(remote);

   }

   /**
    * Utility method for testing that the returned object from a remote bean
    * method is passed by value
    * 
    * Important note on how this works:
    *   1) This method invokes the {@link MyRemote#getPojo()} method on the bean
    *   2) The {@link MyRemote#getPojo()} creates a new {@link SimplePojo} and 
    *       sets the value of a "transient" field of type 'long' to non-zero
    *   3) This new {@link SimplePojo} is then returned back to this method
    *   4) This method then checks the transient field value in the pojo that was returned
    *   5) Since transient fields are NOT serialized/deserialized, its expected that this
    *       field will have a value 0 (= default 'long' value) and NOT the value which was
    *       set during the construction of the pojo. The test succeeds if the transient field
    *       value is 0 (which indicates that the pojo was serialized while being returned), else
    *       it fails.
    *      
    * 
    * @param remote
    * @throws Throwable
    */
   private void testPassByValueForReturnedObject(MyRemote remote) throws Throwable
   {
      SimplePojo returnedPojo = remote.getPojo();
      log.info("Returned object has value = " + returnedPojo.getTransientField());
      assertTrue("The object returned from the remote bean is passed by reference",
            returnedPojo.getTransientField() == 0);

   }

   /**
    * Utility method for testing that the params passed to the remote bean
    * method are passed by value
    * 
    * Important note on how this works:
    *   1) Creates a {@link SimplePojo} and sets the value of the "transient" field of type 'long'
    *       to some non-zero value
    *   2) Then invokes the {@link MyRemote#changeAndReturn(SimplePojo)} and {@link MyRemote#doNothingAndReturn(SimplePojo)}
    *       methods on the bean, passing it the pojo which was created earlier
    *   3) The returned pojo is then checked with the local instance for object identity check (which is expected not to be equal)
    *   4) The returned pojo is also checked for the transient field value.
    *       Since transient fields are NOT serialized/deserialized, its expected that this
    *       field will have a value 0 (= default 'long' value) and NOT the value which was
    *       set in this testcase.
    * 
    * @param remote
    * @throws Throwable
    */
   private void testPassByValueForMethodParameters(MyRemote remote) throws Throwable
   {
      SimplePojo localSimplePojo = new SimplePojo();

      // set some initial value
      int value = 3;
      localSimplePojo.setTransientField(value);
      // call the method on the bean
      SimplePojo returnedPojo = remote.changeAndReturn(localSimplePojo);

      // The passed and the returned objects should not be the same
      assertFalse(
            "The object passed to the method of the remote bean and the object returned from the bean are both the same",
            localSimplePojo == returnedPojo);

      // Any value changed in the method of the remote bean should not 
      // affect the local object - Confirms pass by value
      assertEquals("The object passed to a method of remote bean was modified(passed by reference)", localSimplePojo
            .getTransientField(), value);

      SimplePojo anotherPojo = new SimplePojo();
      SimplePojo returnedObj = remote.doNothingAndReturn(anotherPojo);

      // The passed and the returned objects should not be the same
      assertFalse(
            "The object passed to the method of the remote bean and the object returned from the bean are both the same",
            anotherPojo == returnedObj);

   }

}
