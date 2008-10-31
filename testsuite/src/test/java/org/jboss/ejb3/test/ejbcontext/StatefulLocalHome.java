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
 * A StatefulLocalHome.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: 1.1 $
 */
public interface StatefulLocalHome extends EJBLocalHome
{
   StatefulLocal create() throws CreateException;
}
