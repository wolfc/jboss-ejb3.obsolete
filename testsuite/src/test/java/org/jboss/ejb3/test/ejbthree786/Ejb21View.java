/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree786;

import javax.ejb.EJBObject;
import javax.ejb.Remote;

/**
 * An Ejb21View.
 * 
 * @author <a href="arubinge@redhat.com">ALR</a>
 * @version $Revision:  $
 */
//@Remote
public interface Ejb21View extends EJBObject
{
   String test();
}
