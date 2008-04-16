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

import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.Principal;
import java.util.HashMap;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.jboss.security.AuthorizationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAs;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRoleRef;
import org.jboss.security.audit.AuditLevel;
import org.jboss.security.authorization.AuthorizationContext;
import org.jboss.security.authorization.ResourceKeys;
import org.jboss.security.authorization.resources.EJBResource;
import org.jboss.security.callbacks.SecurityContextCallbackHandler;
import org.jboss.security.identity.RoleGroup;
import org.jboss.security.identity.plugins.SimpleRoleGroup;

/**
 *  Authorization Helper
 *  @author Anil.Saldhana@redhat.com
 *  @since  Apr 16, 2008 
 *  @version $Revision$
 */
public class AuthorizationHelper extends SecurityHelper
{     
   public AuthorizationHelper(SecurityContext sc)
   {
      super(sc); 
   }

   /**
    * Authorize an EJB Invocation
    * @param ejbName Name of the EJB
    * @param ejbMethod EJB Method
    * @param ejbPrincipal Calling Principal
    * @param invocationInterfaceString Invocation String("remote", "local")
    * @param ejbCS EJB CodeSource
    * @param callerSubject Authenticated Caller Subject
    * @param callerRunAs Configured RunAs for the caller
    * @param methodRoles a set of Principal objects authorized for the method
    * @return true - if caller is authorized
    */
   public boolean authorize(String ejbName, 
                            Method ejbMethod, 
                            Principal ejbPrincipal,
                            String invocationInterfaceString, 
                            CodeSource ejbCS, 
                            Subject callerSubject, 
                            RunAs callerRunAs,
                            Set<Principal> methodRoles )
   {
      AuthorizationManager am = securityContext.getAuthorizationManager();
      
      HashMap<String,Object> map =  new HashMap<String,Object>();
      map.put(ResourceKeys.POLICY_REGISTRATION, am); 
      
      String contextID = PolicyContext.getContextID();
      if(contextID == null)
         throw new IllegalStateException("ContextID is null"); 

      EJBResource ejbResource = new EJBResource(map);
      ejbResource.setPolicyContextID(contextID);
      ejbResource.setCallerRunAsIdentity(callerRunAs);
      ejbResource.setEjbName(ejbName);
      ejbResource.setEjbMethod(ejbMethod);
      ejbResource.setPrincipal(ejbPrincipal);
      ejbResource.setEjbMethodInterface(invocationInterfaceString);
      ejbResource.setCodeSource(ejbCS);
      ejbResource.setCallerRunAsIdentity(callerRunAs);
      ejbResource.setCallerSubject(callerSubject);
      //ejbResource.setMethodRoles(methodRoles);
      ejbResource.setEjbMethodRoles(new SimpleRoleGroup(methodRoles));
      
      SecurityContextCallbackHandler sch = new SecurityContextCallbackHandler(this.securityContext); 
      RoleGroup callerRoles = am.getSubjectRoles(callerSubject, sch);
      
      boolean isAuthorized = false;
      try
      {
         int check = am.authorize(ejbResource, callerSubject, callerRoles);
         isAuthorized = (check == AuthorizationContext.PERMIT);
         authorizationAudit((isAuthorized ? AuditLevel.SUCCESS : AuditLevel.FAILURE)
                             ,ejbResource, null);
      }
      catch (Exception e)
      {
         isAuthorized = false;
         if(log.isTraceEnabled())
            log.trace("Error in authorization:",e); 
         authorizationAudit(AuditLevel.ERROR,ejbResource,e);
      } 
      
      return isAuthorized;
   } 
   
   public Principal getCallerPrincipal(RealmMapping rm)
   {
      /* Get the run-as user or authenticated user. The run-as user is
      returned before any authenticated user.
      */
      Principal caller = SecurityActions.getCallerPrincipal(securityContext); 
       
      /* Apply any domain caller mapping. This should really only be
      done for non-run-as callers.
      */
      if (rm != null)
         caller = rm.getPrincipal(caller);
      return caller; 
   } 
   
   public boolean isCallerInRole(String roleName,String ejbName, Principal ejbPrincipal,
         Set<SecurityRoleRef> securityRoleRefs )
   {
      boolean isAuthorized = false;
      AuthorizationManager am = securityContext.getAuthorizationManager();
      
      if(am == null)
         throw new IllegalStateException("AuthorizationManager is null");
      
      HashMap<String,Object> map = new HashMap<String,Object>();

      map.put(ResourceKeys.POLICY_REGISTRATION,am); 
      map.put(ResourceKeys.ROLENAME, roleName);
      map.put(ResourceKeys.ROLEREF_PERM_CHECK, Boolean.TRUE);

      
      EJBResource ejbResource = new EJBResource(map);
      ejbResource.setPolicyContextID(PolicyContext.getContextID());
      
      RunAs callerRunAs = securityContext.getIncomingRunAs();
      
      ejbResource.setEjbName(ejbName);
      ejbResource.setPrincipal(ejbPrincipal);
      ejbResource.setCallerRunAsIdentity(callerRunAs);
      ejbResource.setSecurityRoleReferences(securityRoleRefs); 
      
      //Get the authenticated subject
      Subject subject = null;
      try
      {
         subject = SecurityActions.getActiveSubject();
      }
      catch( Exception e)
      {
         log.trace("Exception in getting subject:",e);
         subject = securityContext.getUtil().getSubject();
      }
      
      ejbResource.setCallerSubject(subject);
      SecurityContextCallbackHandler sch = new SecurityContextCallbackHandler(this.securityContext); 
      RoleGroup callerRoles = am.getSubjectRoles(subject, sch);
      
      try
      {
         int check = am.authorize(ejbResource, subject, callerRoles);
         isAuthorized = (check == AuthorizationContext.PERMIT);
      } 
      catch (Exception e)
      {
         isAuthorized = false; 
         if(log.isTraceEnabled()) 
            log.trace(roleName + "::isCallerInRole check failed:"+e.getLocalizedMessage()); 
         authorizationAudit(AuditLevel.ERROR,ejbResource,e);  
      } 
      return isAuthorized; 
   }  
   
}