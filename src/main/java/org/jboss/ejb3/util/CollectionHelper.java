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
package org.jboss.ejb3.util;

import java.util.Collection;

/**
 * Provide helpful functions for Collection objects.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class CollectionHelper
{
   /**
    * Adds all of the elements in the specified collection to the target collection if
    * a collection is specified.
    * 
    * @param <E>
    * @param target
    * @param c collection containing elements to be added to this collection, may be null
    * @return <tt>true</tt> if this collection changed as a result of the call
    */
   public static <E> boolean addAllIfSet(Collection<E> target, Collection<? extends E> c)
   {
      if(c == null)
         return false;
      return target.addAll(c);
   }
}
