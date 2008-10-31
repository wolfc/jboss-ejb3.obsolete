/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree786;

import javax.ejb.RemoteHome;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteHomeBinding;

@Stateful
@RemoteHome(Ejb21ViewHome.class)
@RemoteHomeBinding(jndiBinding = Ejb21ViewHome.JNDI_NAME_REMOTE_HOME)
public class Ejb21ViewBean
{
   // Class Members

   public static final String TEST_STRING = "Test";

   // Required Implementations

   public String test()
   {
      return Ejb21ViewBean.TEST_STRING;
   }

}
