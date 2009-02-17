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
package org.jboss.ejb3.core.test.ejbthree1549;

import javax.ejb.Local;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.Cache;
import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.PersistenceManager;

/**
 * MyStatefulBean
 * 
 * A SFSB with the following overrides:
 * 
 * 1) Uses a Cache implementation that provides mechanism to
 * block tasks such as passivation and removal, in addition to 
 * adding support for internal lifecycle implementations for these
 * tasks
 * 
 * 2) Uses a backing PersistenceManager that provides mechanism to
 * block completion of passivation from the test until ready
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Local(MyStatefulLocal.class)
// Note that jndi bindings are ignore in the unit tests
@LocalBinding(jndiBinding = MyStatefulLocal.JNDI_NAME)
/*
 * Use a CacheFactory that is extended to enable 
 * blocking hooks that we need for testing
 */
@Cache(ForceEventsCacheFactory.REGISTRY_BIND_NAME)
/*
 * Make instances eligible for timeout very soon after last invocation, and be removed (as default is NEVER)
 */
@CacheConfig(idleTimeoutSeconds = MyStatefulLocal.PASSIVATION_TIMEOUT, removalTimeoutSeconds = MyStatefulLocal.REMOVAL_TIMEOUT)
public class MyStatefulBean implements MyStatefulLocal
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private int counter;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns and increments the counter, which starts at 0
    */
   public int getNextCounter()
   {
      return counter++;
   }
}
