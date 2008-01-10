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
package org.jboss.ejb3.test.composite;

import javax.persistence.Embeddable;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Embeddable
public class FieldCustomerPK implements java.io.Serializable
{
   public long id;
   public String name;


   public FieldCustomerPK()
   {
   }

   public FieldCustomerPK(long id, String name)
   {
      this.id = id;
      this.name = name;
   }

   public int hashCode()
   {
      return (int) id + name.hashCode();
   }

   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (!(obj instanceof FieldCustomerPK)) return false;
      if (obj == null) return false;
      FieldCustomerPK pk = (FieldCustomerPK) obj;
      return pk.id == id && pk.name.equals(name);
   }
}
