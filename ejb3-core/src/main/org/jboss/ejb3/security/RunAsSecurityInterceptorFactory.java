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

import java.util.HashSet;

import javax.annotation.security.RunAs;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.annotation.security.RunAsPrincipal;
import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.metamodel.AssemblyDescriptor;
import org.jboss.ejb3.tx.NullInterceptor;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAsIdentity;

public class RunAsSecurityInterceptorFactory implements AspectFactory
{
   private static final Logger log = Logger.getLogger(RunAsSecurityInterceptorFactory.class);

   public Object createPerVM()
   {
      throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
   }


   protected RunAsIdentity getRunAsIdentity(EJBContainer container)
   {
      RunAs runAs = (RunAs) container.resolveAnnotation(RunAs.class);
      if (runAs == null) return null;
      if (container.getXml() != null && container.getXml().getSecurityIdentity() != null)
      {
         if (container.getXml().getSecurityIdentity().isUseCallerIdentity()) return null;
      }
      RunAsPrincipal rap = (RunAsPrincipal) container.resolveAnnotation(RunAsPrincipal.class);
      String runAsPrincipal = null;
      if (rap != null) 
         runAsPrincipal = rap.value();
      else
      {
         //Check if jboss.xml has it  
         if(container.getXml() != null && container.getXml().getSecurityIdentity() != null)
         {
            runAsPrincipal = container.getXml().getSecurityIdentity().getRunAsPrincipal();
         }
      }

      HashSet extraRoles = new HashSet();  
      AssemblyDescriptor ad = container.getAssemblyDescriptor();
      if(ad != null)
         extraRoles.addAll(ad.getSecurityRolesGivenPrincipal(runAsPrincipal));

      return new RunAsIdentity(runAs.value(), runAsPrincipal, extraRoles);
   }


   public Object createPerClass(Advisor advisor)
   {
      EJBContainer container = (EJBContainer)advisor;
      RunAsIdentity runAsIdentity = getRunAsIdentity(container);
      /*if (runAsIdentity == null)
      {
         return new NullInterceptor();
      }*/

      Object domain = null;
      try
      {
         InitialContext ctx = container.getInitialContext();
         org.jboss.annotation.security.SecurityDomain anSecurityDomain = (org.jboss.annotation.security.SecurityDomain) advisor.resolveAnnotation(org.jboss.annotation.security.SecurityDomain.class);
         if (anSecurityDomain != null)
         {
            String domainName = anSecurityDomain.value();
            domain = SecurityDomainManager.getSecurityManager(domainName, ctx);
         }
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      
      Interceptor interceptor = new NullInterceptor();
      if (domain != null)
      {
         AuthenticationManager manager = (AuthenticationManager) domain;
         RealmMapping mapping = (RealmMapping) domain;
         interceptor = new RunAsSecurityInterceptor(manager, mapping, getRunAsIdentity(container));
      }
      return interceptor;
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
   }

   public String getName()
   {
      return getClass().getName();
   }
}

