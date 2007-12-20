/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1071;

/**
 * A Delegate.
 * 
 * @author <a href="andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
public interface Delegate
{
   public static final String JNDI_NAME_REMOTE = "DelebateBean/remote";
   
   StatefulLocal getBean();
}
