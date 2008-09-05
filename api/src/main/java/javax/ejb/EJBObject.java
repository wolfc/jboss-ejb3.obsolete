/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package javax.ejb;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <P>The EJBObject interface is extended by all enterprise Bean's remote
 * interface. An enterprise Bean's remote interface provides the client's
 * view of an EJB object. An enterprise Bean's remote interface defines
 * the business methods callable by a client.</P>
 * 
 * <P>Each enterprise Bean has a remote interface. The remote interface must
 * extend the javax.ejb.EJBObject interface, and define the enterprise Bean
 * specific business methods.</P>
 * 
 * <P>The enterprise Bean's remote interface is defined by the enterprise Bean
 * provider and implemented by the enterprise Bean container.</P>
 */
public interface EJBObject extends Remote {

  /**
   * Obtain the enterprise Bean's remote home interface. The remote home interface defines the
   * enterprise Bean's create, finder, remove, and home business methods.
   *
   * @return A reference to the enterprise Bean's home interface.
   * @exception java.rmi.RemoteException - Thrown when the method failed due to a system-level failure.
   */
  public EJBHome getEJBHome() throws RemoteException;

   /**
    * <P>Obtain the primary key of the EJB object.</P>
    *
    * <P>This method can be called on an entity bean. An attempt to invoke this method on a session
    * bean will result in RemoteException.</P>
    *
    * @return The EJB object's primary key.
    * @exception java.rmi.RemoteException - Thrown when the method failed due to a system-level failure.
    */
   public Object getPrimaryKey() throws RemoteException;

   /**
    * Remove the EJB object.
    *
    * @exception java.rmi.RemoteException - Thrown when the method failed due to a system-level failure.
    * @exception RemoveException - The enterprise Bean or the container does not allow destruction of the object.
    */
   public void remove() throws RemoteException, RemoveException;

  /**
   * Obtain a handle for the EJB object. The handle can be used at later time to re-obtain a
   * reference to the EJB object, possibly in a different Java Virtual Machine.
   *
   * @return A handle for the EJB object.
   * @exception java.rmi.RemoteException - Thrown when the method failed due to a system-level failure.
   */
  public Handle getHandle() throws RemoteException;

  /**
   * Tests if a given EJB object is identical to the invoked EJB object.
   *
   * @param ejbo - An object to test for identity with the invoked object.
   * @return True if the given EJB object is identical to the invoked object, false otherwise.
   * @exception java.rmi.RemoteException - Thrown when the method failed due to a system-level failure.
   */
  public boolean isIdentical(EJBObject ejbo) throws RemoteException;
}
