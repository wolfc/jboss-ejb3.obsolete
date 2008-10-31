/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1075.remoteonly;

import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.test.ejbthree1075.homeonly.RemoteHomeOnly21Business;

/**
 * A RemoteOnly21Bean.
 * 
 * This EJB should fail deployment as a remote interface is defined, but no remote home is. 
 * 
 * @author <a href="andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
@Stateless
@javax.ejb.Remote(
{RemoteInterfaceOnly21.class, RemoteInterfaceOnly21Business.class})
@RemoteBinding(jndiBinding = RemoteHomeOnly21Business.JNDI_NAME_REMOTE)
public class RemoteInterfaceOnly21Bean implements RemoteInterfaceOnly21Business
{

   public void test()
   {
   }

}
