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

import org.jboss.remoting.InvokerLocator;

/**
 * @{link {@link ClusterFamilyNamePolicy} impl that constructs its family
 * name by extracting the {@link InvokerLocator#getProtocol() protocol} from 
 * the passed <code>localTarget</code>.
 * <p>
 * This policy will not produce unique values if multiple InvokerLocators that 
 * use the same protocol are associated with the target service.
 * {@link InvokerLocatorProtocolPortClusterFamilyNamePolicy} will produce
 * unique values, but doesn't handle the scenario where logically related
 * targets are configured to use different ports on different nodes. 
 * </p>
 *  
 * @author Brian Stansberry
 * @version $Revision: $
 */
public class InvokerLocatorProtocolClusterFamilyNamePolicy implements ClusterFamilyNamePolicy<InvokerLocator>
{

   public String getClusterFamilyName(String serviceName, InvokerLocator localTarget, String partitionName)
   {
      return serviceName + localTarget.getProtocol() + partitionName;
   }

}
