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

import java.security.Identity;
import java.security.Principal;
import java.util.Properties;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.TimerService;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aspects.currentinvocation.CurrentInvocation;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.interceptors.container.InvocationHelper;
import org.jboss.ejb3.security.helpers.EJBContextHelper;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.logging.Logger;
import org.jboss.security.RealmMapping;

/**
 * EJB3 Enterprise Context Implementation
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Anil.Saldhana@redhat.com
 * @version $Revision$
 */
public abstract class EJBContextImpl<T extends Container, B extends BeanContext<T>> implements EJBContext
{
   private static final Logger log = Logger.getLogger(EJBContextImpl.class);
   protected transient T container;
   protected transient RealmMapping rm;
   protected B beanContext;
   protected EJBContextHelper ejbContextHelper;
   
   protected EJBContextImpl(B beanContext)
   {
      assert beanContext != null : "beanContext is null";
      
      this.beanContext = beanContext;
      this.container = beanContext.getContainer();
      this.rm = container.getSecurityManager(RealmMapping.class);
      this.ejbContextHelper = new EJBContextHelper();
      
   }

   protected T getContainer()
   {
      return container;
   }
   
   protected RealmMapping getRm()
   {
      return rm;
   }

   /**
    * 
    */
   public Object lookup(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("name is null");
      String newName;
      if (name.startsWith("/"))
      {
         newName = "env" + name;
      }
      else
      {
         newName = "env/" + name;
      }
      try
      {
         return getContainer().getEnc().lookup(newName);
      }
      catch (NameNotFoundException ignored)
      {
         try 
         {
            return getContainer().getInitialContext().lookup(name);
         } 
         catch (NameNotFoundException ignored2)
         {
            throw new IllegalArgumentException("Unable to find an entry in java:comp/env (or global JNDI) for '" + name + "'");
         }
         catch(NamingException e)
         {
            throw new RuntimeException(e);
         }
      }
      catch(NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("deprecation")
   public Identity getCallerIdentity()
   {
      throw new IllegalStateException("deprecated");
   }
   
   /**
    * @see EJBContext#getCallerPrincipal()
    */
   public Principal getCallerPrincipal()
   {
      EJBContainer ec = (EJBContainer) container;
      SecurityDomain domain = ec.getAnnotation(SecurityDomain.class);
      Principal callerPrincipal = ejbContextHelper.getCallerPrincipal(SecurityActions.getSecurityContext(), 
            rm, domain); 
              
      // This method never returns null.
      if (callerPrincipal == null)
         throw new java.lang.IllegalStateException("No valid security context for the caller identity");
      
      return callerPrincipal;
   }

   
   @SuppressWarnings("deprecation")
   public boolean isCallerInRole(Identity role)
   {
      throw new IllegalStateException("deprecated");
   }
   
   /**
    * @see EJBContext#isCallerInRole(String)
    */
   public boolean isCallerInRole(String roleName)
   {
      EJBContainer ejbc = (EJBContainer)container; 
      //Take care of Policy Context ID for callbacks
      SecurityActions.setContextID(ejbc.getJaccContextId()); 
      
      return ejbContextHelper.isCallerInRole(SecurityActions.getSecurityContext(), 
            ejbc.getAnnotation(SecurityDomain.class), 
            rm, 
            ejbc.getXml(), 
            roleName, 
            ejbc.getEjbName()); 
   }
 

   public TimerService getTimerService() throws IllegalStateException
   {
      Invocation invocation = CurrentInvocation.getCurrentInvocation();
      if(InvocationHelper.isInjection(invocation))
         throw new IllegalStateException("getTimerService() not allowed during injection (EJB3 4.5.2)");
      return getContainer().getTimerService();
   }

   public UserTransaction getUserTransaction() throws IllegalStateException
   {
      return TxUtil.getUserTransaction(beanContext);
   }

   public EJBHome getEJBHome()
   {
      throw new IllegalStateException("EJB 3.0 does not have a home type.");
   }

   public EJBLocalHome getEJBLocalHome()
   {
      throw new IllegalStateException("EJB 3.0 does not have a home type.");
   }

   public Properties getEnvironment()
   {
      throw new EJBException("Deprecated");
   }

   public void setRollbackOnly() throws IllegalStateException
   {
      TxUtil.setRollbackOnly();
   }

   public boolean getRollbackOnly() throws IllegalStateException
   {
      return TxUtil.getRollbackOnly();
   }
}
