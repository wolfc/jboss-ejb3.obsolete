/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1057;

import javax.ejb.EJBLocalObject;

/**
 * 
 * A DelegateBusinessRemote.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com>ALR</a>
 * @version $Revision:  $
 */
public interface DelegateBusinessRemote
{
   public static String JNDI_NAME_REMOTE = "DelegateBean/remote";
   
   EJBLocalObject testGetEjbLocalObject();
}
