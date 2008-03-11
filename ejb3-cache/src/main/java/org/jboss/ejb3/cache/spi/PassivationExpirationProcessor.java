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

package org.jboss.ejb3.cache.spi;

/**
 * An object that can process passivation and expiration of cache entries.
 * 
 * @author Brian Stansberry
 */
public interface PassivationExpirationProcessor
{
   /**
    * Tells the processor to analyze its current content, passivating or expiring
    * any items that meet its rules for passivation or expiration.  This method
    * provides a hook for an external background cleanup thread to trigger
    * passivation and expiration.  This method should not be invoked if 
    * {@link #isPassivationExpirationSelfManaged() the cache is managing its
    * own background process} for this.
    */
   void processPassivationExpiration();
   
   /**
    * Gets whether this cache is running its own background process to trigger
    * passivation and expiration.
    * 
    * @return <code>true</code> if the cache is managing its own background
    *         process; <code>false</code> otherwise.
    */
   boolean isPassivationExpirationSelfManaged();

}
