/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.stateful;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.jboss.ejb3.session.SessionBeanContext;
import org.jboss.ejb3.session.SessionContextDelegateBase;

/**
 * StatefulSessionContextDelegate
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 * @param <T>
 */
public class StatefulSessionContextDelegate extends SessionContextDelegateBase<StatefulContainer>
{

   public StatefulSessionContextDelegate(SessionBeanContext<StatefulContainer> beanContext)
   {
      super(beanContext);
   }

   @Override
   public EJBLocalObject getEJBLocalObject() throws IllegalStateException
   {
      try
      {
         Object id = beanContext.getId();
         EJBLocalObject proxy = null;
         try
         {
            proxy = (EJBLocalObject) container.createProxyLocalEjb21(id);
         }
         // Proxy does not implement EJBLocalObject
         catch (ClassCastException cce)
         {
            // JIRA EJBTHREE-1057
            throw new IllegalStateException("EJB3 Specification Violation: " + container.getBeanClassName()
                  + " does not have a local interface; "
                  + "EJB3 Spec 4.3.3 Bullet 12: Only session beans with a local EJBLocalObject interface "
                  + "can call this method.");

         }
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
         Object id = beanContext.getId();
         EJBObject proxy = null;
         try
         {
            proxy = (EJBObject) container.createProxyRemoteEjb21(id);
         }
         // Proxy does not implement EJBObject
         catch (ClassCastException cce)
         {
            // JIRA EJBTHREE-1057
            throw new IllegalStateException("EJB3 Specification Violation: " + container.getBeanClassName()
                  + " does not have a remote interface; "
                  + "EJB3 Spec 4.3.3 Bullet 10: Only session beans with a remote EJBObject interface "
                  + "can call this method.");
         }
         return proxy;
      }
      catch (Exception e)
      {
         throw new IllegalStateException(e);
      }
   }

}
