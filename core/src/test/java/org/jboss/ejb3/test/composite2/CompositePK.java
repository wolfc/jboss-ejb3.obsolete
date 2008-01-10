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
package org.jboss.ejb3.test.composite2;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
@Embeddable
public class CompositePK implements Serializable
{
   private long id1;
   private long id2;

   public CompositePK()
   {
   }

   public CompositePK(long id1, long id2)
   {
      this.id1 = id1;
      this.id2 = id2;
   }

   public long getId1()
   {
      return id1;
   }

   public void setId1(long id1)
   {
      this.id1 = id1;
   }

   public long getId2()
   {
      return id2;
   }

   public void setId2(long id2)
   {
      this.id2 = id2;
   }

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof CompositePK)) return false;

      final CompositePK compositePK = (CompositePK) o;

      if (id1 != compositePK.id1) return false;
      if (id2 != compositePK.id2) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (int) (id1 ^ (id1 >>> 32));
      result = 29 * result + (int) (id2 ^ (id2 >>> 32));
      return result;
   }


}
