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
package org.jboss.ejb3.test.bank;

/**
 * @see <related>
 * @author $Author: wolfc $
 * @version $Revision: 61136 $
 */
public class CustomerPK implements java.io.Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   public String id;

   public String name;

   // Public --------------------------------------------------------
   public boolean equals(Object pk)
   {
      return ((CustomerPK) pk).id.equals(id)
            && ((CustomerPK) pk).name.equals(name);
   }

   public int hashCode()
   {
      return id.hashCode();
   }

   public String toString()
   {
      return "Customer:" + id + "/" + name;
   }
}
