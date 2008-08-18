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
package org.jboss.ejb3.cluster.metadata;

import java.lang.annotation.Annotation;

import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.impl.ClusteredImpl;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.ClusterConfigMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.spi.signature.DeclaredMethodSignature;

/**
 * Creates a ClusteredImpl to match a ClusterConfigMetaData.
 *
 * @author <a href="mailto:brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: $
 */
public class ClusteredMetaDataBridge implements MetaDataBridge<JBossEnterpriseBeanMetaData>
{
   private static final Logger log = Logger.getLogger(ClusteredMetaDataBridge.class);

   private ClusteredImpl createAnnotationImpl()
   {
      try
      {
         return ClusteredImpl.class.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData metaData, ClassLoader classLoader)
   {
      if (annotationClass == Clustered.class && isClustered(metaData))
      {
         ClusteredImpl impl = createAnnotationImpl();
         ClusterConfigMetaData ccmd = findClusterConfigMetaData(metaData);
         if (ccmd != null)
         {
            if (ccmd.getPartitionName() != null)
               impl.setPartition(ccmd.getPartitionName());
            // FIXME Once @Clustered and ClusteredImpl are updated, just use the string values
            if (ccmd.getBeanLoadBalancePolicy() != null)
               impl.setLoadBalancePolicy(ccmd.getBeanLoadBalancePolicy());
            if (ccmd.getHomeLoadBalancePolicy() != null)
               impl.setHomeLoadBalancePolicy(ccmd.getHomeLoadBalancePolicy());
         }
         return annotationClass.cast(impl);
      }
      return null;
   }

   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData metaData, ClassLoader classLoader, DeclaredMethodSignature method)
   {
      return null;
   }   
   
   private boolean isClustered(JBossEnterpriseBeanMetaData metadata)
   {
      if (metadata instanceof JBossSessionBeanMetaData)
         return ((JBossSessionBeanMetaData) metadata).isClustered();
      return false;
   }
   
   private ClusterConfigMetaData findClusterConfigMetaData(JBossEnterpriseBeanMetaData metaData)
   {
      if (metaData instanceof JBossSessionBeanMetaData)
      {
         return ((JBossSessionBeanMetaData) metaData).getClusterConfig();
      }
      return null;
   }
}
