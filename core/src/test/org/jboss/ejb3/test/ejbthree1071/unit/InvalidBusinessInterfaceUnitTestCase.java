/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1071.unit;

import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1071.Delegate;
import org.jboss.ejb3.test.ejbthree1071.StatefulRemote;
import org.jboss.ejb3.test.ejbthree785.unit.SuperBeanTesterUnitTestCase;
import org.jboss.test.JBossTestCase;

public class InvalidBusinessInterfaceUnitTestCase extends JBossTestCase
{

   // Constructor

   public InvalidBusinessInterfaceUnitTestCase(String name)
   {
      super(name);
   }

   // Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(InvalidBusinessInterfaceUnitTestCase.class, "ejbthree1071.jar");
   }

   // Test

   /**
    * Ensure that a remote business interface extending EJBObject does not deploy
    */
   public void testBusinessRemoteInterfaceExtendsEjbObject() throws Exception
   {
      try
      {
         // Attempt to access a bean that should have failed on deploy
         getInitialContext().lookup(StatefulRemote.JNDI_NAME_REMOTE);
      }
      catch (NameNotFoundException nnfe)
      {
         // Expected
         return;
      }
      // Should not reach this point
      fail(StatefulRemote.class.getName() + " should not have deployed or be available in JNDI at "
            + StatefulRemote.JNDI_NAME_REMOTE);
   }

   /**
    * Ensure that a local business interface extending EJBLocalObject does not 
    * deploy, via Remote delegate with dependency
    */
   public void testBusinessLocalInterfaceExtendsEjbLocalObject() throws Exception
   {
      try
      {
         // Attempt to access a bean that should have failed on deploy
         getInitialContext().lookup(Delegate.JNDI_NAME_REMOTE);
      }
      catch (NameNotFoundException nnfe)
      {
         // Expected
         return;
      }
      // Should not reach this point
      fail(Delegate.class.getName() + " should not have deployed or be available in JNDI at "
            + Delegate.JNDI_NAME_REMOTE);
   }

}
