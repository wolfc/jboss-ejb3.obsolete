/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.stateful;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.Identity;
import java.security.Principal;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.jboss.ejb3.Ejb3Registry;

/**
 * A session context that is serializable.
 * 
 * Since a session context can be serialized with a bean it must not have a
 * direct reference to StatefulBeanContext. Direct instantiation of a
 * StatefulSessionContextImpl after activation is also not possible, because
 * the bean is than not yet in cache. Therefore we use a delegate.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class StatefulSessionContextImpl implements Serializable, SessionContext
{
   private static final long serialVersionUID = 1L;

   /**
    * The container identifier.
    */
   private String containerGuid;
   private String containerClusterUid;
   /**
    * The SFSB identifier.
    */
   private Object id;
   
   private boolean isClustered;
   
   private transient SessionContext delegate;
   
   private static class Serialized implements Serializable
   {
      private static final long serialVersionUID = 1L;
      
      private Object id;
      private String containerClusterUid;
      private String containerGuid;
      private boolean isClustered;
      
      private Object readResolve() throws ObjectStreamException
      {
         return new StatefulSessionContextImpl(containerGuid, containerClusterUid, id, isClustered);
      }
   }
   
   public StatefulSessionContextImpl(String containerGuid, String containerClusterUid, Object id, boolean isClustered)
   {
      assert containerGuid != null : "containerGuid is null";
      assert containerClusterUid != null : "containerClusterUid is null";
      
      this.containerGuid = containerGuid;
      this.containerClusterUid = containerClusterUid;
      this.id = id;
      this.isClustered = isClustered;
   }
   
   public StatefulSessionContextImpl(StatefulBeanContext beanContext)
   {
      assert beanContext != null : "beanContext is null";
      
      this.delegate = new StatefulSessionContextDelegate(beanContext);
      this.containerGuid = Ejb3Registry.guid(beanContext.getContainer());
      this.containerClusterUid =Ejb3Registry.clusterUid(beanContext.getContainer());
      this.id = beanContext.getId();
      this.isClustered = beanContext.getContainer().isClustered();
   }

   private Object writeReplace() throws ObjectStreamException
   {
      Serialized s = new Serialized();
      s.containerGuid = this.containerGuid;
      s.containerClusterUid = this.containerClusterUid;
      s.id = this.id;
      s.isClustered = this.isClustered;
      return s;
   }

   public <T> T getBusinessObject(Class<T> businessInterface) throws IllegalStateException
   {
      return getDelegate().getBusinessObject(businessInterface);
   }

   protected SessionContext getDelegate()
   {
      if(delegate == null)
      {
         StatefulContainer container = (StatefulContainer)Ejb3Registry.getContainer(containerGuid);
         if (container == null && isClustered)
            container = (StatefulContainer)Ejb3Registry.getClusterContainer(containerClusterUid);
         
         delegate = new StatefulSessionContextDelegate(container, id);
      }
      return delegate;
   }
   
   public EJBLocalObject getEJBLocalObject() throws IllegalStateException
   {
      return getDelegate().getEJBLocalObject();
   }

   public EJBObject getEJBObject() throws IllegalStateException
   {
      return getDelegate().getEJBObject();
   }

   public Class<?> getInvokedBusinessInterface() throws IllegalStateException
   {
      return getDelegate().getInvokedBusinessInterface();
   }

   public MessageContext getMessageContext() throws IllegalStateException
   {
      return getDelegate().getMessageContext();
   }

   @SuppressWarnings("deprecation")
   public Identity getCallerIdentity()
   {
      return getDelegate().getCallerIdentity();
   }

   public Principal getCallerPrincipal()
   {
      return getDelegate().getCallerPrincipal();
   }

   public EJBHome getEJBHome()
   {
      return getDelegate().getEJBHome();
   }

   public EJBLocalHome getEJBLocalHome()
   {
      return getDelegate().getEJBLocalHome();
   }

   public Properties getEnvironment()
   {
      return getDelegate().getEnvironment();
   }

   public boolean getRollbackOnly() throws IllegalStateException
   {
      return getDelegate().getRollbackOnly();
   }

   public TimerService getTimerService() throws IllegalStateException
   {
      return getDelegate().getTimerService();
   }

   public UserTransaction getUserTransaction() throws IllegalStateException
   {
      return getDelegate().getUserTransaction();
   }

   @SuppressWarnings("deprecation")
   public boolean isCallerInRole(Identity role)
   {
      return getDelegate().isCallerInRole(role);
   }

   public boolean isCallerInRole(String roleName)
   {
      return getDelegate().isCallerInRole(roleName);
   }

   public Object lookup(String name)
   {
      return getDelegate().lookup(name);
   }

   public void setRollbackOnly() throws IllegalStateException
   {
      getDelegate().setRollbackOnly();
   }
   
   public String toString()
   {
      return super.toString() + "{containerGuid=" + containerGuid + ",id=" + id + "}";
   }
}
