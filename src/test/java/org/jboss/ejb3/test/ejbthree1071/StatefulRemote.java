/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1071;

import javax.ejb.EJBObject;

/**
 * A StatefulRemote.
 * 
 * @author <a href="andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
public interface StatefulRemote extends EJBObject
{
   public static final String JNDI_NAME_REMOTE = "StatefulRemoteBean/remote";
}
