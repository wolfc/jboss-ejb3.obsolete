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
package org.jboss.ejb3.annotation.impl;

import java.lang.annotation.Annotation;

import org.jboss.ejb3.annotation.Clustered;

import org.jboss.ejb3.annotation.defaults.ClusteredDefaults;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 67628 $
 */
public class ClusteredImpl implements Clustered
{
   private String loadBalancePolicy = ClusteredDefaults.LOAD_BALANCE_POLICY_DEFAULT;
   private String homeLoadBalancePolicy = ClusteredDefaults.LOAD_BALANCE_POLICY_DEFAULT;
   private String partition = "${jboss.partition.name:DefaultPartition}";
   

   public String loadBalancePolicy()
   {
      return loadBalancePolicy;
   }
   
   public void setLoadBalancePolicy(String loadBalancePolicy)
   {
      this.loadBalancePolicy = loadBalancePolicy;
   }

   public String partition()
   {
      return partition;
   }
   
   public void setPartition(String partition)
   {
      this.partition = partition;
   }

   public String homeLoadBalancePolicy()
   {
      return homeLoadBalancePolicy;
   }

   public void setHomeLoadBalancePolicy(String homeLoadBalancePolicy)
   {
      this.homeLoadBalancePolicy = homeLoadBalancePolicy;
   }

   public Class<? extends Annotation> annotationType()
   {
      return Clustered.class;
   }
   
   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("[");
      sb.append("loadBalancePolicy=").append(loadBalancePolicy);
      sb.append("homeLoadBalancePolicy=").append(homeLoadBalancePolicy);
      sb.append("partition=").append(partition);
      sb.append("]");
      return sb.toString();
   }
}
