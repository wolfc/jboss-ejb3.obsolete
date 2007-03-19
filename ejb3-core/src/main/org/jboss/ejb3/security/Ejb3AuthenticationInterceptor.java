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

import javax.ejb.EJBAccessException;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.logging.Logger;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.aop.joinpoint.MethodInvocation;

import org.jboss.aspects.security.AuthenticationInterceptor;
import org.jboss.aspects.security.SecurityContext;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping; 
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * Authentication Interceptor
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Anil.Saldhana@jboss.org
 * @version $Revision$
 */
public class Ejb3AuthenticationInterceptor extends AuthenticationInterceptor
{ 
   private static final Logger log = Logger.getLogger(Ejb3AuthenticationInterceptor.class);

   private EJBContainer container;
   protected RealmMapping realmMapping;

   public Ejb3AuthenticationInterceptor(AuthenticationManager manager, Container container)
   {
      super(manager);
      this.container = (EJBContainer)container;
      this.realmMapping = (RealmMapping)manager;
   }

   protected void handleGeneralSecurityException(GeneralSecurityException gse)
   {
      throw new EJBAccessException("Authentication failure", gse);
   }

   public Object invoke(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      MethodInvocation mi = (MethodInvocation)invocation;
      SecurityDomain domain = (SecurityDomain)container.resolveAnnotation(SecurityDomain.class);
      
      if (domain != null && domain.unauthenticatedPrincipal() != null && domain.unauthenticatedPrincipal().length() != 0)
      {
         Principal principal = (Principal)invocation.getMetaData("security", "principal");
         if (principal == null)
            principal = SecurityAssociation.getPrincipal();
           
         if (principal == null)
         {
            invocation.getMetaData().addMetaData("security", "principal", new SimplePrincipal(domain.unauthenticatedPrincipal()));
            
            Object oldDomain = SecurityContext.getCurrentDomain().get();
            
            try
            {
               SecurityContext.getCurrentDomain().set(authenticationManager);
               return invocation.invokeNext();
            }
            finally
            {
               SecurityContext.getCurrentDomain().set(oldDomain);
            }
         }
      }
      try
      {  
         //Set a map of principal-roles that may be configured at deployment level
         if(container.getAssemblyDescriptor() != null)
         {
            SecurityRolesAssociation.setSecurityRoles(container.getAssemblyDescriptor().getPrincipalVersusRolesMap());
         }
         return super.invoke(invocation);
      }
      finally
      { 
         SecurityRolesAssociation.setSecurityRoles(null);
      }
   } 
}
