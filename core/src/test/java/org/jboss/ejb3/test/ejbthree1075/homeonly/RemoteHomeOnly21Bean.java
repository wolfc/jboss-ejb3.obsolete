/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1075.homeonly;

import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * A RemoteHomeOnly21Bean.
 * 
 * This EJB should fail deployment as a remote home is defined, but no remote interface is. 
 * 
 * @author <a href="andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
@Stateless
@RemoteHome(RemoteHomeOnly21Home.class)
@Remote(RemoteHomeOnly21Business.class)
@RemoteBinding(jndiBinding=RemoteHomeOnly21Business.JNDI_NAME_REMOTE)
public class RemoteHomeOnly21Bean implements RemoteHomeOnly21Business
{

   public void test()
   {
   }

}
