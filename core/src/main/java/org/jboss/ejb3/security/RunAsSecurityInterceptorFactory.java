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
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RunAs;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.annotation.RunAsPrincipal;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.tx.NullInterceptor;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.SecurityIdentityMetaData;
import org.jboss.security.RunAsIdentity;

/**
 * RunAs interceptor factory
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Anil.Saldhana@jboss.org
 * @version $Revision$ 
 */
public class RunAsSecurityInterceptorFactory extends PerClassAspectFactoryAdaptor 
implements AspectFactory
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(RunAsSecurityInterceptorFactory.class);
  
   protected RunAsIdentity getRunAsIdentity(EJBContainer container)
   {      
      RunAs runAs = (RunAs) container.getAnnotation(RunAs.class);
      if (runAs == null)
         return null;
      
      RunAsPrincipal rap = (RunAsPrincipal) container.resolveAnnotation(RunAsPrincipal.class);
      String runAsPrincipal = null;
      if (rap != null) 
         runAsPrincipal = rap.value();
      
      Set<String> extraRoles = new HashSet<String>();
      
      JBossEnterpriseBeanMetaData jbEnterpriseBeanMetaData = container.getXml();
      if(jbEnterpriseBeanMetaData != null)
      {
         SecurityIdentityMetaData securityIdentity = jbEnterpriseBeanMetaData.getSecurityIdentity();
         if(securityIdentity.isUseCallerId())
            return null; //Overriden in xml
         String s = securityIdentity.getRunAsPrincipal();
         if(s != null && s.length() != 0)
            runAsPrincipal = s;  
      }
      if(runAsPrincipal != null)
      {
         Map<String,Set<String>> principalVsRoleMap = jbEnterpriseBeanMetaData.getSecurityRolesPrincipalVersusRolesMap();
         Set<String> roles = principalVsRoleMap.get(runAsPrincipal);
         if(roles != null)
            extraRoles.addAll(roles);
      }
       
      JBossAssemblyDescriptorMetaData ad = container.getAssemblyDescriptor();
      if(ad != null && runAsPrincipal != null)
      {
         extraRoles.addAll(ad.getSecurityRoleNamesByPrincipal(runAsPrincipal));
      }
      
      return new RunAsIdentity(runAs.value(), runAsPrincipal, extraRoles);

   }


   public Object createPerClass(Advisor advisor)
   {
      EJBContainer container = EJBContainer.getEJBContainer(advisor);

      Object domain = null;
      try
      {
         InitialContext ctx = container.getInitialContext();
         SecurityDomain securityDomain = (SecurityDomain) advisor.resolveAnnotation(SecurityDomain.class);
         if (securityDomain != null)
         {
            String domainName = securityDomain.value();
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
         interceptor = new RunAsSecurityInterceptorv2(container, getRunAsIdentity(container));
      }
      return interceptor;
   }  
}
