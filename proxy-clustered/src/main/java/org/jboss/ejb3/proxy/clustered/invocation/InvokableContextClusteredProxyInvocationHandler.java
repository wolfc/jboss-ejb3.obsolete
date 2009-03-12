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

package org.jboss.ejb3.proxy.clustered.invocation;

import java.io.Serializable;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.remoting.ClusterConstants;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.ejb3.proxy.impl.invocation.InvokableContextStatefulRemoteProxyInvocationHack;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.remoting.InvokerLocator;

/**
 * Extends the superclass to add additional clustering metadata to each
 * invocation.
 * 
 * @author Brian Stansberry
 *
 */
public class InvokableContextClusteredProxyInvocationHandler extends InvokableContextStatefulRemoteProxyInvocationHack
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /** The serialVersionUID */
   private static final long serialVersionUID = -3220073092992088020L;

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   private FamilyWrapper family;
   private LoadBalancePolicy lbPolicy;
   private String partitionName;
   private boolean usePreferredTarget;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create a new InvokableContextClusteredRemoteProxy.
    * 
    * @param family clustering target information. Cannot be <code>null</code>.
    * @param lb LoadBalancePolicy implementation. Cannot be <code>null</code>.
    * @param partitionName  name of the cluster partition. Cannot be <code>null</code>.
    * @param usePreferredTarget should <code>uri</code> be added to the metadata under key
    *                           {@link ClusterConstants.HA_TARGET}? Should be
    *                           <code>true</code> for SFSB proxies.
    */
   public InvokableContextClusteredProxyInvocationHandler(Object oid, String containerGuid, InvokerLocator uri,
         Interceptor[] interceptors, Serializable sessionId, FamilyWrapper family,
         LoadBalancePolicy lb, String partitionName, boolean usePreferredTarget)
   {
      super(oid, containerGuid, uri, interceptors, sessionId);
      
      assert family != null        : "family is null";
      assert lb != null            : "lb is null";
      assert partitionName != null : "partitionName is null";
      
      this.family = family;
      this.lbPolicy = lb;
      this.partitionName = partitionName;  
      this.usePreferredTarget = usePreferredTarget;
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Overrides the superclass to add
    */
   @Override
   protected void addMetadataToInvocation(MethodInvocation methodInvocation)
   {
      super.addMetadataToInvocation(methodInvocation);
      
      SimpleMetaData metadata = methodInvocation.getMetaData();
      metadata.addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.CLUSTER_FAMILY_WRAPPER, family, PayloadKey.TRANSIENT);
      metadata.addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.LOADBALANCE_POLICY, lbPolicy, PayloadKey.TRANSIENT);
      metadata.addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.PARTITION_NAME, partitionName, PayloadKey.TRANSIENT);
      
      if (this.usePreferredTarget)
      {
         metadata.addMetaData(ClusterConstants.CLUSTERED_REMOTING, ClusterConstants.HA_TARGET, getUri(), PayloadKey.TRANSIENT);
      }
   }
   
}
