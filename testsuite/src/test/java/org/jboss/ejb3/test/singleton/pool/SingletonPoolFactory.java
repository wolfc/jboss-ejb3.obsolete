/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.singleton.pool;

import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.pool.PoolFactory;
import org.jboss.ejb3.pool.PoolFactoryRegistry;

/**
 * A SingletonPoolFactory.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class SingletonPoolFactory implements PoolFactory
{
   private static final String FACTORY_NAME = "SingletonPool";
   
   private PoolFactoryRegistry factoryRegistry;
   
   public void setFactoryRegistry(PoolFactoryRegistry registry)
   {
      this.factoryRegistry = registry;
      if(factoryRegistry.getFactories().containsKey(FACTORY_NAME))
         throw new IllegalStateException(FACTORY_NAME + " is already registered");
      factoryRegistry.getFactories().put(FACTORY_NAME, getClass());
   }
   
   public Pool createPool()
   {
      return new SingletonPool();
   }
   
   public void destroy()
   {
      factoryRegistry.getFactories().remove(FACTORY_NAME);
   }
}
