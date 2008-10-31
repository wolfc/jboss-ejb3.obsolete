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

import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.pool.AbstractPool;

/**
 * A SingletonPool.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class SingletonPool extends AbstractPool
{
   private BeanContext<?> instance;
   
   @Override
   public void initialize(Container container, int maxSize, long timeout)
   {
      super.initialize(container, 1, timeout);
      instance = create(null, null);
      System.out.println("SingletonPool.initialize: maxSize=" + maxSize);
   }
   
   @Override
   public void setMaxSize(int maxSize)
   {
      if(maxSize != 1)
         throw new IllegalArgumentException("Maximum size for this pool must be 1: " + maxSize);
   }

   public void destroy()
   {
      if(instance != null)
         discard(instance);
   }

   public BeanContext<?> get()
   {
      return instance;
   }

   public BeanContext<?> get(Class<?>[] initTypes, Object[] initValues)
   {
      return instance;
   }

   public int getAvailableCount()
   {
      return 1;
   }

   public int getCurrentSize()
   {
      return getCreateCount() - getRemoveCount();
   }

   public int getMaxSize()
   {
      return 1;
   }

   public void release(BeanContext<?> obj)
   {
   }
}
