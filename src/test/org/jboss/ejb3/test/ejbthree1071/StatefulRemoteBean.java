/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1071;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * 
 * A StatefulRemoteBean.
 * 
 * @author <a href="andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
@Stateful
@Remote(StatefulRemote.class)
@RemoteBinding(jndiBinding = StatefulRemote.JNDI_NAME_REMOTE)
public class StatefulRemoteBean
{
}
