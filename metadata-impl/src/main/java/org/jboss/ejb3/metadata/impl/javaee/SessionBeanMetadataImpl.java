/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.metadata.impl.javaee;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.TransactionManagementType;

import org.jboss.ejb3.metadata.spi.javaee.AroundInvokeMetaData;
import org.jboss.ejb3.metadata.spi.javaee.InitMethodMetaData;
import org.jboss.ejb3.metadata.spi.javaee.LifecycleCallbackMetaData;
import org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.NamedMethodMetaData;
import org.jboss.ejb3.metadata.spi.javaee.RemoveMethodMetaData;
import org.jboss.ejb3.metadata.spi.javaee.SecurityRoleRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData;
import org.jboss.ejb3.metadata.spi.javaee.SessionType;
import org.jboss.metadata.ejb.spec.AroundInvokesMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.jboss.metadata.ejb.spec.InitMethodsMetaData;
import org.jboss.metadata.ejb.spec.RemoveMethodsMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferencesMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;

/**
 * SessionBeanMetadataImpl
 *
 * Represents the metadata for a session bean
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SessionBeanMetadataImpl extends EnterpriseBeanMetadataImpl implements SessionBeanMetaData
{

   /**
    * The {@link org.jboss.metadata.ejb.spec.SessionBeanMetaData} from which this
    * {@link SessionBeanMetadataImpl} was constructed
    */
   private org.jboss.metadata.ejb.spec.SessionBeanMetaData delegate;

   /**
    * The around-invoke methods for this bean
    */
   private List<AroundInvokeMetaData> aroundInvokes;

   /**
    * Fully qualified classnames of the business local interfaces of this 
    * session bean
    */
   private Set<String> businessLocals;

   /**
    * Fully qualified classnames of the business remote interfaces of the 
    * session bean
    */
   private Set<String> businessRemotes;

   /**
    * Fully qualified classname of the home interface of this bean
    */
   private String home;

   /**
    * Init methods of this session bean
    */
   private List<InitMethodMetaData> initMethods;

   /**
    * Fully qualified classname of the (EJB 2.x) local interface
    * of this session bean
    */
   private String local;

   /**
    * Fully qualified classname of the local home interface of
    * this session bean
    */
   private String localHome;

   /**
    * Message destination references
    */
   private List<MessageDestinationRefMetaData> messageDestinationRefs;

   /**
    * Post activates of this bean
    */
   private List<LifecycleCallbackMetaData> postActivates;

   /**
    * Pre passivates of this bean
    */
   private List<LifecycleCallbackMetaData> prePassivates;

   /**
    * Fully qualified classname of the (EJB 2.x) remote interface 
    * of this bean
    */
   private String remote;

   /**
    * Remove methods of this session bean
    */
   private List<RemoveMethodMetaData> removeMethods;

   /**
    * Security role references
    */
   private List<SecurityRoleRefMetaData> securityRoleRefs;

   /**
    * Fully qualified classname of the service endpoint
    */
   private String serviceEndpoint;

   /**
    * Session type of the bean
    */
   private SessionType sessionType;

   /**
    * timeout method of this bean
    */
   private NamedMethodMetaData timeoutMethod;

   /**
    * Transaction management type of this bean
    */
   private TransactionManagementType transactionManagementType;

   /**
    * Constructs a {@link SessionBeanMetadataImpl} out of a {@link org.jboss.metadata.ejb.spec.SessionBeanMetaData}
    * 
    * @param sessionBean
    * @throws NullPointerException If the passed <code>sessionBean</code> is null
    */
   public SessionBeanMetadataImpl(org.jboss.metadata.ejb.spec.SessionBeanMetaData sessionBean)
   {
      super(sessionBean);
      this.initialize(sessionBean);
   }

   /**
    * Initializes this {@link SessionBeanMetadataImpl} from the state in <code>sessionBean</code>
    * 
    * @param sessionBean
    * @throws NullPointerException If the passed <code>sessionBean</code> is null
    */
   private void initialize(org.jboss.metadata.ejb.spec.SessionBeanMetaData sessionBean)
   {
      // set the delegate
      this.delegate = sessionBean;

      this.home = this.delegate.getHome();
      this.local = this.delegate.getLocal();
      this.localHome = this.delegate.getLocalHome();
      this.remote = this.delegate.getRemote();
      this.serviceEndpoint = this.delegate.getServiceEndpoint();

      // transaction management type
      this.transactionManagementType = this.delegate.getTransactionType();

      // session type
      org.jboss.metadata.ejb.spec.SessionType delegateSessionType = this.delegate.getSessionType();
      if (delegateSessionType != null)
      {
         this.sessionType = delegateSessionType == org.jboss.metadata.ejb.spec.SessionType.Stateless
               ? SessionType.STATELESS
               : SessionType.STATEFUL;
      }

      // around invokes
      AroundInvokesMetaData delegateAroundInvokes = this.delegate.getAroundInvokes();
      if (delegateAroundInvokes != null)
      {
         this.aroundInvokes = new ArrayList<AroundInvokeMetaData>(delegateAroundInvokes.size());
         for (org.jboss.metadata.ejb.spec.AroundInvokeMetaData aroundInvoke : delegateAroundInvokes)
         {
            this.aroundInvokes.add(new AroundInvokeMetadataImpl(aroundInvoke));
         }
      }

      // business locals
      BusinessLocalsMetaData delegateBusinessLocalsMD = this.delegate.getBusinessLocals();
      if (delegateBusinessLocalsMD != null)
      {
         this.businessLocals = new HashSet<String>(delegateBusinessLocalsMD.size());
         for (String businessLocal : delegateBusinessLocalsMD)
         {
            this.businessLocals.add(businessLocal);
         }
      }

      // business remotes
      BusinessRemotesMetaData delegateBusinessRemotesMD = this.delegate.getBusinessRemotes();
      if (delegateBusinessRemotesMD != null)
      {
         this.businessRemotes = new HashSet<String>(delegateBusinessRemotesMD.size());
         for (String businessRemote : delegateBusinessRemotesMD)
         {
            this.businessRemotes.add(businessRemote);
         }
      }

      // init methods
      InitMethodsMetaData delegateInitMethods = this.delegate.getInitMethods();
      if (delegateInitMethods != null)
      {
         this.initMethods = new ArrayList<InitMethodMetaData>(delegateInitMethods.size());
         for (org.jboss.metadata.ejb.spec.InitMethodMetaData initMethod : delegateInitMethods)
         {
            this.initMethods.add(new InitMethodMetadataImpl(initMethod));
         }
      }

      // message destination refs
      MessageDestinationReferencesMetaData delegateMessageDestRefs = this.delegate.getMessageDestinationReferences();
      if (delegateMessageDestRefs != null)
      {
         this.messageDestinationRefs = new ArrayList<MessageDestinationRefMetaData>(delegateMessageDestRefs.size());
         for (MessageDestinationReferenceMetaData messageDestRef : delegateMessageDestRefs)
         {
            this.messageDestinationRefs.add(new MessageDestinationRefMetadataImpl(messageDestRef));
         }

      }

      // post activates
      LifecycleCallbacksMetaData delegatePostActivates = this.delegate.getPostActivates();
      if (delegatePostActivates != null)
      {
         this.postActivates = new ArrayList<LifecycleCallbackMetaData>(delegatePostActivates.size());
         for (org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData postActivate : delegatePostActivates)
         {
            this.postActivates.add(new LifecycleCallbackMetadataImpl(postActivate));
         }
      }

      // pre passivates
      LifecycleCallbacksMetaData delegatePrePassivates = this.delegate.getPrePassivates();
      if (delegatePrePassivates != null)
      {
         this.prePassivates = new ArrayList<LifecycleCallbackMetaData>(delegatePrePassivates.size());
         for (org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData prePassivate : delegatePrePassivates)
         {
            this.prePassivates.add(new LifecycleCallbackMetadataImpl(prePassivate));
         }
      }

      // remove methods
      RemoveMethodsMetaData delegateRemoveMethods = this.delegate.getRemoveMethods();
      if (delegateRemoveMethods != null)
      {
         this.removeMethods = new ArrayList<RemoveMethodMetaData>(delegateRemoveMethods.size());
         for (org.jboss.metadata.ejb.spec.RemoveMethodMetaData removeMethod : delegateRemoveMethods)
         {
            this.removeMethods.add(new RemoveMethodMetadataImpl(removeMethod));
         }
      }

      // security role references
      SecurityRoleRefsMetaData delegateSecurityRoleRefs = this.delegate.getSecurityRoleRefs();
      if (delegateSecurityRoleRefs != null)
      {
         this.securityRoleRefs = new ArrayList<SecurityRoleRefMetaData>(delegateSecurityRoleRefs.size());
         for (org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData securityRoleRef : delegateSecurityRoleRefs)
         {
            this.securityRoleRefs.add(new SecurityRoleRefMetadataImpl(securityRoleRef));
         }
      }

      // timeout method
      org.jboss.metadata.ejb.spec.NamedMethodMetaData delegateTimeoutMethod = this.delegate.getTimeoutMethod();
      if (delegateTimeoutMethod != null)
      {
         this.timeoutMethod = new NamedMethodMetadataImpl(delegateTimeoutMethod);
      }
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getAroundInvokes()
    */
   public List<AroundInvokeMetaData> getAroundInvokes()
   {
      return this.aroundInvokes;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getBusinessLocals()
    */
   public Set<String> getBusinessLocals()
   {
      return this.businessLocals;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getBusinessRemotes()
    */
   public Set<String> getBusinessRemotes()
   {
      return this.businessRemotes;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getHome()
    */
   public String getHome()
   {
      return this.home;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getInitMethods()
    */
   public List<InitMethodMetaData> getInitMethods() throws IllegalStateException
   {
      if (!this.isStateful())
      {
         throw new IllegalStateException("init-method is applicable only for stateful beans. This bean "
               + this.getEjbName() + " is not stateful");
      }
      return this.initMethods;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getLocal()
    */
   public String getLocal()
   {
      return this.local;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getLocalHome()
    */
   public String getLocalHome()
   {
      return this.localHome;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getMessageDestinationRefs()
    */
   public List<MessageDestinationRefMetaData> getMessageDestinationRefs()
   {
      return this.messageDestinationRefs;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getPostActivates()
    */
   public List<LifecycleCallbackMetaData> getPostActivates()
   {
      return this.postActivates;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getPrePassivates()
    */
   public List<LifecycleCallbackMetaData> getPrePassivates()
   {
      return this.prePassivates;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getRemote()
    */
   public String getRemote()
   {
      return this.remote;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getRemoveMethods()
    */
   public List<RemoveMethodMetaData> getRemoveMethods() throws IllegalStateException
   {
      if (!this.isStateful())
      {
         throw new IllegalStateException("remove-method is applicable only for stateful beans. This bean: "
               + this.getEjbName() + " is not stateful");
      }
      return this.removeMethods;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getSecurityRoleRefs()
    */
   public List<SecurityRoleRefMetaData> getSecurityRoleRefs()
   {
      return this.securityRoleRefs;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getServiceEndpoint()
    */
   public String getServiceEndpoint() throws IllegalStateException
   {
      return this.serviceEndpoint;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getSessionType()
    */
   public SessionType getSessionType()
   {
      return this.sessionType;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getTimeoutMethod()
    */
   public NamedMethodMetaData getTimeoutMethod()
   {
      return this.timeoutMethod;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#getTransactionType()
    */
   public TransactionManagementType getTransactionType()
   {
      return this.transactionManagementType;
   }

   /** 
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#isStateful()
    */
   public boolean isStateful()
   {
      return this.delegate.isStateful();
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#isStateless()
    */
   public boolean isStateless()
   {
      return this.delegate.isStateless();
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setAroundInvokes(java.util.List)
    */
   public void setAroundInvokes(List<AroundInvokeMetaData> aroundInvokes)
   {
      this.aroundInvokes = aroundInvokes;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setBusinessLocals(java.util.Set)
    */
   public void setBusinessLocals(Set<String> businessLocals)
   {
      this.businessLocals = businessLocals;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setBusinessRemotes(java.util.Set)
    */
   public void setBusinessRemotes(Set<String> businessRemotes)
   {
      this.businessRemotes = businessRemotes;

   }

   /** 
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setHome(java.lang.String)
    */
   public void setHome(String homeInterface)
   {
      this.home = homeInterface;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setInitMethods(java.util.List)
    */
   public void setInitMethods(List<InitMethodMetaData> initMethods) throws IllegalStateException
   {
      this.initMethods = initMethods;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setLocal(java.lang.String)
    */
   public void setLocal(String local)
   {
      this.local = local;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setLocalHome(java.lang.String)
    */
   public void setLocalHome(String localHome)
   {
      this.localHome = localHome;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setPostActivates(java.util.List)
    */
   public void setPostActivates(List<LifecycleCallbackMetaData> postActivates)
   {
      this.postActivates = postActivates;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setPrePassivates(java.util.List)
    */
   public void setPrePassivates(List<LifecycleCallbackMetaData> prePassivates)
   {
      this.prePassivates = prePassivates;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setRemote(java.lang.String)
    */
   public void setRemote(String remoteInterface)
   {
      this.remote = remoteInterface;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setRemoveMethods(java.util.List)
    */
   public void setRemoveMethods(List<RemoveMethodMetaData> removeMethods) throws IllegalStateException
   {
      if (!this.isStateful())
      {
         throw new IllegalStateException("remove-method is applicable only for stateful beans. This bean: "
               + this.getEjbName() + " is not stateful");
      }
      this.removeMethods = removeMethods;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setSecurityRoleRefs(java.util.List)
    */
   public void setSecurityRoleRefs(List<SecurityRoleRefMetaData> securityRoleRefs)
   {
      this.securityRoleRefs = securityRoleRefs;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setServiceEndpoint(java.lang.String)
    */
   public void setServiceEndpoint(String value) throws IllegalStateException
   {
      this.serviceEndpoint = value;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setSessionType(SessionType)
    */
   public void setSessionType(SessionType value)
   {
      this.sessionType = value;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setTimeoutMethod(org.jboss.ejb3.metadata.spi.javaee.NamedMethodMetaData)
    */
   public void setTimeoutMethod(NamedMethodMetaData timeoutMethod)
   {
      this.timeoutMethod = timeoutMethod;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData#setTransactionType(TransactionManagementType)
    */
   public void setTransactionType(TransactionManagementType transactionType)
   {
      this.transactionManagementType = transactionType;
   }

}
