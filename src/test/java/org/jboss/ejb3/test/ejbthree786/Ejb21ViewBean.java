/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree786;

import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteBinding;

@Stateful
@Remote(Ejb21View.class)
@RemoteHome(Ejb21ViewHome.class)
@RemoteBinding(jndiBinding = Ejb21ViewBean.JNDI_NAME_REMOTE)
public class Ejb21ViewBean
{

   // Class Members

   public static final String JNDI_NAME_REMOTE = "Ejb21ViewBean/remote";

   public static final String TEST_STRING = "Test";

   // Required Implementations

   public String test()
   {
      return Ejb21ViewBean.TEST_STRING;
   }

}
