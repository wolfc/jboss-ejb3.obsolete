/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1075.unit;

import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1075.homeonly.RemoteHomeOnly21Bean;
import org.jboss.ejb3.test.ejbthree1075.homeonly.RemoteHomeOnly21Business;
import org.jboss.ejb3.test.ejbthree1075.remoteonly.RemoteInterfaceOnly21Business;
import org.jboss.ejb3.test.ejbthree1075.remoteonly.RemoteInterfaceOnly21Bean;
import org.jboss.test.JBossTestCase;

public class Incomplete21ViewUnitTestCase extends JBossTestCase
{

   // Constructor

   public Incomplete21ViewUnitTestCase(String name)
   {
      super(name);
   }

   // Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(Incomplete21ViewUnitTestCase.class, "ejbthree1075-homeonly.jar,ejbthree1075-remoteonly.jar");
   }

   // Test

   /**
    * Ensure that an EJB with remote home and no remote interface fails deployment
    */
   public void testRemoteHomeOnlyFails() throws Exception
   {
      try
      {
         // Attempt to access a bean that should have failed on deploy
         getInitialContext().lookup(RemoteHomeOnly21Business.JNDI_NAME_REMOTE);
      }
      catch (NameNotFoundException nnfe)
      {
         // Expected
         return;
      }
      // Should not reach this point
      fail(RemoteHomeOnly21Bean.class.getName() + " should not have deployed or be available in JNDI at "
            + RemoteHomeOnly21Business.JNDI_NAME_REMOTE);
   }

   /**
    * Ensure that an EJB with remote interfaces but no remote home fails deployment
    */
   public void testRemoteInterfaceOnlyFails() throws Exception
   {
      try
      {
         // Attempt to access a bean that should have failed on deploy
         getInitialContext().lookup(RemoteInterfaceOnly21Business.JNDI_NAME_REMOTE);
      }
      catch (NameNotFoundException nnfe)
      {
         // Expected
         return;
      }
      // Should not reach this point
      fail(RemoteInterfaceOnly21Bean.class.getName() + " should not have deployed or be available in JNDI at "
            + RemoteInterfaceOnly21Business.JNDI_NAME_REMOTE);
   }

}
