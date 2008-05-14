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
package org.jboss.ejb3.stateful;

import java.io.Serializable;

import org.jboss.ejb3.Ejb3Registry;

/**
 * Serializable reference to stateful bean context that can be disconnected
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulBeanContextReference implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7622266426902284032L;
   
   private transient StatefulBeanContext beanContext;
   private Object oid;
   private String containerGuid;
   private String containerClusterUid;
   private boolean isClustered = false;
   
   public StatefulBeanContextReference(StatefulBeanContext beanContext)
   {
      assert beanContext != null : "beanContext is null";
      
      this.beanContext = beanContext;
      oid = beanContext.getId();
      containerGuid = Ejb3Registry.guid(beanContext.getContainer());
      containerClusterUid = Ejb3Registry.clusterUid(beanContext.getContainer());
      isClustered = beanContext.getContainer().isClustered();
   }

   public StatefulBeanContext getBeanContext()
   {
      if (beanContext == null)
      {
         StatefulContainer container = (StatefulContainer)Ejb3Registry.findContainer(containerGuid);
         if (isClustered && container == null)
            container = (StatefulContainer)Ejb3Registry.getClusterContainer(containerClusterUid);
         // We are willing to accept a context that has been marked as removed
         // as it can still hold nested children
         beanContext = container.getCache().get(oid, false);
         
         assert beanContext != null : "beanContext no longer in cache";
      }
      return beanContext;
   }
}
