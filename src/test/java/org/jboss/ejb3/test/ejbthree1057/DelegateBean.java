/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1057;

import javax.ejb.EJB;
import javax.ejb.EJBLocalObject;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * A DelegateBean.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(DelegateBusinessRemote.class)
@RemoteBinding(jndiBinding = DelegateBusinessRemote.JNDI_NAME_REMOTE)
public class DelegateBean implements DelegateBusinessRemote
{

   // Instance Members
   @EJB
   TestBusinessLocal bean;

   // Required Implementations

   public EJBLocalObject testGetEjbLocalObject()
   {
      return bean.testGetEjbLocalObject();
   }

}
