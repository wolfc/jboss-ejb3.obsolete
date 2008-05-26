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
package org.jboss.ejb3.test.proxy.common.container;

import java.util.UUID;

import org.jboss.ejb3.interceptors.container.ContainerMethodInvocation;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.ejb3.proxy.objectstore.ObjectStoreBindings;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * A simple stateless container that binds proxies and can be invoked.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class StatelessContainer extends SessionSpecContainer implements InvokableContext<ContainerMethodInvocation>
{
   public StatelessContainer(JBossSessionBeanMetaData metaData, ClassLoader classLoader) throws ClassNotFoundException
   {
      super(metaData, classLoader);
   }

   /**
    * Creates a unique name for this container
    * 
    * @return
    */
   protected final String createContainerName()
   {
      return ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJBCONTAINER_STATELESS + this.getMetaData().getEjbName() + "/"
            + UUID.randomUUID();
   }

   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   protected String getJndiRegistrarBindName()
   {
      return ObjectStoreBindings.OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SLSB;
   }

   /**
    * Obtains the appropriate bean instance for invocation
    * as called from the specified proxy
    * 
    * @param proxy
    * @return
    */
   protected Object getBeanInstance(Object proxy)
   {
      // Typically this would be obtained from a Pool
      try
      {
         return this.createInstance();
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error in creating new SLSB Bean Instance", t);
      }
   }
}
