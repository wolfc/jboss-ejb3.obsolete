/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbcontext;

import javax.ejb.EJBObject;

/**
 * 
 * A StatelessRemote.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: 1.1 $
 */
public interface StatelessRemote extends EJBObject
{
   void noop();
   
   public Class<?> testInvokedBusinessInterface() throws Exception;
}
