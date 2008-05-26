/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ejb3.test.ejbthree1136;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.CacheManager;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.Fqn;

/**
 * MBean that stores a key/value in a cache during start
 * and then allows a caller to check if the value is still there.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class SFSBCacheManipulator implements SFSBCacheManipulatorMBean
{
   public static final String VALUE = "VALUE";
   
   private Cache clusteredBeanCache;
   private CacheManager cacheManager;
   private String cacheConfigName;
   private String regionRoot;
   
   public Cache getClusteredBeanCache()
   {
      return clusteredBeanCache;
   } 
      
   public String getCacheConfigName()
   {
      return cacheConfigName;
   }

   public void setCacheConfigName(String name)
   {
      cacheConfigName = name;      
   }

   public void setCacheManager(CacheManager cacheManager)
   {
      this.cacheManager = cacheManager;      
   }

   public String getRegionRoot()
   {
      return regionRoot;
   }

   public void setRegionRoot(String regionRoot)
   {
      this.regionRoot = regionRoot;
   }
   
   public Fqn getTestFqn()
   {
      Fqn base = Fqn.fromString(regionRoot);
      return new Fqn(base, "test");
   }

   public void start() throws Exception
   {
      clusteredBeanCache = cacheManager.getCache(cacheConfigName, true);
      if (clusteredBeanCache.getCacheStatus() != CacheStatus.STARTED)
         clusteredBeanCache.start();
      
      Fqn fqn = getTestFqn();
      clusteredBeanCache.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
      clusteredBeanCache.put(fqn, "key", VALUE);
      // "Passivate" the data
      clusteredBeanCache.evict(fqn, true);
   }
   
   public void stop() throws Exception
   {
      clusteredBeanCache.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
      clusteredBeanCache.removeNode(getTestFqn());
      
      cacheManager.releaseCache(cacheConfigName);
   }
   
   public Object getFromBeanCache() throws CacheException
   {
      Fqn fqn = getTestFqn();
      Object obj = clusteredBeanCache.get(fqn, "key");
      // "Re-Passivate" the data
      clusteredBeanCache.evict(fqn, true);
      return obj;
   }
}
