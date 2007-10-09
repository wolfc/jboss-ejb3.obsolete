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

package org.jboss.ejb3.cache;


/**
 * An object that can be cached.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface Cacheable extends Identifiable
{
   /** Possible states of the entry.  See elements for details. */
//   public static enum State { 
//      /**
//       * A reference to the entry's {@link CacheEntry#getObject container object} 
//       * has *not* been handed out to a caller and there is no need to
//       * invoke any @PostActivate callback before handing out a reference.
//       */
//      READY,
//      /** 
//       * A reference to the entry's {@link CacheEntry#getObject container object} 
//       * has been handed out to a caller and has not yet been released.
//       * 
//       * @see Cache#get(Object)
//       * @see Cache#peek(Object)
//       * @see Cache#release(Identifiable)
//       */
//      IN_USE,
//      /**
//       * A reference to the entry's {@link CacheEntry#getObject container object} 
//       * has *not* been handed out to a caller, but any @PostActivate callback 
//       * should be invoked before handing out a reference.
//       */
//      PASSIVATED 
//   };
//   
//   State getCacheState();
//   
//   void setCacheState(State state);
   
//   void setLastUsed(long lastUsed);
   
   /**
    * Gets whether this object is in use by a caller.
    */
   boolean isInUse();
   
   /**
    * Sets whether this object is in use by a caller.
    * 
    * @param inUse
    */
   void setInUse(boolean inUse);
   
   /**
    * Gets the timestamp of the last time this object was in use.
    * 
    * @return
    */
   long getLastUsed();
   
   /**
    * Gets whether this object's internal state has been modified since
    * the last request to this method.
    * 
    * @return
    */
   boolean isModified();   
}
