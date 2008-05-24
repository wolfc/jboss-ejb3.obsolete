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

import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.invocation.StatefulSessionContainerMethodInvocation;
import org.jboss.ejb3.proxy.mc.MicrocontainerBindings;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatefulContainer
 * 
 * A Mock SFSB Container for use in Testing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulContainer extends SessionSpecContainer
      implements
         StatefulSessionInvokableContext<StatefulSessionContainerMethodInvocation>
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   // --------------------------------------------------------------------------------||
   // Constructor ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public StatefulContainer(JBossSessionBeanMetaData metaData, ClassLoader classLoader) throws ClassNotFoundException
   {
      super(metaData, classLoader);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a new user session and returns the unique Session ID
    * 
    * @return
    */
   public Object createSession()
   {
      /* 
       * Just create a UUID, in practice this would create a new instance and 
       * associate it with an ID in the cache 
       */
      return UUID.randomUUID();
   }

   /**
    * Creates a unique name for this container
    * 
    * @return
    */
   protected final String createContainerName()
   {
      return MicrocontainerBindings.MC_NAMESPACE_EJBCONTAINER_STATEFUL + this.getMetaData().getEjbName() + "/"
            + UUID.randomUUID();
   }

}
