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
package org.jboss.ejb3;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SecurityContextAssociation;

/**
 * A collection of privileged actions for this package
 *
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SecurityActions
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

   private static class PeekRunAsRoleAction implements PrivilegedAction
   {
      int depth;

      PeekRunAsRoleAction(int depth)
      {
         this.depth = depth;
      }

      public Object run()
      {
         RunAsIdentity principal = SecurityAssociation.peekRunAsIdentity(depth);
         return principal;
      }
   }
   
   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         Subject subject = SecurityAssociation.getSubject();
         return subject;
      }
   }
   
   private static class PopRunAsIdentityAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new PopRunAsIdentityAction();
      public Object run()
      {
         return SecurityAssociation.popRunAsIdentity();
      }
   }
   
   private static class PushRunAsIdentityAction implements PrivilegedAction
   {
      RunAsIdentity runAsIdentity;
      
      PushRunAsIdentityAction(RunAsIdentity runAsIdentity)
      {
         this.runAsIdentity = runAsIdentity;
      }
      
      public Object run()
      {
         SecurityAssociation.pushRunAsIdentity(runAsIdentity);
         return null;
      }
   }

   static ClassLoader getContextClassLoader()
   {
      return TCLAction.UTIL.getContextClassLoader();
   }

   static ClassLoader getContextClassLoader(Thread thread)
   {
      return TCLAction.UTIL.getContextClassLoader(thread);
   }

   static void setContextClassLoader(ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(loader);
   }

   static void setContextClassLoader(Thread thread, ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(thread, loader);
   }

   static String setContextID(String contextID)
   {
      PrivilegedAction action = new SetContextID(contextID);
      String previousID = (String) AccessController.doPrivileged(action);
      return previousID;
   }

   static RunAsIdentity peekRunAsIdentity(int depth)
   {
      PrivilegedAction action = new PeekRunAsRoleAction(depth);
      RunAsIdentity principal = (RunAsIdentity) AccessController.doPrivileged(action);
      return principal;
   }
   
   static Subject getActiveSubject()
   {
      Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
      return subject;
   }
   
   static void pushRunAsIdentity(RunAsIdentity runAsIdentity)
   {     
      PrivilegedAction action = new PushRunAsIdentityAction(runAsIdentity);
      AccessController.doPrivileged(action);
   }
   
   static void pushRunAs(final RunAsIdentity runAsIdentity)
   {  
      AccessController.doPrivileged(new PrivilegedAction() 
      { 
         public Object run()
         {
            SecurityContext sc = getSecurityContext();
            sc.setOutgoingRunAs(runAsIdentity);
            return null;
         }
      }); 
   }
   
   static SecurityContext getSecurityContext()
   {
      return (SecurityContext) AccessController.doPrivileged(new PrivilegedAction() 
      {

         public Object run()
         { 
            return SecurityContextAssociation.getSecurityContext();
         }
         
      });
   }
   
   static RunAsIdentity popRunAsIdentity()
   {     
      return (RunAsIdentity)AccessController.doPrivileged(PopRunAsIdentityAction.ACTION);
   }
   
   static RunAsIdentity popRunAs()
   {     
      return (RunAsIdentity)AccessController.doPrivileged(new PrivilegedAction() 
      { 
         public Object run()
         {
            SecurityContext sc = getSecurityContext();
            RunAsIdentity ra = (RunAsIdentity) sc.getOutgoingRunAs();
            sc.setOutgoingRunAs(null);
            return ra;
         }
      }); 
   }

   interface TCLAction
   {
      class UTIL
      {
         static TCLAction getTCLAction()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }

         static ClassLoader getContextClassLoader()
         {
            return getTCLAction().getContextClassLoader();
         }

         static ClassLoader getContextClassLoader(Thread thread)
         {
            return getTCLAction().getContextClassLoader(thread);
         }

         static void setContextClassLoader(ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(cl);
         }

         static void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(thread, cl);
         }
      }

      TCLAction NON_PRIVILEGED = new TCLAction()
      {
         public ClassLoader getContextClassLoader()
         {
            return Thread.currentThread().getContextClassLoader();
         }

         public ClassLoader getContextClassLoader(Thread thread)
         {
            return thread.getContextClassLoader();
         }

         public void setContextClassLoader(ClassLoader cl)
         {
            Thread.currentThread().setContextClassLoader(cl);
         }

         public void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            thread.setContextClassLoader(cl);
         }
      };

      TCLAction PRIVILEGED = new TCLAction()
      {
         private final PrivilegedAction getTCLPrivilegedAction = new PrivilegedAction()
         {
            public Object run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         };

         public ClassLoader getContextClassLoader()
         {
            return (ClassLoader) AccessController.doPrivileged(getTCLPrivilegedAction);
         }

         public ClassLoader getContextClassLoader(final Thread thread)
         {
            return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  return thread.getContextClassLoader();
               }
            });
         }

         public void setContextClassLoader(final ClassLoader cl)
         {
            AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  Thread.currentThread().setContextClassLoader(cl);
                  return null;
               }
            });
         }

         public void setContextClassLoader(final Thread thread, final ClassLoader cl)
         {
            AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  thread.setContextClassLoader(cl);
                  return null;
               }
            });
         }
      };

      ClassLoader getContextClassLoader();

      ClassLoader getContextClassLoader(Thread thread);

      void setContextClassLoader(ClassLoader cl);

      void setContextClassLoader(Thread thread, ClassLoader cl);
   }
   
   static SecurityContext createSecurityContext(final String securityDomain) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>()
      { 
         public SecurityContext run() throws Exception
         {
            return SecurityContextFactory.createSecurityContext(securityDomain);
         }});
   }
}
