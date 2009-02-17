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
 
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.mdb.MessagingContainer;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityContext;

/**
 * An interceptor that enforces the run-as identity declared by a bean.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>.
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 61914 $
 */
public class RunAsSecurityInterceptorv2 implements Interceptor
{ 
   private RunAsIdentity runAsIdentity;
   private EJBContainer container;

   public RunAsSecurityInterceptorv2(EJBContainer container, RunAsIdentity id)
   {
      this.runAsIdentity = id; 
      this.container = container;
   }

   protected RunAsIdentity getRunAsIdentity(Invocation invocation)
   { 
      return runAsIdentity;
   }

   /**
    * @see Interceptor#invoke(Invocation)
    */
   public Object invoke(Invocation invocation) throws Throwable
   { 
      SecurityContext cachedContext = null;
      
      //Check for ejbTimeOut
      SecurityHelper shelper = new SecurityHelper();
      if(shelper.isEJBTimeOutCallback(((MethodInvocation) invocation).getMethod())) 
         return invocation.invokeNext();
      
      SecurityContext sc = SecurityActions.getSecurityContext();
      
      cachedContext = sc;
     
      /**
       * An MDB always starts with a null security context coming in
       */
      if(container instanceof MessagingContainer)
      {
         sc = null;
      }
      
      if(sc == null)
      {
         SecurityDomain domain = (SecurityDomain)container.getAnnotation(SecurityDomain.class);
         if(domain != null)
         {
            sc = SecurityActions.createSecurityContext(domain.value());
            SecurityActions.setSecurityContext(sc);
         }  
      }
      if(sc != null)
        sc.setOutgoingRunAs(runAsIdentity);
      
      try
      {
         return invocation.invokeNext(); 
      }
      finally
      {
         if(sc != null)
           SecurityActions.popRunAs();
         SecurityActions.setSecurityContext(cachedContext);
      }
   }

   /**
    * @see Interceptor#getName()
    */
   public String getName()
   { 
      return getClass().getName();
   }  
}