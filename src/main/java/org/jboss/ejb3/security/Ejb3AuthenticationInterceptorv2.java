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
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;

import javax.ejb.EJBAccessException;
import javax.security.auth.Subject;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.RunAs;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityUtil;
import org.jboss.security.identity.Identity;
import org.jboss.security.identity.plugins.SimpleIdentity;
import org.jboss.security.javaee.EJBAuthenticationHelper;
import org.jboss.security.javaee.SecurityHelperFactory;

/**
 *  Authentication Interceptor
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
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
       
      SecurityContext prevSC = SecurityActions.getSecurityContext();
      try
      {
         SecurityContext invSC = (SecurityContext) invocation.getMetaData("security","context"); 
         
         SecurityDomain domain = container.getAnnotation(SecurityDomain.class); 
         
         boolean domainExists = domain != null && domain.value() != null 
                       && domain.value().length() > 0;
          
         /**
          * TODO: Decide if you want to allow zero security based on non-availability
          * of a security domain, as per the configuration on the container
          */
         if(domainExists)
         {  
            String domainValue = canonicalizeSecurityDomain(domain.value());
            
            /* Need to establish the security context. For local calls, we pick the outgoing runas
             * of the existing sc. For remote calls, we create a new security context with the information
             * from the invocation sc
             */
            final SecurityContext sc = SecurityActions.createSecurityContext(domainValue);
            
            if(shelper.isLocalCall(mi))
            {
               if(prevSC == null)
                  throw new IllegalStateException("Local Call: Security Context is null");
               populateSecurityContext(sc, prevSC);  
            }
            else
            { 
              //Remote Invocation
              if(invSC == null)
                throw new IllegalStateException("Remote Call: Invocation Security Context is null");
              
              populateSecurityContext(sc, invSC); 
            }
            
            SecurityActions.setSecurityContext(sc);
               
            //TODO: Need to get the SecurityManagement instance
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
            {
               public Object run() throws Exception
               {
                  sc.setSecurityManagement(getSecurityManagement());
                  return null;
               }
            });
            
              
            //Check if there is a RunAs configured and can be trusted 
            EJBAuthenticationHelper helper = null;
            try
            {
               helper = SecurityHelperFactory.getEJBAuthenticationHelper(sc);
            }
            catch(Exception e)
            {
               throw new RuntimeException(e);
            } 
            boolean trustedCaller = hasIncomingRunAsIdentity(sc) || helper.isTrusted();
            if(!trustedCaller)
            {
               Subject subject = new Subject();
               /**
                * Special Case: Invocation has no principal set, 
                * but an unauthenticatedPrincipal has been configured in JBoss DD
                */
               Principal userPrincipal = sc.getUtil().getUserPrincipal();
               String unauthenticatedPrincipal = domain.unauthenticatedPrincipal();
               if(userPrincipal == null && unauthenticatedPrincipal !=null &&
                     unauthenticatedPrincipal.length() > 0)
               {
                  Identity unauthenticatedIdentity = new SimpleIdentity(unauthenticatedPrincipal);
                  sc.getSubjectInfo().addIdentity(unauthenticatedIdentity);
                  subject.getPrincipals().add(unauthenticatedIdentity.asPrincipal());
               }
               else
               { 
                  //Authenticate the caller now
                  if(!helper.isValid(subject, method.getName()))
                     throw new EJBAccessException("Invalid User"); 
               }
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
         return invocation.invokeNext();  
      }
      finally
      { 
         SecurityActions.setSecurityContext(prevSC); 
      }
   }
   
   private String canonicalizeSecurityDomain(String securityDomain)
   {
	  return SecurityUtil.unprefixSecurityDomain(securityDomain); 
   }
   
   private void populateSecurityContext(SecurityContext to, SecurityContext from)
   {
      SecurityActions.setSubjectInfo(to, from.getSubjectInfo());
      SecurityActions.setIncomingRunAs(to, from.getOutgoingRunAs());
   }
   
   /**
    * TODO: This needs to be injectable
    * @return
    * @throws Exception 
    */
   private ISecurityManagement getSecurityManagement() throws Exception
   {
      Class<?> clazz = SecurityActions.loadClass("org.jboss.security.integration.JNDIBasedSecurityManagement");
      return (ISecurityManagement) clazz.newInstance();    
   }
   
   private boolean hasIncomingRunAsIdentity(SecurityContext sc)
   {
      RunAs incomingRunAs = sc.getIncomingRunAs();
      return incomingRunAs != null && incomingRunAs instanceof RunAsIdentity;
   }
}