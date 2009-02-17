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
 * A StatefulRemote.
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision$
 */
public interface StatefulRemote extends EJBObject
{
   String getState();
}
