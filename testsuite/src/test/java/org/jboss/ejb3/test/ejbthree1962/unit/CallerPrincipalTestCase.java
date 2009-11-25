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
package org.jboss.ejb3.test.ejbthree1962.unit;

import java.security.Principal;

import javax.ejb.SessionContext;

import junit.framework.Test;

import org.jboss.ejb3.security.helpers.EJBContextHelper;
import org.jboss.ejb3.test.ejbthree1962.CallerPrincipalNotAvailableException;
import org.jboss.ejb3.test.ejbthree1962.SessionBeanWithoutSecurityDomain;
import org.jboss.ejb3.test.ejbthree1962.UserManagerRemote;
import org.jboss.test.JBossTestCase;

/**
 * CallerPrincipalTestCase
 * 
 * Tests the fix for https://jira.jboss.org/jira/browse/EJBTHREE-1962
 * 
 * A NullPointerException was being thrown from {@link EJBContextHelper}, on a call to {@link SessionContext#getCallerPrincipal()}
 * when the bean was not configured with @SecurityDomain (or security-domain xml equivalent). 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class CallerPrincipalTestCase extends JBossTestCase
{

   /**
    * @param name
    */
   public CallerPrincipalTestCase(String name)
   {
      super(name);
   }

   /**
    * 
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(CallerPrincipalTestCase.class, "ejbthree1962.jar");
   }

   /**
    * Tests that in the absence of a @SecurityDomain (or security-domain xml equivalent)
    * on a bean, the call to {@link SessionContext#getCallerPrincipal()} doesn't fail
    * with a NullPointerException.
    * @see https://jira.jboss.org/jira/browse/EJBTHREE-1962
    * @throws Exception
    */
   public void testCallerPrincipalInAbsenceOfSecurityDomain() throws Exception
   {
      UserManagerRemote bean = (UserManagerRemote) this.getInitialContext().lookup(
            SessionBeanWithoutSecurityDomain.JNDI_NAME);
      try
      {
         Principal callerPrincipal = bean.getCallerPrincipal();
         fail("Caller principal was *not* associated, but no CallerPrincipalNotAvailableException was thrown");
      }
      catch (CallerPrincipalNotAvailableException cpnae)
      {
         // expected, since when no caller principal is associated an IllegalStateException is thrown
      }

   }
}
