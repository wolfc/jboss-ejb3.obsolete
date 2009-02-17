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
package org.jboss.security.integration;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.security.AuthenticationManager;
import org.jboss.security.AuthorizationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.audit.AuditManager;
import org.jboss.security.identitytrust.IdentityTrustManager;
import org.jboss.security.mapping.MappingManager;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JNDIBasedSecurityManagement implements ISecurityManagement
{
   private InitialContext ctx;
   
   public JNDIBasedSecurityManagement() throws NamingException
   {
      ctx = new InitialContext();
   }
   
   public AuditManager getAuditManager(String securityDomain)
   {
      return lookupDelegate().getAuditManager(securityDomain);
   }

   public AuthenticationManager getAuthenticationManager(String securityDomain)
   {
      return lookupDelegate().getAuthenticationManager(securityDomain);
   }

   public AuthorizationManager getAuthorizationManager(String securityDomain)
   {
      return lookupDelegate().getAuthorizationManager(securityDomain);
   }

   public IdentityTrustManager getIdentityTrustManager(String securityDomain)
   {
      return lookupDelegate().getIdentityTrustManager(securityDomain);
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
   
   private ISecurityManagement lookupDelegate()
   {
      try
      {
         return (ISecurityManagement) ctx.lookup("securityManagement");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
}
