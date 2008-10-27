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
 * force passivation (instead of waiting upon a Thread.sleep timeout)
 * 
 * 2) Uses a backing PersistenceManager that provides mechanism to
 * block completion of passivation from the test until ready
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Local(MyStatefulLocal.class)
@LocalBinding(jndiBinding = MyStatefulLocal.JNDI_NAME)
/*
 * Use a CacheFctory that instead of using a timed reaping Thread,
 * exposes a static "forcePassivation" method for the tests 
 */
@Cache(ForcePassivationCacheFactory.REGISTRY_BIND_NAME)
/*
 * Make instances eligible for timeout very soon after last invocation
 */
@CacheConfig(idleTimeoutSeconds = MyStatefulLocal.PASSIVATION_TIMEOUT)
/*
 * Set up a persistence manager that allows us to block, and therefore
 * lets the test decide how long the processes of performing passivation
 * should take.  Used to manually interleave Threads to target the test case.
 */
@PersistenceManager(BlockingPersistenceManagerFactory.REGISTRY_BIND_NAME)
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
