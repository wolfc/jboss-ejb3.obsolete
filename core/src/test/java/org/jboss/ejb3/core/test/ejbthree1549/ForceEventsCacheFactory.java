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

import org.jboss.ejb3.cache.impl.factory.GroupAwareCacheFactory;
import org.jboss.ejb3.stateful.StatefulBeanContext;

/**
 * ForceEventsCacheFactory
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ForceEventsCacheFactory extends GroupAwareCacheFactory<StatefulBeanContext>
{
   public static final ForceEventsBackingCacheEntryStoreSource storeSource = 
      new ForceEventsBackingCacheEntryStoreSource();
   
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Name under which this Cache Factory will be bound into the registry
    */
   public static final String REGISTRY_BIND_NAME = "ForceEventsCache";

   public ForceEventsCacheFactory()
   {
      super(storeSource);
      setDefaultPassivationExpirationInterval(0);
   }
   
   

}
