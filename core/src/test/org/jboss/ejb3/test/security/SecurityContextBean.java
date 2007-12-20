/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ejb3.test.security;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 67628 $
 */
@Stateless(name="UserInRoleContextSession")
@Remote(org.jboss.ejb3.test.security.SecurityContext.class)
@RemoteBinding(jndiBinding = "spec.UserInRoleContextSession")
@SecurityDomain("spec-test")
@RolesAllowed({"Role1", "Role2"})
@EJBs({@EJB(name="CalledBean", beanInterface=org.jboss.ejb3.test.security.SecurityContext.class, beanName="UserInRoleContextSessionTarget")})
public class SecurityContextBean implements SecurityContext
{
   static Logger log = Logger.getLogger(SecurityContextBean.class);
   
   @Resource  SessionContext sessionContext;

   public void testDomainInteraction(Set expectedRoles)
   {
      // Validate that caller has the expected roles
      validateRoles(expectedRoles, true);
      // Access a bean from another security-domain
      try
      {
         InitialContext ctx = new InitialContext();
         SecurityContext bean = (SecurityContext)ctx.lookup(Container.ENC_CTX_NAME + "/env/CalledBean");
         bean.nestedInteraction(expectedRoles);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         SecurityException se = new SecurityException("DataSource connection failed");
         se.initCause(e);
         throw se;         
      }
      // Validate that caller still has the expected roles
      validateRoles(expectedRoles, true);
   }

   public void nestedInteraction(Set expectedRoles)
      throws SecurityException
   {
      validateRoles(expectedRoles, false);
   }

   /**
    * Validate that the current caller has every role from expectedRoles in the
    * context isCallerInRole set.
    * 
    * @param expectedRoles - Set<String> of the role names
    * @param isCallerInRoleFlag - Should isCallerInRole return true
    * @throws SecurityException - thrown if sessionContext.isCallerInRole(name)
    *    fails for any name in expectedRoles
    */ 
   private void validateRoles(Set expectedRoles, boolean isCallerInRoleFlag)
      throws SecurityException
   {
      Iterator names = expectedRoles.iterator();
      while( names.hasNext() )
      {
         String name = (String) names.next();
         boolean hasRole = sessionContext.isCallerInRole(name);
         if( hasRole != isCallerInRoleFlag )
         {
            throw new SecurityException("Caller does not have role: "+name);
         }
      }      
   }
}
