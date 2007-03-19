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
package org.jboss.ejb3.test.aspectdomain;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.annotation.ejb.cache.Cache;
import org.jboss.annotation.ejb.cache.tree.CacheConfig;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.PoolClass;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
@Stateful(name="DeploymentDescriptorStateful")
@Remote(org.jboss.ejb3.test.aspectdomain.Stateful.class)
@RemoteBinding(jndiBinding = "DeploymentDescriptorStateful")
@PoolClass(value=org.jboss.ejb3.ThreadlocalPool.class, maxSize=30, timeout=10000)
@Cache(org.jboss.ejb3.cache.tree.StatefulTreeCache.class)
@CacheConfig(name="jboss.cache:service=EJB3SFSBClusteredCache", maxSize=100000, idleTimeoutSeconds=300)
public class DeploymentDescriptorStatefulBean
   implements org.jboss.ejb3.test.aspectdomain.Stateful
{
   private static final Logger log = Logger.getLogger(DeploymentDescriptorStatefulBean.class);
   
   public String test() throws Exception
   {
      return "Not intercepted";
   }
}
