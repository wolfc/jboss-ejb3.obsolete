/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbcontext;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * 
 * A StatelessLocalHome.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: 1.1 $
 */
public interface StatelessLocalHome extends EJBLocalHome
{
   StatelessLocal create() throws CreateException;
}
