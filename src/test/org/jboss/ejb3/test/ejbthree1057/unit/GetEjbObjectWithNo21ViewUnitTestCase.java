/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1057.unit;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1057.DelegateBusinessRemote;
import org.jboss.ejb3.test.ejbthree1057.TestBusinessRemote;
import org.jboss.test.JBossTestCase;

/**
 * A GetEjbObjectWithNo21ViewUnitTestCase.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
public class GetEjbObjectWithNo21ViewUnitTestCase extends JBossTestCase
{

   // Constructor

   public GetEjbObjectWithNo21ViewUnitTestCase(String name)
   {
      super(name);
   }

   // Test Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(GetEjbObjectWithNo21ViewUnitTestCase.class, "ejbthree1057.jar");
   }

   // Tests

   /**
    * Ensures that a call to SessionContext.getEJBObject fails when 
    * no EJB 2.1 view is defined
    */
   public void testGetEjbObjectWithNo21ViewFails() throws Exception
   {
      // Obtain test bean
      TestBusinessRemote bean = null;
      try
      {
         bean = (TestBusinessRemote) this.getInitialContext().lookup(TestBusinessRemote.JNDI_NAME_REMOTE);
      }
      catch (NameNotFoundException nnfe)
      {
         log.error("Could not obtain " + TestBusinessRemote.class.getName() + " from expected location "
               + TestBusinessRemote.JNDI_NAME_REMOTE);
      }

      // Attempt to obtain EJBObject
      EJBObject obj = null;
      try
      {
         obj = bean.testGetEjbObject();
      }
      catch (EJBException ejbe)
      {
         if (ejbe.getCause() instanceof IllegalStateException)
         {
            log.info("Expected exception " + ejbe.getClass().getName() + " encountered.");
            return;
         }
      }

      // Should not be reached; fail the test
      fail("Should have received exception while attempting to invoke SessionContext.getEJBObject().");
   }

   /**
    * Ensures that a call to SessionContext.getEJBLocalObject fails when 
    * no EJB 2.1 view is defined
    */
   public void testGetEjbLocalObjectWithNo21ViewFails() throws Exception
   {
      // Obtain test bean
      DelegateBusinessRemote bean = null;
      try
      {
         bean = (DelegateBusinessRemote) this.getInitialContext().lookup(DelegateBusinessRemote.JNDI_NAME_REMOTE);
      }
      catch (NameNotFoundException nnfe)
      {
         log.error("Could not obtain " + DelegateBusinessRemote.class.getName() + " from expected location "
               + DelegateBusinessRemote.JNDI_NAME_REMOTE);
      }

      // Attempt to obtain EJBObject
      EJBLocalObject obj = null;
      try
      {
         obj = bean.testGetEjbLocalObject();
      }
      catch (Exception e)
      {
         if (e.getCause() instanceof IllegalStateException)
         {
            log.info("Expected exception " + e.getClass().getName() + " encountered.");
            return;
         }
      }

      // Should not be reached; fail the test
      fail("Should have received exception while attempting to invoke SessionContext.getEJBLocalObject().");
   }

}
