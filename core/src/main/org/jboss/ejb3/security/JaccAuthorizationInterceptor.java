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

import javax.security.jacc.EJBMethodPermission;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;
import org.jboss.security.RealmMapping;
import org.jboss.security.jacc.DelegatingPolicy;


/**
 * This interceptor is where the JACC authorization is performed.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @version $Revision$
 */
public class JaccAuthorizationInterceptor implements Interceptor
{
   private static final Logger log = Logger.getLogger(JaccAuthorizationInterceptor.class);
   
   public static final String JACC = "JACC";
   public static final String CTX = "ctx";

   private String ejbName;
   private CodeSource ejbCS;
   private RealmMapping realmMapping;

   public JaccAuthorizationInterceptor(String ejbName, CodeSource cs)
   {
      this.ejbName = ejbName;
      this.ejbCS = cs;
   }

   public String getName()
   {
      return "JaccAuthorizationInterceptor";
   }
   
   public void setRealmMapping(RealmMapping ssm)
   {
      this.realmMapping = ssm;
   }

   public Object invoke(Invocation inv) throws Throwable
   {
      try
      {
         checkSecurityAssociation((MethodInvocation) inv);
         return inv.invokeNext();
      }
      catch (ClassCastException e)
      {
         throw new RuntimeException("Jacc authorization is only available for method invocations", e);
      }
   }

   /**
    * Authorize the caller's access to the method invocation
    */
   private void checkSecurityAssociation(MethodInvocation mi) throws Throwable
   {
      String contextID = (String) mi.getMetaData(JACC, CTX);
      SecurityActions.setContextID(contextID);
      
      if(log.isTraceEnabled())
         log.trace("permissions: " + DelegatingPolicy.getInstance().getPermissions(ejbCS));
      
      //EJBArgsPolicyContextHandler.setArgs(mi.getArguments());

      //Set custom JACC policy handlers - Following used in EJB 2, but just seems to be ignored
      //BeanMetaDataPolicyContextHandler.setMetaData(null);

      Method m = mi.getMethod();
      
      SecurityHelper shelper = new SecurityHelper();
      
      String iface = !shelper.isLocalCall(mi) ? "Remote" : "Local";

      EJBMethodPermission methodPerm = new EJBMethodPermission(ejbName, iface, m);
      if(realmMapping != null)
      { 
         JaccHelper.checkPermission(ejbCS, methodPerm,realmMapping);  
      }
      /*// Get the caller
      Subject caller = SecurityActions.getContextSubject(); 

      Principal[] principals = null;
      if( caller != null )
      {
         // Get the caller principals
         Set principalsSet = caller.getPrincipals();
         principals = new Principal[principalsSet.size()];
         principalsSet.toArray(principals);      
      }

      ProtectionDomain pd = new ProtectionDomain (ejbCS, null, null, principals);
      if( policy.implies(pd, methodPerm) == false )
      {
         String msg = "Denied: "+methodPerm+", caller=" + caller;
         SecurityException e = new SecurityException(msg);
         throw e;
      }*/
   }
}
