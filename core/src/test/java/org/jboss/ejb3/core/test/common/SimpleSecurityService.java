/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.common;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.ejb3.NonSerializableFactory;
import org.jboss.ejb3.core.test.common.security.SimplePolicyRegistration;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.AuthorizationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.RealmMapping;
import org.jboss.security.audit.AuditEvent;
import org.jboss.security.audit.AuditManager;
import org.jboss.security.authorization.AuthorizationContext;
import org.jboss.security.authorization.AuthorizationException;
import org.jboss.security.authorization.EntitlementHolder;
import org.jboss.security.authorization.Permission;
import org.jboss.security.authorization.Resource;
import org.jboss.security.authorization.resources.EJBResource;
import org.jboss.security.identity.Identity;
import org.jboss.security.identity.Role;
import org.jboss.security.identity.RoleGroup;
import org.jboss.security.identity.plugins.SimpleRole;
import org.jboss.security.identitytrust.IdentityTrustManager;
import org.jboss.security.mapping.MappingManager;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleSecurityService implements ISecurityManagement
{
   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(SimpleSecurityService.class);
   
   private InitialContext ctx;
   
   public AuditManager getAuditManager(String securityDomain)
   {
      return new SimpleAuditManager(securityDomain);
   }

   public AuthenticationManager getAuthenticationManager(String securityDomain)
   {
      return new SimpleAuthenticationManager(securityDomain);
   }

   public AuthorizationManager getAuthorizationManager(String securityDomain)
   {
      return new SimpleAuthorizationManager(securityDomain);
   }

   public IdentityTrustManager getIdentityTrustManager(String securityDomain)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.security.ISecurityManagement#getMappingManager(java.lang.String)
    */
   public MappingManager getMappingManager(String securityDomain)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }
   
   public void start() throws Exception
   {
      ctx = new InitialContext();
      ctx.bind("java:/policyRegistration", new SimplePolicyRegistration());
      NonSerializableFactory.bind(ctx, "securityManagement", this);
      NonSerializableFactory.bind(ctx, "java:/jaas/test", getAuthenticationManager("test"));
   }
   
   public void stop() throws Exception
   {
      ctx.close();
      ctx = null;
   }
   
   private static abstract class AbstractManager
   {
      private String securityDomain;
      
      private AbstractManager(String securityDomain)
      {
         assert securityDomain != null : "securityDomain is null";
         
         this.securityDomain = securityDomain;
      }
      
      public final String getSecurityDomain()
      {
         return securityDomain;
      }
   }
   
   private static class SimpleAuditManager extends AbstractManager implements AuditManager
   {
      private SimpleAuditManager(String securityDomain)
      {
         super(securityDomain);
      }
      
      public void audit(AuditEvent ae)
      {
         Exception e = ae.getUnderlyingException();
         if(e != null)
            log.warn("Authentication failed", e);
         log.info(ae.toString());
      }
   }
   
   private static class SimpleAuthenticationManager extends AbstractManager implements AuthenticationManager, RealmMapping
   {
      private SimpleAuthenticationManager(String securityDomain)
      {
         super(securityDomain);
      }
      
      public boolean doesUserHaveRole(Principal principal, Set<Principal> roles)
      {
         throw new RuntimeException("NYI");
      }
      
      public Subject getActiveSubject()
      {
         throw new RuntimeException("NYI");
      }

      public Principal getPrincipal(Principal principal)
      {
         //throw new RuntimeException("NYI");
         return principal;
      }
      
      public Principal getTargetPrincipal(Principal anotherDomainPrincipal, Map<String, Object> contextMap)
      {
         throw new RuntimeException("NYI");
      }

      public Set<Principal> getUserRoles(Principal principal)
      {
         throw new RuntimeException("NYI");
      }
      
      public boolean isValid(Principal principal, Object credential)
      {
         throw new RuntimeException("NYI");
      }

      public boolean isValid(Principal principal, Object credential, Subject activeSubject)
      {
         if(principal == null)
            return false;
         // TODO: almost everything is valid for now
         if(principal.getName().startsWith("Invalid"))
            return false;
         activeSubject.getPrincipals().add(principal);
         return true;
      }
   }
   
   private static class SimpleAuthorizationManager extends AbstractManager implements AuthorizationManager
   {
      private SimpleAuthorizationManager(String securityDomain)
      {
         super(securityDomain);
      }
      
      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#authorize(org.jboss.security.authorization.Resource)
       */
      public int authorize(Resource resource) throws AuthorizationException
      {
         // TODO Auto-generated method stub
         //return 0;
         throw new RuntimeException("NYI");
      }

      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#authorize(org.jboss.security.authorization.Resource, javax.security.auth.Subject)
       */
      public int authorize(Resource resource, Subject subject) throws AuthorizationException
      {
         // TODO Auto-generated method stub
         //return 0;
         throw new RuntimeException("NYI");
      }

      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#authorize(org.jboss.security.authorization.Resource, org.jboss.security.identity.Identity, org.jboss.security.authorization.Permission)
       */
      public int authorize(Resource resource, Identity identity, Permission permission) throws AuthorizationException
      {
         // TODO Auto-generated method stub
         //return 0;
         throw new RuntimeException("NYI");
      }

      public int authorize(Resource resource, Subject subject, RoleGroup role) throws AuthorizationException
      {
         log.debug("authorize " + resource + " " + subject + " " + role);
         EJBResource ejbResource = (EJBResource) resource;
         RoleGroup methodRoles = ejbResource.getEjbMethodRoles();
         if(methodRoles == null)
            return AuthorizationContext.PERMIT;
         if(methodRoles.containsRole(SimpleRole.ANYBODY_ROLE))
            return AuthorizationContext.PERMIT;
         for(Principal p : subject.getPrincipals())
         {
            // TODO: not really true, but for the moment lets assume that the principal is also the role
            Role myRole = new SimpleRole(p.getName());
            if(methodRoles.containsRole(myRole))
               return AuthorizationContext.PERMIT;
         }
         return AuthorizationContext.DENY;
      }

      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#authorize(org.jboss.security.authorization.Resource, javax.security.auth.Subject, java.security.acl.Group)
       */
      public int authorize(Resource resource, Subject subject, Group roleGroup) throws AuthorizationException
      {
         // TODO Auto-generated method stub
         //return 0;
         throw new RuntimeException("NYI");
      }

      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#doesUserHaveRole(java.security.Principal, java.util.Set)
       */
      public boolean doesUserHaveRole(Principal principal, Set<Principal> roles)
      {
         // TODO Auto-generated method stub
         //return false;
         throw new RuntimeException("NYI");
      }

      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#getEntitlements(java.lang.Class, org.jboss.security.authorization.Resource, org.jboss.security.identity.Identity)
       */
      public <T> EntitlementHolder<T> getEntitlements(Class<T> clazz, Resource resource, Identity identity)
            throws AuthorizationException
      {
         // TODO Auto-generated method stub
         //return null;
         throw new RuntimeException("NYI");
      }

      public RoleGroup getSubjectRoles(Subject authenticatedSubject, CallbackHandler cbh)
      {
         return null;
      }

      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#getTargetRoles(java.security.Principal, java.util.Map)
       */
      public Group getTargetRoles(Principal targetPrincipal, Map<String, Object> contextMap)
      {
         // TODO Auto-generated method stub
         //return null;
         throw new RuntimeException("NYI");
      }

      /* (non-Javadoc)
       * @see org.jboss.security.AuthorizationManager#getUserRoles(java.security.Principal)
       */
      public Set<Principal> getUserRoles(Principal principal)
      {
         // TODO Auto-generated method stub
         //return null;
         throw new RuntimeException("NYI");
      }
   }

}
