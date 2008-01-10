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
 
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation; 
import org.jboss.ejb3.SecurityActions;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAsIdentity; 
import org.jboss.security.SecurityAssociation; 

import javax.security.auth.Subject;

/**
 * An interceptor that enforces the run-as identity declared by a bean.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>.
 * @version $Revision$
 */
public class RunAsSecurityInterceptor extends org.jboss.aspects.security.RunAsSecurityInterceptor
{
   private static final Logger log = Logger.getLogger(RunAsSecurityInterceptor.class);
   private RunAsIdentity runAsIdentity;

   public RunAsSecurityInterceptor(AuthenticationManager manager, RealmMapping realmMapping, RunAsIdentity id)
   {
      super(manager, realmMapping);
      this.runAsIdentity = id; 
   }

   protected RunAsIdentity getRunAsIdentity(Invocation invocation)
   {
      MethodInvocation mi = (MethodInvocation)invocation;
      return runAsIdentity;
   }

   public Object invoke(Invocation invocation) throws Throwable
   { 
      Subject previousSubject = null;
      try
      {
         RunAsIdentity runAsIdentity = getRunAsIdentity(invocation);
         SecurityActions.pushRunAsIdentity(runAsIdentity);
         
         runAsIdentity = SecurityActions.peekRunAsIdentity(1);
         if (runAsIdentity != null)
         {
            previousSubject = SecurityActions.getActiveSubject();
            java.util.Set newPrincipals = runAsIdentity.getPrincipalsSet();
            Subject newSubject = new Subject(false, newPrincipals, new java.util.HashSet(), new java.util.HashSet());
            SecurityAssociation.setSubject(newSubject);
         }
         
         return invocation.invokeNext(); 
      }
      finally
      {
         if (previousSubject != null)
            SecurityAssociation.setSubject(previousSubject);
         
         SecurityActions.popRunAsIdentity();
      }
   } 

}
