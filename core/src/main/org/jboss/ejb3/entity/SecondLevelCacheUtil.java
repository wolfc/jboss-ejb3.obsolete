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

package org.jboss.ejb3.entity;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.StandardQueryCache;
import org.hibernate.cache.UpdateTimestampsCache;

/**
 * Utilities related to the Hibernate 2nd Level Cache
 * 
 * @author Brian Stansberry
 */
public class SecondLevelCacheUtil
{
   public static final String HIBERNATE_CACHE_REGION_PREFIX = "hibernate.cache.region_prefix";
   
   public static String createCacheRegionPrefix(String earName, String jarName, String unitName)
   {
      StringBuilder sb = new StringBuilder();
      if (earName != null)
      {
         sb.append(earName);
         if (!earName.endsWith(".ear")) 
            sb.append("_ear");
         sb.append(",");
      }
      if (jarName != null)
      {
         sb.append(jarName);
         if (!jarName.endsWith(".jar"))
            sb.append("_jar");
         sb.append(",");
      }
      sb.append(unitName);
      String raw = sb.toString();
      // Replace any '.' otherwise the JBoss Cache integration may replace
      // it with a '/' and it will become a level in the FQN
      String escaped = raw.replace('.', '_');
      return escaped;
   }
   
   public static String createRegionFqn(String regionName, String regionPrefix)
   {
      String escaped = null;
      int idx = -1;
      if (regionPrefix != null)
      {
         idx = regionName.indexOf(regionPrefix);
      }
      
      if (idx > -1)
      {
         int regionEnd = idx + regionPrefix.length();
         String prefix = regionName.substring(0, regionEnd);
         String suffix = regionName.substring(regionEnd);
         suffix = suffix.replace('.', '/');
         escaped = prefix + suffix;
      }
      else
      {
         escaped = regionName.replace('.', '/');
      }
      return escaped;
   }
      
   public static boolean isSharedClassLoaderRegion(String regionName)
   {
      return (StandardQueryCache.class.getName().equals(regionName) 
               || UpdateTimestampsCache.class.getName().equals(regionName));
   }  
   
   /** 
    * Creates a CacheException, but doesn't pass JBC CacheException as the cause
    * as it is a type that likely doesn't exist on a client.
    * Instead creates a Hibernate CacheException with the original exception's
    * stack trace.
    */   
   public static CacheException convertToHibernateException(Exception e)
   {
      CacheException he = null;
      
      if (e instanceof org.jboss.cache.CacheException)
      {
         he = new CacheException(e.getClass().getName() + " " + e.getMessage());
         he.setStackTrace(e.getStackTrace());
      }
      else if (e instanceof CacheException)
      {
         he = (CacheException) e;
      }
      else
      {
         he = new CacheException(e);
      }
      
      return he;
   }
   
   // Prevent instantiation
   private SecondLevelCacheUtil() {}
}
