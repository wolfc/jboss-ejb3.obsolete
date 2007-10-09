/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.cache.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.ejb3.cache.grouped.SerializationGroup;
import org.jboss.ejb3.cache.grouped.SerializationGroupMember;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/**
 * Default implementation of {@link SerializationGroup}.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * @version $Revision: $
 */
public class SerializationGroupImpl implements SerializationGroup
{
   private static final Logger log = Logger.getLogger(SerializationGroupImpl.class);
   private static final long serialVersionUID = 1L;

   private Object id = new GUID();
   /** 
    * The actual underlying objects passed in via addMember(). We store them 
    * here so they aren't lost when they are cleared from the values
    * stored in the "members" map.
    */
   private Map<Object, Object> memberObjects = new HashMap<Object, Object>();
   /** 
    * The active group members.  We don't serialized these. Rather, it is
    * the responsibility of members to reassociate themselves with the 
    * group upon deserialization (via addActive())
    */
   private transient Map<Object, SerializationGroupMember> active = 
         new HashMap<Object, SerializationGroupMember>();
   /**
    * Set of keys passed to {@link #addInUse(Object)}
    */
   private transient Set<Object> inUseKeys = new HashSet<Object>();
   
   private boolean clustered;
   
   private long lastUsed;
   
   public Object getId()
   {
      return id;
   }
   
   public boolean isClustered()
   {
      return clustered;
   }
   
   public void setClustered(boolean clustered)
   {
      this.clustered = clustered;
   }
   
   public void addMember(SerializationGroupMember member)
   {
      Object key = member.getId();
      if (memberObjects.containsKey(key))
         throw new IllegalStateException(member + " is already a member");
      log.trace("add member " + key + ", " + member);
      memberObjects.put(key, member.getSerializableObject());
      active.put(key, member);
   }
   
   public void removeMember(Object key)
   {
      removeActive(key);
      memberObjects.remove(key);
   }
   
   public int size()
   {
      return memberObjects.size();
   }
   
   public Object getMemberObject(Object key)
   {
      return memberObjects.get(key);
   }
   
   public void postActivate()
   {
      // do nothing
   }
   
   public void prePassivate()
   {
      for(SerializationGroupMember member : active.values())
      {
         member.prePassivate();
      }
      active.clear();
   }
   
   public void postReplicate()
   {
      // do nothing
   }
   
   public void preReplicate()
   {
      for(SerializationGroupMember member : active.values())
      {
         member.preReplicate();
      }
      active.clear();
   }
   
   public void addActive(SerializationGroupMember member)
   {
      Object key = member.getId();
      if (!memberObjects.containsKey(key))
         throw new IllegalStateException(member + " is not a member of " + this);
      active.put(key, member);
   }
   
   public void removeActive(Object key)
   {
      active.remove(key);
   }

   public void addInUse(Object key)
   {
      if (!memberObjects.containsKey(key))
         throw new IllegalStateException(key + " is not a member of " + this);
      inUseKeys.add(key);
      lastUsed = System.currentTimeMillis();
   }

   public void removeInUse(Object key)
   {
      if (inUseKeys.remove(key))
      {
         lastUsed = System.currentTimeMillis();
      }
      else if (!memberObjects.containsKey(key))
      {
            throw new IllegalStateException(key + " is not a member of " + this);
      }      
   }

   public long getLastUsed()
   {
      return lastUsed;
   }

   public boolean isInUse()
   {
      return inUseKeys.size() > 0;
   }

   public void setInUse(boolean inUse)
   {
      lastUsed = System.currentTimeMillis();
   }
   
   /**
    * Always returns <code>true</code>.
    */
   public boolean isModified()
   {
      return true;
   }

   private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      active = new HashMap<Object, SerializationGroupMember>();
      inUseKeys = new HashSet<Object>();
   }
   
   
   
}
