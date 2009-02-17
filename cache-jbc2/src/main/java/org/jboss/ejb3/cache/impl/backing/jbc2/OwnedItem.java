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

package org.jboss.ejb3.cache.impl.backing.jbc2;

import org.jboss.cache.Fqn;
import org.jboss.cache.buddyreplication.BuddyManager;
import org.jboss.ejb3.cache.CacheItem;

/**
 * Encapsulates the identifier for a {@link CacheItem} as well as information
 * about what node logical "owns" it.
 * 
 * @author Brian Stansberry
 */
public class OwnedItem
{
   public static final String LOCAL_OWNER = "local_owner";
   public static final String REMOTE_OWNER = "remote_owner";
   
   private static final int FQN_SIZE = JBCBackingCacheEntryStore.FQN_SIZE;
   private static final int KEY_INDEX = FQN_SIZE - 1;
   private static final int REGION_INDEX = FQN_SIZE - 2;
   private static final int OWNER_INDEX = 1;
   private static final int BR_FQN_SIZE = FQN_SIZE + OWNER_INDEX + 1;
   private static final int BR_KEY_INDEX = BR_FQN_SIZE - 1;
   private static final int BR_REGION_INDEX = BR_FQN_SIZE - 2;
   
   private final String owner;
   private final Object id;
   private final Fqn<Object> region;
   private boolean passivating;
   private volatile boolean locallyOwned;
   private long lastUsed;
   
   /**
    * Generates an OwnedItem from the given Fqn if the Fqn is a logical
    * child of <code>base</code>
    * 
    * @param fqn  the Fqn
    * @param checkBuddy <code>true</code> if it is possible that <code>fqn</code>
    *                   belongs to a {@link BuddyManager#BUDDY_BACKUP_SUBTREE_FQN buddy-backup subtree}
    * @param localOwner TODO
    * @param base the base Fqn
    * @return an OwnedItem or <code>null</code> if <code>fqn</code> is not a
    *         logical child of <code>base</code>
    */
   @SuppressWarnings("unchecked")
   public static OwnedItem getOwnedItem(Fqn fqn, boolean checkBuddy, boolean localEvent)
   {
      int size = fqn.size();
      if (size < FQN_SIZE)
         return null;
      
      String owner = null;
      Object key = null;
      Fqn region = null;
      
      boolean buddy = checkBuddy && fqn.get(0).equals(BuddyManager.BUDDY_BACKUP_SUBTREE);
      if (buddy)
      { 
         if (size == BR_FQN_SIZE)
         {
            key = fqn.get(BR_KEY_INDEX);
            owner = (String) fqn.get(OWNER_INDEX);
            region = fqn.getSubFqn(OWNER_INDEX + 1, BR_REGION_INDEX);
         }
      }
      else if (size == FQN_SIZE)
      {
         region = fqn.getSubFqn(0, REGION_INDEX);
         key = fqn.get(KEY_INDEX);
      }
      
      return (key == null ? null : new OwnedItem(owner, key, region, localEvent));
   }
   
   public OwnedItem(Object id, Fqn<Object> region)
   {
      this(null, id, region, true);
   }
   
   private OwnedItem(String owner, Object id, Fqn<Object> region, boolean locallyOwned)
   {
      assert id != null : "id is null";
      assert region != null : "region is null";
      
      this.owner = owner;
      this.id = id;
      this.region = region;
      this.locallyOwned = locallyOwned;
   }
   
   /**
    * Gets the logical "owner" of the item.  
    * 
    * @return A value of {@link #LOCAL_OWNER} means this process is the owner. 
    *         A value of {@link #REMOTE_OWNER} means the cache is using total 
    *         replication, but some other process is the owner. Any other 
    *         value means buddy replication is in use and the object is 
    *         owned by another node; the returned value identifies the 
    *         owner's buddy backup tree. Will not return <code>null</code>.
    */
   public String getOwner()
   {
      return owner != null ? owner : (locallyOwned ? LOCAL_OWNER : REMOTE_OWNER);
   }

   /**
    * Gets the item's unique identifier.
    */
   public Object getId()
   {
      return id;
   }
   
   /**
    * Gets the base Fqn for the region where the item is cached.
    */
   public Fqn<Object> getRegion()
   {
      return region;
   }
   
   public Fqn<Object> getFullFqn()
   {
      if (owner != null)
      {
         Fqn<Object> ownerFqn = new Fqn<Object>(BuddyManager.BUDDY_BACKUP_SUBTREE_FQN, owner);
         Fqn<Object> base = new Fqn<Object>(ownerFqn, region);
         return new Fqn<Object>(base, id);
      }
      else
      {
         return new Fqn<Object>(region, id);
      }
   }

   public boolean isPassivating()
   {
      return passivating;
   }

   public void setPassivating(boolean passivating)
   {
      this.passivating = passivating;
   }

   public boolean isLocallyOwned()
   {
      return locallyOwned;
   }

   public void setLocallyOwned(boolean locallyOwned)
   {
      if (this.locallyOwned != locallyOwned)
      {
         synchronized (this)
         {
            this.locallyOwned = locallyOwned;
         }
      }      
   }

   public long getLastUsed()
   {
      return lastUsed;
   }

   public void setLastUsed(long lastUsed)
   {
      if (lastUsed > this.lastUsed)
      {
         this.lastUsed = lastUsed;
      }
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      
      if (obj instanceof OwnedItem)
      {
         OwnedItem other = (OwnedItem) obj;
         return (id.equals(other.id) && safeEquals(owner, other.owner));
      }
      return false;
   }
   
   @Override
   public int hashCode()
   {
      int result = 17;
      result += 31 * id.hashCode();
      result += 31 * (owner == null ? 0 : owner.hashCode());
      return result;
   }
   
   @Override
   public String toString()
   {
      return new StringBuilder(getClass().getName())
                               .append("[id=")
                               .append(id)
                               .append(",owner=")
                               .append(owner)
                               .append(",region=")
                               .append(region)
                               .append("]")
                               .toString();
   }
   
   private static boolean safeEquals(Object a, Object b)
   {
      return (a == b || (a != null && a.equals(b)));
   }
   
}
