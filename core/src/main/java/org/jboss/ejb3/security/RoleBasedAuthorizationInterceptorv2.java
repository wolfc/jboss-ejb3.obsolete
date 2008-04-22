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
import java.security.CodeSource;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.remoting.InvokerLocator;
import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.NobodyPrincipal;
import org.jboss.security.RunAs;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.identity.plugins.SimpleRoleGroup;
import org.jboss.security.javaee.AbstractEJBAuthorizationHelper;
import org.jboss.security.javaee.SecurityHelperFactory;

/**
 * The RoleBasedAuthorizationInterceptor checks that the caller principal is
 * authorized to call a method by verifing that it contains at least one
 * of the required roled.
 *
 * @author <a href="bill@jboss.org">Bill Burke</a>
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 62539 $
 */
public final class RoleBasedAuthorizationInterceptorv2 implements Interceptor
{
   private static final Logger log = Logger.getLogger(RoleBasedAuthorizationInterceptorv2.class);
   
   private EJBContainer container;

   private CodeSource ejbCS;
   
   private String ejbName;
   
   public RoleBasedAuthorizationInterceptorv2(Container container, 
         CodeSource ejbCS, String ejbName)
   { 
      this.container = (EJBContainer)container;
      this.ejbCS = ejbCS;
      this.ejbName = ejbName;
   }

   protected Set<Principal> getRoleSet(Invocation invocation)
   {
      Method method = ((MethodInvocation)invocation).getActualMethod();

      Class<?>[] classes = new Class[]{DenyAll.class, PermitAll.class, RolesAllowed.class};

      Object annotation = container.resolveAnnotation(method, classes);
      
      int classIndex = 0;
      while (annotation == null && classIndex < 3)
      {
         annotation = container.resolveAnnotation(classes[classIndex++]);
      }
         
      HashSet<Principal> set = new HashSet<Principal>();
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
      //Set the JACC ContextID
      String contextID = container.getJaccContextId();
      SecurityActions.setContextID(contextID);
      
      MethodInvocation mi = (MethodInvocation)invocation;
      //Check for ejbTimeOut
      SecurityHelper shelper = new SecurityHelper(); 
      Method method = mi.getMethod();
      if(shelper.isEJBTimeOutCallback(method) ||
            shelper.containsTimeoutAnnotation(container, method) ||
            shelper.isMDB(container)) 
         return invocation.invokeNext();
      
      try
      {
         SecurityDomain domain = (SecurityDomain)container.getAnnotation(SecurityDomain.class);
         
         boolean domainExists = domain != null && domain.value() != null 
         && domain.value().length() > 0;
         
         if(domainExists)
         {
            SecurityContext sc = SecurityActions.getSecurityContext();
            if(sc == null)
               throw new IllegalStateException("Security Context has not been set");
            Set<Principal> methodRoles = getRoleSet(invocation);
            if (methodRoles == null)
            {
               /*
                 REVISIT: for better message
               String message = "No method permissions assigned. to " +
                     "method=" + invocation.getMethod().getName() +
                     ", interface=" + invocation.getType();
               */
               String message = "No method permissions assigned.";
               log.error(message);
               throw new SecurityException(message);
            }
            
            //Specify any Deployment Level Mapping of Principal - role names
            JBossAssemblyDescriptorMetaData jmd = container.getAssemblyDescriptor();
            if(jmd != null)
            {
               Map<String,Set<String>> principalRoleMap = jmd.getPrincipalVersusRolesMap();
               SecurityRolesAssociation.setSecurityRoles(principalRoleMap);
            }
            InvokerLocator locator = (InvokerLocator) invocation.getMetaData(InvokeRemoteInterceptor.REMOTING, 
                  InvokeRemoteInterceptor.INVOKER_LOCATOR);

            String iface = (locator != null) ? "Remote" : "Local"; 
            
            RunAs callerRunAs = SecurityActions.peekRunAs();
            
            AbstractEJBAuthorizationHelper helper = null;
            try
            {
               helper = SecurityHelperFactory.getEJBAuthorizationHelper(sc); 
            }
            catch(Exception e)
            {
               throw new RuntimeException(e);
            } 
            boolean isAuthorized = helper.authorize(ejbName, 
                             mi.getMethod(), 
                             sc.getUtil().getUserPrincipal(), 
                             iface, 
                             ejbCS, 
                             sc.getUtil().getSubject(), 
                             callerRunAs, 
                             contextID,
                             new SimpleRoleGroup(methodRoles));
            if(!isAuthorized)
               throw new EJBAccessException("Caller unauthorized");
         }  
         return invocation.invokeNext();
      }
      catch (SecurityException throwable)
      {
         log.debug("Authorization failure", throwable);
         throw new EJBAccessException("Authorization failure");
      } finally {
      }
   }

   public String getName()
   { 
      return getClass().getName();
   } 
}