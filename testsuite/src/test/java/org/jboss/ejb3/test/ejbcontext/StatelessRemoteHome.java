/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbcontext;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * 
 * A StatelessRemoteHome.
 * 
 * @author @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: 1.1 $
 */
public interface StatelessRemoteHome extends EJBHome
{
   String JNDI_NAME = "Stateless/home";
   
   StatelessRemote create() throws RemoteException, CreateException;
}
