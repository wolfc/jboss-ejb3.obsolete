/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.stateless;

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

/**
 * A session context that is serializable.
 * 
 * Since a session context can be serialized with a bean it must not have a
 * direct reference to StatelesssSessionContextDelegate.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: 68144 $
 */
public class StatelessSessionContextImpl implements Serializable, SessionContext
{
   private static final long serialVersionUID = 1L;

   private transient SessionContext delegate;

   public StatelessSessionContextImpl(StatelessBeanContext beanContext)
   {
      assert beanContext != null : "beanContext is null";

      this.delegate = new StatelessSessionContextDelegate(beanContext);
   }

   public <T> T getBusinessObject(Class<T> businessInterface) throws IllegalStateException
   {
      return getDelegate().getBusinessObject(businessInterface);
   }

   protected SessionContext getDelegate()
   {
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
}
