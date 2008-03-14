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
import org.jboss.ejb3.cache.api.CacheItem;

/**
 * Encapsulates the identifier for a {@link CacheItem} as well as information
 * about what node logical "owns" it.
 * 
 * @author Brian Stansberry
 */
public class OwnedItem
{
   private static final int FQN_SIZE = JBCIntegratedObjectStore.FQN_SIZE;
   private static final int KEY_INDEX = FQN_SIZE - 1;
   private static final int OWNER_INDEX = 1;
   private static final int BR_FQN_SIZE = FQN_SIZE + OWNER_INDEX + 1;
   private static final int BR_KEY_INDEX = BR_FQN_SIZE - 1;
   
   private final String owner;
   private final Object id;
   private final Fqn region;
   
   /**
    * Generates an OwnedItem from the given Fqn if the Fqn is a logical
    * child of <code>base</code>
    * 
    * @param fqn  the Fqn
    * @param base the base Fqn
    * @param checkBuddy <code>true</code> if it is possible that <code>fqn</code>
    *                   belongs to a {@link BuddyManager#BUDDY_BACKUP_SUBTREE_FQN buddy-backup subtree}
    *                   
    * @return an OwnedItem or <code>null</code> if <code>fqn</code> is not a
    *         logical child of <code>base</code>
    */
   @SuppressWarnings("unchecked")
   public static OwnedItem getOwnedItem(Fqn fqn, boolean checkBuddy)
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
            region = fqn.getSubFqn(OWNER_INDEX, BR_KEY_INDEX);
         }
      }
      else if (size == FQN_SIZE)
      {
         region = fqn.getParent();
         key = fqn.get(KEY_INDEX);
      }
      
      return (key == null ? null : new OwnedItem(owner, key, region));
   }
   
   public OwnedItem(String owner, Object id, Fqn region)
   {
      assert id != null : "id is null";
      assert region != null : "region is null";
      
      this.owner = owner;
      this.id = id;
      this.region = region;
   }
   
   /**
    * Gets the logical "owner" of the item.  A value of <code>null</code>
    * means either there is no logical owner or this process is the owner.
    * (Basically it means the item is stored in the main area of a JBoss CAche
    * tree, not in a named buddy-backup region.)
    * 
    * @return
    */
   public String getOwner()
   {
      return owner;
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
   public Fqn getRegion()
   {
      return region;
   }
   
   public Fqn getFullFqn()
   {
      if (owner != null)
      {
         Fqn ownerFqn = new Fqn(BuddyManager.BUDDY_BACKUP_SUBTREE_FQN, owner);
         Fqn base = new Fqn(ownerFqn, region);
         return new Fqn(base, id);
      }
      else
      {
         return new Fqn(region, id);
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
