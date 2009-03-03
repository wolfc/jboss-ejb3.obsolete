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
package org.jboss.ejb3.nointerface.jndi;

import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * NoInterfaceViewJNDIBinder
 * Responsible for binding objects related to no-interface view of a bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class NoInterfaceViewJNDIBinder
{

   /**
    * Create a JNDIBinder
    * @param sessionBeanMetaData
    * @return
    */
   public static NoInterfaceViewJNDIBinder getJNDIBinder(JBossSessionBeanMetaData sessionBeanMetaData)
   {
      return sessionBeanMetaData.isStateless()
            ? new StatelessNoInterfaceJNDIBinder()
            : new StatefulNoInterfaceJNDIBinder();
   }

   /**
    * Each type of {@link NoInterfaceViewJNDIBinder} is responsible for binding
    * the appropriate objects to jndi
    *
    * @see StatelessNoInterfaceJNDIBinder#bindNoInterfaceView(Class, InvokableContext, JBossSessionBeanMetaData)
    * @see StatefulNoInterfaceJNDIBinder#bindNoInterfaceView(Class, InvokableContext, JBossSessionBeanMetaData)
    *
    * @param beanClass
    * @param container
    * @param sessionBeanMetadata
    * @throws Exception
    */
   public abstract void bindNoInterfaceView(Class<?> beanClass, InvokableContext container,
         JBossSessionBeanMetaData sessionBeanMetadata) throws Exception;
}
