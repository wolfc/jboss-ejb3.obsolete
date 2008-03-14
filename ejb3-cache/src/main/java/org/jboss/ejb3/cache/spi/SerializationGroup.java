package org.jboss.ejb3.cache.spi;

import org.jboss.ejb3.cache.CacheItem;

public interface SerializationGroup<T extends CacheItem>
   extends BackingCacheEntry<T>
{
   void addMember(SerializationGroupMember<T> member);
   void removeMember(Object key);
   int size();
   boolean isInvalid();
   void setInvalid(boolean invalid);
   T getMemberObject(Object key);
   void addActive(SerializationGroupMember<T> member);
   void removeActive(Object key);
   int getInUseCount();
   PassivatingBackingCache<T, SerializationGroup<T>> getGroupCache();
   void setGroupCache(PassivatingBackingCache<T, SerializationGroup<T>> groupCache);
   void postActivate();
   void prePassivate();
   void preReplicate();
   void postReplicate();
}