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
package org.jboss.annotation.security;

import java.lang.annotation.Annotation;

import org.jboss.annotation.security.SecurityDomain;

/**
 * // *
 * 
 * @author <a href="mailto:bill@jboss.org">William DeCoste</a>
 * @version $Revision$
 */
public class SecurityDomainImpl implements SecurityDomain
{
   private String value;
   private String unauthenticatedPrincipal = null;
   
   public SecurityDomainImpl()
   {
      this("");
   }
   
   public SecurityDomainImpl(String value)
   {
      this.value = value;
   }
   
   public String value()
   {
      return value;
   }
   
   public String unauthenticatedPrincipal()
   {
      return unauthenticatedPrincipal;
   }
   
   public void setUnauthenticatedPrincipal(String unauthenticatedPrincipal)
   {
      this.unauthenticatedPrincipal = unauthenticatedPrincipal;
   }

   public Class<SecurityDomain> annotationType()
   {
      return SecurityDomain.class;
   }
   
   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("SecurityDomainImpl[");
      sb.append("value=").append(value);
      sb.append(", unauthenticatedPrincipal=").append(unauthenticatedPrincipal);
      sb.append("]");
      return sb.toString();
   }
}
