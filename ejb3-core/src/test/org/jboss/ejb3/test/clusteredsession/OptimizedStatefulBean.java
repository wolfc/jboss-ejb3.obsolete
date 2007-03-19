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
package org.jboss.ejb3.test.clusteredsession;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.annotation.ejb.Clustered;
import org.jboss.annotation.ejb.cache.tree.CacheConfig;
import org.jboss.ejb3.cache.Optimized;

/**
 * Stateful bean that configures clustering via annotations.
 *
 * @author Ben Wang
 * @version $Revision: 59445 $
 */
@Stateful(name="testOptimizedStateful")
@Clustered
@CacheConfig(maxSize=1000, idleTimeoutSeconds=1)   // this will get evicted the second time eviction thread wakes up
@Remote(StatefulRemote.class)
public class OptimizedStatefulBean 
   extends NonAnnotationStatefulBean
   implements Optimized
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private boolean modified = true;
   
   public boolean isModified()
   {
      boolean answer = modified;
      modified = true;
      return answer;
   }

   @Override
   public int getPostActivate()
   {
      modified = false;
      return super.getPostActivate();
   }

   @Override
   public int getPrePassivate()
   {
      modified = false;
      return super.getPrePassivate();
   }

   @Override
   public void resetActivationCounter()
   {
      modified = false;
      super.resetActivationCounter();
   }
   
   
}
