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
package org.jboss.ejb3.remoting;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aspects.remoting.ClusterConstants;
import org.jboss.logging.Logger;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Registry;

/**
 * Routes the call to the local container, bypassing further client-side
 * interceptors and any remoting layer, if a container with the same OID 
 * and partition name is available.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: 60233 $
 */
public class ClusteredIsLocalInterceptor extends IsLocalInterceptor
{
   private static final long serialVersionUID = 5765933584762500725L;

   private static final Logger log = Logger.getLogger(ClusteredIsLocalInterceptor.class);
   
   public Object invoke(Invocation invocation) throws Throwable
   {
      Container localContainer = findLocalContainer(invocation);
      if (localContainer != null)
      {
         return invokeLocal(invocation, localContainer);
      }
      return invocation.invokeNext();
   }

   private Container findLocalContainer(Invocation invocation)
   {
      String guid = (String)invocation.getMetaData(IS_LOCAL, GUID);
      String partitionName = (String) invocation.getMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.PARTITION_NAME);
      
      Container container = null;
      try
      {
         container = Ejb3Registry.findContainer(guid);
         if (container == null)
         {
            String oid = (String)invocation.getMetaData(Dispatcher.DISPATCHER, Dispatcher.OID);
            container = Ejb3Registry.getClusterContainer(Ejb3Registry.clusterUid(oid, partitionName));
         }
      }
      catch (IllegalStateException ignored)
      {
         if (log.isTraceEnabled())
            log.trace("Cannot find local container for " + guid);
      }
      
      if (container != null)
      {
         if (partitionName != null)
         {
            if (!partitionName.equals(((EJBContainer) container).getPartitionName()))
            {
               if (log.isTraceEnabled())
               {
                  log.trace("Partition (" + ((EJBContainer) container).getPartitionName() + 
                            ") for local container " + guid + " does not match invocation (" +
                            partitionName + ")");
               }
               container = null;
            }
            else if (log.isTraceEnabled())
            {
               log.trace("Partition (" + ((EJBContainer) container).getPartitionName() + 
                     ") for local container " + guid + " matches invocation (" +
                     partitionName + ")");
            }
         }
         else
         {
            log.warn("No PARTITION_NAME metadata associated with invocation");
            container = null;
         }
      }
      
      return container;
   }
}
