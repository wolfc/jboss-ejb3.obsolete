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

import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
  
import org.jboss.security.RunAs;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SubjectInfo;
import org.jboss.security.SecurityContextAssociation;


/**
 * A collection of privileged actions for this package
 * <p/>
 * Copy paste from package org.jboss.ejb.plugins and org.jboss.ejb.
 * Did not want to rely on this from org.jboss.ejb.plugins package since this is
 * packaged in jboss.jar which will not be part of the embedded distribution.
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @author Anil.Saldhana@jboss.org
 * @version $Revison: 1.1$
 */
class SecurityActions
{
   private static class SetContextID implements PrivilegedAction
   {
      String contextID;

      SetContextID(String contextID)
      {
         this.contextID = contextID;
      }

      public Object run()
      {
         String previousID = PolicyContext.getContextID();
         PolicyContext.setContextID(contextID);
         return previousID;
      }
   }

   interface PolicyContextActions
   {
      /**
       * The JACC PolicyContext key for the current Subject
       */
      static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";
      PolicyContextActions PRIVILEGED = new PolicyContextActions()
      {
         private final PrivilegedExceptionAction exAction = new PrivilegedExceptionAction()
         {
            public Object run() throws Exception
            {
               return (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
            }
         };

         public Subject getContextSubject()
                 throws PolicyContextException
         {
            try
            {
               return (Subject) AccessController.doPrivileged(exAction);
            }
            catch (PrivilegedActionException e)
            {
               Exception ex = e.getException();
               if (ex instanceof PolicyContextException)
                  throw (PolicyContextException) ex;
               else
                  throw new UndeclaredThrowableException(ex);
            }
         }
      };

      PolicyContextActions NON_PRIVILEGED = new PolicyContextActions()
      {
         public Subject getContextSubject()
                 throws PolicyContextException
         {
            return (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         }
      };

      Subject getContextSubject()
              throws PolicyContextException;
   }
   
   interface RunAsIdentityActions
   {
      RunAsIdentityActions PRIVILEGED = new RunAsIdentityActions()
      {
         private final PrivilegedAction peekAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.peekRunAsIdentity();
            }
         };

         private final PrivilegedAction popAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.popRunAsIdentity();
            }
         };

         public RunAsIdentity peek()
         {
            return (RunAsIdentity)AccessController.doPrivileged(peekAction);
         }

         public void push(final RunAsIdentity id)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     SecurityAssociation.pushRunAsIdentity(id);
                     return null;
                  }
               }
            );
         }

         public RunAsIdentity pop()
         {
            return (RunAsIdentity)AccessController.doPrivileged(popAction);
         }
      };

      RunAsIdentityActions NON_PRIVILEGED = new RunAsIdentityActions()
      {
         public RunAsIdentity peek()
         {
            return SecurityAssociation.peekRunAsIdentity();
         }

         public void push(RunAsIdentity id)
         {
            SecurityAssociation.pushRunAsIdentity(id);
         }

         public RunAsIdentity pop()
         {
            return SecurityAssociation.popRunAsIdentity();
         }
      };

      RunAsIdentity peek();

      void push(RunAsIdentity id);

      RunAsIdentity pop();
   } 

   static Subject getContextSubject()
           throws PolicyContextException
   {
      if (System.getSecurityManager() == null)
      {
         return PolicyContextActions.NON_PRIVILEGED.getContextSubject();
      }
      else
      {
         return PolicyContextActions.PRIVILEGED.getContextSubject();
      }
   }

   static String setContextID(String contextID)
   {
      PrivilegedAction action = new SetContextID(contextID);
      String previousID = (String) AccessController.doPrivileged(action);
      return previousID;
   }
   
   static RunAsIdentity peekRunAsIdentity()
   {
      if(System.getSecurityManager() == null)
      {
         return RunAsIdentityActions.NON_PRIVILEGED.peek();
      }
      else
      {
         return RunAsIdentityActions.PRIVILEGED.peek();
      }
   }
   
   static void pushRunAsIdentity(RunAsIdentity principal)
   {
      if(System.getSecurityManager() == null)
      {
         RunAsIdentityActions.NON_PRIVILEGED.push(principal);
      }
      else
      {
         RunAsIdentityActions.PRIVILEGED.push(principal);
      }
   }
   
   static RunAsIdentity popRunAsIdentity()
   {
      if(System.getSecurityManager() == null)
      {
         return RunAsIdentityActions.NON_PRIVILEGED.pop();
      }
      else
      {
         return RunAsIdentityActions.PRIVILEGED.pop();
      }
   }
   
   static Principal getCallerPrincipal()
   {
      return (Principal)AccessController.doPrivileged(new PrivilegedAction<Principal>(){

         public Principal run()
         { 
            return SecurityAssociation.getCallerPrincipal();
         }});
   }
   
   static SecurityContext createSecurityContext(final String domainName) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>(){

      public SecurityContext run() throws Exception
      { 
        return SecurityContextFactory.createSecurityContext(domainName);
      }
     });
   }
   
   static SecurityContext createSecurityContext(final Principal p, final Object cred,
         final Subject s, final String domainName) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>()
      {
         public SecurityContext run() throws Exception
         { 
            return SecurityContextFactory.createSecurityContext(p, cred,s,domainName);
         }});
   }
   
   
   static SecurityContext getSecurityContext()
   {
      return  AccessController.doPrivileged(new PrivilegedAction<SecurityContext>(){

         public SecurityContext run()
         { 
            return SecurityContextAssociation.getSecurityContext();
         }});
   }
   
   static void setSecurityContext(final SecurityContext sc)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>(){

         public Object run()
         { 
            SecurityContextAssociation.setSecurityContext(sc);
            return null;
         }});
   }
   
   static void pushSubjectContext(final Principal p, final Object cred, final Subject s)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>(){

         public Object run()
         {
            SecurityContext sc = getSecurityContext(); 
            if(sc == null)
               throw new IllegalStateException("Security Context is null");
            sc.getUtil().createSubjectInfo(p, cred, s); 
            return null;
         }}
      );
   } 
   
   static RunAs peekRunAs()
   {
      return AccessController.doPrivileged(new PrivilegedAction<RunAs>()
      { 
         public RunAs run()
         {
            SecurityContext sc = SecurityContextAssociation.getSecurityContext();
            if(sc == null)
               throw new IllegalStateException("Security Context is null");
            return sc.getIncomingRunAs(); 
         } 
      });
      
   }
   static void pushCallerRunAsIdentity(final RunAs ra)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>(){ 
         public Object run()
         {
            SecurityContext sc = SecurityContextAssociation.getSecurityContext();
            if(sc == null)
               throw new IllegalStateException("Security Context is null");
            sc.setIncomingRunAs(ra);
            return null;
         } 
      }); 
   }
   

   static void popCallerRunAsIdentity()
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>(){ 
         public Object run()
         {
            SecurityContext sc = SecurityContextAssociation.getSecurityContext();
            if(sc == null)
               throw new IllegalStateException("Security Context is null");
            sc.setIncomingRunAs(null);
            return null;
         } 
      }); 
   }
   
   static void setIncomingRunAs(final SecurityContext sc, final RunAs incoming)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         public Object run()
         {
            sc.setIncomingRunAs(incoming); 
            return null;
         } 
      });
   }
   
   static void setOutgoingRunAs(final SecurityContext sc, final RunAs outgoing)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         public Object run()
         {
            sc.setOutgoingRunAs(outgoing); 
            return null;
         } 
      });
   }
   
   static void setSubjectInfo(final SecurityContext sc, final SubjectInfo info)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         public Object run()
         {
            sc.setSubjectInfo(info);  
            return null;
         } 
      });
   } 
   
   static Class<?> loadClass(final String fqn) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>()
      { 
         public Class<?> run() throws Exception
         {
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            return tcl.loadClass(fqn); 
         }
      });
   }
   
   static RunAsIdentity popRunAs()
   {     
      return AccessController.doPrivileged(new PrivilegedAction<RunAsIdentity>() 
      { 
         public RunAsIdentity run()
         {
            SecurityContext sc = getSecurityContext();
            RunAsIdentity ra = (RunAsIdentity) sc.getOutgoingRunAs();
            sc.setOutgoingRunAs(null);
            return ra;
         }
      }); 
   }
}
