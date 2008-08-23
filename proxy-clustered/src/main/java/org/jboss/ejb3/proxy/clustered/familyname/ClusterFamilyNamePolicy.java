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

package org.jboss.ejb3.proxy.clustered.familyname;


/**
 * Pluggable policy for creating a cross-cluster consistent but cross-deployment
 * unique name for the clustering information associated with a service. The
 * fundamental purpose of this policy is to create strings that can be passed
 * to {@link org.jboss.aspects.remoting.FamilyWrapper#FamilyWrapper(String, java.util.List)} and
 * {@link org.jboss.ha.framework.interfaces.ClusteringTargetsRepository#initTarget(String, java.util.List)}.
 * <p>
 * The design intent of this interface is that different implementations will
 * handle the <code>localTarget</code> param passed to 
 * {@link #getClusterFamilyName(String, Object, String)} differently.
 * </p>
 * 
 * @author Brian Stansberry
 * @version $Revision: $
 */
public interface ClusterFamilyNamePolicy<T>
{
   /**
    * Gets a name for the clustering information identified by the given
    * parameters. Invoking this method on different nodes in the cluster, with
    * the same <code>serviceName</code> and the same <code>partitionName</code>
    * but different but logically related <code>localTarget</code> values 
    * must result in the same return value.
    * 
    * @param serviceName the name of the service
    * @param localTarget this node's cluster target for the service
    * @param partitionName the name of the HAPartition
    * 
    * @return the cluster family name.
    */
   String getClusterFamilyName(String serviceName, T localTarget, String partitionName);
}
