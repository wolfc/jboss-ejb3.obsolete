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
package org.jboss.ejb3.test.stateful;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.Cache;
import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.defaults.RemoteBindingDefaults;

/**
 * Same as TreeCacheStatefulBean, but configures a removalTimeoutSeconds to
 * validate metrics when a removal thread is used.
 * 
 * @author Brian Stansberry
 */
@Stateful(name = "TreeCacheRemovalStatefulBean")
@Remote(org.jboss.ejb3.test.stateful.Stateful.class)
@Local(org.jboss.ejb3.test.stateful.StatefulLocal.class)
@RemoteBinding(jndiBinding = "TreeCacheRemovalStateful", interceptorStack = "RemoteBindingStatefulSessionClientInterceptors", factory = RemoteBindingDefaults.PROXY_FACTORY_STATEFUL_REMOTE)
@CacheConfig(name = "jboss.cache:service=EJB3SFSBClusteredCache", maxSize = 1000, idleTimeoutSeconds = 1, removalTimeoutSeconds = 20)
@SecurityDomain("test")
@Resources(
{@Resource(name = "jdbc/ds", mappedName = "java:/DefaultDS")})
@Cache("StatefulTreeCache")
public class TreeCacheRemovalStatefulBean extends TreeCacheStatefulBase
{
   
}
