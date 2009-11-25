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

import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aspects.currentinvocation.CurrentInvocation;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.interceptors.container.InvocationHelper;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityContext;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.authorization.PolicyRegistration;
import org.jboss.security.javaee.AbstractEJBAuthorizationHelper;
import org.jboss.security.javaee.SecurityHelperFactory;
import org.jboss.security.javaee.SecurityRoleRef;

/**
 *  Helper class with programmatic
 *  security methods in EJBContext
 *  @author Anil.Saldhana@redhat.com
 *  @since  Apr 17, 2008 
 *  @version $Revision$
 */
public class EJBContextHelper
{
   public Principal getCallerPrincipal(SecurityContext sc,
         RealmMapping rm, SecurityDomain domain)
   {
      Invocation invocation = getCurrentInvocation("getCallerPrincipal");
      if(isStateless(invocation) && isLifecycleCallback(invocation))
         throw new IllegalStateException("getCallerPrincipal is not allowed in a stateless lifecycle callback (EJB3 4.5.2)");
      
      Principal callerPrincipal = null;
      
      // if we have the security context, then try to
      // get the caller principal out of that
      if (sc != null)
      {
         AbstractEJBAuthorizationHelper helper;
         try
         {
            helper = SecurityHelperFactory.getEJBAuthorizationHelper(sc);
            helper.setPolicyRegistration(getPolicyRegistration());
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         callerPrincipal = helper.getCallerPrincipal();

         if (callerPrincipal == null)
         {
            //try the incoming principal
            callerPrincipal = sc.getUtil().getUserPrincipal();
            if (rm != null)
               callerPrincipal = rm.getPrincipal(callerPrincipal);
         }
      }
      // either security context was absent or
      // could not get the caller principal from security context.
      // So let's try the unauthenticated principal, if the domain
      // is present
      if (callerPrincipal == null)
      {
         if (domain != null)
         {
            String unauth = domain.unauthenticatedPrincipal();
            if (unauth != null && unauth.length() > 0)
            {
               callerPrincipal = new SimplePrincipal(unauth);
            }
         }
      }
      return callerPrincipal; 
   } 
   
   private static Invocation getCurrentInvocation(String reason)
   {
      Invocation current = CurrentInvocation.getCurrentInvocation();
      if(isInjection(current))
         throw new IllegalStateException(reason + " not allowed during injection (EJB3 4.4.1 & 4.5.2)");
      return current;
   }
   
   public boolean isCallerInRole(SecurityContext sc,
         SecurityDomain domain,
         RealmMapping rm,
         JBossEnterpriseBeanMetaData eb, 
         String roleName, 
         String ejbName)
   {
      Invocation invocation = getCurrentInvocation("isCallerInRole");
      if(isStateless(invocation) && isLifecycleCallback(invocation))
         throw new IllegalStateException("getCallerPrincipal is not allowed in a stateless lifecycle callback (EJB3 4.5.2)");
      
      if(sc == null)
      {
         try
         {
            sc = SecurityActions.createSecurityContext(domain.value());
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException(e);
         }              
      }  
      // TODO: this is too slow
      Set<SecurityRoleRefMetaData> roleRefs = new HashSet<SecurityRoleRefMetaData>();
      if(eb != null)
      {
         Collection<SecurityRoleRefMetaData> srf = eb.getSecurityRoleRefs(); 
         if(srf != null)
            roleRefs.addAll(srf);   
      } 
      
      //TODO: Get rid of this conversion asap
      Set<SecurityRoleRef> srset = new HashSet<SecurityRoleRef>();
      for(SecurityRoleRefMetaData srmd: roleRefs)
      {
         srset.add(new SecurityRoleRef(srmd.getRoleName(),srmd.getRoleLink(),null));
      }
      Principal principal = getCallerPrincipal(sc, rm, domain);
      AbstractEJBAuthorizationHelper helper;
      try
      {
         helper = SecurityHelperFactory.getEJBAuthorizationHelper(sc);
         helper.setPolicyRegistration(getPolicyRegistration());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      Subject callerSubject = null;
      try
      {
         callerSubject = SecurityActions.getActiveSubject();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      } 
      return helper.isCallerInRole(roleName, 
                                   ejbName, 
                                   principal, 
                                   callerSubject,
                                   this.getContextID(),
                                   srset);
   }
   
   private static boolean isInjection(Invocation invocation)
   {
      return InvocationHelper.isInjection(invocation);
   }
   
   private static boolean isLifecycleCallback(Invocation invocation)
   {
      return InvocationHelper.isLifecycleCallback(invocation);
   }
   
   private static boolean isStateless(Invocation inv)
   {
      assert inv != null : "inv is null";
      return inv.getAdvisor().resolveAnnotation(Stateless.class) != null;
   }
   
   private PolicyRegistration getPolicyRegistration()
   {
      PolicyRegistration policyRegistration = null;
      try
      {
         InitialContext ic = new InitialContext();
         policyRegistration = (PolicyRegistration) ic.lookup("java:/policyRegistration"); 
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
       return policyRegistration; 
   }
   
   private String getContextID()
   {
      String contextID = PolicyContext.getContextID();
      if(contextID == null)
         throw new IllegalStateException("No policy context id is set");
      return contextID;
   }
}