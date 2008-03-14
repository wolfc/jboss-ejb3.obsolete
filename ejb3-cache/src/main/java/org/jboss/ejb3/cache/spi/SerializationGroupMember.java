package org.jboss.ejb3.cache.spi;

import org.jboss.ejb3.cache.api.CacheItem;

public interface SerializationGroupMember<T extends CacheItem>
  extends BackingCacheEntry<T>
{
   Object getGroupId();
   SerializationGroup<T> getGroup();
   void setGroup(SerializationGroup<T> group);
   void releaseReferences();
   void setPassivatingCache(PassivatingBackingCache<T, SerializationGroupMember<T>> cache);
   void setUnderlyingItem(T obj);
   boolean isPreReplicated();
   void setPreReplicated(boolean preReplicated);
}