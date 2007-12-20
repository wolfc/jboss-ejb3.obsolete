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
package org.jboss.ejb3.security;

import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBAccessException;
import javax.security.auth.Subject;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aspects.security.AuthenticationInterceptor;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.plugins.SecurityContextAssociation;

/**
 * Authentication Interceptor
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 67628 $
 */
public class Ejb3AuthenticationInterceptor extends AuthenticationInterceptor
{ 
   private static final Logger log = Logger.getLogger(Ejb3AuthenticationInterceptor.class);

   private EJBContainer container;
   protected RealmMapping realmMapping;
   
   /**
    * AuthenticationInterceptor which bypasses the AuthenticationManager, so
    * an unauthenticated principal won't be authenticated against an AuthenticationManager.
    */
   private AuthenticationInterceptor unauthenticatedAuthenticationInterceptor;

   public Ejb3AuthenticationInterceptor(final AuthenticationManager manager, Container container)
   {
      super(manager);
      this.container = (EJBContainer)container;
      this.realmMapping = (RealmMapping)manager;
      // TODO: can be optimized to only instantiate when securityDomain has an unauthenticatedPrincipal
      this.unauthenticatedAuthenticationInterceptor = new AuthenticationInterceptor(null)
      {
         @Override
         protected void authenticate(Invocation invocation) throws Exception
         {
            super.authenticate(invocation);
            
            // if we have a manager mimic run as stuff, so we end up with a Subject for JACC
            if(manager != null)
            {
               Principal principal = (Principal)invocation.getMetaData("security", "principal");
               Subject subject = new Subject();
               String securityDomain = manager.getSecurityDomain();
               SecurityContext sc = SecurityActions.createSecurityContext(principal, null, subject, securityDomain);
               SecurityContextAssociation.setSecurityContext(sc);
            }
         }
      };
   }

   protected void handleGeneralSecurityException(GeneralSecurityException gse)
   {
      log.debug("Authentication failure", gse);
      throw new EJBAccessException("Authentication failure");
   }

   public Object invoke(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      SecurityDomain domain = (SecurityDomain)container.resolveAnnotation(SecurityDomain.class);
      
      if (domain != null && domain.unauthenticatedPrincipal() != null && domain.unauthenticatedPrincipal().length() != 0)
      {
         Principal principal = (Principal)invocation.getMetaData("security", "principal");
         if (principal == null)
            principal = SecurityAssociation.getPrincipal();
         
         if (principal == null)
         {
            // we don't have a principal, but we do have an unauthenticatedPrincipal we can use
            principal = new TrustedPrincipal(domain.unauthenticatedPrincipal());
            
            // this will be picked up by the AuthenticationInterceptor
            invocation.getMetaData().addMetaData("security", "principal", principal);
         }
         
         // Either we got it from an earlier pass or we just instantiated it
         if(principal != null && principal instanceof TrustedPrincipal)
         {
            // call an AuthenticationInterceptor which doesn't authenticate
            return unauthenticatedAuthenticationInterceptor.invoke(invocation);
         }
      }
      try
      {  
         //Set a map of principal-roles that may be configured at deployment level
         if(container.getAssemblyDescriptor() != null)
         {
            Map<String, Set<String>> securityRoles = null;
            //SecurityRolesAssociation.setSecurityRoles(container.getAssemblyDescriptor().getPrincipalVersusRolesMap());
            SecurityRolesAssociation.setSecurityRoles(securityRoles);
         }
         return super.invoke(invocation);
      }
      finally
      { 
         SecurityRolesAssociation.setSecurityRoles(null);
      }
   }
   
   class TrustedPrincipal extends SimplePrincipal
   {
      private static final long serialVersionUID = 1L;

      public TrustedPrincipal(String name)
      {
         super(name);
      }
   }
}
