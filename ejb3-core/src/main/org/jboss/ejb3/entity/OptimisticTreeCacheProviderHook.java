/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.entity;

import java.util.Properties;

import org.hibernate.cache.CacheException;

/**
 * Trivial {@link TreeCacheProviderHook} subclass that logs a warning in
 * {@link #start(Properties) start} if the underlying JBoss Cache 
 * is not configured for optimistic locking.  Like the superclass,
 * will provide working Cache implementations whether JBoss Cache is
 * configured for optimistic locking or not; the only added behavior
 * is the logging of the warning if the JBoss Cache configuration doesn't 
 * match the intent implied by the use of this class.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @author Brian Stansberry
 */
public class OptimisticTreeCacheProviderHook 
   extends TreeCacheProviderHook 
{
   public void start(Properties properties) throws CacheException
   {
      super.start(properties);
      
      if (getCacheFactory().isOptimistic() == false)
      {
         log.warn("JBoss Cache is not configured for optimistic locking; " +
         "provided Cache implementations therefore will not implement OptimisticCache");
      }
   }

}
