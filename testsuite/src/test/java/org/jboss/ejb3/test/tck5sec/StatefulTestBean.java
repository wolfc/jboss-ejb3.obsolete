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
package org.jboss.ejb3.test.tck5sec;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateful
@Remote(StatefulSessionTest.class)
@RunAs("Manager")
//@org.jboss.annotation.security.RunAsPrincipal("javajoe")
public class StatefulTestBean implements StatefulSessionTest
{
   private static final Logger log = Logger.getLogger(StatefulTestBean.class);

   @EJB
   private StatefulSession ejbRef;

   @Resource
   SessionContext sessionContext;
   
   @RolesAllowed({"Administrator", "Manager", "VP", "Employee"})
   public boolean EjbSecRoleRef(String role)
   {
      log.info("Starting Security role reference positive test");
      log.info("isCallerInRole(" + role + ")= "
            + sessionContext.isCallerInRole(role));
       try {
           boolean result = ejbRef.EjbSecRoleRef(role);
           if ( ! result )
               return false;
           return true;
       } catch ( Exception e ) {
             e.printStackTrace();
          return false; 
       }
   }

   @RolesAllowed( { "Administrator", "Manager", "VP", "Employee" })
   public boolean EjbOverloadedSecRoleRefs(String role1, String role2)
   {
      log.info("Starting Overloaded security role references test");
      
      log.info("isCallerInRole(" + role1 + ")= "
            + sessionContext.isCallerInRole(role1) + " isCallerInRole(" + role2
            + ")= " + sessionContext.isCallerInRole(role2));
      
      try
      {
         boolean result = ejbRef.EjbOverloadedSecRoleRefs(role1);
         if (!result)
         {
            log.info("EjbOverloadedSecRoleRefs(emp_secrole_ref) returned false");
            return false;
         }

         result = ejbRef.EjbOverloadedSecRoleRefs(role1, role2);
         if (result)
         {

            log.info("EjbOverloadedSecRoleRefs(emp_secrole_ref,mgr_secrole_ref) returned true");
            return false;
         }
         return true;
      } catch (Exception e)
      {
         log.info("EjbOverloadedSecRoleRefs(" + role1 + "," + role2
               + ") failed with Exception: ", e);
         return false;
      }
   }
}
