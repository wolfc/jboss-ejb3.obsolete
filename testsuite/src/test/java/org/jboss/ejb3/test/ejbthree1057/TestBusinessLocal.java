/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1057;

import javax.ejb.EJBLocalObject;

/**
 * A TestBusinessLocal.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
public interface TestBusinessLocal
{
   public static final String JNDI_NAME_LOCAL = "TestBean/local";
   
   EJBLocalObject testGetEjbLocalObject();
}
