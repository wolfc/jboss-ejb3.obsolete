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
package org.jboss.ejb3.test.ejbthree1962;

import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.ApplicationException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * SessionBeanWithoutSecurityDomain
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote (UserManagerRemote.class)
@RemoteBinding (jndiBinding = SessionBeanWithoutSecurityDomain.JNDI_NAME)
public class SessionBeanWithoutSecurityDomain implements UserManagerRemote
{

   public static final String JNDI_NAME = "ejbthree1962-slsb-without-security-domain";
   
   
   @Resource
   private SessionContext sessContext;
   
   /**
    * {@inheritDoc}
    */
   public Principal getCallerPrincipal() throws CallerPrincipalNotAvailableException
   {
      // as per the API, the getCallerPrincipal never returns null.
      // if there is no principal associated then an IllegalStateException is thrown
      try
      {
         return this.sessContext.getCallerPrincipal();
      }
      catch (IllegalStateException ise)
      {
         throw new CallerPrincipalNotAvailableException();
      }
      
   }

}
