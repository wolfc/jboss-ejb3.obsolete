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

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;

import org.jboss.ejb3.session.SessionBeanContext;
import org.jboss.ejb3.session.SessionContextDelegateBase;

/**
 * A session context that is serializable.
 * 
 * Since a session context can be serialized with a bean it must not have a
 * direct reference to StatelesssSessionContextDelegate.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: 68144 $
 */
public class StatelessSessionContextImpl extends SessionContextDelegateBase<StatelessContainer>
      implements
         SessionContext
{
   public StatelessSessionContextImpl(SessionBeanContext<StatelessContainer> beanContext)
   {
      super(beanContext);
   }

   @Override
   public EJBLocalObject getEJBLocalObject() throws IllegalStateException
   {
      try
      {
         EJBLocalObject proxy = (EJBLocalObject) (StatelessContainer) container.createProxyLocalEjb21();
         return proxy;
      }
      catch (Exception e)
      {
         throw new IllegalStateException(e);
      }
   }

   @Override
   public EJBObject getEJBObject() throws IllegalStateException
   {
      try
      {
         EJBObject proxy = (EJBObject) container.createProxyRemoteEjb21();
         return proxy;
      }
      catch (Exception e)
      {
         throw new IllegalStateException(e);
      }
   }
}
