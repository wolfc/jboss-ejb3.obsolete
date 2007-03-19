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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.security.AuthenticationManager;
import org.jboss.ejb3.Container;

public class AuthenticationInterceptorFactory implements AspectFactory
{
   public Object createPerVM()
   {
      throw new RuntimeException("PER_VM not supported for this interceptor factory, only PER_CLASS");
   }

   public Object createPerClass(Advisor advisor)
   {
      Object domain = null;
      Container container = (Container)advisor;
      try
      {
         InitialContext ctx = container.getInitialContext();
         SecurityDomain securityAnnotation = (SecurityDomain) advisor.resolveAnnotation(SecurityDomain.class);
         if (securityAnnotation != null)
         {
            domain = SecurityDomainManager.getSecurityManager(securityAnnotation.value(),ctx);
         }
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      AuthenticationManager manager = (AuthenticationManager) domain;
      return new Ejb3AuthenticationInterceptor(manager, container);
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

