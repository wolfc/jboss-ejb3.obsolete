package org.jboss.ejb3.cache.impl;

import java.io.Serializable;

import org.jboss.ejb3.cache.Cacheable;
import org.jboss.ejb3.cache.PassivatingCache;
import org.jboss.ejb3.cache.grouped.SerializationGroup;
import org.jboss.ejb3.cache.grouped.SerializationGroupMember;

/**
 * Default implementation of {@link SerializationGroupMember}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class SerializationGroupMemberImpl<T extends Cacheable & Serializable> 
 implements Cacheable, SerializationGroupMember, Serializable
{
   private static final long serialVersionUID = 1L;
   
   private Object id;
   /**
    * The underlying object (e.g. bean context).
    * Preferably, this field would be transient. It isn't now because it is 
    * possible this entry will never be assigned to a PassivationGroup,
    * in which case we need to serialize obj.
    * TODO Relying on nulling this field is fragile. Can we make this
    * field transient by ensuring we only use this cache class with bean 
    * classes that are sure to be part of a group?
    */
   private T obj;
   /**
    * Hack. We hold two refs to our object; one we clear in prePassivate,
    * one we keep, but it's transient.  getSerializableObject() returns
    * whichever is available, making it available for passivation callbacks.
    */
   private transient T transientObj;
   /** The group. Never serialize the group; only the groupCache does that */
   private transient SerializationGroup group;
   private Object groupId;
   private long lastUsed;
   /** The cache that's handling us */
   private transient PassivatingCache<SerializationGroupMemberImpl<T>> delegate;
   
   SerializationGroupMemberImpl(T obj, PassivatingCache<SerializationGroupMemberImpl<T>> delegate)
   {
      assert obj != null : "obj is null";
      assert delegate != null : "delegate is null";
      
      this.obj = transientObj = obj;
      this.id = obj.getId();
      this.delegate = delegate;
   }
   
   public Object getId()
   {
      return id;
   }
   
   public boolean isClustered()
   {
      // Value from the containing cache
      return delegate.isClustered();
   }
   
   @SuppressWarnings("unchecked")
   public T getSerializableObject()
   {      
      return obj == null ? transientObj : obj;
   }
   
   public void setSerializableObject(T obj)
   {
      this.obj = transientObj = obj;
   }
   
   public SerializationGroup getGroup()
   {
      return group;
   }

   public void setGroup(SerializationGroup group)
   {
      this.group = group;
   }

   public Object getGroupId()
   {
      return groupId;
   }

   public void setGroupId(Object groupId)
   {
      this.groupId = groupId;
   }

   // Called by PassivationGroup prior to its passivating
   public void prePassivate()
   {
      // make sure we don't passivate the group twice
      group = null;
      
      // null out obj so when delegate passivates this entry
      // we don't serialize it. It serializes with the PassivationGroup only  
      // We still have a ref to transientObj, so it can be retrieved
      // for passivation callbacks
      obj = null;
      
      delegate.passivate(this.id);
   }
   
   // Called by PassivationGroup prior to its replicating
   public void preReplicate()
   {
      // make sure we don't replicate the group twice
      group = null;
      // null out obj so when delegate passivates this entry
      // we don't serialize it. It serializes with the PassivationGroup only
      obj = null;
      
      delegate.replicate(this.id);
   }
   
   public long getLastUsed()
   {
      return obj == null ? lastUsed : obj.getLastUsed();
   }

   public boolean isInUse()
   {
      return obj == null ? false : obj.isInUse();
   }

   public void setInUse(boolean inUse)
   {
      if (obj != null)
      {
         obj.setInUse(inUse);
         lastUsed = obj.getLastUsed();
      }
   }
   
   /**
    * Allows our controlling {@link PassivatingCache} to provide
    * us a reference after deserialization.
    * 
    * @param delegate
    */
   public void setPassivatingCache(PassivatingCache<SerializationGroupMemberImpl<T>> delegate)
   {
      assert delegate != null : "delegate is null";
      
      this.delegate = delegate;
   }
   
   public boolean isModified()
   {
      return (obj != null && obj.isModified());
   }

   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + ",obj=" + obj + ",groupId=" + groupId + ",group=" + group + "}";
   }
}