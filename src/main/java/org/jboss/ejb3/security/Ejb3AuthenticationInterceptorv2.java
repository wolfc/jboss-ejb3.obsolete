/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
import java.security.Principal;

import javax.ejb.EJBAccessException;
import javax.security.auth.Subject;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityIdentity;
import org.jboss.security.SecurityUtil;
import org.jboss.security.integration.JNDIBasedSecurityManagement;
import org.jboss.security.integration.ejb.EJBAuthenticationHelper;

//$Id$

/**
 *  Authentication Interceptor
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @author Anil.Saldhana@redhat.com
 *  @since  Aug 16, 2007 
 *  @version $Revision$
 */
public class Ejb3AuthenticationInterceptorv2 implements Interceptor
{ 
   protected Logger log = Logger.getLogger(this.getClass()); 
   private EJBContainer container; 
   
   public  Ejb3AuthenticationInterceptorv2(Container container)
   { 
     this.container = (EJBContainer) container; 
   }
   
   public String getName()
   { 
      return getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   { 
      //Check for ejbTimeOutCallback or ejbTimeOut method
      SecurityHelper shelper = new SecurityHelper();
      MethodInvocation mi = (MethodInvocation) invocation;
      Method method = mi.getMethod();
      if(shelper.isEJBTimeOutCallback(method) ||
            shelper.containsTimeoutAnnotation(container, method) ||
            shelper.isMDB(container)) 
         return invocation.invokeNext();
      
      SecurityIdentity si = null;
      SecurityContext sc = SecurityActions.getSecurityContext();
      SecurityContext invSC = (SecurityContext) invocation.getMetaData("security","context"); 
      
      SecurityDomain domain = (SecurityDomain)container.resolveAnnotation(SecurityDomain.class);
      
      boolean domainExists = domain != null && domain.value() != null 
                    && domain.value().length() > 0;
       
      /**
       * TODO: Decide if you want to allow zero security based on non-availability
       * of a security domain, as per the configuration on the container
       */
      if(domainExists)
      { 
         Principal p = null;
         Object cred = null;
         
         //There is no security context at all
         if(sc == null && invSC == null)
         {
            sc = SecurityActions.createSecurityContext(domain.value());
            SecurityActions.setSecurityContext(sc); 
         }
         
         if(shelper.isLocalCall(mi))
         {
            if(sc == null)
               throw new IllegalStateException("Security Context null on Local call");
            si = sc.getUtil().getSecurityIdentity();
         }
         else
         {
            if(invSC == null && sc == null)
               throw new IllegalStateException("Security Context is not available");
            
            //If there was a SecurityContext over the invocation, that takes preference
            if(invSC != null)
            {
               sc = invSC;
               p = sc.getUtil().getUserPrincipal();
               cred = sc.getUtil().getCredential();
               String unprefixed = SecurityUtil.unprefixSecurityDomain(domain.value());
               sc = SecurityActions.createSecurityContext(p, 
                     cred, null, unprefixed); 
               
               //Set the security context
               SecurityActions.setSecurityContext(sc);
               sc.getUtil().setSecurityIdentity(invSC.getUtil().getSecurityIdentity());
            }
         }
         
         sc = SecurityActions.getSecurityContext();
         //TODO: Need to get the SecurityManagement instance
         sc.setSecurityManagement(new JNDIBasedSecurityManagement());
           
         //Check if there is a RunAs configured and can be trusted 
         EJBAuthenticationHelper helper = new EJBAuthenticationHelper(sc);
         boolean trustedCaller = helper.isTrusted();
         if(!trustedCaller)
         {
            Subject subject = new Subject();
            //Authenticate the caller now
            if(!helper.isValid(subject, method.getName()))
               throw new EJBAccessException("Invalid User"); 
            helper.pushSubjectContext(subject);
         }
         else
         {  
            //Trusted caller. No need for authentication. Straight to authorization
         } 
      }
      else
      {
         //domain == null
         /**
          * Special Case when a bean with no security domain defined comes with a security
          * context attached.
          */
         if(invSC != null)
         {
            SecurityActions.setSecurityContext(invSC);
         }
      }
      try
      { 
         if(sc != null)
           SecurityActions.pushCallerRunAsIdentity(sc.getOutgoingRunAs());
         return invocation.invokeNext();  
      }
      finally
      {
         if(shelper.isLocalCall(mi) && si != null)
            SecurityActions.getSecurityContext().getUtil().setSecurityIdentity(si);
      }
   }  
}