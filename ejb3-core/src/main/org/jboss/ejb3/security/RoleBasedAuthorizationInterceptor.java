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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.logging.Logger;
import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.NobodyPrincipal;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;

/**
 * The RoleBasedAuthorizationInterceptor checks that the caller principal is
 * authorized to call a method by verifing that it contains at least one
 * of the required roled.
 *
 * @author <a href="bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public final class RoleBasedAuthorizationInterceptor extends org.jboss.aspects.security.RoleBasedAuthorizationInterceptor
{
   private static final Logger log = Logger.getLogger(RoleBasedAuthorizationInterceptor.class);
   
   private EJBContainer container;
   
   public RoleBasedAuthorizationInterceptor(AuthenticationManager manager, RealmMapping realmMapping, Container container)
   {
      super(manager, realmMapping);
      this.container = (EJBContainer)container;
   }

   protected Set getRoleSet(Invocation invocation)
   {
      Method method = ((MethodInvocation)invocation).getActualMethod();

      Class[] classes = new Class[]{DenyAll.class, PermitAll.class, RolesAllowed.class};

      Object annotation = container.resolveAnnotation(method, classes);
      
      int classIndex = 0;
      while (annotation == null && classIndex < 3)
      {
         annotation = container.resolveAnnotation(classes[classIndex++]);
      }
         
      HashSet set = new HashSet();
      if (annotation != null)
      {
         if (annotation instanceof DenyAll)
         {
            set.add(NobodyPrincipal.NOBODY_PRINCIPAL);
         }
         else if (annotation instanceof PermitAll)
         {
            set.add(AnybodyPrincipal.ANYBODY_PRINCIPAL);
         }
         else if (annotation instanceof RolesAllowed)
         {
            RolesAllowed permissions = (RolesAllowed) annotation;
            for (int i = 0; i < permissions.value().length; i++)
            {
               set.add(new SimplePrincipal(permissions.value()[i]));
            }
         }
         else
            set.add(AnybodyPrincipal.ANYBODY_PRINCIPAL);
      }
      else
         set.add(AnybodyPrincipal.ANYBODY_PRINCIPAL);

      return set;
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      try
      {
         return super.invoke(invocation);
      }
      catch (SecurityException throwable)
      {
         throw new EJBAccessException("Authorization failure", throwable);
      } finally {
      }
   }

}
