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

package org.jboss.ejb3.cache.grouped;

import org.jboss.ejb3.cache.Identifiable;

/**
 * A member of a {@link SerializationGroup}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface SerializationGroupMember extends Identifiable
{
   /**
    * Gets the underlying object that should be serialized as part of 
    * serialization of the group.
    * 
    * @return
    */
   Object getSerializableObject();
   
   /**
    * Prepare the group member for passivation. Ensure any @PrePassivate 
    * callback is invoked on the underlying object.  Ensure any reference to 
    * the {@link #getSerializableObject() underlying object} or to the 
    * {@link SerializationGroup} is nulled.
    */
   void prePassivate();
   
   /**
    * Gets whether this member supports clustering functionality.
    * 
    * @return <code>true</code> if clustering is supported, <code>false</code>
    *         otherwise
    */
   boolean isClustered();
   
   /**
    * Prepare the group member for replication. Ensure any required callback
    * (e.g. @PrePassivate) is invoked on the underlying object.  Ensure any 
    * reference to the {@link #getSerializableObject() underlying object} or to 
    * the {@link SerializationGroup} is nulled. 
    * 
    * @throws UnsupportedOperationException if {@link #isClustered()} returns
    *                                       <code>false</code>
    */
   void preReplicate();
}
