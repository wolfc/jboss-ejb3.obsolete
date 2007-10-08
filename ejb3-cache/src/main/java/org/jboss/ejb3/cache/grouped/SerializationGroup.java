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
package org.jboss.ejb3.cache.grouped;

import java.io.Serializable;

import org.jboss.ejb3.cache.Cacheable;

/**
 * Defines a group of serializable objects which must be serialized in
 * one unit of work.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Brian Stansberry
 * @version $Revision: $
 */
public interface SerializationGroup extends Cacheable, Serializable
{
   /**
    * Gets whether this groups supports (and requires) clustering functionality
    * from its members.
    * 
    * @return <code>true</code> if clustering is supported, <code>false</code>
    *         otherwise
    */
   boolean isClustered();
   
   /**
    * Sets whether this groups supports (and requires) clustering functionality
    * from its members.
    * 
    * @return
    */
   void setClustered(boolean clustered);
   
   /**
    * Initially associate a new member with the group. Also
    * {@link #addActive(SerializationGroupMember) marks the member as
    * active}.
    * 
    * @param member
    * 
    * @throws IllegalStateException if <code>member</code> was previously
    *                               added to the group
    * @throws IllegalArgumentException if the 
    *   {@link SerializationGroupMember#isClustered() member's support for clustering}
    *   does not match {@link #isClustered() our own}.
    */
   void addMember(SerializationGroupMember member);
   
   /**
    * Remove the specified member from the group.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    */
   void removeMember(Object key);
   
   /**
    * Gets the number of group members.
    */
   int size();
   
   /**
    * Returns the {@link SerializationGroupMember#getSerializableObject() member object}
    * associated with the member whose {@link Identifiable#getId() id}
    * matches <code>key</code>.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @return the object associated with the member, or <code>null</code> if
    *         <code>key</code> does not identify a member.
    */
   Object getMemberObject(Object key);
   
   /**
    * Records that the given member is "active"; i.e. needs to have
    * @PrePassivate callbacks invoked before serialization.
    * 
    * @param member the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   void addActive(SerializationGroupMember member);
   
   /**
    * Records that the given member is no longer "active"; i.e. does not need
    * to have @PrePassivate callbacks invoked before serialization.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   void removeActive(Object key);
   
   /**
    * Notification that the given member is "in use", and therefore the
    * group should not be serialized.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   void addInUse(Object key);
   
   /**
    * Notification that the given member is no longer "in use", and therefore 
    * should not prevent the group being serialized.
    * 
    * @param key the {@link Identifiable#getId() id} of the member
    * 
    * @throws IllegalStateException if <code>member</code> wasn't previously
    *                               added to the group via
    *                               {@link #addMember(SerializationGroupMember)}
    */
   void removeInUse(Object key);
   
   /**
    * Prepare members for passivation.
    */
   void prePassivate();
   
   /**
    * Notification that the group has been activated from a passivated state.
    */
   void postActivate();;
   
   /**
    * Prepare members for replication.
    */
   void preReplicate();
   
   /**
    * Notification that the previously replicated group has been retrieved from 
    * a clustered cache.
    */
   void postReplicate();
}
