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
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.TimerService;
import javax.ejb.TransactionManagementType;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.ejb3.tx.UserTransactionImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRoleRef;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.integration.ejb.EJBAuthorizationHelper;
import org.jboss.security.plugins.SecurityContextAssociation;

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
   
   /** Principal for the bean associated with the call **/
   private Principal beanPrincipal;

   protected EJBContextImpl(B beanContext)
   {
      assert beanContext != null : "beanContext is null";
      
      this.beanContext = beanContext;
      this.container = beanContext.getContainer();
      this.rm = container.getSecurityManager(RealmMapping.class);
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

   /*public Principal getCallerPrincipal()
   {
      Principal principal = null;
      
      RunAsIdentity runAsIdentity = SecurityActions.peekRunAsIdentity(1);
    
      principal = SecurityAssociation.getCallerPrincipal();
      
      if (getRm() != null)
      {
         principal = getRm().getPrincipal(principal);
      }
      
      // This method never returns null.
      if (principal == null)
         throw new java.lang.IllegalStateException("No valid security context for the caller identity");

      return principal;
   }
*/
   
   public Principal getCallerPrincipal()
   {
      if(beanPrincipal == null)
      {
         EJBContainer ec = (EJBContainer) container;
         
         Principal callerPrincipal = null;
         
         RealmMapping rm = container.getSecurityManager(RealmMapping.class); 
         
         SecurityContext sc = SecurityContextAssociation.getSecurityContext();
         if(sc == null)
         {
            SecurityDomain domain =(SecurityDomain)ec.resolveAnnotation(SecurityDomain.class);
            String unauth = domain.unauthenticatedPrincipal();
            if(unauth != null && unauth.length() > 0)
            if(domain.unauthenticatedPrincipal() != null)
              callerPrincipal = new SimplePrincipal(unauth);             
         }
         else
         {
            EJBAuthorizationHelper helper = new EJBAuthorizationHelper(sc); 
            callerPrincipal = helper.getCallerPrincipal(rm); 
         }
         
         if(callerPrincipal == null)
         {
            //try the incoming principal
            callerPrincipal = sc.getUtil().getUserPrincipal();
            if(rm != null)
               callerPrincipal = rm.getPrincipal(callerPrincipal);
         } 
         
         if(callerPrincipal == null)
         {
            SecurityDomain domain =(SecurityDomain)ec.resolveAnnotation(SecurityDomain.class);
            String unauth = domain.unauthenticatedPrincipal();
            if(unauth != null && unauth.length() > 0)
            if(domain.unauthenticatedPrincipal() != null)
              callerPrincipal = new SimplePrincipal(unauth);
         }
         
         // This method never returns null.
         if (callerPrincipal == null)
            throw new java.lang.IllegalStateException("No valid security context for the caller identity");
  
         beanPrincipal = callerPrincipal;
      }      
      return beanPrincipal;
   }

   
   @SuppressWarnings("deprecation")
   public boolean isCallerInRole(Identity role)
   {
      throw new IllegalStateException("deprecated");
   }
   
   public boolean isCallerInRole(String roleName)
   {
      EJBContainer ejbc = (EJBContainer)container;
      SecurityContext sc = SecurityContextAssociation.getSecurityContext();
      if(sc == null)
      {
         SecurityDomain domain =(SecurityDomain)ejbc.resolveAnnotation(SecurityDomain.class);
         try
         {
            sc = SecurityActions.createSecurityContext(domain.value());
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException(e);
         }              
      }
      // TODO: this is to slow
      Set<SecurityRoleRefMetaData> roleRefs = new HashSet<SecurityRoleRefMetaData>();
      JBossEnterpriseBeanMetaData eb = ejbc.getXml();
      if(eb != null)
      {
         Collection<SecurityRoleRefMetaData> srf = eb.getSecurityRoleRefs(); 
         if(srf != null)
            roleRefs.addAll(srf);   
      } 
      
      //TODO: Get rid of this conversion asap
      Set<SecurityRoleRef> srset = new HashSet<SecurityRoleRef>();
      for(SecurityRoleRefMetaData srmd: roleRefs)
      {
         srset.add(new SecurityRoleRef(srmd.getRoleName(),srmd.getRoleLink(),null));
      }
      Principal principal = getCallerPrincipal();
      EJBAuthorizationHelper helper = new EJBAuthorizationHelper(sc);
      return helper.isCallerInRole(roleName, 
                                   ejbc.getEjbName(), 
                                   principal, 
                                   srset);
   }

   /*public boolean isCallerInRole(String roleName)
   {
      // TODO revert to aspects.security.SecurityContext impl when JBoss AOP 1.1 is out.
      Principal principal = getCallerPrincipal();
      
      // Check the caller of this beans run-as identity
      // todo use priveleged stuff in ejb class
      RunAsIdentity runAsIdentity = SecurityActions.peekRunAsIdentity(1);

      if (principal == null && runAsIdentity == null)
         return false;

      if (getRm() == null)
      {
         String msg = "isCallerInRole() called with no security context. "
                      + "Check that a security-domain has been set for the application.";
         throw new IllegalStateException(msg);
      }
      
      //Ensure that you go through the security role references that may be configured
      EJBContainer ejbc = (EJBContainer)container;
      if(ejbc.getXml() != null)
      {
         Collection<SecurityRoleRef> securityRoleRefs = ejbc.getXml().getSecurityRoleRefs();
         for(SecurityRoleRef roleRef: securityRoleRefs)
         {
            String refName = roleRef.getRoleName(); 
            if(roleName.equals(refName))
               roleName = roleRef.getRoleLink();
         } 
      } 

      HashSet set = new HashSet();
      set.add(new SimplePrincipal(roleName));

      // This is work in progress - currently, getRm().doesUserHaveRole(principal, set)
      // and getRm().getUserRoles(principal) ignores the principal parameter and is not
      // using the principal from the pushed RunAsIdentity
      boolean doesUserHaveRole = false;
      if (runAsIdentity != null)
         doesUserHaveRole = runAsIdentity.doesUserHaveRole(set);
       
      if (!doesUserHaveRole)
         doesUserHaveRole = getRm().doesUserHaveRole(principal, set);
      
      java.util.Set roles = getRm().getUserRoles(principal);
    
      return doesUserHaveRole;
   }*/

   public TimerService getTimerService() throws IllegalStateException
   {
      return getContainer().getTimerService();
   }

   public UserTransaction getUserTransaction() throws IllegalStateException
   {
      TransactionManagementType type = TxUtil.getTransactionManagementType(getContainer());
      if (type != TransactionManagementType.BEAN) throw new IllegalStateException("Container " + getContainer().getEjbName() + ": it is illegal to inject UserTransaction into a CMT bean");

      return new UserTransactionImpl();
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
      // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
      TransactionManagementType type = TxUtil.getTransactionManagementType(getContainer());
      if (type != TransactionManagementType.CONTAINER) throw new IllegalStateException("Container " + getContainer().getEjbName() + ": it is illegal to call setRollbackOnly from BMT: " + type);

      try
      {
         TransactionManager tm = TxUtil.getTransactionManager();

         // The getRollbackOnly and setRollBackOnly method of the SessionContext interface should be used
         // only in the session bean methods that execute in the context of a transaction.
         if (tm.getTransaction() == null)
            throw new IllegalStateException("setRollbackOnly() not allowed without a transaction.");

         tm.setRollbackOnly();
      }
      catch (SystemException e)
      {
         log.warn("failed to set rollback only; ignoring", e);
      }
   }

   public boolean getRollbackOnly() throws IllegalStateException
   {
      // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
      TransactionManagementType type = TxUtil.getTransactionManagementType(getContainer());
      if (type != TransactionManagementType.CONTAINER)
         throw new IllegalStateException("Container " + getContainer().getEjbName() + ": it is illegal to call getRollbackOnly from BMT: " + type);

      try
      {
         TransactionManager tm = TxUtil.getTransactionManager();

         // The getRollbackOnly and setRollBackOnly method of the SessionContext interface should be used
         // only in the session bean methods that execute in the context of a transaction.
         if (tm.getTransaction() == null)
            throw new IllegalStateException("getRollbackOnly() not allowed without a transaction.");

         // EJBTHREE-805, consider an asynchronous rollback due to timeout
         int status = tm.getStatus();
         return status == Status.STATUS_MARKED_ROLLBACK
             || status == Status.STATUS_ROLLING_BACK
             || status == Status.STATUS_ROLLEDBACK;
      }
      catch (SystemException e)
      {
         log.warn("failed to get tx manager status; ignoring", e);
         return true;
      }
   }
}
