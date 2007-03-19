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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.Identity;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.aop.Advisor;
import org.jboss.ejb3.security.SecurityDomainManager;
import org.jboss.ejb3.stateless.StatelessBeanContext;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.ejb3.tx.UserTransactionImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.SecurityRoleRefMetaData;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class BaseSessionContext implements SessionContext, MessageDrivenContext, Externalizable
{
   private static final long serialVersionUID = -2485139227124937217L;
   
   private static final Logger log = Logger.getLogger(BaseSessionContext.class);
   protected transient Container container;
   protected transient RealmMapping rm;
   protected BaseContext baseContext;

   public BaseSessionContext()
   {
   }

   public void setBaseContext(BaseContext baseContext)
   {
      this.baseContext = baseContext;
   }

   public Container getContainer()
   {
      return container;
   }

   public void setContainer(Container container)
   {
      this.container = container;
      try
      {
         InitialContext ctx = container.getInitialContext();
         setupSecurityDomain(container, ctx);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   private void setupSecurityDomain(Container container, InitialContext ctx)
           throws NamingException
   {
      SecurityDomain securityAnnotation = (SecurityDomain) ((Advisor) container).resolveAnnotation(SecurityDomain.class);
      if (securityAnnotation == null) return;
      Object domain = SecurityDomainManager.getSecurityManager(securityAnnotation.value(), ctx);
      rm = (RealmMapping) domain;
   }

   protected RealmMapping getRm()
   {
      return rm;
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(container.getObjectName().getCanonicalName());
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      container = Ejb3Registry.getContainer(in.readUTF());
      InitialContext ctx = container.getInitialContext();
      try
      {
         setupSecurityDomain(container, ctx);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }

   }


   //----------------

   public Object lookup(String name)
   {
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
      catch (NamingException ignored)
      {
         try 
         {
            return getContainer().getInitialContext().lookup(name);
         } 
         catch (NamingException ignored2)
         {
            
         }
      }
      return null;
   }

   public Identity getCallerIdentity()
   {
      throw new IllegalStateException("deprecated");
   }

   public Principal getCallerPrincipal()
   {
      Principal principal = SecurityAssociation.getCallerPrincipal();
      if (getRm() != null)
      {
         principal = getRm().getPrincipal(principal);
      }

      // This method never returns null.
      if (principal == null)
         throw new java.lang.IllegalStateException("No valid security context for the caller identity");

      return principal;
   }

   public boolean isCallerInRole(Identity role)
   {
      throw new IllegalStateException("deprecated");
   }

   public boolean isCallerInRole(String roleName)
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
         List<SecurityRoleRefMetaData> securityRoleRefs = ejbc.getXml().getSecurityRoleReferences();
         for(SecurityRoleRefMetaData srmd: securityRoleRefs)
         {
            String rname = srmd.getName(); 
            if(roleName.equals(rname))
               roleName = srmd.getLink();
         } 
      } 

      HashSet set = new HashSet();
      set.add(new SimplePrincipal(roleName));

      if (runAsIdentity == null)
         return getRm().doesUserHaveRole(principal, set);
      else
         return runAsIdentity.doesUserHaveRole(set);
   }

   public TimerService getTimerService() throws IllegalStateException
   {
      return getContainer().getTimerService();
   }

   public UserTransaction getUserTransaction() throws IllegalStateException
   {
      TransactionManagementType type = TxUtil.getTransactionManagementType(((Advisor) getContainer()));
      if (type != TransactionManagementType.BEAN) throw new IllegalStateException("Container " + getContainer().getEjbName() + ": it is illegal to inject UserTransaction into a CMT bean");

      return new UserTransactionImpl();
   }

   public EJBHome getEJBHome()
   {
      throw new EJBException("EJB 3.0 does not have a home type.");
   }

   public EJBLocalHome getEJBLocalHome()
   {
      throw new EJBException("EJB 3.0 does not have a home type.");
   }

   public Properties getEnvironment()
   {
      throw new EJBException("Deprecated");
   }

   public void setRollbackOnly() throws IllegalStateException
   {
      // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
      TransactionManagementType type = TxUtil.getTransactionManagementType(((Advisor) getContainer()));
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
      TransactionManagementType type = TxUtil.getTransactionManagementType(((Advisor) getContainer()));
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

   public EJBLocalObject getEJBLocalObject() throws IllegalStateException
   {
      try
      {
         return (EJBLocalObject)container.getInitialContext().lookup(ProxyFactoryHelper.getLocalJndiName(container, false));
      }
      catch (NamingException e)
      {
         throw new IllegalStateException(e);
      }
   }

   public EJBObject getEJBObject() throws IllegalStateException
   {
      try
      {
         return (EJBObject)container.getInitialContext().lookup(ProxyFactoryHelper.getRemoteJndiName(container, false));
      }
      catch (NamingException e)
      {
         throw new IllegalStateException(e);
      }
   }

   public Object getBusinessObject(Class businessInterface) throws IllegalStateException
   {
      return ((EJBContainer)container).getBusinessObject(baseContext, businessInterface); 
   }
   
   public Class getInvokedBusinessInterface() throws IllegalStateException
   {
      return ((SessionContainer)container).getInvokedBusinessInterface();
   }

   public MessageContext getMessageContext() throws IllegalStateException
   {
      // disallowed for stateful session beans (EJB3 FR 4.4.1 p 81)
      if(baseContext instanceof StatelessBeanContext)
      {
         MessageContext ctx = ((StatelessBeanContext) baseContext).getMessageContextJAXRPC();
         if(ctx == null)
            throw new IllegalStateException("No message context found");
         return ctx;
      }
      throw new UnsupportedOperationException("Only stateless beans can have a message context");
   }

}
