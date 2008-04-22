/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.security.helpers;
 
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;


/**
 *  Privileged Blocks 
 *  @author Anil.Saldhana@redhat.com
 *  @since  May 19, 2007 
 *  @version $Revision$
 */
class SecurityActions
{ 
   static Principal getCallerPrincipal(final SecurityContext securityContext)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Principal>()
      {

         public Principal run()
         { 
            Principal caller = null;
            
            if(securityContext != null)
            {
               caller = securityContext.getIncomingRunAs(); 
               //If there is no caller run as, use the call principal
               if(caller == null)
                  caller = securityContext.getUtil().getUserPrincipal();
            }
            return caller;
         }
       });
   }
   
   static SecurityContext getSecurityContext()
   {
      return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>()
      {

         public SecurityContext run()
         { 
            return SecurityContextAssociation.getSecurityContext();
         }
      });
   }
   
   static Exception getContextException()
   {
      return AccessController.doPrivileged(new PrivilegedAction<Exception>()
      {
         static final String EX_KEY = "org.jboss.security.exception";
         public Exception run()
         { 
            SecurityContext sc = getSecurityContext();
            return (Exception) sc.getData().get(EX_KEY); 
         }
      });
   }
   
   static Subject getActiveSubject() throws PolicyContextException, PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Subject>()
      { 
         public Subject run() throws Exception
         { 
            return (Subject) PolicyContext.getContext(SecurityConstants.SUBJECT_CONTEXT_KEY); 
         }
      });    
   } 
   
   static SecurityContext createSecurityContext(final String securityDomain) 
   throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>()
      { 
         public SecurityContext run() throws Exception
         {
            return SecurityContextFactory.createSecurityContext(securityDomain);
         }});
   }
}