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

import org.jboss.ejb3.cache.Identifiable;
import org.jboss.ejb3.cache.PassivatingCache;

/**
 * Allows objects to be the members of a SerializationGroup.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public interface GroupedPassivatingCache<T extends Identifiable> extends PassivatingCache<T>
{
   /**
    * Assign the given object to the given group.  The group will be
    * of the {@link SerializationGroup} implementation type returned
    * by {@link #createGroup()}.
    * 
    * @param obj
    * @param group
    * 
    * @throws IllegalArgumentException if the 
    *   {@link SerializationGroup#isClustered() group's support for clustering}
    *   does not match {@link Cache#isClustered() our own}.
    */
   void setGroup(T obj, SerializationGroup group);
}
