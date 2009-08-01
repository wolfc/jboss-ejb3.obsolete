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
package org.jboss.ejb3.nointerface.test.viewcreator.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.ejb3.nointerface.factory.MCAwareStatefulNoInterfaceViewFactory;
import org.jboss.ejb3.nointerface.test.common.AbstractNoInterfaceTestCase;
import org.jboss.ejb3.nointerface.test.viewcreator.ChildBean;
import org.jboss.ejb3.nointerface.test.viewcreator.SimpleSFSBeanWithoutInterfaces;
import org.jboss.ejb3.nointerface.test.viewcreator.SimpleSLSBWithoutInterface;
import org.jboss.ejb3.nointerface.test.viewcreator.StatefulBeanWithInterfaces;
import org.jboss.ejb3.nointerface.test.viewcreator.StatefulLocalBeanWithInterfaces;
import org.jboss.ejb3.nointerface.test.viewcreator.StatelessBeanWithInterfaces;
import org.jboss.ejb3.nointerface.test.viewcreator.StatelessLocalBeanWithInterfaces;
import org.jboss.ejb3.test.common.MetaDataHelper;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * NoInterfaceEJBViewCreatorTestCase
 *
 * Tests the no-inteface view for beans. The test cases here mainly
 * ensure that the no-interface view is created and bound. More rigorous
 * testing of the no-interface view will be done in the "profile3_1" environment.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceEJBViewCreatorTestCase extends AbstractNoInterfaceTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceEJBViewCreatorTestCase.class);

   /**
    * Starts the bootstrap and deploys the test classes
    *
    * @throws Exception
    */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractNoInterfaceTestCase.bootstrap();
      AbstractNoInterfaceTestCase.startServerConfiguration();
      deploy(getTestClassesURL());
   }

   /**
    * Shutdown
    * @throws Exception
    */
   @AfterClass
   public static void afterClass() throws Exception
   {
      shutdown();
   }

   /**
    * Ensure that the no-interface view for an SLSB is bound to jndi
    *
    * @throws Exception
    */
   @Test
   public void testSLSBNoInterfaceBinding() throws Exception
   {

      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSLSBWithoutInterface.class);

      // Right now, the no-interface view is bound to the ejb-name/no-interface
      String noInterfaceJndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      assertBoundAndOfExpectedType(new InitialContext(), noInterfaceJndiName, SimpleSLSBWithoutInterface.class);

   }

   /**
    * Ensure that the no-interface view (and corresponding factory) for an SFSB are bound to jndi
    *
    * @throws Exception
    */
   @Test
   public void testSFSBNoInterfaceBindings() throws Exception
   {
      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSFSBeanWithoutInterfaces.class);
      Context ctx = new InitialContext();

      // Right now, the no-interface view is bound to the ejb-name/no-interface
      String noInterfaceJndiName = sessionBeanMetadata.getEjbName() + "/no-interface";
      String statefulProxyFactoryJndiName = sessionBeanMetadata.getEjbName() + "/no-interface-stateful-proxyfactory";

      // check the proxy factory
      assertBoundAndOfExpectedType(ctx, statefulProxyFactoryJndiName, MCAwareStatefulNoInterfaceViewFactory.class);

      // check the view
      assertBoundAndOfExpectedType(ctx, noInterfaceJndiName, SimpleSFSBeanWithoutInterfaces.class);
   }

   /**
   * Test that the no-interface view works as expected when the bean extends from some other class
   *
   * @throws Exception
   */
   @Test
   public void testNoInterfaceViewForInheritedBean() throws Exception
   {

      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper.getMetadataFromBeanImplClass(ChildBean.class);
      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      // ensure that the bean is bound
      Context ctx = new InitialContext();
      assertBoundAndOfExpectedType(ctx, jndiName, ChildBean.class);

   }

   /**
    * Test to ensure that the no-interface view instance does NOT consider
    * a final method on the bean while creating the view
    *
    * @throws Exception
    */
   @Test
   public void testFinalMethodsAreNotConsideredInView() throws Exception
   {
      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSLSBWithoutInterface.class);
      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      // let's just assume the bean is bound and is of the expected type.
      // there are other tests which test the correctness of the bindings.
      Context ctx = new InitialContext();
      SimpleSLSBWithoutInterface bean = (SimpleSLSBWithoutInterface) ctx.lookup(jndiName);

      // Nothing fancy to test - just ensure that the declared methods in the proxy does
      // NOT contain a "final" method. Just check on method name, should be enough
      Method[] declaredMethods = bean.getClass().getDeclaredMethods();
      for (Method declaredMethod : declaredMethods)
      {
         if (declaredMethod.getName().equals("someFinalMethod"))
         {
            fail("No-interface view has overriden a final method. It shouldn't have.");
         }
      }

   }

   /**
   * Test to ensure that the no-interface view instance does NOT consider
   * a static method on the bean while creating the view
   *
   * @throws Exception
   */
   @Test
   public void testStaticMethodsAreNotConsideredInView() throws Exception
   {
      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSFSBeanWithoutInterfaces.class);
      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      // let's just assume the bean is bound and is of the expected type.
      // there are other tests which test the correctness of the bindings.
      Context ctx = new InitialContext();
      SimpleSFSBeanWithoutInterfaces bean = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);

      // Nothing fancy to test - just ensure that the declared methods in the proxy does
      // NOT contain a "final" method. Just check on method name, should be enough
      Method[] declaredMethods = bean.getClass().getDeclaredMethods();
      for (Method declaredMethod : declaredMethods)
      {
         if (declaredMethod.getName().equals("someStaticMethod"))
         {
            fail("No-interface view has overriden a static method. It shouldn't have.");
         }
      }
   }

   /**
    * The spec says that if the bean has been marked as local bean using
    * @LocalBean, then even if it implements any other interfaces, a no-interface view must
    * be created. This test case ensures that this requirement is handled correctly
    *
    * @throws Exception
    */
   @Test
   public void testLocalBeanWithInterfaces() throws Exception
   {
      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(StatelessLocalBeanWithInterfaces.class);
      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      Context ctx = new InitialContext();
      assertBoundAndOfExpectedType(ctx, jndiName, StatelessLocalBeanWithInterfaces.class);

   }

   /**
    * Test to ensure that a no-interface view is NOT created for session beans
    * which implement an interface (and do not explicitly mark themselves @LocalBean)
    *
    * @throws Exception
    */
   @Test
   public void testBeanWithInterfacesIsNotEligibleForNoInterfaceView() throws Exception
   {
      JBossSessionBeanMetaData slsbMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(StatelessBeanWithInterfaces.class);
      String slsbNoInterfaceViewJNDIName = slsbMetadata.getEjbName() + "/no-interface";

      JBossSessionBeanMetaData sfsbMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(StatefulBeanWithInterfaces.class);
      String sfsbNoInterfaceViewJNDIName = sfsbMetadata.getEjbName() + "/no-interface";
      String sfsbNoInterfaceViewFactoryJNDIName = sfsbMetadata.getEjbName() + "/no-interface-stateful-proxyfactory";

      Context ctx = new InitialContext();
      // we have to ensure that there is NO no-interface view for these beans (because they are not eligible)
      try
      {
         Object obj = ctx.lookup(slsbNoInterfaceViewJNDIName);
         // this is a failure because there should not be a no-interface view for these beans
         fail("A SLSB with interfaces was marked as eligible for no-interface view. Shouldn't have been. Found object of type "
               + obj.getClass() + " in the jndi for jndiname " + slsbNoInterfaceViewJNDIName);
      }
      catch (NameNotFoundException nnfe)
      {
         // expected
      }

      // now for sfsb, test that neither the factory nor the view are NOT bound

      // test factory binding
      try
      {
         Object obj = ctx.lookup(sfsbNoInterfaceViewFactoryJNDIName);
         // this is a failure because there should not be a no-interface view for these beans
         fail("A SFSB factory for no-interface view was created for a bean implementing interfaces. Shouldn't have been. Found object of type "
               + obj.getClass() + " in the jndi for jndiname " + sfsbNoInterfaceViewFactoryJNDIName);
      }
      catch (NameNotFoundException nnfe)
      {
         // expected
      }
      // sfsb no-interface view
      try
      {
         Object obj = ctx.lookup(sfsbNoInterfaceViewJNDIName);
         // this is a failure because there should not be a no-interface view for these beans
         fail("A no-interface view for SFSB was created for a bean implementing interfaces. Shouldn't have been. Found object of type "
               + obj.getClass() + " in the jndi for jndiname " + sfsbNoInterfaceViewJNDIName);
      }
      catch (NameNotFoundException nnfe)
      {
         // expected
      }

   }

   /**
    * Test that invocations on no-interface view of a SLSB work as expected
    *
    * @throws Exception
    */
   @Test
   public void testInvocationOnSLSBNoInterfaceView() throws Exception
   {
      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSLSBWithoutInterface.class);
      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      Context ctx = new InitialContext();
      assertBoundAndOfExpectedType(ctx, jndiName, SimpleSLSBWithoutInterface.class);
      // at this point we are sure we will get the no-interface view
      SimpleSLSBWithoutInterface bean = (SimpleSLSBWithoutInterface) ctx.lookup(jndiName);

      // invoke a method
      String name = "jai";
      String returnedMessage = bean.sayHi(name);
      assertNotNull("Invocation on no-interface view for SLSB returned a null message", returnedMessage);
      assertEquals("Invocation on no-interface view method of SLSB returned an unexpected return message",
            returnedMessage, "Hi " + name);

      JBossSessionBeanMetaData childBeanMetadata = MetaDataHelper.getMetadataFromBeanImplClass(ChildBean.class);
      String childBeanJndiName = childBeanMetadata.getEjbName() + "/no-interface";
      assertBoundAndOfExpectedType(ctx, childBeanJndiName, ChildBean.class);
      // at this point we are sure we will get the no-interface view
      ChildBean childBean = (ChildBean) ctx.lookup(childBeanJndiName);

      // invoke method
      int returnedNumber = childBean.echoNumberFromChild(10);
      assertEquals("Method invocation on child bean returned incorrect value", returnedNumber, 10);

   }

   /**
    * Test that invocations on no-interface view of SFSB work as expected
    *
    * @throws Exception
    */
   @Test
   public void testInvocationOnSFSBNoInterfaceView() throws Exception
   {
      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSFSBeanWithoutInterfaces.class);
      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      Context ctx = new InitialContext();
      assertBoundAndOfExpectedType(ctx, jndiName, SimpleSFSBeanWithoutInterfaces.class);
      // at this point we are sure we will get the no-interface view
      SimpleSFSBeanWithoutInterfaces bean = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);

      // invoke a method
      int qty = bean.getQtyPurchased();
      assertEquals("Invocation on no-interface view method of SFSB returned an unexpected return value", qty,
            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY);

   }

   /**
    * Test that sessions are created as expected for stateful session beans
    *
    * @throws Exception
    */
   @Test
   public void testSessionCreationForSFSBNoInterfaceViews() throws Exception
   {
      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSFSBeanWithoutInterfaces.class);
      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";

      Context ctx = new InitialContext();
      // let's assume the lookup returns the correct type.
      // there are other test cases to ensure it does return the correct type
      SimpleSFSBeanWithoutInterfaces firstSFSB = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);
      // ensure this is a clean bean
      int initQty = firstSFSB.getQtyPurchased();
      assertEquals("SFSB instance is not new", initQty, SimpleSFSBeanWithoutInterfaces.INITIAL_QTY);
      // now change the state of the sfsb instance
      firstSFSB.incrementPurchaseQty();
      int incrementedValueForFirstSFSB = firstSFSB.getQtyPurchased();
      assertEquals("SFSB instance's value not incremented", incrementedValueForFirstSFSB,
            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY + 1);

      // now lookup another bean
      SimpleSFSBeanWithoutInterfaces secondSFSB = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);
      // ensure this is a clean bean
      int initQtyForSecondBeanInstance = secondSFSB.getQtyPurchased();
      assertEquals("Second instance of SFSB is not new", initQtyForSecondBeanInstance,
            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY);
      // now change the state of the sfsb instance by some x amount
      int incrementBy = 10;
      secondSFSB.incrementPurchaseQty(incrementBy);
      int incrementedValueForSecondSFSB = secondSFSB.getQtyPurchased();
      assertEquals("Second SFSB instance's value not incremented", incrementedValueForSecondSFSB,
            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY + incrementBy);

      // let's also (again) check that the first SFSB still has it's own values and hasn't been
      // affected by changes made to second SFSB
      assertEquals("Value in first SFSB was changed when second SFSB was being modified", incrementedValueForFirstSFSB,
            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY + 1);

      // also check equality of two sfsb instances - they should not be equal
      assertFalse("Both the instances of the SFSB are the same", firstSFSB.equals(secondSFSB));

      // let's also check whether the bean is equal to itself
      assertTrue("Incorrect equals implementation - returns false for the same sfsb instance", firstSFSB
            .equals(firstSFSB));
      assertTrue("equals returned false for the same sfsb instance", secondSFSB.equals(secondSFSB));

   }

   /**
    * Tests that the {@link Object#equals(Object)} method on a no-interface view of a SLSB, behaves
    * as per the contract
    *
    * @throws Exception
    */
   @Test
   public void testEqualsOnSLSBNoInterfaceView() throws Exception
   {
      JBossSessionBeanMetaData slsbMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSLSBWithoutInterface.class);
      String jndiName = slsbMetadata.getEjbName() + "/no-interface";

      Context ctx = new InitialContext();
      assertBoundAndOfExpectedType(ctx, jndiName, SimpleSLSBWithoutInterface.class);
      // at this point we are sure we will get the no-interface view
      SimpleSLSBWithoutInterface slsbOne = (SimpleSLSBWithoutInterface) ctx.lookup(jndiName);

      SimpleSLSBWithoutInterface slsbTwo = (SimpleSLSBWithoutInterface) ctx.lookup(jndiName);

      // compare two instances of no-interface view of the same bean
      assertEquals("Two no-interface views for the same SLSB are not equal", slsbOne, slsbTwo);

      // compare against null
      assertFalse("NULL and a no-interface view should not be \"equal\"", slsbOne.equals(null));

      // compare with itself
      assertTrue("equals() on the same instance of no-interface view for SLSB should always be true", slsbTwo
            .equals(slsbTwo));

      // another different SLSB
      JBossSessionBeanMetaData anotherSLSBMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(StatelessLocalBeanWithInterfaces.class);
      String oneMoreSLSBJndiName = anotherSLSBMetadata.getEjbName() + "/no-interface";

      assertBoundAndOfExpectedType(ctx, oneMoreSLSBJndiName, StatelessLocalBeanWithInterfaces.class);
      // at this point we are sure we will get the no-interface view
      StatelessLocalBeanWithInterfaces differentSLSB = (StatelessLocalBeanWithInterfaces) ctx
            .lookup(oneMoreSLSBJndiName);

      // compare no-interface views of 2 different SLS beans
      assertFalse("no-interfaces views of different SLSB classes should not be equal", slsbOne.equals(differentSLSB));

      // SFSB
      JBossSessionBeanMetaData sfsbMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSFSBeanWithoutInterfaces.class);
      String sfsbJndiName = sfsbMetadata.getEjbName() + "/no-interface";

      assertBoundAndOfExpectedType(ctx, sfsbJndiName, SimpleSFSBeanWithoutInterfaces.class);
      // at this point we are sure we will get the no-interface view
      SimpleSFSBeanWithoutInterfaces sfsb = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(sfsbJndiName);

      // compare a no-interface view of SLSB against no-interface view of SFSB
      assertFalse("no-interface view of SLSB should not be equal to no-interface view of SFSB", slsbTwo.equals(sfsb));

   }

   /**
    * Tests that the {@link Object#equals(Object)} method on a no-interface view of a SFSB, behaves
    * as per the contract
    *
    * @throws Exception
    */
   @Test
   public void testEqualsOnSFSBNoInterfaceView() throws Exception
   {
      JBossSessionBeanMetaData sfsbMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSFSBeanWithoutInterfaces.class);
      String jndiName = sfsbMetadata.getEjbName() + "/no-interface";

      Context ctx = new InitialContext();
      assertBoundAndOfExpectedType(ctx, jndiName, SimpleSFSBeanWithoutInterfaces.class);
      // at this point we are sure we will get the no-interface view
      SimpleSFSBeanWithoutInterfaces sfsbOne = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);

      SimpleSFSBeanWithoutInterfaces sfsbTwo = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);

      // compare two instances of no-interface view of the same bean
      assertFalse("Two no-interface views/sessions for the same SFSB are equal", sfsbOne.equals(sfsbTwo));

      // compare against null
      assertFalse("NULL and a no-interface view should not be \"equal\"", sfsbOne.equals(null));

      // compare with itself
      assertTrue("equals() on the same instance of no-interface view for SFSB should always be true", sfsbTwo
            .equals(sfsbTwo));

      // another different SFSB
      JBossSessionBeanMetaData anotherSFSBMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(StatefulLocalBeanWithInterfaces.class);
      String oneMoreSFSBJndiName = anotherSFSBMetadata.getEjbName() + "/no-interface";

      assertBoundAndOfExpectedType(ctx, oneMoreSFSBJndiName, StatefulLocalBeanWithInterfaces.class);
      // at this point we are sure we will get the no-interface view
      StatefulLocalBeanWithInterfaces differentSFSB = (StatefulLocalBeanWithInterfaces) ctx.lookup(oneMoreSFSBJndiName);

      // compare no-interface views of 2 different SFS beans
      assertFalse("no-interfaces views of different SFSB classes should not be equal", sfsbOne.equals(differentSFSB));

      // SLSB
      JBossSessionBeanMetaData slsbMetadata = MetaDataHelper
            .getMetadataFromBeanImplClass(SimpleSLSBWithoutInterface.class);
      String slsbJndiName = slsbMetadata.getEjbName() + "/no-interface";

      assertBoundAndOfExpectedType(ctx, slsbJndiName, SimpleSLSBWithoutInterface.class);
      // at this point we are sure we will get the no-interface view
      SimpleSLSBWithoutInterface slsb = (SimpleSLSBWithoutInterface) ctx.lookup(slsbJndiName);

      // compare a no-interface view of SFSB against no-interface view of SLSB
      assertFalse("no-interface view of SFSB should not be equal to no-interface view of SLSB", sfsbOne.equals(slsb));

   }

   /**
    * Utility method for testing that the bean is bound at the <code>jndiName</code>
    * and is of the <code>expectedType</code>
    *
    * @param ctx JNDI Context
    * @param jndiName The jndiname to lookup
    * @param expectedType The object returned from the jndi will be expected to be of this type
    *
    * @throws Exception
    */
   private void assertBoundAndOfExpectedType(Context ctx, String jndiName, Class<?> expectedType) throws Exception
   {
      logger.debug("Looking up " + jndiName + " expectedType " + expectedType.getName());
      Object bean = ctx.lookup(jndiName);

      assertNotNull("No-interface view for " + expectedType + " returned null from JNDI name " + jndiName, bean);

      assertTrue("No-interface view at jndiname " + jndiName + " for " + expectedType + " is not an instance of  "
            + expectedType.getName(), expectedType.isAssignableFrom(bean.getClass()));

   }
}
