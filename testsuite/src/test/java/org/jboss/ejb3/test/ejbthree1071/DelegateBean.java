/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1071;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * A StatefulRemoteBean.
 * 
 * @author <a href="andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
@Stateless
@Remote(Delegate.class)
@RemoteBinding(jndiBinding = Delegate.JNDI_NAME_REMOTE)
public class DelegateBean implements Delegate
{
   // Instance Members
   @EJB
   StatefulLocal bean;

   public StatefulLocal getBean()
   {
      return bean;
   }

}
