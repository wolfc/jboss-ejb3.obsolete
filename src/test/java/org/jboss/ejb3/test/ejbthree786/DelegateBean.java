/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree786;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

@Stateless
@RemoteBinding(jndiBinding = DelegateBean.JNDI_NAME_REMOTE)
public class DelegateBean implements Delegate
{
   // Class Members
   public static final String JNDI_NAME_REMOTE = "DelegateBean/remote";

   // Instance Members

   @EJB
   private RemoveStatefulLocal stateful;

   @EJB
   private RemoveStatelessLocal stateless;

   // Required Implementations

   public String invokeStatefulRemove()
   {
      return stateful.remove();
   }

   public String invokeStatelessRemove()
   {
      return stateless.remove();
   }

}
