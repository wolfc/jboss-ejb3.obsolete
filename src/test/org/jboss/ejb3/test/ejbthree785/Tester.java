/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree785;

import javax.ejb.Remote;

import org.jboss.ejb3.annotation.RemoteBinding;

@Remote
@RemoteBinding(jndiBinding = Tester.JNDI_NAME_REMOTE)
public interface Tester
{
   public static final String JNDI_NAME_REMOTE = "TesterBean/remote";

   String sayHiTo(String name);
}
