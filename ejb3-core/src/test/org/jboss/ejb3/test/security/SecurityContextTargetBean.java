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

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@Stateless(name="UserInRoleContextSessionTarget")
@Remote(org.jboss.ejb3.test.security.SecurityContext.class)
@RemoteBinding(jndiBinding = "spec.UserInRoleContextSessionTarget")
@SecurityDomain("spec-test-domain2")
@PermitAll
public class SecurityContextTargetBean 
{
   static Logger log = Logger.getLogger(SecurityContextTargetBean.class);
   
   @Resource  SessionContext sessionContext;

   public void testDomainInteraction(Set expectedRoles)
   {
      // Validate that caller has the expected roles
      validateRoles(expectedRoles, true);
      // Access a bean from another security-domain
      try
      {
         InitialContext ctx = new InitialContext();
         SecurityContext bean = (SecurityContext)ctx.lookup(Container.ENC_CTX_NAME + "/env/ejb/CalledBean");
         bean.nestedInteraction(expectedRoles);
      }
      catch(Exception e)
      {
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
