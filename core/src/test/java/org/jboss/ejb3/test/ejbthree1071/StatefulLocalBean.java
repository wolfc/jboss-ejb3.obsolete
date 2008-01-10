/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1071;

import javax.ejb.Local;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.LocalBinding;

/**
 * 
 * A StatefulRemoteBean.
 * 
 * @author <a href="andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision:  $
 */
@Stateful
@Local(StatefulLocal.class)
@LocalBinding(jndiBinding = StatefulLocal.JNDI_NAME_LOCAL)
public class StatefulLocalBean
{
}
