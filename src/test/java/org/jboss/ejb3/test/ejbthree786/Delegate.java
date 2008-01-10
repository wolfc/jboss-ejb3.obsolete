/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree786;

import javax.ejb.Remote;

/**
 * A Delegate to invoke upon local views of the Remove EJBs
 * 
 * @author <a href="arubinge@redhat.com">ALR</a>
 * @version $Revision:  $
 */
@Remote
public interface Delegate
{
   String invokeStatelessRemove();

   String invokeStatefulRemove();
}
